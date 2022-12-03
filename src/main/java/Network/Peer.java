package Network;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Block.Blockchain;
import transactions.TransactionOutput;



public class Peer extends Thread {
	private String name;
	private String address;
	private int port;
	private boolean listening = true;
	private List<String> peers = new ArrayList<String>();
	private List<ServerThread> serverThreads = new ArrayList<ServerThread>();
	public static Blockchain blockchain = new Blockchain();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
	public List<ServerThread> getServerThreads() {
		return serverThreads;
	}
	public Peer(String name, String address, int port)
	{
		this.name = name;
		this.address = address;
		this.port = port;
		
		
		List<String> peersFile = new ArrayList<String>();
		if(peersFile.size() <= 0)
		{
			//this.connectToSeedNode("127.0.0.1", 6969);
		}
		else {
			for (String peerInfo : peersFile) {
				String ip = peerInfo.split("/")[0];
				int portNumber = Integer.parseInt(peerInfo.split("/")[1]);
				this.connect(ip, portNumber);
			}
			
		}
	
	}
	@Override
	public void run() {
		try {
			
			this.startHost();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void startHost() throws IOException
	{
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(this.port);
			System.out.println("peer "+ this.port +" is listening!...");
			while (listening) {
				Socket socket = serverSocket.accept();
				ServerThread serverThread = new ServerThread(socket,this);
				serverThreads.add(serverThread);
				serverThread.start();
			}
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(serverSocket != null)
				serverSocket.close();
		}
	}
	
	public void connectToSeedNode(String address, int port)
	{
		try {
			Socket socket = new Socket(address,port);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF("CONNECT:"+this.port);
			ServerThread serverThread = new ServerThread(socket,this);
			serverThread.start();
	
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// client luu dc thong tin cua server
	
	// 
	
	// connect to address with port
	public void connect(String address, int port)
	{
		try 
		{
			Socket socket = new Socket(address,port);
			ServerThread serverThread = new ServerThread(socket,this);
			String remoteHost = socket.getInetAddress().getHostAddress();
			int remotePort = socket.getPort();
				peers.add(remoteHost+"/"+remotePort);
				serverThreads.add(serverThread);
				serverThread.start();
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				dos.writeUTF(new Message("VERSION_REQ:_").toString());
				dos.writeUTF(new Message("GET_ADDR:"+this.port).toString());
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}

	void stopHost() {
	     listening = false;
	}
	// may nhan duoc tin nhan va tien hanh phan hoi
	
	
	
/*		
Định nghĩa các giao thức giữa 2 peer
1. yêu cầu version
VERSION_REQ:
-> khi nhận đc cú pháp trên, gửi đi VERSION_RES:version cao nhất 
VERSION_RES:version cao nhất 
-> khi nhận đc cú pháp trên, kiểm tra version có cao nhất ko
2.	
	BLOCK:dữ liệu  block
->khi nhận đc cú pháp trên, thực hiện kiểm tra block, thêm block vào blockchain
3.
 	GET_BLOCK:vị trí thứ i
-> khi nhận đc cú pháp trên, gửi đi block tại vị trí thứ i: cú pháp BLOCK:dữ liệu block
4. 
ADDR: dữ liệu địa chỉ ip, port của các peer
-> khi nhận được cú pháp trên thì thực hiện kiểm tra các ip, port và thêm vào bảng định tuyến của mình

GET_ADDR:vị trí thứ i
-> khi nhận đc cú pháp trên, gửi đi dữ liệu ADDR:dữ liệu peer thứ i
*/		
	
	void broadcast(Message message)
	{
		try {
			for(int i = 0; i< this.serverThreads.size(); i++)
			{
				DataOutputStream dos = new DataOutputStream(this.serverThreads.get(i).socket.getOutputStream());
				dos.writeUTF(message.toString());
			}
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<String> getPeers() {
		return peers;
	}
	public void setPeers(List<String> peers) {
		this.peers = peers;
	}
	
}
