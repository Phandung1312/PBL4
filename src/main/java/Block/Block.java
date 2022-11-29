package Block;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Block.*;
import Utils.CommonUtils;
import transactions.*;

public class Block {
	private int id;
	private long nonce;
	private String merkleRoot;
	private String timeStamp;
	private String hash;
	private String previousHash;
	private int difficulty;
	public  List<Transaction> transactions = new ArrayList<Transaction>(); 
	public static String concatBlock(Block block) {
		StringBuilder sb = new StringBuilder();
		sb.append(block.getId());
		sb.append(block.getNonce());
		sb.append(block.getTimeStamp());
		sb.append(block.getMerkleRoot());
		sb.append(block.getPreviousHash());
		return sb.toString();
	}
	public static boolean isValidHash(String hash,int prefix) {
		String prefixString = new String(new char[prefix]).replace('\0', '0');
		if (hash == null || hash.length() == 0) {
			return false;
		}
		if(hash.substring(0,prefix).equals(prefixString)) return true;
		else return false;
	}
	public String mineBlock() {
		this.merkleRoot = CommonUtils.getMerkleRoot(transactions);
		String target = CommonUtils.getDificultyString(difficulty);
		this.hash = this.getHash(); 
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			this.hash = calculateHash(this);
		}
		return hash;
	}
	public static String calculateHash(Block block) {
		return CommonUtils.Sha256(concatBlock(block));
	}
	public static Block newGenesisBlock(Transaction txCoinBase) {
		Block newBlock = new Block();
		newBlock.transactions.add(txCoinBase);
		newBlock.setId(1);
		newBlock.setTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		newBlock.setDifficulty(4);
		newBlock.setPreviousHash("0");
		return newBlock;
	}
	public static Block generateBlock(Block oldBlock, int difficulty, List<Transaction> transactions) {
		Block newBlock = new Block();
		newBlock.transactions = transactions;
		newBlock.setId(oldBlock.getId() + 1);
		newBlock.setTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		newBlock.setDifficulty(difficulty);
		newBlock.setPreviousHash(oldBlock.getHash());
		return newBlock;
	}
	public static boolean isBlockValid(Block newBlock, Block oldBlock) {
		if (oldBlock.getId() + 1 != newBlock.getId()) {
			return false;
		}
		if (!oldBlock.getHash().equals(newBlock.getPreviousHash())) {
			return false;
		}
		if (!calculateHash(newBlock).equals(newBlock.getHash())) {
			return false;
		}
		return true;
	}
	public List<Transaction> getTransactions(){
		return this.transactions;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getNonce() {
		return nonce;
	}
	public void setNonce(long nonce) {
		this.nonce = nonce;
	}
	public String getMerkleRoot() {
		return merkleRoot;
	}
	public void setMerkleRoot(String merkleRoot) {
		this.merkleRoot = merkleRoot;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPreviousHash() {
		return previousHash;
	}
	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
	public int getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(int difficutly) {
		this.difficulty = difficutly;
	}
}