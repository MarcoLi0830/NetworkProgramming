

import java.util.HashMap;
import java.util.TimerTask;

public class changeDistTask extends TimerTask{

	public void run()
	{
		float[] f = null;
		boolean flag = false;
		HashMap<String, Float> dist_map = null;
		for(String node: DvrPr.nei_cost_change.keySet())
		{
			f = DvrPr.nei_cost_change.get(node);
			dist_map = DvrPr.dist_table.get(node);
			if(f[0] != f[1])
			{
			  flag = true;
			  dist_map.put(node, f[1]);	
			  for(String node2: dist_map.keySet())
			  {
				  if(!node2.equals(node))
					 dist_map.put(node2, dist_map.get(node2) - f[0]+f[1]);  
			  }
			}
		}
		
		if(flag)
			DvrPr.recompMin_dist();
	}
	
}
