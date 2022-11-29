package transactions;

import java.security.PublicKey;

public class TransactionInput {
	private String txID; //reference to transaction which transaction Output was created
	private int txOutputIndex ; //index of transaction output referenced
	private byte[] signature;
	 private byte[] sender;
	
	public TransactionInput(String transactionID,int outIndex,byte[] signature,byte[] pubkey) {
		this.txID = transactionID;
		this.txOutputIndex = outIndex;
		this.signature = signature;
		this.sender = pubkey;
	}
//	public static TransactionInput newTransactionInput(String transactionID,int outIndex,byte[] signature,PublicKey pubkey) {
//		TransactionInput newTxInput = new TransactionInput(transactionID, outIndex, signature, pubkey);
//		return newTxInput;
//	}
	public String getTxID() {
		return txID;
	}

	public void setTxID(String txID) {
		this.txID = txID;
	}

	public int getTxOutputIndex() {
		return txOutputIndex;
	}

	public void setTxOutputIndex(int txOutputIndex) {
		this.txOutputIndex = txOutputIndex;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public byte[] getSender() {
		return sender;
	}

	public void setSender(byte[] sender) {
		this.sender = sender;
	}
}
