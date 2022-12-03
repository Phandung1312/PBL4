package Block;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
	private  List<Transaction> transactions = new ArrayList<Transaction>(); 
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
	public String mine() {
		this.merkleRoot = CommonUtils.getMerkleRoot(transactions);
		String target = CommonUtils.getDificultyString(difficulty);
		this.hash = calculateHash(this); 
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
		newBlock.setHash(newBlock.mine());
		return newBlock;
	}
	public static Block generateBlock(Block oldBlock, int difficulty, List<Transaction> transactions) {
		Block newBlock = new Block();
		newBlock.transactions = transactions;
		newBlock.setId(oldBlock.getId() + 1);
		newBlock.setTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		newBlock.setDifficulty(difficulty);
		newBlock.setPreviousHash(oldBlock.getHash());
		newBlock.setHash(newBlock.mine());
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
	public boolean addTransaction(Transaction transaction) {
		if(transaction == null){
			return false;		
		}
		if((!"0".equals(this.getPreviousHash()))) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	public boolean equals(Block compareBlock) {
		if(this.getId() != compareBlock.getId()) return false;
		if(this.getMerkleRoot() != compareBlock.getMerkleRoot()) return false;
		if(this.getNonce() != compareBlock.getNonce()) return false;
		if(this.getDifficulty() != compareBlock.getDifficulty()) return false;
		if(this.getPreviousHash() != compareBlock.getPreviousHash()) return false;
		if(this.getTimeStamp() != compareBlock.getTimeStamp()) return false;
		if(this.getTransactions() != compareBlock.getTransactions()) return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\tid: " + getId() + "\n");
		sb.append("\tnonce: " + getNonce() + "\n");
		sb.append("\tTransaction: " + getTransactions().size() + "\n");
		sb.append("\tprevious: " + getPreviousHash() + "\n");
		sb.append("\thash: " + getHash() + "\n},\n");
		return sb.toString();
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