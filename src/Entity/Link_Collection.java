package Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import Algorithm.AllPaths;
import Algorithm.Utils;

public class Link_Collection {//the flow it contained is a copy of the original flow, the main purpose is to record the backup path
       public List<Link_FowC> allLinks ; //allLink store all link's flow info 
       public Graph graph;
       public int index; 
       /*****************************************************/  
       Map<Integer, Double> mapPR_LSLD     =  new HashMap<Integer, Double>();
       Map<Integer, Double> mapDOSLFR_LSLD =  new HashMap<Integer, Double>();
       Map<Integer, Integer> mapNOBP_LSLD  =  new HashMap<Integer, Integer>();
       Map<Integer, Double> mapPR_LSFD     =  new HashMap<Integer, Double>();
       Map<Integer, Double> mapDOSLFR_LSFD =  new HashMap<Integer, Double>();
       Map<Integer, Integer> mapNOBP_LSFD  =  new HashMap<Integer, Integer>();
       
       Map<Integer, Double>  mapPR_LSLDAndLSFD     = new HashMap<Integer, Double>();
       Map<Integer, Double>  mapDOSLFR_LSLDAndLSFD = new HashMap<Integer, Double>();
       Map<Integer, Integer> mapNOBP_LSLDAndLSFD   = new HashMap<Integer, Integer>();
       
       double tempDOSLFRSumLSLD = 0.0; // 存所有时延的和
       double tempDOSLFRSumLSFD = 0.0;
	/*
	 * public double tempDOSLFRSumLSLD; // 存所有时延的和 public double tempDOSLFRSumLSFD;
	 * // 存所有时延的和
	 * 
	 * Map<Integer, Double> mapPR_LSLD; Map<Integer, Double> mapDOSLFR_LSLD;
	 * Map<Integer, Integer> mapNOBP_LSLD; Map<Integer, Double> mapPR_LSFD;
	 * Map<Integer, Double> mapDOSLFR_LSFD; Map<Integer, Integer> mapNOBP_LSFD;
	 * 
	 * Map<Integer, Double> mapPR_LSLDAndLSFD; Map<Integer, Double>
	 * mapDOSLFR_LSLDAndLSFD; Map<Integer, Integer> mapNOBP_LSLDAndLSFD;
	 */
       
       /*****************************************************/
       
       
       public Link_Collection(Graph graph,TrafficDemand trafficdemand,int index) {
    	   /******************************************/
    	  
    	   /*****************************************/
    	   this.graph= graph;
    	   allLinks = new ArrayList<>();
    	   this.index=index;
    	   for (Link link: this.graph.links) { // for each link, store the link-related flow into Link_FowC
    		   Link_FowC tmplink = new Link_FowC(link,trafficdemand,index);
    		   allLinks.add(tmplink);
    	   }
       }
       public void FR_LSLD() {
    	   //for each link , calculate the sp, and then update flow's path at time period index(shuold first uninstall the previous path)
    	   // after install the sp , you can derive some metrics you want.
      		/*****************************************************************/
    	   resetMapsAndSums(); // 重置所有参数
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   for (Link_FowC linkflow: allLinks) {//在计算相关指标时，可能只在一条链路上，进行操作 （所有的链路）
    		   //1)calculate the sp 
    		   boolean isNOBPHavePath = false;
    		   for(Flow flow: linkflow.link_flowlist) { // 经过这条链路的所有流
    			   boolean flag = getPath_LSLD(flow,this.index,linkflow.link);// set the sp, for this flow.
    			   if (flag) { // 找到路径
    				   ++tempPRSum;
    				   isNOBPHavePath = true;
    			   }
    		   }
    		   if (isNOBPHavePath)  ++NOBP;

    		   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size();
    	   } 
    	   // 计算并写入PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	1.0 * PRTemp         / allLinks.size();    // PR:     path不空的和 /  所有链路的流
			   DOSLFR = tempDOSLFRSumLSLD    / allLinks.size();    // DOSLFR： path不空时，累加时延和 /  所有链路的流
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSLD.put(index, PR);
    	   mapDOSLFR_LSLD.put(index, DOSLFR);
    	   mapNOBP_LSLD.put(index, NOBP);

       }

       public boolean getPath_LSLD(Flow flow, int index, Link link) { // link is 
    	/*******************默认没有找到路径*********************************************/
    	boolean isGetPath_LSLD = false;
    	/****************************************************************************/
   		SimpleGraph<Node, DefaultEdge> jgraph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);

