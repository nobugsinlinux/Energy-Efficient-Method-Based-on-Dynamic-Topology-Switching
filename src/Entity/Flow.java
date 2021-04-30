package Entity;

import java.util.ArrayList;
import java.util.List;

public class Flow {
	public Node src;
	public Node dst;
	public String pair;

	public List<Double> demands;
	public List<Path> paths;
	
	public Flow(Flow flow) {
		src=flow.src;
		dst=flow.dst;
		pair=flow.pair;
		demands=new ArrayList<>(96);
		paths=new ArrayList<>(96);
		int scale=flow.demands.size()/96;
		for (int i = 0; i < 96; i++) {
			double demand=0;
			for (int j = i; j < flow.demands.size(); j+=96) {
				demand+= flow.demands.get(j);
			}
			demand/=scale;   
			demands.add(demand);
			paths.add(null);
		}
	}

	public Flow() {
	}
}
