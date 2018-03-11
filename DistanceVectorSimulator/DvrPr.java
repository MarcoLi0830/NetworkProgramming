

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;

public class DvrPr {
	public static HashMap<String, Float> min_dist = new HashMap<String, Float>();
	public static HashMap<String, String> min_to= new HashMap<String, String>();
	public static HashMap<String,HashMap<String, Float>> dist_table = new HashMap<String,HashMap<String, Float>>();
	public static HashMap<String, Integer> nei_port = new HashMap<String, Integer>();
	public static HashMap<Integer, String> port_nei = new HashMap<Integer, String>();
	public static HashMap<String, Timer> nei_Timer = new HashMap<String, Timer>();
	public static HashMap<String, float[]> nei_cost_change = new HashMap<String, float[]>();
	public static ArrayList<String> dead_nei = new ArrayList<String>(); 
	public static String L_node = null;
	public static int L_port = 0;
	public static boolean Poi_Rev = false;
	public static ArrayList<String> Poi_Rev_Node;
	public static int counter = 0;
	public static void main(String[] args) throws FileNotFoundException, SocketException, InterruptedException {
		// TODO Auto-generated method stub
		final int MAX_PACKET_SIZE=65507;
		byte[] buffer=new byte[MAX_PACKET_SIZE];
		Scanner sc = new Scanner(new BufferedReader(new FileReader(args[2])));
		L_node = args[0];
		L_port = Integer.valueOf(args[1]);
		if(args.length == 4)
			Poi_Rev = true;
		int numNeb = sc.nextInt();
		 Thread.sleep(5000);
		 if(Poi_Rev)
			 initialize_pr(sc);
		 else
			 initialize(sc);
		 
		 for(String node2: DvrPr.min_dist.keySet())
			{
				if(DvrPr.min_dist.get(node2)!=-1)
					System.out.println("shortest path to node "+node2+": "+" The next hop is: "+min_to.get(node2)+" and the cost is: "+DvrPr.min_dist.get(node2));
			}
		@SuppressWarnings("resource")
		DatagramSocket server=new DatagramSocket(L_port);
		DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
		while(true)
		{
			
			try {
				server.receive(packet);
				String recvStr = new String(packet.getData(), 0, packet.getLength());
				StringTokenizer stn = new StringTokenizer(recvStr, "@");
				String msgType = stn.nextToken();
				msgController(msgType, stn);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	public static void initialize_pr(Scanner sc)
	{
		String node = null;
		float cost = 0;
		float cost2 = 0;
		int port = 0;
		while(sc.hasNextLine()&&sc.hasNext())
		{
			HashMap<String, Float> subDist_table = new HashMap<String, Float>();
			node = sc.next();
			cost = sc.nextFloat();
			cost2 = sc.nextFloat();
			port = sc.nextInt();
			min_dist.put(node, cost);
			min_to.put(node, node); //(destination, through node)
			float[] costs = {cost,cost2};
			nei_cost_change.put(node, costs);
			subDist_table.put(node, cost);
			dist_table.put(node, subDist_table);
			nei_port.put(node, port);
			port_nei.put(port, node);
			Timer timer = new Timer(true);
			HBTask task = new HBTask();
			task.setNode(node);
			timer.schedule(task, 30*1000);
			nei_Timer.put(node, timer);
		}
		sendHeartBeatThread shbT = new sendHeartBeatThread();
		float[] f=null;
		for(String nei:nei_cost_change.keySet())
		{	
			f=nei_cost_change.get(nei);
			System.out.println(nei+": "+f[0]+" "+f[1]);
		}
		shbT.start();
		sendDVThread sdvT = new sendDVThread();
		sdvT.start();
		if(Poi_Rev)
		{
			Timer timer_changedist = new Timer(true);
			changeDistTask t = new changeDistTask();
			timer_changedist.schedule(t, 60*1000);
		}
	}
	
	public static void initialize(Scanner sc)
	{
		String node = null;
		float cost = 0;
		int port = 0;
		while(sc.hasNextLine()&&sc.hasNext())
		{
			HashMap<String, Float> subDist_table = new HashMap<String, Float>();
			node = sc.next();
			cost = sc.nextFloat();
			port = sc.nextInt();
			min_dist.put(node, cost);
			min_to.put(node, node); //(destination, through node)
			subDist_table.put(node, cost);
			dist_table.put(node, subDist_table);
			nei_port.put(node, port);
			port_nei.put(port, node);
			Timer timer = new Timer(true);
			HBTask task = new HBTask();
			task.setNode(node);
			timer.schedule(task, 30*1000);
			nei_Timer.put(node, timer);
		}
		sendHeartBeatThread shbT = new sendHeartBeatThread();
		shbT.start();
		sendDVThread sdvT = new sendDVThread();
		sdvT.start();
	}
	
	
	public static synchronized void msgController(String msgType, StringTokenizer stn)
	{
		if(msgType.equals("HB"))
		{
			int senderPort = Integer.valueOf(stn.nextToken());
			String sender = port_nei.get(senderPort);
			
			Timer oTimer = nei_Timer.get(sender);
			oTimer.cancel();
			Timer timer = new Timer(true);
			HBTask task = new HBTask();
			task.setNode(sender);
			timer.schedule(task, 7*1000);
			nei_Timer.put(sender, timer);
			
		}
		else if(msgType.equals("DV"))
		{
			int senderPort = Integer.valueOf(stn.nextToken());
			String sender = port_nei.get(senderPort);
			UpdateDist_Table(stn, sender);
			
			
		}
	}
	
	
	public static void UpdateDist_Table(StringTokenizer stn, String sender)
	{
		HashMap<String, Float> d_table = dist_table.get(sender);
		HashMap<String, Float> new_table = new  HashMap<String, Float>();
		new_table.put(sender, d_table.get(sender));
		boolean flag =false;
		while(stn.hasMoreTokens())
		{
			String[] strA = stn.nextToken().split(" ");
			if(strA.length == 2)
			{
				if(dead_nei.contains(strA[0]))
				{
					new_table.put(strA[0], (float) -1.0);
				}
				else if(!strA[0].equals(L_node))
				{
					String To = strA[0];
					float cost =Float.valueOf(strA[1]);
					if(d_table.containsKey(To)&&d_table.get(To) == -1.0)
					{
						new_table.put(To, (float) -1.0);
					}
					else if(cost == -1.0)
					{
						new_table.put(To, (float) -1.0);
					}
					else
					{
						new_table.put(To, cost+d_table.get(sender));
					}
						
				}
			}
			else
			{
				
				if(dead_nei.contains(strA[1]))
				{
					new_table.put(strA[1], (float) -1.0);
				}
				else if(!strA[1].equals(L_node))
				{
					String To = strA[1];
					float cost =Float.valueOf(strA[2]);
					if(d_table.containsKey(To)&&d_table.get(To) == -1.0)
					{
						new_table.put(To, (float) -1.0);
					}
					else if(cost == -1.0)
					{
						new_table.put(To, (float) -1.0);
					}
					else
					{
						if(strA[0].equals(L_node))
						{
							new_table.put(To, (float) 100000.0);
							flag = true;
								
						}
						else
							new_table.put(To, cost+d_table.get(sender));
					}
						
				}
			}
		}
		if(Poi_Rev)
		{	
			
			if(flag&& counter < 6)
			{
				counter ++;
				for(String throughNode: dist_table.keySet())
				{
					System.out.println("Through: "+throughNode);
					HashMap<String, Float> distMap = dist_table.get(throughNode);
					for(String to: distMap.keySet())
					{
						System.out.println("To "+to+": "+distMap.get(to));
					}
				}
			}
		}
			dist_table.put(sender, new_table);  
			recompMin_dist();
		
		
	}
	
	public static synchronized void recompMin_dist()
	{
		boolean flag = false;
		String DVString;
		HashMap<String, Float> new_min_dist = new HashMap<String, Float>();
		HashMap<String, String> new_min_to = new HashMap<String, String>();
		for(String node:nei_port.keySet())
		{
			HashMap<String, Float> distMap = dist_table.get(node);
			
			for(String throughNode : distMap.keySet())
			{
				float cost = distMap.get(throughNode);
				if(!new_min_dist.containsKey(throughNode))
				{
					new_min_dist.put(throughNode, cost);
					new_min_to.put(throughNode, node);
				}
				else
				{
					if (cost < new_min_dist.get(throughNode))
					{
						new_min_dist.put(throughNode, cost);
						new_min_to.put(throughNode, node);
					}
				}
			}
		}
		
		if(Poi_Rev)
		{
			Poi_Rev_Node = new ArrayList<String>(); 
			for(String dest: new_min_dist.keySet())
			{
				//System.out.println("dest:"+dest+" "+"through: "+new_min_to.get(dest));
				if(!new_min_to.get(dest).equals(dest))
				{
					Poi_Rev_Node.add(new_min_to.get(dest));
				}
			}
			
		}
		
		//System.out.println("new size: "+new_min_dist.size() +" "+"old size: "+min_dist.size());
		if(new_min_dist.size() == min_dist.size())
		{
			for(String node: min_dist.keySet())
			{
				if((!new_min_dist.containsKey(node)) || (!new_min_dist.get(node).equals(min_dist.get(node))))
				{
					// sendDV here:
					DVString = sendDVThread.getDV();
					System.out.println("min_dist of "+DvrPr.L_node);
					min_dist = new_min_dist;
					min_to = new_min_to;
					for(String node2: DvrPr.min_dist.keySet())
					{
						if(DvrPr.min_dist.get(node2)!=-1)
							System.out.println("shortest path to node "+node2+": "+" The next hop is: "+min_to.get(node2)+" and the cost is: "+DvrPr.min_dist.get(node2));
					}
					
					System.out.println();
					if(Poi_Rev)
					{
						for(String throughNode: dist_table.keySet())
						{
							System.out.println("Through: "+throughNode);
							HashMap<String, Float> distMap = dist_table.get(throughNode);
							for(String to: distMap.keySet())
							{
								System.out.println("To "+to+": "+distMap.get(to));
							}
						}
					}
					
					for(int port: DvrPr.nei_port.values())
					{
					   sendDVThread.sendDV(DVString, port);
					}
					
					break;
				}
				
			}
			min_dist = new_min_dist;
		}
		else
		{
			// sendDV here:
			DVString = sendDVThread.getDV();
			System.out.println("min_dist of "+DvrPr.L_node);
			min_dist = new_min_dist;
			min_to = new_min_to;
			for(String node: DvrPr.min_dist.keySet())
			{
				if(DvrPr.min_dist.get(node)!=-1)
				System.out.println("shortest path to node "+node+": "+" The next hop is: "+min_to.get(node)+" and the cost is: "+DvrPr.min_dist.get(node));
			}
			System.out.println();
			if(Poi_Rev)
			{	
				for(String throughNode: dist_table.keySet())
				{
					System.out.println("Through: "+throughNode);
					HashMap<String, Float> distMap = dist_table.get(throughNode);
					for(String to: distMap.keySet())
					{
						System.out.println("To "+to+": "+distMap.get(to));
					}
				}
			}
			
			for(int port: DvrPr.nei_port.values())
			{
			   sendDVThread.sendDV(DVString, port);
			}
			
		}
		
	}
	
}
