package Network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ReceiveThread extends Thread {
	private Socket socket;
	private List<Message> receiveData = new  ArrayList<Message>(); 
	
	public ReceiveThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		if(this.socket != null)
		{
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(this.socket.getInputStream());
				String s;
				while ((s = dis.readUTF()) != null) {	
					this.receiveData.add(new Message(s));
				}
				dis.close();
			} catch (Exception e) {
				System.err.println("Loi line 30");
//				e.printStackTrace();
				
			}
			finally {
				if(this.socket != null)
					try {
						dis.close();
						this.socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		
	}
	 public List<Message> getDataList() throws Exception
	{
		List<Message> dataList = new ArrayList<Message>(receiveData);
			for (Message message : dataList) {
				receiveData.remove(message);
			}
		return dataList;
			
	}
}
