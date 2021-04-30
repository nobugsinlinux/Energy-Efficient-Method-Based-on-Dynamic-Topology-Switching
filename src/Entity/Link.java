package Entity;

import org.jgrapht.graph.DefaultEdge;

public class Link extends DefaultEdge {
	
	

	

	@Override
	public String toString() {
		return "Link [srcID=" + srcID + ", dstID=" + dstID + "]";
	}

	private static final long serialVersionUID = 1L;
	/*
	 *1)LSLD���о�������·���������е� ����·����src,dest) pro: O(n)
			��������Դ�����١�short:�������У��������ӵ��
	  2)LSFD �������ȣ���ÿһ����ѡһ������·������link ��src ��flow dst. O(n*m)
	  3)FSFD ���������յ����¹滮������������·�� (O(n*m))
	         ���ϵ�Сӵ������Խ��ԽС������Խ��Խ��
	  ours: ����ʹ�õ�һ�ַ�ʽ���滮����·����ѡһ����ӵ����·������׼��������·������ת�����������������������������ɿ�����ֵ��ÿһ����·��Ҫ�жϣ������ѡ������ѡ��ڶ��֣�
	    �õ�ʲô���ݣ���ָ��        1��ӵ���ĸ��� or ���ϻָ�����(�������ʲô)�� first ��ʽ123�����Ǳ���·���������·���� ÿһ����·������һ�ߣ�
	  *                2�������б���·�������˶��ٵ���· ���ָ�� 
	  *                3������LSLD(ÿ���Ҳ�ӵ����·������������·�������ң��Ƚ���)+LSFD
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

	public boolean isReliable(double demand, double ��) {
		// TODO Auto-generated method stub
		if((rate + demand) / bandWidth > ��) {
			return false;
		}
		return true;
	}
}