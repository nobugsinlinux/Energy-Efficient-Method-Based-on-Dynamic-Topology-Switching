package Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Algorithm.Utils;

public class Graph {

	public List<Node> nodes = new ArrayList<>();
	public List<Link> links = new ArrayList<>();

	public Map<String, Node> nodeMap;
	public Map<String, Link> linkMap;

	public Graph() {
		// TODO Auto-generated constructor stub
		nodes.add(new Node("at1.at", 0));
		nodes.add(new Node("be1.be", 1));
		nodes.add(new Node("ch1.ch", 2));
		nodes.add(new Node("cz1.cz", 3));
		nodes.add(new Node("de1.de", 4));
		nodes.add(new Node("es1.es", 5));
		nodes.add(new Node("fr1.fr", 6));
		nodes.add(new Node("gr1.gr", 7));
		nodes.add(new Node("hr1.hr", 8));
		nodes.add(new Node("hu1.hu", 9));
		nodes.add(new Node("ie1.ie", 10));
		nodes.add(new Node("il1.il", 11));
		nodes.add(new Node("it1.it", 12));
		nodes.add(new Node("lu1.lu", 13));
		nodes.add(new Node("nl1.nl", 14));
		nodes.add(new Node("ny1.ny", 15));
		nodes.add(new Node("pl1.pl", 16));
		nodes.add(new Node("pt1.pt", 17));
		nodes.add(new Node("se1.se", 18));
		nodes.add(new Node("si1.si", 19));
		nodes.add(new Node("sk1.sk", 20));
		nodes.add(new Node("uk1.uk", 21));

		links.add(new Link("at1.at_ch1.ch", "at1.at", "ch1.ch", 0, 2,4));
		links.add(new Link("at1.at_de1.de", "at1.at", "de1.de", 0, 4,4));
		links.add(new Link("at1.at_hu1.hu", "at1.at", "hu1.hu", 0, 9,3));
		links.add(new Link("at1.at_ny1.ny", "at1.at", "ny1.ny", 0, 15,2));
		links.add(new Link("at1.at_si1.si", "at1.at", "si1.si", 0, 19,4));
		links.add(new Link("be1.be_fr1.fr", "be1.be", "fr1.fr", 1, 6,4));
		links.add(new Link("be1.be_lu1.lu", "be1.be", "lu1.lu", 1, 13,2));
		links.add(new Link("be1.be_nl1.nl", "be1.be", "nl1.nl", 1, 14,4));
		links.add(new Link("ch1.ch_fr1.fr", "ch1.ch", "fr1.fr", 2, 6,2));
		
		links.add(new Link("ch1.ch_it1.it", "ch1.ch", "it1.it", 2, 12,4));
		links.add(new Link("cz1.cz_de1.de", "cz1.cz", "de1.de", 3, 4,4));
		links.add(new Link("cz1.cz_pl1.pl", "cz1.cz", "pl1.pl", 3, 16,4));
		links.add(new Link("cz1.cz_sk1.sk", "cz1.cz", "sk1.sk", 3, 20,2));
		links.add(new Link("de1.de_fr1.fr", "de1.de", "fr1.fr", 4, 6,2));
		links.add(new Link("de1.de_gr1.gr", "de1.de", "gr1.gr", 4, 7,3));
		links.add(new Link("de1.de_ie1.ie", "de1.de", "ie1.ie", 4, 10,2));
		links.add(new Link("de1.de_it1.it", "de1.de", "it1.it", 4, 12,4));
		links.add(new Link("de1.de_nl1.nl", "de1.de", "nl1.nl", 4, 14,2));
		
		links.add(new Link("de1.de_se1.se", "de1.de", "se1.se", 4, 18,3));
		links.add(new Link("es1.es_fr1.fr", "es1.es", "fr1.fr", 5, 6,3));
		links.add(new Link("es1.es_it1.it", "es1.es", "it1.it", 5, 12,4));
		links.add(new Link("es1.es_pt1.pt", "es1.es", "pt1.pt", 5, 17,2));
		links.add(new Link("fr1.fr_lu1.lu", "fr1.fr", "lu1.lu", 6, 13,2));
		links.add(new Link("fr1.fr_uk1.uk", "fr1.fr", "uk1.uk", 6, 21,3));
		links.add(new Link("gr1.gr_it1.it", "gr1.gr", "it1.it", 7, 12,4));
		links.add(new Link("hr1.hr_hu1.hu", "hr1.hr", "hu1.hu", 8, 9,3));
		links.add(new Link("hr1.hr_si1.si", "hr1.hr", "si1.si", 8, 19,3));
		
		links.add(new Link("hu1.hu_sk1.sk", "hu1.hu", "sk1.sk", 9, 20,4));
		links.add(new Link("ie1.ie_uk1.uk", "ie1.ie", "uk1.uk", 10, 21,2));
		links.add(new Link("il1.il_it1.it", "il1.il", "it1.it", 11, 12,2));
		links.add(new Link("il1.il_nl1.nl", "il1.il", "nl1.nl", 11, 14,4));
		links.add(new Link("nl1.nl_uk1.uk", "nl1.nl", "uk1.uk", 14, 21,4));
		links.add(new Link("ny1.ny_uk1.uk", "ny1.ny", "uk1.uk", 15, 21,4));
		links.add(new Link("pl1.pl_se1.se", "pl1.pl", "se1.se", 16, 18,2));
		links.add(new Link("pt1.pt_uk1.uk", "pt1.pt", "uk1.uk", 17, 21,4));
		links.add(new Link("se1.se_uk1.uk", "se1.se", "uk1.uk", 18, 21,2));
		System.out.println("node num is "+ nodes.size()+" link num is "+ links.size());
		initMap();
	}

	public void testinitmap() {
		System.out.println(linkMap.get(Utils.getPair(21, 18)));
	}
	public void initMap() {
		nodeMap = new HashMap<String, Node>();
		for (int i = 0; i < nodes.size(); i++) {
			nodeMap.put(nodes.get(i).name, nodes.get(i));
		}

		linkMap = new HashMap<String, Link>();
		//System.out.println("links's size is"+links.size());
		for (int i = 0; i < links.size(); i++) {
			//System.out.println(links.get(i));
			linkMap.put(Utils.getPair(links.get(i).srcID, links.get(i).dstID), links.get(i));
		}
	}
	
	public Link getLink(String pair){
		return linkMap.get(pair);
	}

	public void resetLinks() {
		for (int i = 0; i < links.size(); i++) {
			links.get(i).rate = 0;
		}
	}

	public void initLinks(boolean topoState) {
		for (int i = 0; i < links.size(); i++) {
			links.get(i).topoState = topoState;
			links.get(i).rate = 0;
		}
	}

	public List<Link> getWorkLinks() {
		List<Link> workLinks = new LinkedList<>();
		for (Link link : links) {
			if (link.topoState) {
				workLinks.add(link);
			}
		}
		return workLinks;
	}

	public List<Link> getSleepLinks() {
		List<Link> sleepLinks = new LinkedList<>();
		for (Link link : links) {
			if (!link.topoState) {
				sleepLinks.add(link);
			}
		}
		return sleepLinks;
	}

	public int getWorkLinkNum() {
		return getWorkLinks().size();
	}

	public int getSleepLinkNum() {
		return links.size() - getWorkLinkNum();
	}

	public int getNodeNumber() {
		// TODO Auto-generated method stub
		
		return this.nodes.size();
	}
}
