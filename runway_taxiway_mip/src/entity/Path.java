package entity;

import java.util.ArrayList;
import java.util.List;

public class Path {
	private int id;
	private List<Node> nodeList = new ArrayList<>();
	private List<Arc> arcList = new ArrayList<>();
	private int unimpededTime;  //length
	private Flight f;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Node n:nodeList){
			sb.append(n+"-");
		}
		return sb.toString();
	}
	
	public Flight getF() {
		return f;
	}

	public void setF(Flight f) {
		this.f = f;
	}

	public List<Node> commonNode(Path otherPath){
		//exclude destination node
		List<Node> commonNodeList = new ArrayList<>();
		for(int i=1;i<nodeList.size()-1;i++){
			Node node = nodeList.get(i);
			if(otherPath.getNodeList().contains(node)){
				commonNodeList.add(node);
			}
		}
		return commonNodeList;
	}
	
	public List<Arc> common(Path otherPath){
		//take-over conflict
		List<Arc> commonArcList = new ArrayList<>();
		for(int i=0;i<nodeList.size()-1;i++){
			Node n1 = nodeList.get(i);
			Node n2 = nodeList.get(i+1);
			for(int j=0;j<otherPath.getNodeList().size()-1;j++){
				Node n3 = otherPath.getNodeList().get(j);
				if(n3.equals(n1)){
					Node n4 = otherPath.getNodeList().get(j+1);
					if(n4.equals(n2)){
						commonArcList.add(arcList.get(i));
					}
				}
			}
		}
		return commonArcList;
	}
	
	public List<Arc> reverseArc(Path otherPath){
		//facing reverse arc
		List<Arc> commonArcList = new ArrayList<>();
		for(int i=0;i<nodeList.size()-1;i++){
			Node n1 = nodeList.get(i);
			Node n2 = nodeList.get(i+1);
			for(int j=0;j<otherPath.getNodeList().size()-1;j++){
				Node n3 = otherPath.getNodeList().get(j);
				if(n3.equals(n2)){
					Node n4 = otherPath.getNodeList().get(j+1);
					if(n4.equals(n1)){
						commonArcList.add(arcList.get(i));
					}
				}
			}
		}
		return commonArcList;
	}
	
	public int getUnimpededTime() {
		return unimpededTime;
	}
	public void setUnimpededTime(int unimpededTime) {
		this.unimpededTime = unimpededTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Node> getNodeList() {
		return nodeList;
	}
	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}
	public List<Arc> getArcList() {
		return arcList;
	}
	public void setArcList(List<Arc> arcList) {
		this.arcList = arcList;
	}

	public int repeteNodeSize(Path p) {
		// TODO Auto-generated method stub
		int repeteSize = 0;
		for(Node n:p.getNodeList()){
			for(Node n1:nodeList){
				if(n.equals(n1)){
					repeteSize++;
					continue;
				}
			}
		}
		return repeteSize;
	}
	
	
}
