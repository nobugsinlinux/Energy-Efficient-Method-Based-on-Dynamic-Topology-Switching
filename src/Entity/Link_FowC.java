package Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Link_FowC {
    public Link link;
    public List<Flow> link_flowlist;//store the flow that pass through this link
	public Link_FowC (Link link, TrafficDemand trafficdemand , int index) {
		link_flowlist =new ArrayList<>();
		this.link=link;
		for(Flow flow : trafficdemand.flows) {
		   Path path= flow.paths.get(index);
		   for (Link tmplink: path.links) {
			if (tmplink.name.equals(link.name)) {
				link_flowlist.add(flow);
				break;
			  }
		    }
		}
		
	}
	
}
