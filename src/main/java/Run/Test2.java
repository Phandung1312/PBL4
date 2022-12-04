package Run;

import java.security.PublicKey;
import java.security.Security;

import Block.Blockchain;
import Network.Peer;
import Wallet.Wallet;
import storage.DBBlockUtils;

public class Test2 {
	public static void main(String[] args) {
		Peer p1 = new Peer("P4", "127.0.0.1", 5000);
		p1.start();
		p1.connect("localhost",5004);
			}
}
