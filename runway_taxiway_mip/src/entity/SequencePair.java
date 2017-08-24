package entity;

public class SequencePair {
	private Flight headF;
	private Flight tailF;
	private Node node;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("<"+headF.getCallsign()+","+tailF.getCallsign()+"> n:"+node.getCallsign());
		return sb.toString();
	}
	public Flight getHeadF() {
		return headF;
	}
	public void setHeadF(Flight headF) {
		this.headF = headF;
	}
	public Flight getTailF() {
		return tailF;
	}
	public void setTailF(Flight tailF) {
		this.tailF = tailF;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	};
	
}