   		List<Node> nodes = this.graph.nodes;
   		for (int i = 0; i < nodes.size(); i++) {
   			jgraph.addVertex(nodes.get(i));
   		}

   		List<Link> links = this.graph.getWorkLinks();
   		for (int i = 0; i < links.size(); i++) {
   			//加一个条件，判断是否为故障边
   			if (!links.get(i).name.equals(link.name)) {
   				jgraph.addEdge(nodes.get(links.get(i).srcID), nodes.get(links.get(i).dstID));
   			}
   			
   		}
   		
   		DijkstraShortestPath<Node, DefaultEdge> dijk = new DijkstraShortestPath<>(jgraph);
   		//for LSLD : 
   		List<Node> tmp_Node=flow.paths.get(index).nodes; //这条流在时刻index 的路径节点列表
   		Node src=null;
   		Node dst=null;
   		
   		for(Node node : nodes ) {
   			if(node.ID == link.srcID ) {
   				src = node;
   			}
   			else if(node.ID == link.dstID) {
   				dst =node;
   			}
   			else continue;
   		}
   		
   		GraphPath<Node, DefaultEdge> dijkPath_middle = dijk.getPath(src, dst);//? 如果相同会如何？

   		if ( dijkPath_middle !=null  ) {
   			
   			/*******************找到路径*****************************/
   			isGetPath_LSLD = true; 
   			/*******************************************************/
   			List<Node> node_middle=  dijkPath_middle.getVertexList();
   	   		/*System.out.println("link info"+link);
   	   		System.out.println("generate nodes"+node_middle);
   	   		System.out.println("flow's node"+tmp_Node);*/
   	   		Path path = new Path();
   	   		//judge whether generate node need reverse
   	   		int srcid=link.srcID,srcindex=-1;
   	   		int dstid=link.dstID,dstindex=-1;
   	   		
   	   		for(int i=0;i<tmp_Node.size();i++) {
   	   			if(srcid==tmp_Node.get(i).ID)srcindex=i;
   	   			else if(dstid==tmp_Node.get(i).ID) dstindex=i;
   	   			else continue;
   	   		}
   	   		if(srcindex>dstindex)Collections.reverse(node_middle);
   			
   			path.nodes = new ArrayList<>();
   			for(int i=0;i< tmp_Node.size(); i++ ) {
   				if(tmp_Node.get(i).ID==node_middle.get(0).ID) {
   					for(Node node: node_middle) path.nodes.add(node);
   				}
   				else if(tmp_Node.get(i).ID==node_middle.get(node_middle.size()-1).ID) continue;
   				else path.nodes.add(tmp_Node.get(i));
   			}
   			
   			path.links(graph);
   			for(int i=0; i< path.nodes.size();i++) {
   				if(path.nodes.get(i).ID==srcid || path.nodes.get(i).ID==dstid ) {
   					for(int j=i;j<path.links.size();j++) { // 遍历所有节点i，遇到  故障点，j从i开始累加时延
   						//这里面进行备份路径时延计算
   						/******************************************************************/
   						tempDOSLFRSumLSLD += path.links.get(j).latency; // 从j开始累加时延 latency
   						/******************************************************************/
   					}
   					break;
   				}
   			}
   		
   		}
   		/**********************返回是否找到路径的标志*********************/
   		return isGetPath_LSLD; 
   		/*****************************************************************/
   		//flow.paths.set(index, path);
   	}
       public void FR_LSFD() {
    	   
      /*****************************************************************/
    	   resetMapsAndSums(); // 重置所有参数
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   
      	   for (Link_FowC linkflow: allLinks) { //在计算相关指标时，可能只在一条链路上，进行操作
    		   //1)calculate the sp 
      		   //System.out.println("the link is "+ linkflow.link.name);
    		   for(Flow flow: linkflow.link_flowlist) {
    			   boolean flag = getPath_LSFD(flow,this.index,linkflow.link);// set the sp, for this flow.
    			   if (flag) { // 找到路径
    				   ++tempPRSum;
    				   ++NOBP;
    			   }
    		   }
    		   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size();
    	    }
      	   // 计算并写入PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	1.0 * PRTemp        / allLinks.size();    // PR:     path不空的和 /  所有链路的流
			   DOSLFR = tempDOSLFRSumLSFD   / allLinks.size();    // DOSLFR： path不空时，累加时延和 /  所有链路的流
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSFD.put(index, PR);
    	   mapDOSLFR_LSFD.put(index, DOSLFR);
    	   mapNOBP_LSFD.put(index, NOBP);
    		  
       }
       
   public boolean getPath_LSFD(Flow flow, int index, Link link) { // link is 
	   
	   /*******************默认没有找到路径*************************/
      
	   boolean isGetPath_LSFD = false;   
       
       /************************************************/
   	   SimpleGraph<Node, DefaultEdge> jgraph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);

   		List<Node> nodes = this.graph.nodes;
   		for (int i = 0; i < nodes.size(); i++) {
   			jgraph.addVertex(nodes.get(i));
   			
   		}
           
   		List<Link> links = this.graph.getWorkLinks();
   		
   		for (int i = 0; i < links.size(); i++) {
   			//加一个条件，判断是否为故障边
   			if (!links.get(i).name.equals(link.name)) {
   				jgraph.addEdge(nodes.get(links.get(i).srcID), nodes.get(links.get(i).dstID));
   			}
   			
   		}
   		
   		DijkstraShortestPath<Node, DefaultEdge> dijk = new DijkstraShortestPath<>(jgraph);
   		List<Node> tmp_Node=flow.paths.get(index).nodes;
   		Node src=null;
   		Node dst=null;
   		
   		for(Node node : tmp_Node ) {
   			if(node.ID == link.srcID ) {
   				src = node;
   				break;
   			}
   			else if(node.ID == link.dstID) {
   				src =node;
   				break;
   			}
   			else continue;
   		}
   		GraphPath<Node, DefaultEdge> dijkPath = dijk.getPath(src, flow.dst);
  			
   		  if (dijkPath != null) {
   			
   	   			
   	   			/*******************找到路径*****************************/
   			isGetPath_LSFD = true; 
   	   			/*******************************************************/
   	   			List<Node> node_middle=  dijkPath.getVertexList();
   	   			
   	   	   		/*System.out.println("link info"+link);
   	   	   		System.out.println("generate nodes"+node_middle);
   	   	   		System.out.println("flow's node"+tmp_Node);*/
   	   	   		Path path = new Path();
   	   	   		//judge whether generate node need reverse
   	   	   		path.nodes= new ArrayList();
   	   	         for(Node node: node_middle) {
   	   	        	 path.nodes.add(node);
   	   	         }
   	   			path.links(graph);
   	   			
   	   			for(Link l:path.links) {
   	   			   tempDOSLFRSumLSLD += l.latency;
   	   			}
   	   			
   	   			
   	   		
   	   		}
   		return isGetPath_LSFD; 
   		/*****************************************************************/
   	} 
   
   
