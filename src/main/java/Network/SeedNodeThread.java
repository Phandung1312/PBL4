package Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SeedNodeThread extends Thread {
	
	Socket socket;
	SeedNode seedNode;
	public SeedNodeThread(Socket s, SeedNode seedNode) {
		this.socket = s;
		this.seedNode = seedNode;
	}
	
	@Override
	public void run() {
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			while (true) {
				String s = "";			// CONNECT:port
				if ( (s = dis.readUTF()) != null) {
					Message receiveData = new Message(s);
					String cmd = receiveData.getCmd();
					String msg = receiveData.getMsg();
					switch (cmd) {
					case "CONNECT": 
						String peerInfo = socket.getInetAddress().getHostAddress() + "/" + msg;
						if(!this.seedNode.getPeersOnline().contains(peerInfo))
							this.seedNode.addPeerOnline(peerInfo);
						for(int i = 0 ; i< this.seedNode.getPeersOnline().size(); i++)
						{
							if(!this.seedNode.getPeersOnline().get(i).equals(peerInfo))
							dos.writeUTF("ADDR:" + this.seedNode.getPeersOnline().get(i));
						}
						break;
					}
				}
			}
			
		} catch (Exception e) {
			this.seedNode.getSeedNodeThreads().remove(this);
			this.seedNode.getPeersOnline().remove(socket.getInetAddress().getHostAddress() + "/" + socket.getPort());
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(socket.getInetAddress().getHostAddress()+"  da ngat ket noi");
		}
	}

}
