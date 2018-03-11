

import java.util.HashMap;
import java.util.TimerTask;

public class HBTask extends TimerTask{
	String node_del;
	
	public void setNode(String node)
	{
		this.node_del = node;
	}
	
	public void run() {  
        DvrPr.nei_port.remove(node_del);
        DvrPr.nei_Timer.remove(node_del);
        DvrPr.dist_table.remove(node_del);
        DvrPr.min_dist.put(node_del, (float) -1.0);  // set the cost of that node in DV to -1, indicating this neighbor
        										 //has failed.
        DvrPr.dead_nei.add(node_del);
        System.out.println("delete: "+node_del);
        for(String node:DvrPr.nei_port.keySet())
        {
        	HashMap<String, Float> distMap = DvrPr.dist_table.get(node);
        	for(String toNode: distMap.keySet())
        	{
        		if(toNode.equals(node_del))
        		{
        		   distMap.put(toNode, (float) -1.0);	
        		}
        	}
        	DvrPr.dist_table.put(node, distMap);
        }
        
        for(String node2: DvrPr.min_dist.keySet())
		{
			if(DvrPr.min_dist.get(node2)!=-1)
				System.out.println("shortest path to node "+node2+": "+" The next hop is: "+DvrPr.min_to.get(node2)+" and the cost is: "+DvrPr.min_dist.get(node2));
		}
        
        DvrPr.recompMin_dist();
    }  
}