/*--------------------------   LSLD AND LSFD          -----------------------------------------------*/
   
       public void FR_LSLDandLSFD(AllPaths allPaths) {//our method
      		/*************************** 重置所有参数**************************************/
    	   	resetMapsAndSums(); 
    	   	
      		/*****************************************************************/

    	   Map<String, List<Path>> allPaths_tmp = allPaths.allPaths;
    	    // LSLD calculate all path between the corresponding link,
    	   
    	   /************************初始化********************************/
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   /*****************************************************************/
    	   
    	   for (Link_FowC linkflow: allLinks) {//在计算相关指标时，可能只在一条链路上，进行操作
   			   String pair = Utils.getPair(linkflow.link.srcID, linkflow.link.dstID);
   			   
    		   List<Path> paths = new ArrayList<>(allPaths_tmp.get(pair));
    		   
    		   boolean flag_all=false;//indicate whether there is a path chosen; 
    		   for(Path path:paths) {  //1)first for every link, calculate the non-congested path for all it's carried flow 
    			   boolean flag=true;//indicate whether path is overload
    			   if(judge_path(path,linkflow))continue;
    			   ArrayList<Integer> store_index=new ArrayList<Integer>();
    			   int i=0;
    			   
    			   for(Flow flow: linkflow.link_flowlist) {//move traffic to path
    				   store_index.add(i++);
    				   if(path.add_withoutchangelinkstate(flow.demands.get(this.index))) continue;
    				   else {
    					   flag=false;
    					   break;
    				   }
    			   } 
    			   //undo the previous installation
    			   for(Integer index2: store_index) {
    					 path.sub(linkflow.link_flowlist.get(index2).demands.get(this.index));
    			   }
    			   if(flag==true) {
    				   flag_all=true;
    				   
    				    for(Flow flow:linkflow.link_flowlist) {
    				    	
    			   		  getpath_LSLDandLSFD(path,flow,index, linkflow.link); //set backup-path for this flow
    			   		  /****************************************************/
    			   		  ++tempPRSum; // 只要flag=true，就在里层for循环里++
    			   		  
    			   		  for (int k = 0; k < path.nodes.size(); ++k) {
    			   			  if (path.nodes.get(k).ID == linkflow.link.srcID || path.nodes.get(k).ID == linkflow.link.dstID) {
    			   				  for (int j = k; j < path.links.size(); ++j) { // 遍历所有节点i，遇到  故障点，j从i开始累加时延
    			   					  //这里面进行备份路径时延计算
    		   						  /******************************************************************/
    		   						  tempDOSLFRSumLSLD += path.links.get(j).latency; // 从j开始累加时延 latency
    		   						  /******************************************************************/
    		   					  }
    		   					  break;
    		   				  }
    		   			  }
    			   		  
    			   		 /**********************************************************/
    				    }
    				    ++NOBP;   // 只有flag=true才在里层for循环外加一次
    				    PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // 不管flag是不是=true，只要是LSLD就++
    				    break;
    		       }
    			   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // 这个值不管flag是不是true都要累加，所以如果上面flag=false在这里+
				   
    		    }
    		    if(flag_all==false) {	//LSFD method
    			   for(Flow flow: linkflow.link_flowlist) {
        			   getPath_LSFD(flow,this.index,linkflow.link);// set the sp, for this flow.
        			   ++tempPRSum; // 只要flag=true，就在里层for循环里++
        			   ++NOBP;      // 只有flag=true就在里层for循环里加一次
        			   
        		   }
    			   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // 不管flag是不是=true，就要++
    		    }
    		    
             }
    	   // 计算并写入PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	                           1.0 * PRTemp   / allLinks.size();    // PR:     path不空的和 /  所有链路的流
			   DOSLFR = (tempDOSLFRSumLSFD + tempDOSLFRSumLSLD)   / allLinks.size();    // DOSLFR： path不空时，累加时延和 /  所有链路的流
			  // 这里没有计算tempDOSLFRSumLSLD！！！！
			   
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSLDAndLSFD.put(index, PR);
    	   mapDOSLFR_LSLDAndLSFD.put(index, DOSLFR);
    	   mapNOBP_LSLDAndLSFD.put(index, NOBP);
    	   
    }
