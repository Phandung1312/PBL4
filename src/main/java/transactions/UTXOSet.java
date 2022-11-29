package transactions;

import java.awt.RenderingHints.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Maps;

import Block.*;
import Utils.CommonUtils;
import lombok.Synchronized;
import storage.DBBlockUtils;

public class UTXOSet {
	private Blockchain blockchain;
	
	public UTXOSet (Blockchain blockchain) {
		this.blockchain = blockchain;
	}
	public SpendableOutputResult findSpendOutputResult(byte[] pubKey,double amount) {
		 Map<String, int[]> unspentOuts = Maps.newHashMap();
	        int accumulated = 0;
	        Map<String, byte[]> chainstateBucket = DBBlockUtils.getInstance().getChainstateBucket();
	        for (Map.Entry<String, byte[]> entry : chainstateBucket.entrySet()) {
	            String txId = entry.getKey();
	            List<TransactionOutput> txOutputs = (List<TransactionOutput>) CommonUtils.deserialize(entry.getValue());

	            for (int outId = 0; outId < txOutputs.size(); outId++) {
	                TransactionOutput txOutput = txOutputs.get(outId);
	                if (txOutput.isMine(pubKey) && accumulated < amount) {
	                    accumulated += txOutput.getValue();

	                    int[] outIds = unspentOuts.get(txId);
	                    if (outIds == null) {
	                        outIds = new int[]{outId};
	                    } else {
	                        outIds = ArrayUtils.add(outIds, outId);
	                    }
	                    unspentOuts.put(txId, outIds);
	                    if (accumulated >= amount) {
	                        break;
	                    }
	                }
	            }
	        }
	        return new SpendableOutputResult(accumulated, unspentOuts);
	}
	public List<TransactionOutput> findUTXOs(byte[] pubKeyHash) {
		List<TransactionOutput> utxos = new ArrayList<TransactionOutput>();
        Map<String, byte[]> chainstateBucket = DBBlockUtils.getInstance().getChainstateBucket();
        if (chainstateBucket.isEmpty()) {
            return utxos;
        }
        for (byte[] value : chainstateBucket.values()) {
        	List<TransactionOutput> txOutputs = (List<TransactionOutput>) CommonUtils.deserialize(value);
            for (TransactionOutput txOutput : txOutputs) {
                if (txOutput.isMine(pubKeyHash)) {
                    utxos.add(txOutput);
                }
            }
        }
        return utxos;
    }
	/**
     *  Dieu chinh lai chi muc index tx
     */
	@Synchronized
    public void reIndex() {
        System.out.println("Start to reIndex UTXO set !");
        DBBlockUtils.getInstance().cleanChainStateBucket();
        Map<String, ArrayList<TransactionOutput>> allUTXOs = blockchain.findAllUTXOs();
        for (Map.Entry<String, ArrayList<TransactionOutput>> entry : allUTXOs.entrySet()) {
            DBBlockUtils.getInstance().putUTXOs(entry.getKey(), entry.getValue());
        }
        System.out.println("ReIndex UTXO set finished ! ");
    }
	/**
     * Cap nhat lai UTXO:      
     * 1）Xoa dau ra chi tieu khoi UTXO   
     * 2）Them dau ra chua chi tieu vao UTXO
     *
     * @param tipBlock Khoi moi nhat moi duoc them vao
     */
	 @Synchronized
	    public void update(Block tipBlock) {
	        if (tipBlock == null) {
	            throw new RuntimeException("Fail to update UTXO set ! ");
	        }
	        for (Transaction transaction : tipBlock.getTransactions()) {
	            if (!transaction.isCoinbase()) {
	                for (TransactionInput txInput : transaction.getTXInput()) {
	                    List<TransactionOutput> remainderUTXOs = new ArrayList<TransactionOutput>();
	                    String txId = txInput.getTxID();
	                    List<TransactionOutput> txOutputs = DBBlockUtils.getInstance().getUTXOs(txId);

	                    if (txOutputs == null) {
	                        continue;
	                    }

	                    for (int outIndex = 0; outIndex < txOutputs.size(); outIndex++) {
	                        if (outIndex != txInput.getTxOutputIndex()) {
	                        	remainderUTXOs.add(txOutputs.get(outIndex));
	                        }
	                    }

	                    // Neu giao dich nay da chi tieu het thi xoa trong db
	                    if (remainderUTXOs.size() == 0) {
	                        DBBlockUtils.getInstance().deleteUTXOs(txId);
	                    } else {
	                        DBBlockUtils.getInstance().putUTXOs(txId, remainderUTXOs);
	                    }
	                }
	            }
	            //Them cac UTXO moi vao
	            List<TransactionOutput> txOutputs = transaction.getTXOutput();
	            String txId = transaction.getTxID();
	            DBBlockUtils.getInstance().putUTXOs(txId, txOutputs);
	        }

	    }
}
