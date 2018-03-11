

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileReader;
import java.util.Scanner;

public class ClientSideThread extends Thread{
	public Socket socket;
	public BufferedReader reader;
	private PrintWriter writer;
	private User user=null;
	private ArrayList<ClientSideThread> clientsList;
//	private ArrayList<String> onlineList;
	
	public ClientSideThread(Socket socket, ArrayList<ClientSideThread> clientsList) throws IOException
	{
		this.socket = socket;
		this.clientsList = clientsList;
	//	this.onlineList = onlineList;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream());
		
		//receive the user info(username, ip address and password)
		String str = reader.readLine();
		StringTokenizer stn = new StringTokenizer(str, "@");
		String username = stn.nextToken();
		String ipAddress = stn.nextToken();
		String password = stn.nextToken();
		
		//invoke checkpwd() to check username and password
		if(checkPwd(username,password))
		{
			user = new User(username, ipAddress);
			clientsList.add(this);
		writer.println("Welcome to the greatest messaging application ever!");
		//System.out.println(user.getName()+" has connected , Ip:" +user.getIP());
		writer.flush();
		String str2 = "";
		/*
		 * update the online user list and
		 * tell all the other online users to
		 * update their own online users list.
		 * 
		 */
		if(clientsList.size()>0)
		{
			for(int i=0;i<=clientsList.size()-1;i++)
			{
				str2 += clientsList.get(i).getUser().getName()+"@";
				str2 += clientsList.get(i).getUser().getIP()+"@";
			}
		}
		writer.println("REFRESH@"+clientsList.size()+"@"+str2);
		writer.flush(); //let the user get online User list;
		
		for(int i= 0;i<=clientsList.size()-1;i++)
		{
			if(!clientsList.get(i).getUser().getName().equals(user.getName()))
			{
				clientsList.get(i).getWriter().println("ADD@"+user.getName()+"@"+user.getIP());
				clientsList.get(i).getWriter().flush();
			}
		}
		
		/*
		 * to check if the user has offline messages
		 */
		printOffMsg(user);
		
		}
		else
		{
			writer.println("PWDFAILED@");
			writer.flush();
			reader.close();
			writer.close();
			
			
		}
	}
	
	/*
	 * To check if the user has offline messages,
	 * if he dose, send all the offline messages from the 
	 * list to his client thread.
	 */
	public void printOffMsg(User puser)
	{
		ArrayList<String> offMsg = server.offlineMsg.get(puser.getName());
		while(offMsg.size()!=0)
		{
			writer.println(offMsg.remove(0));
			writer.flush();
		}
		server.offlineMsg.put(puser.getName(), offMsg);
		
	}
	
