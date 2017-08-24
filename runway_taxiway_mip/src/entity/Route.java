package entity;

import java.util.ArrayList;
import java.util.List;

import data.ReadData;

public class Route {
	private int id;
	private Flight f;
	private List<Node> nodeList = new ArrayList<>();
	private List<Integer> timeList = new ArrayList<>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Flight getF() {
		return f;
	}
	public void setF(Flight f) {
		this.f = f;
	}
	public List<Node> getNodeList() {
		return nodeList;
	}
	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}
	public List<Integer> getTimeList() {
		return timeList;
	}
	public void setTimeList(List<Integer> timeList) {
		this.timeList = timeList;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(f.getCallsign()+" ");
		for(int i=0;i<nodeList.size();i++){
			sb.append(nodeList.get(i).getCallsign()+"["+timeList.get(i)+"]-");
		}
		return sb.toString();
	}
	public List<ConstraintNode> conflictNode(Route otherRoute){
		//exclude origin and destination node
		List<ConstraintNode> conflictNodeList = new ArrayList<>();
		for(int i=1;i<nodeList.size()-1;i++){
			Node node = nodeList.get(i);
			if(otherRoute.getNodeList().contains(node)){
				if(Math.abs(timeList.get(i)-otherRoute.getTimeList().get(otherRoute.getNodeList().indexOf(node)))<ReadData.taxiSep){
					ConstraintNode cn = new ConstraintNode();
					cn.setF1(this.f);
					cn.setF2(otherRoute.getF());
					cn.setNode(node);
					conflictNodeList.add(cn);
				}
			}
		}
		return conflictNodeList;
	}
	
	public List<ConstraintArc> conflictArc(Route otherRoute){
		//take-over conflict
		List<ConstraintArc> conflictArcList = new ArrayList<>();
		for(int i=1;i<nodeList.size()-1;i++){
			Node n1 = nodeList.get(i);
			Node n2 = nodeList.get(i+1);
			for(int j=1;j<otherRoute.getNodeList().size()-1;j++){
				Node n3 = otherRoute.getNodeList().get(j);
				if(n3.equals(n1)){
					Node n4 = otherRoute.getNodeList().get(j+1);
					if(n4.equals(n2)){
						int t1 = timeList.get(i);
						int t2 = timeList.get(i+1);
						int t3 = otherRoute.getTimeList().get(j);
						int t4 = otherRoute.getTimeList().get(j+1);
						if((t1-t3)*(t2-t4)<0){  
							ConstraintArc ca = new ConstraintArc();
							ca.setF1(this.f);
							ca.setF2(otherRoute.getF());
							ca.setArc(ReadData.arcMap.get(n1.getCallsign()+"_"+n2.getCallsign()));
							conflictArcList.add(ca);
						}
					}
				}
			}
		}
		return conflictArcList;
	}
	
	public List<ConstraintArc> conflictReverseArc(Route otherRoute){
		//facing reverse arc
		List<ConstraintArc> conflictArcList = new ArrayList<>();
		for(int i=1;i<nodeList.size()-1;i++){
			Node n1 = nodeList.get(i);
			Node n2 = nodeList.get(i+1);
			for(int j=1;j<otherRoute.getNodeList().size()-1;j++){
				Node n3 = otherRoute.getNodeList().get(j);
				if(n3.equals(n2)){
					Node n4 = otherRoute.getNodeList().get(j+1);
					if(n4.equals(n1)){
						int t1 = timeList.get(i);
						int t2 = timeList.get(i+1);
						int t3 = otherRoute.getTimeList().get(j);
						int t4 = otherRoute.getTimeList().get(j+1);
						if((t2-t3)*(t1-t4)<0){
							ConstraintArc ca = new ConstraintArc();
							ca.setF1(this.f);
							ca.setF2(otherRoute.getF());
							ca.setArc(ReadData.arcMap.get(n1.getCallsign()+"_"+n2.getCallsign()));
							conflictArcList.add(ca);
						}
					}
				}
			}
		}
		return conflictArcList;
	}
	
}
