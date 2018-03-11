

public class ClientUser {
	private String name;
	private String ip;
	private String passWord;
	public ClientUser(String name, String ip)
	{
		this.name = name;
		this.ip = ip;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String passWord()
	{
		return passWord;
	}
	
	public String getIP()
	{
		return ip;
	}
}
