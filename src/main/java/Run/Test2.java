package Run;

import java.security.PublicKey;
import java.security.Security;

import Block.Blockchain;
import Network.Peer;
import Wallet.Wallet;
import storage.DBBlockUtils;

public class Test2 {
	public static void main(String[] args) {
		Wallet a = new Wallet();
		String b = a.getPublicKey().toString();
	}
}
