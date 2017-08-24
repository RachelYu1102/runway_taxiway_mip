package entity;

public class ConstraintArc {
	private int id;
	private Arc arc;
	private Flight f1;
	private Flight f2;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Arc getArc() {
		return arc;
	}
	public void setArc(Arc arc) {
		this.arc = arc;
	}
	public Flight getF1() {
		return f1;
	}
	public void setF1(Flight f1) {
		this.f1 = f1;
	}
	public Flight getF2() {
		return f2;
	}
	public void setF2(Flight f2) {
		this.f2 = f2;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("<"+arc.getStartNode()+","+arc.getEndNode()+"> "+f1.getCallsign()+"-"+f2.getCallsign());
		return sb.toString();
	}
}
