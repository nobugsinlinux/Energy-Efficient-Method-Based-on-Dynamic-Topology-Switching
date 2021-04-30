package Entity;

public  class Node {
	public String name;
	public int ID;
	
	public Node(String name,int ID) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.ID=ID;
	}

	@Override
	public String toString() {
		return "Node [name=" + name + ", ID=" + ID + "]";
	}
	
}
