package Network;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;




public class SeedNode {
	private String name;
	private String address;
	private int port;
	private boolean listening = true;
	private List<String> peersOnline = new ArrayList<String>();	//PEER ONLINE ip/port
	private List<SeedNodeThread> seedNodeThreads = new ArrayList<SeedNodeThread>();
	Boolean isOnl = false;
	public SeedNode(String name, String ip, int port)  {
		this.name = name;
		this.address = ip;
		this.port = port;
		ServerSocket server = null;
		try {
			server = new ServerSocket(this.port);
			System.out.println("Seednode is listening ...");
			while (listening) {
				Socket socket = server.accept();
				System.out.println("Nhan ket noi tu:"+ socket.getInetAddress().getHostAddress()+"/"+ socket.getPort());
				SeedNodeThread thread = new SeedNodeThread(socket, this);
				seedNodeThreads.add(thread);
				thread.start();
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				server.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	public void addPeerOnline(String peer)
	{
		if(peer.contains("/"))
		this.peersOnline.add(peer);
	}
	public void deletePeerOnline(String peer)
	{
		this.peersOnline.remove(peer);
	}
	public int getPeerPort(String peer)
	{
		return Integer.parseInt(peer.split("/")[1]);
	}
	public String getPeerIPAddress(String peer)
	{
		return peer.split("/")[0];
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public boolean isListening() {
		return listening;
	}
	public void setListening(boolean listening) {
		this.listening = listening;
	}
	public List<String> getPeersOnline() {
		return peersOnline;
	}
	public void setPeersOnline(List<String> peersOnline) {
		this.peersOnline = peersOnline;
	}
	public List<SeedNodeThread> getSeedNodeThreads() {
		return seedNodeThreads;
	}
	public void setSeedNodeThreads(List<SeedNodeThread> seedNodeThreads) {
		this.seedNodeThreads = seedNodeThreads;
	}
	public Boolean getIsOnl() {
		return isOnl;
	}
	public void setIsOnl(Boolean isOnl) {
		this.isOnl = isOnl;
	}
	
	
}

