package transactions;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import Block.Blockchain;
import Utils.BtcAddressUtils;
import Utils.CommonUtils;
import Wallet.Wallet;
import Wallet.WalletUtils;
import storage.DBBlockUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

public class Transaction {
	private static final int SUBSIDY = 10;
	public String txID;
	public List<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	public long createTime;

	public Transaction(String txID, List<TransactionInput> inputs, List<TransactionOutput> outputs, long createTime) {
		this.txID = txID;
		this.inputs = inputs;
		this.outputs = outputs;
		this.createTime = createTime;
	}

	public Transaction(String txID, TransactionInput input, TransactionOutput output, long createTime) {
		this.txID = txID;
		this.inputs.add(input);
		this.outputs.add(output);
		this.createTime = createTime;
	}

	public boolean isCoinbase() {
		return this.getTXInput().size() == 1 && this.getTXInput().get(0).getTxID().length() == 0
				&& this.getTXInput().get(0).getTxOutputIndex() == -1;
	}

	public static Transaction newCoinBase(String reciever) {
		TransactionInput txInput = new TransactionInput(new String(""), -1, null, null);
		TransactionOutput txOutput = new TransactionOutput(SUBSIDY, reciever.getBytes());
		Transaction tx = new Transaction(null, txInput, txOutput, System.currentTimeMillis());
		tx.setTxID(tx.calulateHash());
		return tx;
	}

	public void newTransaction(String sender, String reciepient, double amount, Blockchain blockchain) {
		Wallet senWallet = WalletUtils.getInstance().getWallet(sender);
		 byte[] pubKey = senWallet.getPublicKey();
		 byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(pubKey);
		SpendableOutputResult result = new UTXOSet(blockchain).findSpendOutputResult(pubKey, amount);
		double accumulated = result.getAccumulated();
		Map<String, int[]> unspentOuts = result.getUnSpendOuts();
		if (accumulated < amount) {
			System.out.println("ERROR : Not enough funds! : Accumulated =" + accumulated + " < amount = " + amount);
		}
		Iterator<Map.Entry<String, int[]>> iterator = unspentOuts.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, int[]> entry = iterator.next();
			String txId = entry.getKey();
			int[] outIds = entry.getValue();
			for(int outIndex : outIds) {
				inputs.add(new TransactionInput(txId, outIndex, null, reciepient.getBytes()));
			}
		}
		outputs.add(new TransactionOutput(amount, reciepient.getBytes()));
		if (accumulated > amount) {
			outputs.add(new TransactionOutput(accumulated - amount, sender.getBytes()));
		}
		this.setTxID(this.calulateHash());
		
	}

	public Transaction trimmedCopy() {
		List<TransactionInput> tmpInputs = new ArrayList<TransactionInput>();
		for (TransactionInput txInput : this.getTXInput()) {
			tmpInputs.add(
					new TransactionInput(txInput.getTxID(), txInput.getTxOutputIndex(), null, txInput.getSender()));
		}
		List<TransactionOutput> tmpOutputs = new ArrayList<TransactionOutput>();
		for (TransactionOutput txOutput : this.getTXOutput()) {
			tmpOutputs.add(new TransactionOutput(txOutput.getValue(), txOutput.getReciepient()));
		}
		return new Transaction(this.getTxID(), tmpInputs, tmpOutputs, this.getCreateTime());
	}

	public void sign(BCECPrivateKey privateKey, Map<String, Transaction> prevTxMap) throws Exception{
		// Neu la transaction dau tien thi khong can ky
		if (this.isCoinbase())
			return;
		for (TransactionInput txInput : this.getTXInput()) {
			if (prevTxMap.get(txInput.getTxID()) == null) {
				throw new RuntimeException("ERROR: Previous transaction is not correct");
			}
		}
		// Kiem tra lai dau vao giao dich co chinh xac hay khong (co tim thay du lieu
		// tuong thich hay khong)
		Transaction txCopy = this.trimmedCopy();
		
		Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);
 
		txCopy.setTxID(txCopy.calulateHash());
		ecdsaSign.update(txCopy.getTxID().getBytes());
		byte[] signature = ecdsaSign.sign();
		for (int i = 0; i < this.getTXInput().size(); i++) {
			this.getTXInput().get(i).setSignature(signature);
		}
	}

	public boolean verifySignature(Map<String, Transaction> prevTxMap) throws Exception {
		for (TransactionInput txInput : this.getTXInput()) {
			if (prevTxMap.get(txInput.getTxID()) == null) {
				throw new RuntimeException("ERROR: Previous transaction is not correct");
			}
		}
		Transaction txCopy = this.trimmedCopy();
		
		Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
		// Xac minh chu ki tung txinput
		for (TransactionInput txInput : this.getTXInput()) {
			 BigInteger x = new BigInteger(1, Arrays.copyOfRange(txInput.getSender(), 1, 33));
	            BigInteger y = new BigInteger(1, Arrays.copyOfRange(txInput.getSender(), 33, 65));
	            ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);

	            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
	            PublicKey publicKey = keyFactory.generatePublic(keySpec);
	            ecdsaVerify.initVerify(publicKey);
	            ecdsaVerify.update(txCopy.getTxID().getBytes());
	            if(!ecdsaVerify.verify(txInput.getSignature())) {
	            	return false;
	            }
		}
		return true;
	}

	private String calulateHash() {
		byte[] serializeBytes = CommonUtils.serialize(this);
		Transaction copyTx = (Transaction) CommonUtils.deserialize(serializeBytes);
		copyTx.setTxID("");
		serializeBytes = DigestUtils.sha256(CommonUtils.serialize(copyTx));
		return Hex.encodeHexString(serializeBytes);
	}

	public String getTxID() {
		return txID;
	}

	public void setTxID(String txID) {
		this.txID = txID;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<TransactionInput> getTXInput() {
		return this.inputs;
	}

	public List<TransactionOutput> getTXOutput() {
		return this.outputs;
	}

}
