package Run;

import Block.Blockchain;
import storage.DBBlockUtils;

public class RunApplication {
	public static void main(String[] args) {
		System.out.println(DBBlockUtils.getInstance().getLastBlockHash());
		DBBlockUtils.getInstance().closeDB();
	}
}
