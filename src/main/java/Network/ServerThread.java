package Network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Security;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Block.Block;

public class ServerThread extends Thread {
	private final Gson gson = new GsonBuilder().create();
	Peer peer;
	Socket socket;
	ServerThread(Socket socket,Peer peer)
	{
		this.socket = socket;
		this.peer = peer;
	}
	@Override
	public void run() {
		try {
			System.out.println("Ket noi tu:" +peer.getName()+":"+ socket.getInetAddress()+":" + socket.getPort());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			ReceiveThread receiveThread = new ReceiveThread(socket);
			receiveThread.start();
			while (true) {
				List<Message> receiveData = receiveThread.getDataList();
				if ( receiveData.size() > 0) {
					for(int i = 0; i < receiveData.size(); i++)
					{
						Message data  = receiveData.get(i);
						String cmd = data.getCmd();
						String msg = data.getMsg();
						switch (cmd) {
						case "VERSION_RES": // khi cmd = VERACK, nhan dc version
							check(dos,  msg);
							break;
						case "VERSION_REQ": // gui di phien ban moi nhat block
							int localVersion = Peer.blockchain.getVersion();
							dos.writeUTF(new Message("VERSION_RES:"+localVersion).toString());
							break;
						case "BLOCK": // nhan block, valid block, them vao blockchain
							addBlock(msg);
							break;
						case "GET_BLOCK": //client yeu cau block tai vi tri thu i
							int index = Integer.parseInt(msg);
							Block block = Peer.blockchain.getListBlock().get(index);
							if(block != null) {
								System.out.println("Sending block " + index+1 +" "+gson.toJson(block)+ " to peer");
								dos.writeUTF(new Message("BLOCK:"+gson.toJson(block)).toString());
							}
							break;
						case "ADDR": // thuc hien ket noi
							if(msg.contains("/"))
							{
								if(!this.peer.getPeers().contains(msg))
								{
									String ip  = msg.split("/")[0];
									int port = Integer.parseInt(msg.split("/")[1]);
									this.peer.connect(ip, port);
								}
							}
							else {
								System.out.println("ADDR: ko dung cu phap");
							}
							
							break;
						case "GET_ADDR": //client yeu cau du lieu ip, port tu peer khac
//							String ip = this.socket.getInetAddress().toString().substring(1, socket.getInetAddress().toString().length());
//							String peerInfo = ip + "/" + msg;
////							if (!this.peer.getPeers().contains(peerInfo))
////								this.peer.getPeers().add(peerInfo);
							for(int j = 0 ; j < this.peer.getPeers().size() ; j++)
							{
								dos.writeUTF(new Message("ADDR:"+this.peer.getPeers().get(j)).toString());
							}

//							this.peer.broadcast(new Message("ADDR", peerInfo));
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				String peerInfo = socket.getInetAddress()+ "/"+socket.getPort();
				this.peer.getPeers().remove(peerInfo);
				this.socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	public void check(DataOutputStream dos,  String msg) throws IOException 
	{
		int bestVersion = Integer.parseInt(msg);
		int localVersion = Peer.blockchain.getVersion();
		if(bestVersion > localVersion)
		{
			int i = localVersion;
			while (i < bestVersion) {
				dos.writeUTF(new Message("GET_BLOCK:"+i).toString());
				i++;
			}
		}
		else {
			dos.writeUTF(new Message("VERSION_RES:"+localVersion).toString());
//			System.out.println("Your block is the best version");
		}
	}
	synchronized void addBlock(String msg)			// di chuyen ham nay den noi can thiet
	{
		Block receiveBlock = gson.fromJson(msg, Block.class);
		if(Peer.blockchain.getListBlock().contains(receiveBlock) ) {
			return;
		}
		
		Block currentBlock = Peer.blockchain.getLastBlock();
		if(currentBlock != null) {
			System.out.println("Block in addBlock:"+currentBlock.toString());
			if(!Block.isBlockValid(receiveBlock, currentBlock)) {
				return;
			}
		}
		else {
			if(!Block.isGenesisBlock(receiveBlock)) {
				return;
			}
		}
		Peer.blockchain.getListBlock().add(receiveBlock);
		System.out.println("Added block " + receiveBlock.getId() + " with hash: ["+ receiveBlock.getHash() + "]");
		System.out.println(Peer.blockchain.getListBlock().get(0).toString());
		
	}
}
