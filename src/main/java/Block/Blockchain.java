package Block;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidAttributeValueException;
import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import Utils.CommonUtils;
import storage.DBBlockUtils;
import transactions.*;


public class Blockchain {
	private String lastBlockHash;
	public Blockchain(String blockHash) {
		this.lastBlockHash = blockHash;
	}
	public static Blockchain initBlockchainFromDB() {
		String lastBlockHash = DBBlockUtils.getInstance().getLastBlockHash();
        if (lastBlockHash == null) {
            throw new RuntimeException("ERROR: Fail to init blockchain from DB. ");
        }
        return new Blockchain(lastBlockHash);
	}
	public static Blockchain createBlockchain(String pubkey) {
		String lastBlockHash = DBBlockUtils.getInstance().getLastBlockHash();
		if(CommonUtils.isBlank(lastBlockHash)) {
			Transaction txCoinBase = Transaction.newCoinBase(pubkey);
			Block genesisBlock = Block.newGenesisBlock(txCoinBase);
			genesisBlock.mineBlock();
			lastBlockHash = genesisBlock.getHash();
			DBBlockUtils.getInstance().putBlock(genesisBlock);
			DBBlockUtils.getInstance().putLastBlockHash(lastBlockHash);
		}
		return new Blockchain(lastBlockHash);
	}
	public Block mineBlock(List<Transaction> transactions) {
		for(Transaction tx : transactions) {
			if(!this.verifyTransactions(tx)) {
				System.out.println("ERROR: Fail to mine block ! Invalid transaction ! tx=" + tx.toString());
				 throw new RuntimeException("ERROR: Fail to mine block ! Invalid transaction ! ");
			}
		}
		String lastBlockHash = DBBlockUtils.getInstance().getLastBlockHash();
		if(lastBlockHash == null) {
			throw new RuntimeException("ERROR: Fail to get last block hash ! ");
		}
		Block oldBlock = DBBlockUtils.getInstance().getBlock(lastBlockHash);
		Block newBlock = Block.generateBlock(oldBlock, 4, transactions);
		newBlock.mineBlock();
		this.addBlock(newBlock);
		return newBlock;
	}
	private void addBlock(Block block) {
		DBBlockUtils.getInstance().putBlock(block);
		DBBlockUtils.getInstance().putLastBlockHash(block.getHash());
		this.lastBlockHash = block.getHash();
	}
	public BlockchainIterator getBlockchainIterator() {
		return new BlockchainIterator(null);
	}
	public Map<String,ArrayList<TransactionOutput>>  findAllUTXOs(){
		Map <String,ArrayList<TransactionOutput>> allUTXOs = new HashMap<String,ArrayList<TransactionOutput>>();
		Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs();// Lay danh sach Spent TXOs
		for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();blockchainIterator.hashNext();) { // Duyet toan bo transaction blockchain
			Block block = blockchainIterator.next();
			for (Transaction transaction : block.getTransactions()) {
				String txID = transaction.getTxID();
				int[] spentOutIndexArray = allSpentTXOs.get(txID);//Lay toan bo SpentTXOs cua transaction nay
				ArrayList<TransactionOutput> txOuputs = new ArrayList<TransactionOutput>(transaction.getTXOutput());
				for (int outIndex = 0 ; outIndex < txOuputs.size() ; outIndex++) {
					//Neu txOutput nay khong co trong spentOutput
					if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
                        continue;
                    }
					ArrayList<TransactionOutput> UTXOArray = new ArrayList<TransactionOutput>(allUTXOs.get(txID));
						UTXOArray.add(txOuputs.get(outIndex));
						allUTXOs.put(txID, UTXOArray);
				}
			}
		}
		return allUTXOs;
	}
	private Map<String,int[]> getAllSpentTXOs(){
		// Gom id transaction va chi so mang cua transaction output chua su dung
		Map<String, int[]> spentTXOs =  new HashMap<String,int[]>();
		for( BlockchainIterator blockchainIterator = this.getBlockchainIterator();blockchainIterator.hashNext();) { // Duyet toan bo transaction blockchain 
			Block block = blockchainIterator.next();
			for(Transaction transaction : block.getTransactions()) { 
				if(transaction.isCoinbase()) { //Neu la transaction co so dau tien thi bo qua
					continue;
				}
				for(TransactionInput txInput : transaction.getTXInput()) {
					String inTxID = txInput.getTxID();
					 int[] spentOutIndexArray = spentTXOs.get(inTxID); // Lay danh sach OutIndex cua transaction theo id (neu co)
					 if (spentOutIndexArray == null) { 
	                        spentOutIndexArray = new int[]{txInput.getTxOutputIndex()};
	                    } else { //Them chi so OutIndex moi vua tim duoc
	                        spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
	                    }
					 spentTXOs.put(inTxID, spentOutIndexArray);// Them lai vao danh sach
				}
			}
		}
		return spentTXOs;
	}
	//Tim transaction bang id
	private Transaction findTransaction(String txID) {
		for(BlockchainIterator blockchainIterator = this.getBlockchainIterator();blockchainIterator.hashNext();) {
			Block block = blockchainIterator.next();
			for (Transaction tx : block.getTransactions()) {
				if(tx.getTxID().equals(txID)) {
					return tx;
				}
			}
		}
		throw new RuntimeException("ERROR: Can not found transaction by txId ! ");
	}
	public void signTransaction(Transaction tx,BCECPrivateKey privateKey) throws Exception {
		//Lay tat ca transaction duoc tham chieu boi txInput cua transaction dau vao(tx)
		Map<String,Transaction> prevTxMap = new HashMap<String, Transaction>();
		for (TransactionInput txInput : tx.getTXInput()) {
			Transaction prevTx = this.findTransaction(txInput.getTxID());
			prevTxMap.put(txInput.getTxID(),prevTx);
		}
		tx.sign(privateKey, prevTxMap);
	}
	private boolean verifyTransactions(Transaction tx) {
		if(tx.isCoinbase()) {
			return true;
		}
		Map<String,Transaction> prevTxMap = new HashMap<String, Transaction>();
		for (TransactionInput txInput : tx.getTXInput()) {
			Transaction prevTx = this.findTransaction(txInput.getTxID());
			prevTxMap.put(txInput.getTxID(),prevTx);
		}
		try {
			return tx.verifySignature(prevTxMap);
		} catch (Exception e) {
			throw new RuntimeException("Fail to verify transaction ! transaction invalid ! ", e);
		}
	}
	public void addTransaction(Transaction transaction) {
		
	}
}
