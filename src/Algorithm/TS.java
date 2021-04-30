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

public class TS {
	double max_λ ;//λ
	double min_ξ ;//ξ
	double re_γ ;//γ
	double α;
	int Δt;
	int t0;
	Link MluLink;
	double MLU;
	int large_or_smaller;
	/**********  定义 *  ***********/
	 Map<Integer, Double> mapSLR = new HashMap<Integer, Double>();// SLR的map结构，k -> i; v -> SLR
	 Map<Integer, Double> mapMLU = new HashMap<Integer, Double>(); // MLU的map结构，k -> i; v -> MLU
	 Map<Integer, Integer> mapNOSL = new HashMap<Integer, Integer>();// 实际能耗
	 int NOTS = 0;                     // 执行拓扑切换的次数
	 
	/******************************/
	
	public TS(Graph graph, TrafficDemand trafficDemand,AllPaths allPaths, double upper,double lower){
		 max_λ = upper;//λ
		 min_ξ = lower;//ξ
		 
		 re_γ= 0.9;//γ
		 Δt = 8;
		 t0 = -8;
		 /**********************  初始化       *****************************/
		 NOTS = 0;
		 large_or_smaller=0;// 在阈值中间
		 /*******************************************************************/
		
		
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
						    		  if(large_or_smaller==1) {
						    			  α= 0.8;
						    		  }
						    		  if(large_or_smaller==2) {
						    			  α= 0.6;
						    		  }
						    		  if(temp_link.isReliable(flow.demands.get(i), α) == false) {
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
				
				
				
				//System.out.println("*******after switching , the number of sleep link is "+ graph.getSleepLinkNum());
				int NOSL = graph.getSleepLinkNum(); // 休眠链路数目
				int NOL  = graph.links.size();     //  总的链路数目
				
				double SLR = 0.0;
				try {
					SLR = 1.0 * NOSL / NOL;
					
					
				} catch (Exception e) {
					System.out.println("Exception : NOL is 0 !");
				}
				mapSLR.put(i, SLR); // i对应的SLR存入
				mapNOSL.put(i,NOSL);
			}
			else {
				//TS_Reroute();
				//System.out.println(i);
				//1)计算MLU.和其对应的链路link
				double TempMlu=this.MLU;
				//2)获取经过MLU_link链路上所有的网络流，存到flows_link中
				List<Flow> flows_link= new ArrayList<>();
				//System.out.print(this.MluLink.name);
				for(Flow flow: trafficDemand.flows) {
					if(Math.abs(flow.demands.get(i))>0) {
					Path path_flow=flow.paths.get(i);
					for(Link link: path_flow.links) {
						
						
						if (link.name.equals(this.MluLink.name)){
							flows_link.add(flow);
							break;
						}
					}
					}
					
				}
				//3)对flow_links 按该时刻的流大小 从大到小排序 
				int tmp_i=i;
				Collections.sort(flows_link, new Comparator<Flow>() {
					@Override
					public int compare(Flow o1, Flow o2) {
						// TODO Auto-generated method stub
						return (int) (o2.demands.get(tmp_i).compareTo(o1.demands.get(tmp_i)));
					}
					
				});
				//4)对flow_links上的流 变更路径让他不在经过这条路径。变更之前判断MLU是否变得更小。
				
				String MluLink_name = new String(MluLink.name);
				
				for(Flow tmpFlow: flows_link) {
					//4.1 first find the path that didn't pass through the MluLink
					List<Path> tmpFlow_paths = allPaths.getPaths(tmpFlow.pair, 2);
					// for every path in tmpFlow_paths check whether it pass through the MluLink
					
					for(Path path : tmpFlow_paths) {
						if(path.Judge_workingT())continue;   //  如果路径上有链路处于休眠状态
						if(path.Judge_Link(MluLink))continue;// 不经过这条MluLink链路
						else {//path is find 
							double tmpFlow_demand = tmpFlow.demands.get(i);
							Path formerpath = tmpFlow.paths.get(i);
							//for simplicity, first sub the demand , if MLU don't get smaller , restore the the demand.
							formerpath.sub(tmpFlow_demand);
							//加之前判断是否超过MLU
							if(path.Judge_whetherCongestion(tmpFlow_demand,TempMlu)) {
								formerpath.add(tmpFlow_demand);//超过后，修改回之前的状态
								break;
							}
							
							path.add(tmpFlow_demand);//
							getMLU(graph);
							if(this.MLU<TempMlu) { //调整流tmpFlow 的路径
								tmpFlow.paths.set(i, path);//找到了这条路径
								break;
							
							}
							else {//reverse the operation , if the reroute operation fail.
								path.sub(tmpFlow_demand);
								formerpath.add(tmpFlow_demand);
								getMLU(graph);
							}
							
						}
					}
					if(!MluLink_name.equals(this.MluLink.name))break;//MLU不在最大
					
				}
				//5)结束条件1）遍历完 2）MLU不在最大。所以变更每一条路径后，都要计算MLU,如果不是最大，则停止
				
			}
			
			
			
			/******************************************************************************************************/
		
			
			//MLU计算  ：getMLU(Graph graph) 
			//getMLU(graph);
			mapMLU.put(i, this.MLU);
			
			/******************************************************************************************************/
			
			
			//Link_Collection link_cl = new Link_Collection(graph,trafficDemand,i);
			//link_cl.FR_LSLD();
			//link_cl.FR_LSFD();
			//link_cl.FR_LSLDandLSFD(allPaths);
		/*	//R_Forecast(); TODO not realized in paper
			//FR_Route() 故障恢复相关
			//just calculate the path 
			Link_Collection link_cl = new Link_Collection(graph,trafficDemand,i);
			
			link_cl.FR_LSLD();
			//
			
			 * 
			 */
			
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
		System.out.printf("λ is %f , ξ is %f  , enery-saving is %.2f  , 拓扑切换的次数为  %d \n ",max_λ,min_ξ, sum_SLR,NOTS);
		
		*/
		
		/***********************************MLU*******************************************************************/
		
       double sum_enerygysaving=0.0;
       int duration=0;
		
		
		List<Integer> list=new ArrayList<Integer>(mapNOSL.keySet());
		Collections.sort(list);
		for (int i=0;i<list.size();i++) {
			duration=list.get(i)-pretimeStep;
			
			sum_enerygysaving+=15*60*duration*2*23.98*mapNOSL.get(list.get(i));
			pretimeStep=list.get(i);
		}
	    
		System.out.printf("λ is %.3f , ξ is %.3f  , enery-saving is %.2f\n", max_λ,min_ξ,sum_enerygysaving);
	}

	


