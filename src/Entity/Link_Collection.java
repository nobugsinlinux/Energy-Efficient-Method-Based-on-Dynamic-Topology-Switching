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
       
       double tempDOSLFRSumLSLD = 0.0; // ������ʱ�ӵĺ�
       double tempDOSLFRSumLSFD = 0.0;
	/*
	 * public double tempDOSLFRSumLSLD; // ������ʱ�ӵĺ� public double tempDOSLFRSumLSFD;
	 * // ������ʱ�ӵĺ�
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
    	   resetMapsAndSums(); // �������в���
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   for (Link_FowC linkflow: allLinks) {//�ڼ������ָ��ʱ������ֻ��һ����·�ϣ����в��� �����е���·��
    		   //1)calculate the sp 
    		   boolean isNOBPHavePath = false;
    		   for(Flow flow: linkflow.link_flowlist) { // ����������·��������
    			   boolean flag = getPath_LSLD(flow,this.index,linkflow.link);// set the sp, for this flow.
    			   if (flag) { // �ҵ�·��
    				   ++tempPRSum;
    				   isNOBPHavePath = true;
    			   }
    		   }
    		   if (isNOBPHavePath)  ++NOBP;

    		   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size();
    	   } 
    	   // ���㲢д��PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	1.0 * PRTemp         / allLinks.size();    // PR:     path���յĺ� /  ������·����
			   DOSLFR = tempDOSLFRSumLSLD    / allLinks.size();    // DOSLFR�� path����ʱ���ۼ�ʱ�Ӻ� /  ������·����
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSLD.put(index, PR);
    	   mapDOSLFR_LSLD.put(index, DOSLFR);
    	   mapNOBP_LSLD.put(index, NOBP);

       }

       public boolean getPath_LSLD(Flow flow, int index, Link link) { // link is 
    	/*******************Ĭ��û���ҵ�·��*********************************************/
    	boolean isGetPath_LSLD = false;
    	/****************************************************************************/
   		SimpleGraph<Node, DefaultEdge> jgraph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);

   		List<Node> nodes = this.graph.nodes;
   		for (int i = 0; i < nodes.size(); i++) {
   			jgraph.addVertex(nodes.get(i));
   		}

   		List<Link> links = this.graph.getWorkLinks();
   		for (int i = 0; i < links.size(); i++) {
   			//��һ���������ж��Ƿ�Ϊ���ϱ�
   			if (!links.get(i).name.equals(link.name)) {
   				jgraph.addEdge(nodes.get(links.get(i).srcID), nodes.get(links.get(i).dstID));
   			}
   			
   		}
   		
   		DijkstraShortestPath<Node, DefaultEdge> dijk = new DijkstraShortestPath<>(jgraph);
   		//for LSLD : 
   		List<Node> tmp_Node=flow.paths.get(index).nodes; //��������ʱ��index ��·���ڵ��б�
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
   		
   		GraphPath<Node, DefaultEdge> dijkPath_middle = dijk.getPath(src, dst);//? �����ͬ����Σ�

   		if ( dijkPath_middle !=null  ) {
   			
   			/*******************�ҵ�·��*****************************/
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
   					for(int j=i;j<path.links.size();j++) { // �������нڵ�i������  ���ϵ㣬j��i��ʼ�ۼ�ʱ��
   						//��������б���·��ʱ�Ӽ���
   						/******************************************************************/
   						tempDOSLFRSumLSLD += path.links.get(j).latency; // ��j��ʼ�ۼ�ʱ�� latency
   						/******************************************************************/
   					}
   					break;
   				}
   			}
   		
   		}
   		/**********************�����Ƿ��ҵ�·���ı�־*********************/
   		return isGetPath_LSLD; 
   		/*****************************************************************/
   		//flow.paths.set(index, path);
   	}
       public void FR_LSFD() {
    	   
      /*****************************************************************/
    	   resetMapsAndSums(); // �������в���
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   
      	   for (Link_FowC linkflow: allLinks) { //�ڼ������ָ��ʱ������ֻ��һ����·�ϣ����в���
    		   //1)calculate the sp 
      		   //System.out.println("the link is "+ linkflow.link.name);
    		   for(Flow flow: linkflow.link_flowlist) {
    			   boolean flag = getPath_LSFD(flow,this.index,linkflow.link);// set the sp, for this flow.
    			   if (flag) { // �ҵ�·��
    				   ++tempPRSum;
    				   ++NOBP;
    			   }
    		   }
    		   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size();
    	    }
      	   // ���㲢д��PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	1.0 * PRTemp        / allLinks.size();    // PR:     path���յĺ� /  ������·����
			   DOSLFR = tempDOSLFRSumLSFD   / allLinks.size();    // DOSLFR�� path����ʱ���ۼ�ʱ�Ӻ� /  ������·����
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSFD.put(index, PR);
    	   mapDOSLFR_LSFD.put(index, DOSLFR);
    	   mapNOBP_LSFD.put(index, NOBP);
    		  
       }
       
   public boolean getPath_LSFD(Flow flow, int index, Link link) { // link is 
	   
	   /*******************Ĭ��û���ҵ�·��*************************/
      
	   boolean isGetPath_LSFD = false;   
       
       /************************************************/
   	   SimpleGraph<Node, DefaultEdge> jgraph = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);

   		List<Node> nodes = this.graph.nodes;
   		for (int i = 0; i < nodes.size(); i++) {
   			jgraph.addVertex(nodes.get(i));
   			
   		}
           
   		List<Link> links = this.graph.getWorkLinks();
   		
   		for (int i = 0; i < links.size(); i++) {
   			//��һ���������ж��Ƿ�Ϊ���ϱ�
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
   			
   	   			
   	   			/*******************�ҵ�·��*****************************/
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
      		/*************************** �������в���**************************************/
    	   	resetMapsAndSums(); 
    	   	
      		/*****************************************************************/

    	   Map<String, List<Path>> allPaths_tmp = allPaths.allPaths;
    	    // LSLD calculate all path between the corresponding link,
    	   
    	   /************************��ʼ��********************************/
    	   int tempPRSum = 0; 
    	   int NOBP = 0;
    	   int PRTemp = 0;
    	   /*****************************************************************/
    	   
    	   for (Link_FowC linkflow: allLinks) {//�ڼ������ָ��ʱ������ֻ��һ����·�ϣ����в���
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
    			   		  ++tempPRSum; // ֻҪflag=true���������forѭ����++
    			   		  
    			   		  for (int k = 0; k < path.nodes.size(); ++k) {
    			   			  if (path.nodes.get(k).ID == linkflow.link.srcID || path.nodes.get(k).ID == linkflow.link.dstID) {
    			   				  for (int j = k; j < path.links.size(); ++j) { // �������нڵ�i������  ���ϵ㣬j��i��ʼ�ۼ�ʱ��
    			   					  //��������б���·��ʱ�Ӽ���
    		   						  /******************************************************************/
    		   						  tempDOSLFRSumLSLD += path.links.get(j).latency; // ��j��ʼ�ۼ�ʱ�� latency
    		   						  /******************************************************************/
    		   					  }
    		   					  break;
    		   				  }
    		   			  }
    			   		  
    			   		 /**********************************************************/
    				    }
    				    ++NOBP;   // ֻ��flag=true�������forѭ�����һ��
    				    PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // ����flag�ǲ���=true��ֻҪ��LSLD��++
    				    break;
    		       }
    			   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // ���ֵ����flag�ǲ���true��Ҫ�ۼӣ������������flag=false������+
				   
    		    }
    		    if(flag_all==false) {	//LSFD method
    			   for(Flow flow: linkflow.link_flowlist) {
        			   getPath_LSFD(flow,this.index,linkflow.link);// set the sp, for this flow.
        			   ++tempPRSum; // ֻҪflag=true���������forѭ����++
        			   ++NOBP;      // ֻ��flag=true�������forѭ�����һ��
        			   
        		   }
    			   PRTemp += 1.0 * tempPRSum / linkflow.link_flowlist.size(); // ����flag�ǲ���=true����Ҫ++
    		    }
    		    
             }
    	   // ���㲢д��PR, DOSLFR
    	   double PR = 0.0, DOSLFR = 0.0;
		   try {	
			   PR     =	                           1.0 * PRTemp   / allLinks.size();    // PR:     path���յĺ� /  ������·����
			   DOSLFR = (tempDOSLFRSumLSFD + tempDOSLFRSumLSLD)   / allLinks.size();    // DOSLFR�� path����ʱ���ۼ�ʱ�Ӻ� /  ������·����
			  // ����û�м���tempDOSLFRSumLSLD��������
			   
		   } catch(Exception e) {
			   System.out.println("Exception: numbers have 0");
		   }
    	   mapPR_LSLDAndLSFD.put(index, PR);
    	   mapDOSLFR_LSLDAndLSFD.put(index, DOSLFR);
    	   mapNOBP_LSLDAndLSFD.put(index, NOBP);
    	   
    }
/***************** �Ѳ�������  **********************/
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
