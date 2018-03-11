

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSideThread extends Thread{
	private ServerSocket socketServer;
	private int max;
	public ArrayList<ClientSideThread> clientsList;
	public ArrayList<String> onlineList;
	public ServerSideThread(ServerSocket socket, ArrayList<ClientSideThread> clients)
	{
		max = 10;
		this.socketServer = socket;
		this.clientsList = clients;
	//	this.onlineList = onlineList;
	}
	
	public void run()
	{
		while(true)
		{
			//start a while loop to accept 
			//incoming socket connections from
			//users.
			try {
				
				Socket  socket = socketServer.accept();
				//after accepting a user socket connection,
				//create a new client thread to receive 
				//messages from that user.
				ClientSideThread client = new ClientSideThread(socket, clientsList);
				if(client.getUser()!=null)
					client.start();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				break;
			}
		}
		
	}
	
	
	
}
