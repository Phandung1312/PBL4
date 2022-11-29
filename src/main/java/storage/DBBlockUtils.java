package storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Block.Block;
import Utils.CommonUtils;
import lombok.Getter;
import transactions.TransactionOutput;



public class DBBlockUtils {
	private static final String DB_FILE ="blockchain.db";
	private static final String BLOCKS_BUCKET_KEY = "blocks";
	private static final String CHAINSTATE_BUCKET_KEY = "chainstate";
	 private static final String LAST_BLOCK_KEY = "l";
	 private RocksDB rocksDB;
	 /**
	     * Luu tru chuoi block duoi dang byte
	     */
	 private Map<String, byte[]> blocksBucket;
	 /**
	     * Luu tru UTXOs duoi dang byte
	     */
	    @Getter
	    private Map<String, byte[]> chainstateBucket;
	private volatile static DBBlockUtils instance; // Co the phat sinh loi khong dong bo (dung volatile)
	
	public static DBBlockUtils getInstance() {
		if (instance == null) {
            synchronized (DBBlockUtils.class) {
                if (instance == null) {
                    instance = new DBBlockUtils();
                }
            }
        }
        return instance;
	}
	private DBBlockUtils() {
        openDB();
        initBlockBucket();
        initChainStateBucket();
    }
	private void openDB() {
        try {
            rocksDB = RocksDB.open(DB_FILE);
        } catch (RocksDBException e) {
            System.out.println("Fail to open db!Try again.");
            throw new RuntimeException("Fail to open db ! ", e);
        }
    }
	private void initBlockBucket() {
        try {
            byte[] blockBucketKey = CommonUtils.serialize(BLOCKS_BUCKET_KEY);
            byte[] blockBucketBytes = rocksDB.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blocksBucket = (Map) CommonUtils.deserialize(blockBucketBytes);
            } else {
                blocksBucket = Maps.newHashMap();
                rocksDB.put(blockBucketKey, CommonUtils.serialize(blocksBucket));
            }
        } catch (RocksDBException e) {
        	 System.out.println("Fail to init block bucket!Try again.");
            throw new RuntimeException("Fail to init block bucket ! ", e);
        }
    }
	 private void initChainStateBucket() {
	        try {
	            byte[] chainstateBucketKey = CommonUtils.serialize(CHAINSTATE_BUCKET_KEY);
	            byte[] chainstateBucketBytes = rocksDB.get(chainstateBucketKey);
	            if (chainstateBucketBytes != null) {
	                chainstateBucket = (Map) CommonUtils.deserialize(chainstateBucketBytes);
	            } else {
	                chainstateBucket = Maps.newHashMap();
	                rocksDB.put(chainstateBucketKey, CommonUtils.serialize(chainstateBucket));
	            }
	        } catch (RocksDBException e) {
	        	System.out.println("Fail to init chainstate bucket!Try again.");
	            throw new RuntimeException("Fail to init chainstate bucket ! ", e);
	        }
	    }
	 public void putLastBlockHash(String lastBlockhash) {
	        try {
	            blocksBucket.put(LAST_BLOCK_KEY, CommonUtils.serialize(lastBlockhash));
	            rocksDB.put(CommonUtils.serialize(BLOCKS_BUCKET_KEY), CommonUtils.serialize(blocksBucket));
	        } catch (RocksDBException e) {
	            System.out.println("Fail to put last block hash ! Last Blockhash = "+lastBlockhash );
	            throw new RuntimeException("Fail to put last block hash ! tipBlockHash=" + lastBlockhash, e);
	        }
	    }
	 public String getLastBlockHash() {
	        byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
	        if (lastBlockHashBytes != null) {
	            return (String) CommonUtils.deserialize(lastBlockHashBytes);
	        }
	        return "";
	    }
	 public void putBlock(Block block) {
	        try {
	            blocksBucket.put(block.getHash(), CommonUtils.serialize(block));
	            rocksDB.put(CommonUtils.serialize(BLOCKS_BUCKET_KEY), CommonUtils.serialize(blocksBucket));
	        } catch (RocksDBException e) {
	            System.out.println("Fail to put block ! block =" + block.toString());
	            throw new RuntimeException("Fail to put block ! block=" + block.toString(), e);
	        }
	    }
	 public Block getBlock(String blockHash) {
	        byte[] blockBytes = blocksBucket.get(blockHash);
	        if (blockBytes != null) {
	            return (Block) CommonUtils.deserialize(blockBytes);
	        }
	        throw new RuntimeException("Fail to get block ! blockHash=" + blockHash);
	    }
	 public void cleanChainStateBucket() {
	        try {
	            chainstateBucket.clear();
	        } catch (Exception e) {
	           System.out.println("Fail to clear chainstate bucket ! ");
	            throw new RuntimeException("Fail to clear chainstate bucket ! ", e);
	        }
	    }
	 public void putUTXOs(String key, List<TransactionOutput> utxos) {
	        try {
	            chainstateBucket.put(key, CommonUtils.serialize(utxos));
	            rocksDB.put(CommonUtils.serialize(CHAINSTATE_BUCKET_KEY), CommonUtils.serialize(chainstateBucket));
	        } catch (Exception e) {
	            System.out.println("Fail to put UTXOs into chainstate bucket ! key=" + key);
	            throw new RuntimeException("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
	        }
	    }
	 public ArrayList<TransactionOutput> getUTXOs(String key) {
	        byte[] utxosByte = chainstateBucket.get(key);
	        if (utxosByte != null) {
	            return (ArrayList<TransactionOutput>) CommonUtils.deserialize(utxosByte);
	        }
	        return null;
	    }
	 public void deleteUTXOs(String key) {
	        try {
	            chainstateBucket.remove(key);
	            rocksDB.put(CommonUtils.serialize(CHAINSTATE_BUCKET_KEY), CommonUtils.serialize(chainstateBucket));
	        } catch (Exception e) {
	            System.out.println("Fail to delete UTXOs by key ! key=" + key);
	            throw new RuntimeException("Fail to delete UTXOs by key ! key=" + key, e);
	        }
	    }
	 public void closeDB() {
	        try {
	            rocksDB.close();
	        } catch (Exception e) {
	            System.out.println("Fail to close db ! ");
	            throw new RuntimeException("Fail to close db ! ", e);
	        }
	    }
	public Map<String, byte[]> getBlocksBucket() {
		return blocksBucket;
	}
	public void setBlocksBucket(Map<String, byte[]> blocksBucket) {
		this.blocksBucket = blocksBucket;
	}
	public Map<String, byte[]> getChainstateBucket() {
		return chainstateBucket;
	}
	public void setChainstateBucket(Map<String, byte[]> chainstateBucket) {
		this.chainstateBucket = chainstateBucket;
	}
	 
}

