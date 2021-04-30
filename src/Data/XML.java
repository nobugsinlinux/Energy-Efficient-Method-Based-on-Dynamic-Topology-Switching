package Data;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import Algorithm.Utils;
import Entity.TrafficDemand;

public class XML {

	String filePath = "E:\\eclipse-workspace\\Routing\\data\\directed-geant-uhlig-15min-over-4months-ALL\\";
	String fileName = "demandMatrix-geant-uhlig-15min-20050";

	public XML(TrafficDemand trafficDemand) {

		for (int month = 5; month <= 5; month++) {//������Ϊ���� 
			int day = 1, max = 31;
			if (month == 5)
				day = 5;
			if (month == 6)
				max = 30;
            max=7;
			// ����ÿһ��
			while (day <= max) {
				for (int time = 0; time < 96; time += 1) {//15����Ϊһ��ʱ�̣�24*4 �÷����������ǽ�����ʱ�̵������ȶ���traffic demand ��
					String fileTime = getTime(month, day, time);
					try {
						File f = new File(filePath + fileName + fileTime);
						SAXReader reader = new SAXReader();
						Document doc = reader.read(f);
						Element root = doc.getRootElement();
						Element demands = root.element("demands");
						trafficDemand.addAll(); //guess ��ʼʱdemand Ϊ0,  �� path  ??Ϊʲôÿ��ʱ��ǰҪ�����������
						for (Iterator<Element> iterator = demands.elementIterator("demand"); iterator.hasNext();) {
							Element demand = (Element) iterator.next();
							trafficDemand.addOne(//�����������ƺ�����
									Utils.getPair(demand.elementText("source"), demand.elementText("target")),
									scale(Double.valueOf(demand.elementText("demandValue"))));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				day++;
			}
		}
	}

	private double scale(double demand) {
		return demand * 1.0;
	}

	private String getTime(int month, int day, int time) {
		// TODO Auto-generated method stub
		String ret = "" + month;

		if (day < 10)
			ret += "0" + day + "-";
		else
			ret += day + "-";

		int hour = time / 4, min = (time % 4) * 15;
		if (hour < 10)
			ret += "0" + hour;
		else
			ret += hour;
		if (min == 0)
			ret += "00" + ".xml";
		else
			ret += min + ".xml";
		return ret;
	}
}
