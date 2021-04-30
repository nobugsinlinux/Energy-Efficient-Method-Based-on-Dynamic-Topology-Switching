package Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import Entity.Flow;
import Entity.Graph;
import Entity.Link;
import Entity.Node;
import Entity.Path;
import Entity.TrafficDemand;

public class TLS_backup {
	public Graph graph;
	public TrafficDemand trafficDemand;
	public double MLU;

	public boolean greedyDelete(int index) {
		List<Link> links = graph.getWorkLinks();
		Collections.sort(links, new Comparator<Link>() {
			public int compare(Link h1, Link h2) {
				return Double.compare(h1.rate, h2.rate);
			}
		});
		for (Link link : links) {
			link.topoState = false;
			if (route(index)) {
				greedyDelete(index);
				return true;
			}
			link.topoState = true;
		}
		return false;
	}

	public boolean route(int index) {
		graph.resetLinks();
		for (Flow flow : trafficDemand.flows) {
			getPath(flow, index);
			if (flow.paths.get(index).nodes != null
					&& flow.paths.get(index).isAvailable(flow.demands.get(index), MLU)) {
				flow.paths.get(index).add(flow.demands.get(index));
			} else {
				return false;
			}
		}
		return true;
	}

	public void getPath(Flow flow, int index) {
		SimpleGraph<Node, DefaultEdge> jgraph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);

		List<Node> nodes = graph.nodes;
		for (int i = 0; i < nodes.size(); i++) {
			jgraph.addVertex(nodes.get(i));
		}

		List<Link> links = graph.getWorkLinks();
		for (int i = 0; i < links.size(); i++) {//加一个条件，判断是否为故障边
			jgraph.addEdge(nodes.get(links.get(i).srcID), nodes.get(links.get(i).dstID));
		}

		DijkstraShortestPath<Node, DefaultEdge> dijk = new DijkstraShortestPath<>(jgraph);
		GraphPath<Node, DefaultEdge> dijkPath = dijk.getPath(flow.src, flow.dst);
		Path path = new Path();
		if (dijkPath != null) {
			path.nodes = new ArrayList<>(dijkPath.getVertexList());
			path.links(graph);
		}
		flow.paths.set(index, path);
	}

	public int[] offPeak() {
		int[] ret = new int[4];
		boolean[][] flag = new boolean[96][96];
		for (int i = 25; i < 26; i++) {
			graph.initLinks(true);
			greedyDelete(i);
			for (int j = 0; j < trafficDemand.size(); j++) {
				flag[i][j] = route(j);
			}
			int left = 0, right = 0, save = 0;
			for (int j = 20; j > -76; j--) {
				left = (j + 96) % 96;
				if (!flag[i][left]) {
					left++;
					break;
				}
			}
			for (int j = 20; j < 96; j++) {
				right = j;
				if (!flag[i][right]) {
					right--;
					break;
				}
			}
			save = ((right - left + 97) % 97) * graph.getSleepLinkNum();
			if (ret[3] < save) {
				ret[0] = i;
				ret[1] = left;
				ret[2] = right;
				ret[3] = save;
			}
			// System.out.println(i + " " + save + " " + graph.getSleepLinkNum()
			// + " " + left + " " + right);
		}
		for (int i = 3; i < ret.length; i++) {
			// System.out.println(ret[i]/3456.0);
		}
		return ret;
	}

	public TLS_backup(Graph graph, TrafficDemand trafficDemand, double MLU) {
		this.graph = graph;
		this.trafficDemand = new TrafficDemand(trafficDemand);
		this.MLU = MLU;
		int[] offPeak = offPeak();
		this.trafficDemand = trafficDemand;

		List<Double> ALUs = new ArrayList<>();
		List<Double> MLUs = new ArrayList<>();
		List<Map<String, Double>> LUs = new ArrayList<>();

		graph.initLinks(true);
		greedyDelete(25);
		int work = 0;
		for (int i = 0; i < trafficDemand.size(); i++) {
			work = graph.links.size();
			graph.resetLinks();
			if (offPeak[1] > offPeak[2]) {
				if ((i % 96) < offPeak[2] || (i % 96) > offPeak[1]) {
					route(i);
					work = graph.getWorkLinkNum();
				}
			} else {
				if ((i % 96) < offPeak[2] && (i % 96) > offPeak[1]) {
					route(i);
					work = graph.getWorkLinkNum();
				}
			}
			for (Flow flow : trafficDemand.flows) {
				flow.paths.get(i).add(flow.demands.get(i));
			}
			double ALU = 0.0;
			double MLU1 = 0.0;
			Map<String, Double> LU = new HashMap<>();
			for (String pair : graph.linkMap.keySet()) {
				double temp = graph.linkMap.get(pair).getLU();
				ALU += temp;
				if (temp > MLU1) {
					MLU1 = temp;
				}
				LU.put(pair, temp);
			}
			ALU /= work;
			ALUs.add(ALU);
			// System.out.println(ALU);
			MLUs.add(MLU1);
			// System.out.println(MLU);
			LUs.add(LU);
		}
		double u = 0;
		for (int i = 0; i < MLUs.size(); i++) {
			if(u<MLUs.get(i)){
				u=MLUs.get(i);
			}
		}
	}
}