/***************** 把参数重置  **********************/
   public void resetMapsAndSums() {
	   
		/*
		 * mapPR_LSLD.clear(); mapDOSLFR_LSLD.clear(); mapNOBP_LSLD.clear();
		 * mapPR_LSFD.clear(); mapDOSLFR_LSFD.clear(); mapNOBP_LSFD.clear();
		 * 
		 * mapPR_LSLDAndLSFD.clear(); mapDOSLFR_LSLDAndLSFD.clear();
		 * mapNOBP_LSLDAndLSFD.clear();
		 */
       
       tempDOSLFRSumLSLD = 0.0; 
       tempDOSLFRSumLSFD = 0.0;
   }
	  
/*****************************************************/
       
       
	private boolean judge_path(Path path, Link_FowC linkflow) {
		// TODO Auto-generated method stub
		for(Link link:path.links) {
			if(link.name.equals(linkflow.link.name))return false;
		}
		return true;
	}

	private void getpath_LSLDandLSFD(Path path, Flow flow, int index2, Link link) {
		// TODO Auto-generated method stub
		Path path2 = new Path();
   		//judge whether generate node need reverse
   		int srcid=link.srcID,srcindex=-1;
   		int dstid=link.dstID,dstindex=-1;
   		List<Node> tmp_Node=flow.paths.get(index2).nodes;
   		for(int i=0;i<tmp_Node.size();i++) {
   			if(srcid==tmp_Node.get(i).ID)srcindex=i;
   			else if(dstid==tmp_Node.get(i).ID) dstindex=i;
   			else continue;
   		}
   		if(srcindex>dstindex)Collections.reverse(path.nodes);
   		    path2.nodes = new ArrayList<>();
   			for(int i=0;i< tmp_Node.size(); i++ ) {
   				if(tmp_Node.get(i).ID==path.nodes.get(0).ID) {
   					for(Node node: path.nodes) path2.nodes.add(node);
   				}
   				else if(tmp_Node.get(i).ID==path.nodes.get(path.nodes.size()-1).ID) continue;
   				else path2.nodes.add(tmp_Node.get(i));
   	     }
   		path2.links(graph);
   		
	}
      	  
}
