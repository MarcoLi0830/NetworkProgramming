

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class MessageThread extends Thread{
	BufferedReader reader;
	PrintWriter writer;
	Socket socket;
	
	Map<String, ClientUser> onlineList;
	
	
	public MessageThread(BufferedReader reader, PrintWriter writer, Socket socket,
			Map<String, ClientUser> onlineList)
	{
		
		this.reader = reader;
		this.writer = writer;
		this.socket = socket;
		
		this.onlineList = onlineList;
	}
	/*
	 * close the connection with the server.
	 * And then set the start state to "0"
	 * so that the user can log in next time.
	 */
	public void closeConn() throws IOException
	{
		if(reader!=null)
			reader.close();
		if(writer != null)
			writer.close();
		if(socket!=null)
			socket.close();
		client.isStart.set(0, 0);
	}
	
	public void run()
	{
		String msg = null;
		String name;
		String IP;
		while(true)
		{
			try {
				msg = reader.readLine();
				ClientUser user=null;
				StringTokenizer stn = new StringTokenizer(msg, "@");
				String command = stn.nextToken();
				/*
				 * if the command from server is "CLOSE",
				 * invoke close function.
				 */
				if(command.equals("CLOSE"))
				{
					System.out.println("Server stopped!");
					closeConn();
				}
				/*
				 * if the command is "REFRESH", 
				 * update the online user list.
				 */
				else if(command.equals("REFRESH"))
				{
					//onlineList.clear();
					int size = Integer.valueOf(stn.nextToken());
					for (int i =1;i<=size;i++)
					{
						name = stn.nextToken();
						IP = stn.nextToken();
						user = new ClientUser(name,IP);
						onlineList.put(name, user);
					}
					
				}
				/*
				 * if the command is "DELETEU", remove the 
				 * entry whose key is the user to be deleted from 
				 * the online user list.
				 */
				else if(command.equals("DELETEU"))
				{
					name = stn.nextToken();
					onlineList.remove(name);
					System.out.println(name +" logged out.");
				}
				/*
				 * if the command is "ADD", add the new user 
				 * to the online user list.
				 */
				else if(command.equals("ADD"))
				{
					name = stn.nextToken();
					IP = stn.nextToken();
					user = new ClientUser(name, IP);
					onlineList.put(name, user);
					System.out.println(name +" logged in.");
				}
				/*
				 * if the command is "PWDFAILED",
				 * just close the connection with the 
				 * server.
				 */
				else if(command.equals("PWDFAILED"))
				{
					closeConn();
				}
				/*
				 * if the message contains other words, just 
				 * display what they are.
				 */
				else
					System.out.println(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				break;
			}
		}
	}
	
}
