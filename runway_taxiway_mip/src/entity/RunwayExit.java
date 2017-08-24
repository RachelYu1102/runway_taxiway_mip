package entity;

import java.util.ArrayList;
import java.util.List;

public class RunwayExit {
	private int id;
	private Node node;
		
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("runway exit<"+id+"> n "+node);
		return sb.toString();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}

}