	/*
	 * This method is used to check whether the username and password are valid.
	 */
	@SuppressWarnings("resource")
	public boolean checkPwd(String name, String pwd) throws FileNotFoundException
	{
		String line;
		String[] words;
		long[] state;
		

		Scanner sc1 = new Scanner(new BufferedReader(new FileReader("credentials.txt")));
		while(sc1.hasNext())
		{
			line = sc1.nextLine().trim();
			words = line.split(" ");
			if(!words[0].equals(name))
				continue;
			else
			{
				state = server.userState.get(words[0]);
				/*
				 * if the password check fails more than three times,
				 * set a Timer to run a task to block user from logging
				 * for "blockTime".
				 */
				if(state[0]==3)
				{
					TimerTask task = new TimerTask() {   
						public void run() {   
						    state[0] = 0;
						}   
						};   
					Timer timer = new Timer(true);
					timer.schedule(task, server.blockTime*1000);
					writer.println("Your account is blocked due to multiple login failures. Please try again later");
					writer.flush();
					return false;
				}
				else
				{
					/*
					 * if the password check fails,
					 * the failure counter increases 1.
					 */
					if(!words[1].equals(pwd))
						{
							state[0] ++;
							server.userState.put(words[0], state);
							writer.println("Invalid Password. Please try again"+"; total times: "+ state[0]);
							writer.flush();
							return false;
						}
					else
					{
						//if the online state is 1, stop user from logging in.
						if(state[1]!=0)
						{
							writer.println("The user is online already!");
							writer.flush();
							return false;
						}
						/*
						 * if the username and password check succeeds, 
						 * set online state to 1, failure counter to 0 and
						 * set log in time.
						 */
						else
						{
							state[0] = 0;
							state[1] = 1;
							state[2] = System.currentTimeMillis()/1000;
							server.userState.put(words[0], state);
							return true;
						}
						
					}
				}
				
			}
		}
		writer.println("Invalid username. Please try again!");
		writer.flush();
		return false;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public PrintWriter getWriter()
	{
		return writer;
	}
	
	/*
	 * if a certain user is blocked by someone,
	 * this indicates the string list of that user is not empty,
	 * so return true. Or return false.
	 */
	public  boolean checkIfBlocked()
	{
		ArrayList<String> blockers = server.blockedList.get(user.getName());
		if(blockers.size()!=0)
			return true;
		else
			return false;
			
	}
	
	public  void sendMessage(String dst, String message)
	{
		boolean blocked = checkIfBlocked();
		
		if(dst.equals("ALL"))
		{
			/*
			 * if a user is blocked by someone, his broadcast message cannot be sent
			 * to the users who blocked him.
			 */
			if(blocked)
			{
				ArrayList<String> blockers = server.blockedList.get(user.getName());
				for(int i = 0; i<=clientsList.size()-1;i++)
				{
					User testUser = clientsList.get(i).getUser();
					if(blockers.contains(testUser.getName()))
						continue;
					else
					{
						if(!clientsList.get(i).getUser().getName().equals(user.getName()))
						{
							clientsList.get(i).getWriter().println(user.getName()+": "+message+ "[group text]");
							clientsList.get(i).getWriter().flush();
						}
						
					}	
				}
				writer.println("Your message could not be delivered to some recipients");
				writer.flush();
			}
			/*
			 * if a user is not blocked by anyone, 
			 * his broadcast message will be sent to
			 * every online user.
			 */
			else
			{
				for(int i = 0; i<=clientsList.size()-1;i++)
				{
					if(!clientsList.get(i).getUser().getName().equals(user.getName()))
					{
						clientsList.get(i).getWriter().println(user.getName()+": "+message+ "[group text]");
						clientsList.get(i).getWriter().flush();
					}
				}
			}	
		}
		else
		{
			if(server.offlineMsg.containsKey(dst))  //check whether the destination exists.
			{
				
				long[] state = server.userState.get(dst);
				ArrayList<String> blockedName = server.blockList.get(dst);
				/*
				 * if the destination has blocked the sender,
				 * send blocked message to that sender.
				 */
				if(blockedName.contains(user.getName()))
				{
					writer.println("Your message could not be delivered as the recipient has blocked you");
					writer.flush();
				}
				else
				{
					if(state[1]!=0)
					{	for(int i = 0; i<=clientsList.size()-1;i++)
						{
							if(clientsList.get(i).getUser().getName().equals(dst))
								{
									clientsList.get(i).getWriter().println(user.getName()+": "+message);	
									clientsList.get(i).getWriter().flush();
								}		
						}
					}
					/*
					 * if the destination is offline, add this message to 
					 * the offline message list of the destination user.
					 */
					else
					{
						ArrayList<String> offMsg = server.offlineMsg.get(dst);
						offMsg.add(user.getName()+": "+message);
						server.offlineMsg.put(dst, offMsg);
					}
				}
			}
			/*
			 * if the destination is not in the user name list, 
			 * send error message to the user.
			 */
			else
			{
				writer.println("Error. The user dosen't exist.");
				writer.flush();
			}
		}	
	}
	
	/*
	 *  This method will print all the users 
	 *  who logged in after "time" seconds from now.
	 */
	public void printSinceUser(int time)
	{
		long logTime = 0;
		long nowTime = System.currentTimeMillis()/1000;
			for(String key:server.userState.keySet())
			{
				if(!key.equals(user.getName())) //not include the user it self.
				{
					logTime = server.userState.get(key)[2];
					if(nowTime - time<=logTime)
					{
						writer.println(key);
						writer.flush();
					}
				}
			}
				
		
	}
	
	@SuppressWarnings("deprecation")
	public void run()
	{
		String msg = null;
		while(true)
		{
			/*
			 * specify a task used to forcibly log out a user
			 * who has kept the idle state for "idleTime". This task
			 * will send DELETEU command to all the other users and 
			 * change the online state of the idle user to 0 and remove the
			 * idle user from the online user list.
			 */
			TimerTask task = new TimerTask() {   
				public void run() {   
				   try {
					   for (int i=clientsList.size()-1; i>=0; i--)
						{
							clientsList.get(i).getWriter().println("DELETEU@"+user.getName());
							clientsList.get(i).getWriter().flush();
							
						}
						
						for (int i=clientsList.size()-1; i>=0; i--)
						{
							if(clientsList.get(i).getUser() == user)
							{
							long[]	state = server.userState.get(user.getName());
							state[1] = 0;
							server.userState.put(user.getName(), state);
								ClientSideThread ct = clientsList.get(i);
								clientsList.remove(i);
								//update the online-user list.
								ct.stop();
								
							}
						}
					   writer.println("You are logged out by the server due to idling state.");
					   writer.flush();
					   writer.println("PWDFAILED@");
					   writer.flush();
					   writer.close();
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				}   
				}; 
			Timer timer = new Timer(true);
			timer.schedule(task, server.idleTime*1000);
			try {
				msg = reader.readLine();
				//if the server thread receives a command from a user,
				//cancel the Timer, which means that user is active at the moment.
				timer.cancel();
				StringTokenizer stn = new StringTokenizer(msg, " ");
				String firstComm = stn.nextToken();
				
				/*
				 * if the command is "CLOSE", delete the sender from the online user list,
				 * and tell all the other users to delete the sender from their online user
				 * list. And then change the sender's online state to "0", ending the connection 
				 * with him.
				 */
				if(msg.equals("CLOSE"))
				{
					
					
					reader.close();
					writer.close();
					socket.close();
					
					for (int i=clientsList.size()-1; i>=0; i--)
					{
						clientsList.get(i).getWriter().println("DELETEU@"+user.getName());
						clientsList.get(i).getWriter().flush();
					}
					
					for (int i=clientsList.size()-1; i>=0; i--)
					{
						if(clientsList.get(i).getUser() == user)
						{
						long[]	state = server.userState.get(user.getName());
						state[1] = 0;
						server.userState.put(user.getName(), state);
							ClientSideThread ct = clientsList.get(i);
							clientsList.remove(i);


							//update the online-user list.
							ct.stop();
							return;
						}
					}
				}
				/*
				 * if the command is "broadcast", specify the first argument as "ALL"
				 * while invoking the function sendMessage().
				 */
				else if(firstComm.equals("broadcast"))
				{
					String content = "";
					while(stn.hasMoreTokens())
					{
						content += stn.nextToken()+" ";
					}
					 sendMessage("ALL", content);
				}
				/*
				 * if the command is "block", block the target user.
				 */
				else if(firstComm.equals("block"))
				{
					String blockName = stn.nextToken();
					//if the target user is not found, send error message to the sender.
					if(!server.blockList.containsKey(blockName))
						{
							writer.println("No such user found!");
							writer.flush();
						}
					else
					{
						//A user cannot block himself.
						if(blockName.equals(user.getName()))
						{
							writer.println("Error. Cannot block self");
							writer.flush();
						}
						else
						{
							//update the two block lists.
							ArrayList<String> blockedNames = server.blockList.get(user.getName());
							blockedNames.add(blockName);
							server.blockList.put(user.getName(), blockedNames);
							
							ArrayList<String> blockers = server.blockedList.get(blockName);
							blockers.add(user.getName());
							server.blockedList.put(blockName, blockers);
							writer.println(blockName+" is blocked");
							writer.flush();
						}
					}
					
				}
				/*
				 * if the command is "unblock", remove the target user from 
				 * the block list.
				 */
				else if(firstComm.equals("unblock"))
				{
					String unBlockName = stn.nextToken();
					//if the target user is not found, send error message to the sender.
					if(!server.blockList.containsKey(unBlockName))
					{
						writer.println("No such user found!");
						writer.flush();
					}
					else
					{
						if(unBlockName.equals(user.getName()))
						{
							writer.println("Error. Cannot unblock self");
							writer.flush();
						}
						else
						{
							//update the two block lists.
							ArrayList<String> blockedNames = server.blockList.get(user.getName());
							
							if(blockedNames.contains(unBlockName))
							{
								blockedNames.remove(unBlockName);
								server.blockList.put(user.getName(), blockedNames);
								ArrayList<String> blockers = server.blockedList.get(unBlockName);
								blockers.remove(user.getName());
								server.blockedList.put(unBlockName, blockers);
								writer.println(unBlockName + " is unblocked.");
								writer.flush();
							}
							//if the user is not blocked at first, send error message to the sender.
							else
							{
								writer.println("Error. "+unBlockName+" was not blocked.");
								writer.flush();
							}
						}
					}
					
				}
				/*
				 * if the command is whoelsesince, 
				 * invoke printSinceUser and specify the argument as the time for now.
				 */
				else if(firstComm.equals("whoelsesince"))
				{
					int time = Integer.valueOf(stn.nextToken());
					printSinceUser(time);
				}
				
				
				else
				{
					String to = stn.nextToken();
					String content = "";
					
					while(stn.hasMoreTokens())
					{
						content += stn.nextToken()+" ";
					}
					 sendMessage(to, content);//dispatch the message
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				break;
			}
		}
	}
}
