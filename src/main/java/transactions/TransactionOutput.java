package transactions;

import java.security.PublicKey;

import Utils.CommonUtils;

public class TransactionOutput {
	public String id;
	public PublicKey reciepient; 
	public double value; 
	public String parentTransactionId;
	
	public TransactionOutput(PublicKey reciepient, double value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = CommonUtils.Sha256(CommonUtils.getStringFromKey(reciepient)+Double.toString(value)+parentTransactionId);
	}
	

	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PublicKey getReciepient() {
		return reciepient;
	}

	public void setReciepient(PublicKey reciepient) {
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
