package entity;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private int id;
	private String callsign;
	private List<Arc> outArcList = new ArrayList<>();
	private List<Arc> inArcList = new ArrayList<>();
	private Node spot;  // used for group gate, only exist for gate node
	private List<Flight> flightList = new ArrayList<>(); //used for group flights, only exist for spot node
	private int label; //used in Dijkstra shortest path
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(callsign);
		return sb.toString();
	}

	public List<Flight> getFlightList() {
		return flightList;
	}

	public void setFlightList(List<Flight> flightList) {
		this.flightList = flightList;
	}

	public int getLabel() {
		return label;
	}

	public Node getSpot() {
		return spot;
	}

	public void setSpot(Node spot) {
		this.spot = spot;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public List<Arc> getOutArcList() {
		return outArcList;
	}

	public void setOutArcList(List<Arc> outArcList) {
		this.outArcList = outArcList;
	}

	public List<Arc> getInArcList() {
		return inArcList;
	}

	public void setInArcList(List<Arc> inArcList) {
		this.inArcList = inArcList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callsign == null) ? 0 : callsign.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (callsign == null) {
			if (other.callsign != null)
				return false;
		} else if (!callsign.equals(other.callsign))
			return false;
		if (id != other.id)
			return false;
		return true;
	}


}
