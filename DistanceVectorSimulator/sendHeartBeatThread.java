

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class sendHeartBeatThread extends Thread{
	
	public void run()
	{
		
		TimerTask task = new TimerTask() {
			public void run()
			{
				for(int port: DvrPr.nei_port.values())
				{
				    sendHB(port);	
				}
			}
		};
		
		Timer timer = new Timer(true);
		timer.schedule(task, 1000,2000);
	}
	
	public static void sendHB(int port)
	{
String hostname="localhost";
		
		try {
			DatagramSocket client=new DatagramSocket();
			InetAddress server=InetAddress.getByName(hostname);
			String info = "HB@"+DvrPr.L_port;
			byte[] strByte = info.getBytes("UTF-8");
			DatagramPacket thepacket=new DatagramPacket(strByte,strByte.length,server,port);
			client.send(thepacket);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
