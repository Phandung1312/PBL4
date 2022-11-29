package Wallet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.collect.Maps;

import Utils.Base58Check;
import lombok.Cleanup;

public class WalletUtils {
	 private final static String WALLET_FILE = "wallet.dat";
	 private static final String ALGORITHM = "AES";
	 private static final byte[] CIPHER_TEXT = "2oF@5sC%DNf32y!TmiZi!tG9W5rLaniD".getBytes();
	private volatile static WalletUtils instance;

    public static WalletUtils getInstance() {
        if (instance == null) {
            synchronized (WalletUtils.class) {
                if (instance == null) {
                    instance = new WalletUtils();
                }
            }
        }
        return instance;
    }
    private WalletUtils() {
        initWalletFile();
    }
    private void initWalletFile() {
        File file = new File(WALLET_FILE);
        if (!file.exists()) {
            this.saveToDisk(new Wallets());
        } else {
            this.loadFromDisk();
        }
    }
    
    public Set<String> getAddresses() {
        Wallets wallets = this.loadFromDisk();
        return wallets.getAddresses();
    }
    
    public Wallet getWallet(String address) {
        Wallets wallets = this.loadFromDisk();
        return wallets.getWallet(address);
    }
    public Wallet createWallet() {
        Wallet wallet = new Wallet();
        Wallets wallets = this.loadFromDisk();
        wallets.addWallet(wallet);
        this.saveToDisk(wallets);
        return wallet;
    }
    
    private void saveToDisk(Wallets wallets) {
        try {
            if (wallets == null) {
                throw new Exception("ERROR: Fail to save wallet to file !");
            }
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            // Create cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            SealedObject sealedObject = new SealedObject(wallets, cipher);
            // Wrap the output stream
            @Cleanup CipherOutputStream cos = new CipherOutputStream(
                    new BufferedOutputStream(new FileOutputStream(WALLET_FILE)), cipher);
            @Cleanup ObjectOutputStream outputStream = new ObjectOutputStream(cos);
            outputStream.writeObject(sealedObject);
        } catch (Exception e) {
            throw new RuntimeException("Fail to save wallet to disk !");
        }
    }
    private Wallets loadFromDisk() {
        try {
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sks);
            @Cleanup CipherInputStream cipherInputStream = new CipherInputStream(
                    new BufferedInputStream(new FileInputStream(WALLET_FILE)), cipher);
            @Cleanup ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) inputStream.readObject();
            return (Wallets) sealedObject.getObject(cipher);
        } catch (Exception e) {
            throw new RuntimeException("Fail to load wallet from disk ! ");
        }
    }
    public static class Wallets implements Serializable {

        private static final long serialVersionUID = -2542070981569243131L;

        private Map<String, Wallet> walletMap = Maps.newHashMap();

        /**
         * 添加钱包
         *
         * @param wallet
         */
        private void addWallet(Wallet wallet) {
            try {
                this.walletMap.put(wallet.getAddress(), wallet);
            } catch (Exception e) {

                throw new RuntimeException("Fail to add wallet !");
            }
        }

       
        Set<String> getAddresses() {
            if (walletMap == null) {
                throw new RuntimeException("Fail to get addresses ! ");
            }
            return walletMap.keySet();
        }

      
        Wallet getWallet(String address) {
            
            try {
                Base58Check.base58ToBytes(address);
            } catch (Exception e) {
                throw new RuntimeException("Fail to get wallet ! ");
            }
            Wallet wallet = walletMap.get(address);
            if (wallet == null) {
                throw new RuntimeException("Fail to get wallet ! ");
            }
            return wallet;
        }
    }
}