	private void R_Forecast() {
		// TODO Auto-generated method stub
		
	}

	private void TS_Reroute() {
		// TODO Auto-generated method stub
		/*
1)计算MLU.和其对应的链路link
2)获取link链路上的所有经过的网络流，遍历所有网络流是否经过改link ->网络流  flows_link
3)对flow_links排序 从大到小
4)对flow_links上的流进行，变更路径让他不在经过这条路径。if变更之前判断MLU是否变得更小。
5)结束条件1）遍历完 2）MLU不在最大。所以变更每一条路径后，都要计算MLU,如果不是最大，则停止
*/
		
       
	}

	private void TS_Implementation() {
		// TODO Auto-generated method stub
		
	}

	private void TS_Calculation() {
		// TODO Auto-generated method stub
	/*	1清空
		2流排序
		3选路
		4设置路径
	*/
		
	}

	

	private boolean TS_Judge(Graph graph, int t) {//t is current time
		// TODO Auto-generated method stub
		
		
		boolean isSwitch=false;
		α = 0; 
		
		getMLU(graph);
		
		if(MLU> max_λ ) {	
			
			if(graph.getWorkLinks().size() == graph.links.size()) {
				return  isSwitch;
			}
			else if(MLU> re_γ) {
				large_or_smaller=1;//lager
				isSwitch = true;
				return  isSwitch;
			}
			else if(t-t0 <= Δt) {
				return  isSwitch;
			}
			else {
				large_or_smaller=0;
				isSwitch=true;
				α =min_ξ;
			}
		}
		if(MLU < min_ξ) {
			
			if(graph.getWorkLinkNum() == graph.getNodeNumber()-1) {//WT∈MSTs
				
				
				return isSwitch;
			}
			else if(t-t0 <= Δt) {
				
				return  isSwitch;
			}
			else {
				 large_or_smaller=0;
				 isSwitch=true;
				 α =max_λ;
				 return  isSwitch;
			}
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
