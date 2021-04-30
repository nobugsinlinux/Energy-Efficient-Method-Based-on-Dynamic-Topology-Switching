package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Algorithm.AllPaths;
import Algorithm.Route;
import Algorithm.TLS_backup;
import Algorithm.TLS;
import Algorithm.TS;
import Algorithm.Utils;
import Data.XML;
import Entity.Flow;
import Entity.Graph;
import Entity.Link;
import Entity.Node;
import Entity.Path;
import Entity.TrafficDemand;

public class Main {
	public static Graph graph;
	public static AllPaths allPaths;
	public static TrafficDemand trafficDemand;
	public static XML xml;

	public static void testAllPaths() {
		graph = new Graph();
		allPaths = new AllPaths(graph);
		for (int i = 0; i < graph.nodes.size(); i++) {
			for (int j = i + 1; j < graph.nodes.size(); j++) {
				String pair = Utils.getPair(i, j);
				List<Path> paths = allPaths.getPaths(pair, 0);
				System.out.println(pair + ":" + paths.size());
			}
		}
	}

	public static void testXML() {
		graph = new Graph();
		allPaths = new AllPaths(graph);
		trafficDemand = new TrafficDemand(graph);
		xml = new XML(trafficDemand);

		for (Flow flow : trafficDemand.flows) {
			for (Double demand : flow.demands) {
				System.out.print(demand + " ");
			}
			System.out.println();
		}
	}

	public static void testInit() {
		graph = new Graph();
		allPaths = new AllPaths(graph);
		trafficDemand = new TrafficDemand(graph);
		xml = new XML(trafficDemand);
		Route.init(trafficDemand, allPaths);
		for (Flow flow : trafficDemand.flows) {
			Path path = flow.paths.get(0);
			System.out.print(flow.pair + " ");
			for (Node node : path.nodes) {
				System.out.print(node.ID + " ");
			}
			System.out.println();
		}
	}

	public static void init() {
		graph = new Graph();
		
		allPaths = new AllPaths(graph);
		trafficDemand = new TrafficDemand(graph);
		xml = new XML(trafficDemand);
		graph.initLinks(false);
		
	//	System.out.println(graph.getWorkLinkNum());
		//Route.init(trafficDemand, allPaths);
		 for (Flow flow : trafficDemand.flows) {
			Path path=allPaths.getPath(flow.pair, 1, flow.demands.get(0), 1.0);
			//System.out.println(graph.getWorkLinkNum());
			flow.paths.set(0, path);
			//System.out.println("flow's name is"flow.pair);
			//if(Math.abs(flow.demands.get(0))>0)System.out.println("the path for flow "+flow.pair+" is "+path+ " flow's demand is "+flow.demands.get(0));
			if(Math.abs(flow.demands.get(0))>0) path.add(flow.demands.get(0));
		}
		//System.out.println(graph.getWorkLinkNum());
		//graph.testinitmap();
	}
	public static void init_tls() {
		graph = new Graph();
		
		allPaths = new AllPaths(graph);
		trafficDemand = new TrafficDemand(graph);
		xml = new XML(trafficDemand);
		
		
	
		Route.init(trafficDemand, allPaths);
		
	}

	public static void LUs() {
  		List<Double> ALUs = new ArrayList<>();
		List<Double> MLUs = new ArrayList<>();
		List<Map<String, Double>> LUs = new ArrayList<>();
		for (int i = 0; i < trafficDemand.size(); i++) {
			graph.initLinks(true);
			for (Flow flow : trafficDemand.flows) {
				for (Link link : flow.paths.get(i).links) {
					link.rate += flow.demands.get(i);
				}
			}
			double ALU = 0.0;
			double MLU = 0.0;
			Map<String, Double> LU = new HashMap<>();
			for (String pair : graph.linkMap.keySet()) {
				double temp = graph.linkMap.get(pair).getLU();
				ALU += temp;
				if (temp > MLU) {
					MLU = temp;
				}
				LU.put(pair, temp);
			}
			ALU /= graph.getWorkLinkNum();
			ALUs.add(ALU);
			System.out.println(ALU);
			MLUs.add(MLU);
			// System.out.println(MLU);
			LUs.add(LU);
		}
	}

	public static void tls(double MLU) {
		TLS_backup tls = new Algorithm.TLS_backup(graph, trafficDemand, MLU);
	}

	public static void main(String[] args) {
		
		init();
		//TS (our method)
		//ts();
		
	    //TLS (baseline method)
		tls();
		
		
		
		
		
	}

	private static void tls() {
		// TODO Auto-generated method stub
		double[] alpha= {0.5,0.6,0.7,0.8,1.0};
		for (int i=0;i<alpha.length;i++) {
			TLS tls = new TLS(graph, trafficDemand,allPaths,alpha[i]);
			System.out.println();
		}
	}

	private static void ts() {
		// TODO Auto-generated method stub
		double[] upper= {0.5,0.6,0.7,0.8,1.0};
		double[] lower= {0.4,0.5,0.6,0.7,0.8};
		for (int i=0;i<upper.length;i++) {
			for(int j=0;j<lower.length;j++) {
				if (upper[i]>lower[j]) {
					
					TS ts = new TS(graph, trafficDemand,allPaths,upper[i],lower[j]);
					
				}
			}
			System.out.println("\n");
		}
	}
}
