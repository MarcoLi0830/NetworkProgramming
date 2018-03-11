

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class server {

	public static HashMap<String,ArrayList<String>> blockedList = null;
	public static HashMap<String,ArrayList<String>> blockList = null; 
	public static HashMap<String,long[]> userState = null;
	public static HashMap<String, ArrayList<String>> offlineMsg = null;
	public static int blockTime = 0;
	public static int SPort = 0;
	public static int idleTime = 0;
	
	@SuppressWarnings({ "resource", "deprecation", "null" })
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null ;
		//create a arraylist to store the the connected user.
		ArrayList<ClientSideThread> clients = new ArrayList<ClientSideThread>();
		//assign the arguments from the Terminal to three variables.
		blockTime = Integer.valueOf(args[1]);
		SPort = Integer.valueOf(args[0]);
		idleTime = Integer.valueOf(args[2]);
		//initialize the start state.
		char[]  isStart = {'N'};
		ServerSideThread SThread = null;
		Scanner sc = new Scanner(System.in);
		while (true)
		{
			//start a loop to receive commands from Terminal.
			String CMD = sc.nextLine();
			switch(CMD)
				{
			/*
			 * if the command is "start server", invoke function and return
			 * a Thread variable, pointing to the new created thread which 
			 * is used to create another thread to receive message from a user.
			 */
			case "start server":
				SThread = serverStart(serverSocket,isStart,clients);
				
				break;
			/*
			 * if the command is "close server", stop the serverSide thread
			 * and tell all the client side threads to close the connections. 
			 */
			case "close server":
				
				if(SThread != null)
					SThread.stop();
				closeServer(serverSocket, clients);
				break;
			/*
			 * allow the server to inspect the online 
			 * users by traversing the online user list.
			 */
			case "online user":
				showOnlineUsers(clients);
				break;
			default:
				break;
				} 
		}
		
	}
	
	/*
	 * A function used to start a server, and invoke the initialized function
	 * initLists().
	 */
	public static ServerSideThread serverStart(ServerSocket serverSocket, char[] isStart, 
			ArrayList<ClientSideThread> clients) throws IOException
	{
		try
		 { serverSocket = new ServerSocket(SPort); 
		   ServerSideThread SThread = new ServerSideThread(serverSocket, clients);
		   SThread.start();
		   isStart[0] = 'Y';
		   System.out.println("server started! port:"+SPort);
		   initLists();
		   return SThread;
		 }catch(BindException e)
		{
			 isStart[0] = 'N';
			 System.out.println("Thr port has been used, please change another one!");
			 return null;
		}	
	}
	/*
	 * A close function used to tell all the online
	 * users to close connections.
	 */
	public static void closeServer(ServerSocket serverSocket, ArrayList<ClientSideThread> clientsList ) throws IOException
	{
		for(int i=clientsList.size()-1; i>=0;i--)
		{
			clientsList.get(i).getWriter().println("CLOSE@");
			clientsList.get(i).getWriter().flush();
			clientsList.get(i).reader.close();
			clientsList.get(i).getWriter().close();
			clientsList.get(i).socket.close();
			clientsList.remove(i);
			
		}
	}
	/*
	 * An initialized function used to initialize all the 
	 * static Hash maps and read the user file into the lists. 
	 */
	@SuppressWarnings("resource")
	public static void initLists() throws FileNotFoundException
	{
		blockList = new HashMap<String,ArrayList<String>>(); 
		blockedList = new HashMap<String,ArrayList<String>>();
		userState = new HashMap<String,long[]>();
		offlineMsg =  new HashMap<String,ArrayList<String>>();
		String line;
		String[] words;
		Scanner sc1 = new Scanner(new BufferedReader(new FileReader("credentials.txt")));
		while(sc1.hasNext())
		{
			line = sc1.nextLine().trim();
			words = line.split(" ");
			blockList.put(words[0], new ArrayList<String>());
			blockedList.put(words[0], new ArrayList<String>());
			userState.put(words[0], new long[3]);
			offlineMsg.put(words[0], new ArrayList<String>());
		}
	}
	
	public static void showOnlineUsers(ArrayList<ClientSideThread> clients)
	{
		System.out.println("Online users: ");
		 for(int i=0; i<=clients.size()-1; i++)
		 {
			 System.out.println(clients.get(i).getUser().getName());
		 }
	}
	
	
}
