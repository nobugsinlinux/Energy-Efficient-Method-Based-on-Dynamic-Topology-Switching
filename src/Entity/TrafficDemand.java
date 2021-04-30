package Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Algorithm.Utils;

public class TrafficDemand {
	public List<Flow> flows;
	public Map<String, Flow> flowMap;

	public TrafficDemand(Graph graph) {
		flows = new ArrayList<>();
		flowMap = new HashMap<String, Flow>();
		for (int i = 0; i < graph.nodes.size(); i++) {
			for (int j = 0; j < graph.nodes.size(); j++) {
				if (i != j) {
					Flow flow = new Flow();
					flow.src = graph.nodes.get(i);
					flow.dst = graph.nodes.get(j);
					flow.pair = Utils.getPair(flow.src.ID, flow.dst.ID);
					flow.demands=new ArrayList<>();
					flow.paths=new ArrayList<>();
					flows.add(flow);
					flowMap.put(Utils.getPair(flow.src.name, flow.dst.name), flow);
				}
			}
		}
	}

	public TrafficDemand(TrafficDemand trafficDemand) {
		flows = new ArrayList<>();
		flowMap = new HashMap<String, Flow>();
		for (Flow flow0 : trafficDemand.flows) {
			Flow flow= new Flow(flow0);
			flows.add(flow);
			flowMap.put(Utils.getPair(flow.src.name, flow.dst.name), flow);
		}
	}
	
	public void addAll() {
		for (Flow flow : flows) {
			flow.demands.add(0.0);
			flow.paths.add(null);
		}
	}

	public void addOne(String name, double demand) { 
		Flow flow = flowMap.get(name);
		flow.demands.set(flow.demands.size() - 1, demand);
	}

	public int size() {
		return flows.get(0).demands.size();
	}
}
