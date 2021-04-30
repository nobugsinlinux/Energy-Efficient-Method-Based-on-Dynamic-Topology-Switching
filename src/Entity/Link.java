package Entity;

import org.jgrapht.graph.DefaultEdge;

public class Link extends DefaultEdge {
	
	

	

	@Override
	public String toString() {
		return "Link [srcID=" + srcID + ", dstID=" + dstID + "]";
	}

	private static final long serialVersionUID = 1L;
	/*
	 *1)LSLD所有经过该链路的流，都切到 备份路径（src,dest) pro: O(n)
			流表项资源消耗少。short:流量集中，容易造成拥塞
	  2)LSFD 按流粒度，对每一条流选一条备份路径，从link 的src 到flow dst. O(n*m)
	  3)FSFD 流的起点和终点重新规划，不经过该链路。 (O(n*m))
	         从上到小拥塞概率越来越小，消耗越来越大。
	  ours: 尽量使用第一种方式，规划备份路径，选一个不拥塞的路径（标准？？备份路径接受转移流，还能正常工作？？不超过可靠性阈值，每一条链路都要判断），如果选不到就选择第二种，
	    得到什么数据？？指标        1）拥塞的概率 or 故障恢复概率(具体针对什么)。 first 方式123，都是备份路径就是最短路径， 每一条链路都遍历一边，
	  *                2）算所有备份路径经过了多少的链路 这个指标 
	  *                3）我们LSLD(每次找不拥塞的路径？？在所有路径里面找，比较器)+LSFD
	  *             
*/
	public String name;

	public int srcID;
	public String srcName;
	public int srcPort;

	public int dstID;
	public String dstName;
	public int dstPort;

	public double rate;
	public double bandWidth;
	public double latency;
	public boolean topoState;

	public Link(String name, String srcName, String dstName, int srcID, int dstID, double latency) {
		this.name = name;
		this.srcName = srcName;
		this.dstName = dstName;
		this.srcID = srcID;
		this.dstID = dstID;

		this.rate = 0;
		this.bandWidth = 20000;
		this.latency = latency;
		
		this.topoState = true;
	}

	public boolean isAvailable(double demand, double MLU) {
		return (rate + demand) / bandWidth <= MLU;
	}

	public double getLU() {
		return rate / bandWidth;
	}

	public boolean isReliable(double demand, double α) {
		// TODO Auto-generated method stub
		if((rate + demand) / bandWidth > α) {
			return false;
		}
		return true;
	}
}