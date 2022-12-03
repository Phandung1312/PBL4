package transactions;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import Network.Peer;
import Utils.CommonUtils;

public class Transaction {
	
	public String transactionId;
	public PublicKey sender; 
	public PublicKey reciepient; 
	public float value; 
	public byte[] signature; 
	
	public List<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; 
	
	// Constructor: 
	public Transaction(PublicKey sender,PublicKey reciepent) {
		this.sender = sender;
		this.reciepient = reciepent;
	}
	public Transaction(PublicKey from, PublicKey to, float value,  List<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	public boolean isCoinBase() {
	return this.getTransactionId().equals("0")
			&& this.getInputs().size() == 0 ;
	}
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
				
		
		for(TransactionInput i : inputs) {
			i.UTXO = Peer.UTXOs.get(i.transactionOutputId);
		}

		if(getInputsValue() < 1) {
			System.out.println("Transaction Inputs too small: " + getInputsValue());
			System.out.println("Please enter the amount greater than " + 1);
			return false;
		}
		
		
		float leftOver = getInputsValue() - value; 
		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); 
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); 	
				
		
		for(TransactionOutput o : outputs) {
			Peer.UTXOs.put(o.id , o);
		}
		
		
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; 
			Peer.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; 
			total += i.UTXO.value;
		}
		return total;
	}
	
	public void generateSignature(PrivateKey privateKey) {
		String data = CommonUtils.getStringFromKey(sender) + CommonUtils.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = CommonUtils.applyECDSASig(privateKey,data);		
	}
	
	public boolean verifySignature() {
		String data = CommonUtils.getStringFromKey(sender) + CommonUtils.getStringFromKey(reciepient) + Float.toString(value)	;
		return CommonUtils.verifyECDSASig(sender, data, signature);
	}
	
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	private String calulateHash() {
		sequence++;
		return CommonUtils.Sha256(
				CommonUtils.getStringFromKey(sender) +
				CommonUtils.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public PublicKey getSender() {
		return sender;
	}
	public void setSender(PublicKey sender) {
		this.sender = sender;
	}
	public PublicKey getReciepient() {
		return reciepient;
	}
	public void setReciepient(PublicKey reciepient) {
		this.reciepient = reciepient;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public byte[] getSignature() {
		return signature;
	}
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	public List<TransactionInput> getInputs() {
		return inputs;
	}
	public void setInputs(List<TransactionInput> inputs) {
		this.inputs = inputs;
	}
	public List<TransactionOutput> getOutputs() {
		return outputs;
	}
	public void setOutputs(List<TransactionOutput> outputs) {
		this.outputs = outputs;
	}
	
}
