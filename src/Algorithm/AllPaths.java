package Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Entity.Graph;
import Entity.Link;
import Entity.Node;
import Entity.Path;

public class AllPaths {

	public Map<String, List<Path>> allPaths = new HashMap<String, List<Path>>();

	public List<Node> nodes;
	public boolean[][] matrix;

	public AllPaths(Graph graph) {
		nodes = graph.nodes;
		matrix = new boolean[nodes.size()][nodes.size()];
		for (Link link : graph.links) {
			//System.out.println(link.topoState);
			if(link.topoState){
				matrix[link.srcID][link.dstID] = true;
				matrix[link.dstID][link.srcID] = true;
			}
		}
		
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				String pair = Utils.getPair(i, j);
				//System.out.println("src: "+i+" dst "+j+" "+pair );
				List<List<Node>> nodesList = findAllPath(nodes.get(i), nodes.get(j));
				List<Path> paths = new ArrayList<>();
				for (List<Node> nodes : nodesList) {
					Path path = new Path();
					path.nodes = nodes;//这个是跟实际路径相符的节点列表
					path.links(graph);
					paths.add(path);
				}
				
				Collections.sort(paths, new Comparator<Path>() {
					@Override
					public int compare(Path o1, Path o2) {
						// TODO Auto-generated method stub
						return Integer.compare(o1.nodes.size(), o2.nodes.size());
					}//按照hop count 从小到大
				});
				//System.out.println("the corresponding paths is " + paths);
				allPaths.put(pair, paths);
			}//对每一个节点对
		}
	}


	public List<Path> getPaths(String pair,int sort) {
		
		List<Path> paths = new ArrayList<>(allPaths.get(pair));
		if(sort==1){
			Collections.sort(paths, new Comparator<Path>() {
				@Override
				public int compare(Path o1, Path o2) {
					// TODO Auto-generated method stub
					return Integer.compare(o1.getCost(), o2.getCost());
				}
			});
		}else if(sort==2){
			Collections.sort(paths, new Comparator<Path>() {
				@Override
				public int compare(Path o1, Path o2) {
					// TODO Auto-generated method stub
					return Double.compare(o1.getLatency(), o2.getLatency());
				}
			});
		}
		return paths;
	}
	
	public Path getPath(String pair, int sort, double demand, double MLU) {
		//只返回一条路径
		List<Path> paths = new ArrayList<>(getPaths(pair,Math.abs(sort)));
		if(sort<=0){
			return paths.get(0).isAvailable(demand, MLU)?paths.get(0):null;
		}else{
			for (Path path : paths) {
				if(path.isAvailable(demand, MLU)){
					return path;
				}
			}
			return null;
		}
	}
	
	// 深度优先遍历
	public List<List<Node>> findAllPath(Node start, Node end) {
		List<List<Node>> paths = new ArrayList<>();
		Deque<Node> stack = new LinkedList<>();
		// 防止产生回路
		boolean[] isVisited = new boolean[nodes.size()];

		// initial
		paths.clear();
		stack.clear();
		for (int i = 0; i < isVisited.length; i++) {
			isVisited[i] = false;
		}
		stack.addFirst(start);
		isVisited[start.ID] = true;
		Node current = null;
		// next 为 null 说明当前current的相邻节点一个都没有访问
		Node next = null;
		Node AdjacencyNode = null;
		while (!stack.isEmpty()) {
			current = stack.peekFirst();
			if (current.equals(end)) {
				List<Node> path = new ArrayList<>();
				for (Node v : stack) {
					path.add(v);
				}
				Collections.reverse(path);
				paths.add(path);
				AdjacencyNode = stack.pollFirst();
				isVisited[AdjacencyNode.ID] = false;
			} else {
				// 访问top_node的下一个邻接点
				next = nextNode(current, AdjacencyNode, isVisited);
				if (next != null) {
					stack.addFirst(next);
					isVisited[next.ID] = true;
					AdjacencyNode = null;
				} else {
					// 不存在临接点，将stack top元素退出
					AdjacencyNode = stack.pollFirst();
					isVisited[AdjacencyNode.ID] = false;
				}
			}
		}
		return paths;
	}

	private Node nextNode(Node node, Node next, boolean[] isVisited) {
		int i = 0;
		if (next == null) {
			i = 0;
		} else {
			i = next.ID + 1;
		}
		for (; i < matrix[node.ID].length; i++) {
			if (matrix[node.ID][i] && isVisited[i] == false) {
				return nodes.get(i);
			}
		}
		return null;
	}
}
