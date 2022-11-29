package transactions;

import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.Arrays;

public class TransactionOutput {
	public double value;
	public byte[] reciepient; //also known as the new owner of these coins.
	
	//Constructor
	public TransactionOutput(double value, byte[] rePublicKey) {
		this.value = value;
		this.reciepient = rePublicKey;
	}
	
	//Check if coin belongs to you
	public boolean isMine(byte[] publicKey) {
		return Arrays.equals(this.reciepient,publicKey);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public byte[] getReciepient() {
		return reciepient;
	}

	public void setReciepient(byte[] reciepient) {
		this.reciepient = reciepient;
	}
	
}