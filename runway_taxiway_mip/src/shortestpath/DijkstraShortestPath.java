package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comparator.LabelNodeComparator;
import data.ReadData;
import entity.*;

public class DijkstraShortestPath {
	private int bigM = 10000;
	
	public void findShortestPath(Flight f){
		for(Node n:ReadData.nodeList){
			n.setLabel(bigM);
		}
		f.getOriginNode().setLabel(0);
		List<Node> tempList = new ArrayList<>();
		tempList.addAll(ReadData.nodeList);
		tempList.remove(0);
		List<Node> sList = new ArrayList<>();
		while(!sList.contains(f.getDestinNode())){
			Collections.sort(tempList, new LabelNodeComparator());
			Node u = tempList.get(0);
			sList.add(u);
			tempList.remove(0);
			for(Arc a:u.getOutArcList()){
				if(u.getLabel()+a.getLength()<a.getEndNode().getLabel()){
					a.getEndNode().setLabel(u.getLabel()+a.getLength());
				}
			}
		}
		f.setEstimatedDestinTime(f.getStartTime()+f.getPushbackDuration()+f.getDestinNode().getLabel());
	}
}
