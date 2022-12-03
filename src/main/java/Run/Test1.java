package Run;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Block.Block;
import Network.Peer;
import Wallet.Wallet;

import transactions.*;

public class Test1 {
	public static void main(String[] args) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Wallet coinbase = new Wallet();
		Wallet a = new Wallet();
		//System.out.println(a.getAddress());
		Transaction genesisTransaction = new Transaction(coinbase.publicKey, a.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
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
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Transaction.class, new TransactionInstanceCreator());
		final Gson gson = gsonBuilder.create();
		final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(a.getPublicKey());
		String b = gson.toJson(genesisBlock);
		Block newblock = gson.fromJson(b, Block.class);
		System.out.println(newblock.toString());
	}
	
}
