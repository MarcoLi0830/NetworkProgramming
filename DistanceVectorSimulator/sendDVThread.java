

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class sendDVThread extends Thread{
	public void run()
	{
		
		TimerTask task = new TimerTask() {
			public void run()
			{
				String DVString = getDV();
				for(int port: DvrPr.nei_port.values())
				{
				    sendDV(DVString, port);
				}
			}
		};
		
		Timer timer = new Timer(true);
		timer.schedule(task, 1000,5000);
		
		
	}
	
	public static String getDV()
	{
		String str = "";
		if(DvrPr.Poi_Rev)
		{
			for(Entry<String, Float> entry:DvrPr.min_dist.entrySet()) 
			{
	
					if(!DvrPr.min_to.get(entry.getKey()).equals(entry.getKey()))
					{
						str += DvrPr.min_to.get(entry.getKey())+" "+entry.getKey()+" "+entry.getValue()+"@";
					//	System.out.println("PR sent to:"+str);
					}
					else
						str += entry.getKey()+" "+entry.getValue()+"@";
			}
		}
		else
		{
			for(Entry<String, Float> entry:DvrPr.min_dist.entrySet()) 
			{
				str += entry.getKey()+" "+entry.getValue()+"@"; 
			}
		
		}
		return str;
	}
	
	public static void sendDV(String DVstr, int port)
	{
		String hostname="localhost";
		try {
			DatagramSocket client=new DatagramSocket();
			InetAddress server=InetAddress.getByName(hostname);
			String info = "DV@"+DvrPr.L_port+"@"+DVstr;
			byte[] strByte = info.getBytes("UTF-8");
			DatagramPacket thepacket=new DatagramPacket(strByte,strByte.length,server,port);
			client.send(thepacket);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
