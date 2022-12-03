package transactions;

import java.lang.reflect.Type;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.InstanceCreator;

public class TransactionInstanceCreator implements InstanceCreator<Transaction>{
	public String transactionId; 
	public PublicKey sender;
	public PublicKey reciepient;
	public float value; 
	public byte[] signature;
	
	public List<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	public TransactionInstanceCreator() {
		
	}
	@Override
	public Transaction createInstance(Type type) {
		Transaction transaction = new Transaction(sender,reciepient);
		return transaction;
	}
}
