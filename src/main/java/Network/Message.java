package Network;



public class Message {
	
	private String msg;
	private String cmd;
	public Message() {
		
	}
	public Message(String data)
	{
		if(!data.equals("") && data.contains(":"))
		{
			int flag = data.indexOf(":");
			this.cmd = flag >= 0 ? data.substring(0, flag) : data;
			this.msg =  flag >= 0 ? data.substring(flag + 1) : "";
		}
		
		
	}
	public Message(String cmd, String msg){
		this.cmd = cmd;
		this.msg = msg;
		
	}
	@Override
	public String toString() {
		
		return cmd +":"+msg;
	}
	public String getCmd() {
		return cmd;
	}
	public String getMsg() {
		return msg;
	}
}