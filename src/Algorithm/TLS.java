package Algorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import Entity.Flow;
import Entity.Graph;
import Entity.Link;
import Entity.Link_Collection;
import Entity.Node;
import Entity.Path;
import Entity.TrafficDemand;

public class TLS {
	
	double alpha;
	double al;
	int t0;
	Link MluLink;
	double MLU;
	int large_or_smaller;
	/**********  定义 *  ***********/
	 Map<Integer, Double> mapSLR = new HashMap<Integer, Double>();// SLR的map结构，k -> i; v -> SLR
	 Map<Integer, Double> mapMLU = new HashMap<Integer, Double>(); // MLU的map结构，k -> i; v -> MLU
	 Map<Integer, Integer> mapNOSL = new HashMap<Integer, Integer>();// 实际能耗 
	 int NOTS = 0;                     // 执行拓扑切换的次数
	 
	/**
	 * @param a 
	 * @param lower 
	 * @param upper ****************************/
	
	public TLS(Graph graph, TrafficDemand trafficDemand,AllPaths allPaths, double a){
		 
		 alpha=a;
		 /**********************  初始化       *****************************/
		 NOTS = 0;
		 large_or_smaller=0;// 在阈值中间
		
		
		
		// trafficDemand.flows.get(0).demands.size()
		for(int i=0; i<trafficDemand.flows.get(0).demands.size(); i++){// i遍历所有时刻的流的需求
			if(i != 0){//        用上一次的路径进行初始化
					graph.initLinks(false);	
					for (int j=0; j<trafficDemand.flows.size(); j++){ // 对于所有流
						Flow tmpflow=trafficDemand.flows.get(j);
						Path path = tmpflow.paths.get(i-1);
						
						path.add(tmpflow.demands.get(i));// 先把这个流的路径暂时设置成上一个时刻的路径
						//System.out.println(tmpflow.demands.get(i));
						tmpflow.paths.set(i, path);
					}
					
				}
			if(i% 48==0) {// 一天中的两个时刻
				
				
				
				if(TS_Judge(graph,i)) {//  只在这个方法里面改变链路的状态，进行拓扑切 换
					t0=i;
					/**********************/
					//System.out.println("before switching , the number of sleep link is "+ graph.getSleepLinkNum());
					//System.out.println("Begin Topo switching ");
					++NOTS; // 累积拓扑转换次数
					
					/**********************/
					
					
					
					graph.initLinks(false);
				    //按照这一i时刻流的大小排序 
					
					int tmp_i=i;
					Collections.sort(trafficDemand.flows, new Comparator<Flow>() {//2流排序 从大到小
						@Override
						public int compare(Flow o1, Flow o2) {
							// TODO Auto-generated method stub
							return (int) (o2.demands.get(tmp_i).compareTo(o1.demands.get(tmp_i)));
						}
						
					});
					
					
					
					//System.out.println("MLU's link name is "+ MluLink.name);
					for(Flow flow: trafficDemand.flows) {
						
						
						if(Math.abs(flow.demands.get(i))>0.0000001) {
							      List<Path> paths =  new ArrayList<>();
							      List<Path> paths_KSP=allPaths.getPaths(flow.pair, 1);//
							      for(Path temp_path: paths_KSP) {
							    	  boolean flag=true;
							    	  for(Link temp_link:temp_path.links) {
							    		  
							    		  if(temp_link.isReliable(flow.demands.get(i), alpha+(1-alpha-0.1)) == false) {
							    			  flag=false;
							    			  break;
							    		  }
							    	  }
							    	  //System.out.println(flag);
							    	  if(flag== true) {
							    		  paths.add(temp_path);
							    	  }
							    	  
							      }
							      //System.out.println(paths.size());
							      flow.paths.set(i, paths.get(0));
							      for(Path path: paths) {
							    	  if(path.getCost()< flow.paths.get(i).getCost() ) {
							    		  flow.paths.set(i, path);
							    	  }
							      }
							
							      flow.paths.get(i).add(flow.demands.get(i));//equals to WT=WT+flow.path;
						}
						
					}
					
					
					
				
					
				}
			}
			getMLU(graph);
			
			
			
			
			
			/******************************************************************************************************/
			//SLR指标计算：
			int NOSL = graph.getSleepLinkNum(); // 休眠链路数目
			int NOL  = graph.links.size();     //  总的链路数目
			double SLR = 0.0;
			try {
				SLR = 1.0 * NOSL / NOL;
				//System.out.println("NOSL:" + NOSL + "NOL" + NOL + "SLR:" + SLR);
			} catch (Exception e) {
				System.out.println("Exception : NOL is 0 !");
			}
			mapSLR.put(i, SLR); // i对应的SLR存入
			mapNOSL.put(i,NOSL);
			//MLU计算  ：getMLU(Graph graph) 
			//getMLU(graph);
			mapMLU.put(i, this.MLU);
			
		
			
		}
		

		int pretimeStep=-1;
		
		
		/***********************************************SLR******************************************************/
	/*	double sum_SLR=0.0;
		int duration=0;
		
		
		List<Integer> list=new ArrayList<Integer>(mapSLR.keySet());
		Collections.sort(list);
		for (int i=0;i<list.size();i++) {
			duration=list.get(i)-pretimeStep;
			
			sum_SLR+=duration*mapSLR.get(list.get(i));
			pretimeStep=list.get(i);
		}
		System.out.printf("α  is %f  , enery-saving is %.2f  , 拓扑切换的次数为  %d \n ", alpha, sum_SLR,NOTS);
		
		*/
		
		/***********************************MLU*******************************************************************/
		double sum_MLU=0.0;
		
		for(int i=0;i<mapMLU.size();i++) {
			
			sum_MLU+=mapMLU.get(i);
		}
		System.out.printf("average MLU is  %.3f\n", sum_MLU/mapMLU.size());
		
		
			
			
			 double sum_enerygysaving=0.0;
		       int duration=0;
				
				
				List<Integer> list=new ArrayList<Integer>(mapNOSL.keySet());
				Collections.sort(list);
				for (int i=0;i<list.size();i++) {
					duration=list.get(i)-pretimeStep;
					
					sum_enerygysaving+=15*60*duration*2*23.98*mapNOSL.get(list.get(i));
					pretimeStep=list.get(i);
				}
			    
				System.out.printf(" alpha is %.2f, enery-saving is %.2f\n",  alpha ,sum_enerygysaving);
	}

	private boolean TS_Judge(Graph graph, int t) {//t is current time
		// TODO Auto-generated method stub
		
		getMLU(graph);
		
		if(MLU> alpha ) {
			return true;
		}
		
	  	 return false;
	}
private void getMLU(Graph graph) {
		// TODO Auto-generated method stub
		MLU=0;
		List<Link> WorkLink=graph.getWorkLinks(); 
		//System.out.println("getMLU "+graph.getWorkLinks().size());
		for(Link link: WorkLink) {//getMLU(TM)
			double TempLu=link.getLU();
			if(MLU<TempLu) {
				MLU=TempLu;
				this.MluLink=link;
			}
		}
	
	}
	}
