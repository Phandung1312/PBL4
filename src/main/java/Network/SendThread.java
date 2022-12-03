package Network;


import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// luong de gui du lieu
public class SendThread extends Thread {
	private Socket socket;
	public List<Message> sendData = new ArrayList<Message>();
	public SendThread(Socket socket) {
		this.socket = socket;
	}
	private boolean runFlag = true;
	@Override
	public void run() {
		try {
			DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
			while (runFlag) {
				if(!sendData.isEmpty())
				{
					for (Message message : sendData) {
						dos.writeUTF(message.toString());
					}
					sendData.clear();
//                    outputBuffer.add(null);
				}
				else {
//					System.out.println("sendData = null line 32 senthread.java");
				}
			}
			dos.close();
			
			
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	public void sendData(Message data) {
		try {
			if(data != null)
			sendData.add(data);
			else {
				System.out.println("data is null");
			}
		} catch (Exception e) {
			 e.printStackTrace();
		}
		
	}

	/**
	 * maybe
	 */
	public void shutdown() {
		runFlag = false;
	}
}
