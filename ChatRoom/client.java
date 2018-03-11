

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class client {
	public static ArrayList<Integer> isStart = new ArrayList<Integer>();
	@SuppressWarnings({ "deprecation", "resource", "unused" })
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Socket socket = null ;
		MessageThread messageThread = null;
		PrintWriter writer = null ;
		BufferedReader reader = null;
		Scanner userInfo = new Scanner(System.in);
		Map<String, ClientUser> onlineList =  null;
		ClientUser myInfo = null;
		InetAddress addr = InetAddress.getLocalHost(); 	
		
			isStart.add(0, 0); //initialize the start state. "0" means not logged yet, "1" means logged.
			Scanner sc = new Scanner(System.in);
			while(true)
			{
				//start while loop to receive the command from Terminal.
				String CMD = null;
				CMD = sc.nextLine();
				if(CMD.equals("")) //if command is "", don't do anything.
					continue;
				if(CMD.equals("connect"))
				{
					//System.out.println(isStart[0]);
					if(isStart.get(0)!='Y')
					{
						String username=null;
						String pwd = null;
						try{
							while(true)
							{
								System.out.println("Please input your username:");
								username = userInfo.nextLine().trim();
								if(!username.equals(""))
									break;
							}
							while(true)
							{
								System.out.println("Please input your password:");
								pwd= userInfo.nextLine().trim();
								if(!pwd.equals(""))
									break;
							}
							
							
							//get socket, InputStream and OutputStream.
							socket = new Socket(args[0], Integer.valueOf(args[1]));
							writer = new PrintWriter(socket.getOutputStream());
							reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							//set initialized information, including username, ip address and password.
							String Message = "";
							Message += username;
							Message +="@"+addr.getHostAddress();
							Message +="@"+pwd;
							//invoke send function, sending the initial info to the server.
							sendMessage(Message,writer);
							//initialize a list to store the online user info.
							onlineList = new HashMap<String, ClientUser>();
							//create a new thread to receive infomation from the server.
							messageThread = new MessageThread(reader, writer,socket,onlineList);
							messageThread.start();
							myInfo = new ClientUser(username, addr.getHostAddress());
							isStart.set(0, 1);
							}catch(Exception e)
							{
								isStart.set(0, 0);
								System.out.print("connection failed!");
							}
					}
					else  // if start state is "1", display error message.
					{
						System.out.println("You have connected to the server!");
					}
				}
				else if(CMD.equals("logout"))
				{
					if(isStart.get(0)==1)
					{sendMessage("CLOSE",writer);
					messageThread.stop();
					closeConn(reader, writer, socket, isStart);}
					else //if the user hasn't logged yet, display error message.
						System.out.print("Please log in first!");
					
				}
				/*if the command is "whoelse",
				 * traverse the onlineList and print 
				 * each entry's key except the user itself. 
				 */
				else if(CMD.equals("whoelse")) 
				{
					if(isStart.get(0)==1)
					{for(String name: onlineList.keySet())
					{
						if(!name.equals(myInfo.getName()))
							System.out.println(name);
					}
					}
					else
						System.out.print("Please log in first!");
						
				}
				/*
				 * if the command contains other key words,
				 * check whether the first word is "message", "whoelsesince",
				 * "block","unblock" or "broadcast". If not, display the 
				 * error message.
				 */
				else
				{
					
					String[] strs = CMD.split(" ");
					if(strs[0].equals("message")||strs[0].equals("whoelsesince")||strs[0].equals("broadcast")||strs[0].equals("block")||strs[0].equals("unblock"))
						sendMessage(CMD,writer);
					else
						System.out.println("Error. Invalid command");
				}
			}
	}
	
	public  synchronized static void closeConn(BufferedReader reader, PrintWriter writer, Socket socket, ArrayList<Integer> isStart) throws IOException
	{
		if(reader!=null)
			reader.close();
		if(writer != null)
			writer.close();
		if(socket!=null)
			socket.close();
		isStart.set(0, 0);
		System.out.println("log out successfully!");
	}
	
	public static void sendMessage(String msg, PrintWriter writer)
	{
		writer.println(msg);
		writer.flush();
	}
	
	

}

