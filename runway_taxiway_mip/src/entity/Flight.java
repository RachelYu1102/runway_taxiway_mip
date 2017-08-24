package entity;

import java.util.ArrayList;
import java.util.List;

public class Flight {
	private int id;
	private String callsign;  //"a" - arrival; "d" - departure
	private Node originNode;
	private Node destinNode;
	private int startTime;
	private String weightCategory;
	private int pushbackDuration;  //buffer included, only for departure
	private boolean arr = true;
	private int FCFSposition;
	private int estimatedDestinTime; //set in DijkstraShortestPath (for departure)
	private int latestTime;
	private List<Node> candidateDestinNodes = new ArrayList<>(); //for arrival of same class, allow gate swap
	private Route route;
	private Path path;
	private int runwayTime;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(callsign+" "+originNode+"["+startTime+","+latestTime+"]-"+destinNode);
		return sb.toString();
	}

	public Path getPath() {
		return path;
	}
	public int getRunwayTime() {
		return runwayTime;
	}
	public void setRunwayTime(int runwayTime) {
		this.runwayTime = runwayTime;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public Route getRoute() {
		return route;
	}
	public void setRoute(Route route) {
		this.route = route;
	}
	public List<Node> getCandidateDestinNodes() {
		return candidateDestinNodes;
	}
	public void setCandidateDestinNodes(List<Node> candidateDestinNodes) {
		this.candidateDestinNodes = candidateDestinNodes;
	}
	public int getEstimatedDestinTime() {
		return estimatedDestinTime;
	}
	public void setEstimatedDestinTime(int estimatedDestinTime) {
		this.estimatedDestinTime = estimatedDestinTime;
	}
	public int getLatestTime() {
		return latestTime;
	}
	public void setLatestTime(int latestTime) {
		this.latestTime = latestTime;
	}
	public int getFCFSposition() {
		return FCFSposition;
	}
	public void setFCFSposition(int fCFSposition) {
		FCFSposition = fCFSposition;
	}
	public boolean isArr() {
		return arr;
	}
	public void setArr(boolean arr) {
		this.arr = arr;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Node getOriginNode() {
		return originNode;
	}
	public void setOriginNode(Node originNode) {
		this.originNode = originNode;
	}
	public Node getDestinNode() {
		return destinNode;
	}
	public void setDestinNode(Node destinNode) {
		this.destinNode = destinNode;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	
	public String getWeightCategory() {
		return weightCategory;
	}
	public void setWeightCategory(String weightCategory) {
		this.weightCategory = weightCategory;
	}
	public int getPushbackDuration() {
		return pushbackDuration;
	}
	public void setPushbackDuration(int pushbackDuration) {
		this.pushbackDuration = pushbackDuration;
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
		result = prime * result + ((destinNode == null) ? 0 : destinNode.hashCode());
		result = prime * result + id;
		result = prime * result + ((originNode == null) ? 0 : originNode.hashCode());
		result = prime * result + pushbackDuration;
		result = prime * result + startTime;
		result = prime * result + ((weightCategory == null) ? 0 : weightCategory.hashCode());
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
		Flight other = (Flight) obj;
		if (callsign == null) {
			if (other.callsign != null)
				return false;
		} else if (!callsign.equals(other.callsign))
			return false;
		if (destinNode == null) {
			if (other.destinNode != null)
				return false;
		} else if (!destinNode.equals(other.destinNode))
			return false;
		if (id != other.id)
			return false;
		if (originNode == null) {
			if (other.originNode != null)
				return false;
		} else if (!originNode.equals(other.originNode))
			return false;
		if (pushbackDuration != other.pushbackDuration)
			return false;
		if (startTime != other.startTime)
			return false;
		if (weightCategory == null) {
			if (other.weightCategory != null)
				return false;
		} else if (!weightCategory.equals(other.weightCategory))
			return false;
		return true;
	}
		
}
