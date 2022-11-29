package Block;

import org.apache.commons.lang3.StringUtils;
import storage.DBBlockUtils;

public class BlockchainIterator {

    private String currentBlockHash;

    public BlockchainIterator(String currentBlockHash) {
        this.currentBlockHash = currentBlockHash;
    }
    
    public boolean hashNext() {
        if (StringUtils.isBlank(currentBlockHash)) {
            return false;
        }
        Block lastBlock = DBBlockUtils.getInstance().getBlock(currentBlockHash);
        if (lastBlock == null) {
            return false;
        }
        if (lastBlock.getPreviousHash().length() == 0) {
            return true;
        }
        return DBBlockUtils.getInstance().getBlock(lastBlock.getPreviousHash()) != null;
    }


    public Block next() {
        Block currentBlock = DBBlockUtils.getInstance().getBlock(currentBlockHash);
        if (currentBlock != null) {
            this.currentBlockHash = currentBlock.getPreviousHash();
            return currentBlock;
        }
        return null;
    }
}