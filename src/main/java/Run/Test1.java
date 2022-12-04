package Run;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Block.Block;
import Network.Peer;
import Utils.CommonUtils;
import Wallet.Wallet;
import storage.DBBlockUtils;
import transactions.*;

public class Test1 {
	public static void main(String[] args) {
		//Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Wallet coinbase = new Wallet();
		Wallet a = new Wallet();
		//System.out.println(a.getAddress());
		Transaction genesisTransaction = new Transaction(coinbase.getPublicKey(), a.getPublicKey(), 100f, null);
		genesisTransaction.generateSignature(coinbase.getPrivateKey());
		genesisTransaction.setTransactionId("0");
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId));
		Peer p1 = new Peer("P4", "127.0.0.1", 5004);
		p1.start();
		Peer.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		Block genesisBlock = new Block();
		genesisBlock.setPreviousHash("0");
		genesisBlock.setId(1);
		genesisBlock.setDifficulty(4);
		genesisBlock.setTimeStamp("2017-07-13 22:32:00");//my son's birthday
		genesisBlock.addTransaction(genesisTransaction);
		genesisBlock.setHash(genesisBlock.mine());
		Peer.blockchain.getListBlock().add(genesisBlock);
		System.out.println(Peer.blockchain.getListBlock().get(0).toString());
		Transaction newTx = a.sendFunds(coinbase.getPublicKey(), 10f);
		Block temBlock = new Block();
		temBlock.addTransaction(newTx);
		Block newBlock = Block.generateBlock(Peer.blockchain.getLastBlock(),4,temBlock.getTransactions() );
		if(Block.isBlockValid(newBlock,Peer.blockchain.getLastBlock())) {
			Peer.blockchain.getListBlock().add(newBlock);
			Peer.UTXOs = Peer.blockchain.findAllUTXOs();
		}
		else {
			System.out.println("Sai xac minh block");
		}
		byte [] temp = CommonUtils.serialize(newBlock);
		Block copyBlock = (Block) CommonUtils.deserialize(temp);
		System.out.println(copyBlock.toString());
		}
	
}
