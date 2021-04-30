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
	double max_�� ;//��
	double min_�� ;//��
	double re_�� ;//��
	double ��;
	int ��t;
	int t0;
	Link MluLink;
	double MLU;
	int large_or_smaller;
	/**********  ���� *  ***********/
	 Map<Integer, Double> mapSLR = new HashMap<Integer, Double>();// SLR��map�ṹ��k -> i; v -> SLR
	 Map<Integer, Double> mapMLU = new HashMap<Integer, Double>(); // MLU��map�ṹ��k -> i; v -> MLU
	 Map<Integer, Integer> mapNOSL = new HashMap<Integer, Integer>();// ʵ���ܺ�
	 int NOTS = 0;                     // ִ�������л��Ĵ���
	 
	/******************************/
	
	public TS(Graph graph, TrafficDemand trafficDemand,AllPaths allPaths, double upper,double lower){
		 max_�� = upper;//��
		 min_�� = lower;//��
		 
		 re_��= 0.9;//��
		 ��t = 8;
		 t0 = -8;
		 /**********************  ��ʼ��       *****************************/
		 NOTS = 0;
		 large_or_smaller=0;// ����ֵ�м�
		 /*******************************************************************/
		
		
		// trafficDemand.flows.get(0).demands.size()
		for(int i=0; i<trafficDemand.flows.get(0).demands.size(); i++){// i��������ʱ�̵���������
			
			
			
			if(i != 0){//        ����һ�ε�·�����г�ʼ��
				graph.initLinks(false);	
				for (int j=0; j<trafficDemand.flows.size(); j++){ // ����������
					Flow tmpflow=trafficDemand.flows.get(j);
					Path path = tmpflow.paths.get(i-1);
					path.add(tmpflow.demands.get(i));// �Ȱ��������·����ʱ���ó���һ��ʱ�̵�·��
					//System.out.println(tmpflow.demands.get(i));
					tmpflow.paths.set(i, path);
				}
				
			}
			
			
			if(TS_Judge(graph,i)) {//  ֻ�������������ı���·��״̬������������ ��
				t0=i;
				/**********************/
				//System.out.println("before switching , the number of sleep link is "+ graph.getSleepLinkNum());
				//System.out.println("Begin Topo switching ");
				++NOTS; // �ۻ�����ת������
				
				/**********************/
				
				
				
				graph.initLinks(false);
			    //������һiʱ�����Ĵ�С���� 
				
				int tmp_i=i;
				Collections.sort(trafficDemand.flows, new Comparator<Flow>() {//2������ �Ӵ�С
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
						    			  ��= 0.8;
						    		  }
						    		  if(large_or_smaller==2) {
						    			  ��= 0.6;
						    		  }
						    		  if(temp_link.isReliable(flow.demands.get(i), ��) == false) {
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
				int NOSL = graph.getSleepLinkNum(); // ������·��Ŀ
				int NOL  = graph.links.size();     //  �ܵ���·��Ŀ
				
				double SLR = 0.0;
				try {
					SLR = 1.0 * NOSL / NOL;
					
					
				} catch (Exception e) {
					System.out.println("Exception : NOL is 0 !");
				}
				mapSLR.put(i, SLR); // i��Ӧ��SLR����
				mapNOSL.put(i,NOSL);
			}
			else {
				//TS_Reroute();
				//System.out.println(i);
				//1)����MLU.�����Ӧ����·link
				double TempMlu=this.MLU;
				//2)��ȡ����MLU_link��·�����е����������浽flows_link��
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
				//3)��flow_links ����ʱ�̵�����С �Ӵ�С���� 
				int tmp_i=i;
				Collections.sort(flows_link, new Comparator<Flow>() {
					@Override
					public int compare(Flow o1, Flow o2) {
						// TODO Auto-generated method stub
						return (int) (o2.demands.get(tmp_i).compareTo(o1.demands.get(tmp_i)));
					}
					
				});
				//4)��flow_links�ϵ��� ���·���������ھ�������·�������֮ǰ�ж�MLU�Ƿ��ø�С��
				
				String MluLink_name = new String(MluLink.name);
				
				for(Flow tmpFlow: flows_link) {
					//4.1 first find the path that didn't pass through the MluLink
					List<Path> tmpFlow_paths = allPaths.getPaths(tmpFlow.pair, 2);
					// for every path in tmpFlow_paths check whether it pass through the MluLink
					
					for(Path path : tmpFlow_paths) {
						if(path.Judge_workingT())continue;   //  ���·��������·��������״̬
						if(path.Judge_Link(MluLink))continue;// ����������MluLink��·
						else {//path is find 
							double tmpFlow_demand = tmpFlow.demands.get(i);
							Path formerpath = tmpFlow.paths.get(i);
							//for simplicity, first sub the demand , if MLU don't get smaller , restore the the demand.
							formerpath.sub(tmpFlow_demand);
							//��֮ǰ�ж��Ƿ񳬹�MLU
							if(path.Judge_whetherCongestion(tmpFlow_demand,TempMlu)) {
								formerpath.add(tmpFlow_demand);//�������޸Ļ�֮ǰ��״̬
								break;
							}
							
							path.add(tmpFlow_demand);//
							getMLU(graph);
							if(this.MLU<TempMlu) { //������tmpFlow ��·��
								tmpFlow.paths.set(i, path);//�ҵ�������·��
								break;
							
							}
							else {//reverse the operation , if the reroute operation fail.
								path.sub(tmpFlow_demand);
								formerpath.add(tmpFlow_demand);
								getMLU(graph);
							}
							
						}
					}
					if(!MluLink_name.equals(this.MluLink.name))break;//MLU�������
					
				}
				//5)��������1�������� 2��MLU����������Ա��ÿһ��·���󣬶�Ҫ����MLU,������������ֹͣ
				
			}
			
			
			
			/******************************************************************************************************/
		
			
			//MLU����  ��getMLU(Graph graph) 
			//getMLU(graph);
			mapMLU.put(i, this.MLU);
			
			/******************************************************************************************************/
			
			
			//Link_Collection link_cl = new Link_Collection(graph,trafficDemand,i);
			//link_cl.FR_LSLD();
			//link_cl.FR_LSFD();
			//link_cl.FR_LSLDandLSFD(allPaths);
		/*	//R_Forecast(); TODO not realized in paper
			//FR_Route() ���ϻָ����
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
		System.out.printf("�� is %f , �� is %f  , enery-saving is %.2f  , �����л��Ĵ���Ϊ  %d \n ",max_��,min_��, sum_SLR,NOTS);
		
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
	    
		System.out.printf("�� is %.3f , �� is %.3f  , enery-saving is %.2f\n", max_��,min_��,sum_enerygysaving);
	}

	


	private void R_Forecast() {
		// TODO Auto-generated method stub
		
	}

	private void TS_Reroute() {
		// TODO Auto-generated method stub
		/*
1)����MLU.�����Ӧ����·link
2)��ȡlink��·�ϵ����о����������������������������Ƿ񾭹���link ->������  flows_link
3)��flow_links���� �Ӵ�С
4)��flow_links�ϵ������У����·���������ھ�������·����if���֮ǰ�ж�MLU�Ƿ��ø�С��
5)��������1�������� 2��MLU����������Ա��ÿһ��·���󣬶�Ҫ����MLU,������������ֹͣ
*/
		
       
	}

	private void TS_Implementation() {
		// TODO Auto-generated method stub
		
	}

	private void TS_Calculation() {
		// TODO Auto-generated method stub
	/*	1���
		2������
		3ѡ·
		4����·��
	*/
		
	}

	

	private boolean TS_Judge(Graph graph, int t) {//t is current time
		// TODO Auto-generated method stub
		
		
		boolean isSwitch=false;
		�� = 0; 
		
		getMLU(graph);
		
		if(MLU> max_�� ) {	
			
			if(graph.getWorkLinks().size() == graph.links.size()) {
				return  isSwitch;
			}
			else if(MLU> re_��) {
				large_or_smaller=1;//lager
				isSwitch = true;
				return  isSwitch;
			}
			else if(t-t0 <= ��t) {
				return  isSwitch;
			}
			else {
				large_or_smaller=0;
				isSwitch=true;
				�� =min_��;
			}
		}
		if(MLU < min_��) {
			
			if(graph.getWorkLinkNum() == graph.getNodeNumber()-1) {//WT��MSTs
				
				
				return isSwitch;
			}
			else if(t-t0 <= ��t) {
				
				return  isSwitch;
			}
			else {
				 large_or_smaller=0;
				 isSwitch=true;
				 �� =max_��;
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
