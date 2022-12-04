package transactions;

import java.security.PublicKey;

import org.apache.commons.codec.binary.Hex;

import Utils.CommonUtils;

public class TransactionOutput {
	public String id;
	public byte[] reciepient; 
	public double value; 
	public String parentTransactionId;
	
	public TransactionOutput(byte[] reciepient, double value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = CommonUtils.Sha256(Hex.encodeHexString(reciepient)+Double.toString(value)+parentTransactionId);
	}
	

	public boolean isMine(byte[] publicKey) {
		return (publicKey == reciepient);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getReciepient() {
		return reciepient;
	}

	public void setReciepient(byte[] reciepient) {
		this.reciepient = reciepient;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getParentTransactionId() {
		return parentTransactionId;
	}

	public void setParentTransactionId(String parentTransactionId) {
		this.parentTransactionId = parentTransactionId;
	}
	
}
