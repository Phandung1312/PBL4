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
import org.apache.logging.log4j.core.util.SystemNanoClock;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import Utils.CommonUtils;
import storage.DBBlockUtils;
import transactions.*;


public class Blockchain {
	private List<Block> listBlock = new ArrayList<>();
	public Blockchain() {
		
	}
	public Blockchain(List<Block> newList) {
		this.listBlock = newList;
	}
//	public void addBlock(List<Transaction> transactions) {
//		for (Transaction transaction : transactions) {
//			if(!this.verifyTransactions(transaction)) {
//				System.out.println("Fail to add new block ! Invalid transaction");
//				throw new RuntimeException("Fail to add new block ! Invalid transaction");
//			}
//		}
//		Block block = Block.generateBlock(this.listBlock.get(this.listBlock.size()-1), 4, transactions);
//		this.listBlock.add(block);
//	}
	public HashMap<String,TransactionOutput>  findAllUTXOs(){
		HashMap<String,TransactionOutput> allUTXOs = new HashMap<>();
		List<String> allSpentTXOs = this.getAllSpentTXOs();
		for (Block block : listBlock) {
			for (Transaction tx : block.getTransactions()) {
				for (TransactionOutput txOutput : tx.getOutputs()) {
					if(allSpentTXOs.contains(txOutput.getId())) {
						continue;
					}
					allUTXOs.put(txOutput.getId(), txOutput);
				}
			}
		}
		return allUTXOs;
	}
	private List<String> getAllSpentTXOs(){
		List<String> spentTXOs = new ArrayList<>();
		for (Block block : listBlock) {
			for (Transaction tx : block.getTransactions()) {
				if(tx.isCoinBase()) {
					continue;
				}
				for (TransactionInput txInput : tx.getInputs()) {
					spentTXOs.add(txInput.getTransactionOutputId());
				}
			}
		}
		return spentTXOs;
	}
	//Tim transaction bang id
	
	public boolean verifyTransactions(Transaction tx) {
		if(tx.isCoinBase()) {
			return true;
		}
		HashMap<String,TransactionOutput> allUTXOs = this.findAllUTXOs();
		for(TransactionInput txInput : tx.getInputs()) {
			if(allUTXOs.get(txInput.getTransactionOutputId()) == null ) {
				return false;
			}
		}
		if(!tx.verifySignature()) {
			return false;
		}
		return true;
	}
	public Block getLastBlock() {
		int indexLastBlock = this.listBlock.size()-1;
		return this.listBlock.get(indexLastBlock);
	}
	public int getVersion() {
		return this.listBlock.size();
	}
	public List<Block> getListBlock() {
		return listBlock;
	}
	public void setListBlock(List<Block> listBlock) {
		this.listBlock = listBlock;
	}
	
	
	
}
