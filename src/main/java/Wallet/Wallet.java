package Wallet;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import Network.Peer;
import transactions.Transaction;
import transactions.TransactionInput;
import transactions.TransactionOutput;

public class Wallet {
	
//	public PrivateKey privateKey;
//	public PublicKey publicKey;
	private BCECPrivateKey privateKey;
	 private byte[] publicKey;
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public Wallet() {
		generateKeyPair();
	}
		
	public void generateKeyPair() {
//		try {
//			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
//			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
//			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
//			keyGen.initialize(ecSpec, random); 
//	        KeyPair keyPair = keyGen.generateKeyPair();
//	        privateKey = keyPair.getPrivate();
//	        publicKey = keyPair.getPublic();
//		}catch(Exception e) {
//			throw new RuntimeException(e);
//		}
		try {
            KeyPair keyPair = newECKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

            byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);

            this.setPrivateKey(privateKey);
            this.setPublicKey(publicKeyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Fail to init wallet ! ", e);
        }
	}
	 private KeyPair newECKeyPair() throws Exception {
	        // 注册 BC Provider
	        Security.addProvider(new BouncyCastleProvider());
	        // 创建椭圆曲线算法的密钥对生成器，算法为 ECDSA
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
	        // 椭圆曲线（EC）域参数设定
	        // bitcoin 为什么会选择 secp256k1，详见：https://bitcointalk.org/index.php?topic=151120.0
	        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
	        keyPairGenerator.initialize(ecSpec, new SecureRandom());
	        return keyPairGenerator.generateKeyPair();
	    }
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: Peer.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { 
            	UTXOs.put(UTXO.id,UTXO); 
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	 
	public Transaction sendFunds(byte[] recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		System.out.println("Your balance is "+getBalance());
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}

	public BCECPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(BCECPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	
	
}
