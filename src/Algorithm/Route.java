package Algorithm;

import Entity.Flow;
import Entity.Path;
import Entity.TrafficDemand;

public class Route {
	public static void init(TrafficDemand trafficDemand, AllPaths allPaths) {
		for (Flow flow : trafficDemand.flows) {
			Path path=allPaths.getPath(flow.pair, 2, flow.demands.get(0), 1.0);
			//System.out.println(graph.getWorkLinkNum());
			flow.paths.set(0, path);
			//System.out.println("flow's name is"flow.pair);
			//if(Math.abs(flow.demands.get(0))>0)System.out.println("the path for flow "+flow.pair+" is "+path+ " flow's demand is "+flow.demands.get(0));
			if(Math.abs(flow.demands.get(0))>0) path.add(flow.demands.get(0));
		}
		
		
	}
	
	
}
