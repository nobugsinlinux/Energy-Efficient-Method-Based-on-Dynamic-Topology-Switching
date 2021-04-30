package Entity;

import java.util.ArrayList;
import java.util.List;

import Algorithm.Utils;

public class Path {
	public List<Node> nodes;
	public List<Link> links;
	
	@Override
	public String toString() {
		return "Path [nodes=" + nodes + "]";
	}

	public int getCost(){
		int ret=0;
		for (Link link : links) {
			if(!link.topoState){
				ret++;
			}
		}
		return ret;
	}
	
	public double getLatency(){
		double ret=0;
		for (Link link : links) {
			ret+=link.latency;
		}
		return ret;
	}
	
	public boolean isAvailable(double demand, double MLU) {
		for (Link link : links) {
			if(!link.isAvailable(demand, MLU)){
				return false;
			}
		}
		return true;
	}
	
	public void add(double demand) {
		
		for (Link link : links) {
			link.rate+=demand;
			link.topoState = true;
		}
	}
	public boolean add_withoutchangelinkstate(double demand) {
		for (Link link : links) {
			link.rate+=demand;
			if (link.rate>link.bandWidth)return false;
		}
		return true;
	}
	
	public void sub(double demand) {
		for (Link link : links) {
			link.rate-=demand;
		}
	}
	
	public void links(Graph graph) {
		links = new ArrayList<>();
		for (int k = 1; k < nodes.size(); k++) {
			//System.out.println(graph.getLink(Utils.getPair(nodes.get(k - 1).ID, nodes.get(k).ID)));
			links.add(graph.getLink(Utils.getPair(nodes.get(k - 1).ID, nodes.get(k).ID)));
		}
	}
	public boolean Judge_Link(Link MluLink) {
		for (Link link : links) {
			if (link.name.equals(MluLink.name)) return true;
		}
		return false;
	}

	public boolean Judge_workingT() {//如果路径上有链路处于休眠状态
		// TODO Auto-generated method stub
		for (Link link : links) {
			if(!link.topoState) return true;
		}
		return false;
		
	}

	
	

public boolean Judge_whetherCongestion(double tmpFlow_demand, double max_λ) {
	// TODO Auto-generated method stub
	for(Link link: links) {
			if(!link.isAvailable(tmpFlow_demand,max_λ)) {
				
				return false;
			}
		}
	return true;
}
}
	
