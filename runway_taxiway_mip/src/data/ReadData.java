package data;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import comparator.EstimatedtakeofftimeFlightComparator;
import comparator.StarttimeFlightComparator;
import entity.*;
import shortestpath.DijkstraShortestPath;

public class ReadData {
	public static List<Flight> flightList;
	public static List<Node> gateList;
	public static List<Node> spotList;
	public static List<RunwayExit> exitList;
	public static List<RunwayEntry> entryList;
	public static List<Flight> arrList;
	public static List<Flight> depList;
	public static List<Node> nodeList;
	public static Map<String,Node> nodeMap;  //string: node callsign
	public static List<Arc> arcList;  //include pseudo-arcs (start and end)
	public static Map<String,Arc> arcMap;  //string: node-node
	public static List<Arc> pseudoOriginArcs;
	public static List<Arc> pseudoDestinArcs;
	public static Map<String, Integer> typeSprDepMap; //separation for departure based on aircraft type
	public static Map<String, Integer> sprMap; //separation for departure flight
	public static Map<String, Integer> typeSprArrMap; //separation for departure based on aircraft type

	private String fltData = "data/flight.csv";
	private String arcData = "data/arc.csv";
	private String arrSepData = "data/arrSeparation.csv";
	private String depSepData = "data/depSeparation.csv";

	public static int taxiSep = 30;
	private int earliestEndTime = 0;
	public static double gap = 0;
	public static int timewindowDuration = 900;
	public static int compactDenominator = 1;
	
	public void setData(String flightData, String arcData){
		this.fltData = flightData;
		this.arcData = arcData;
	}
	public void setData(String flightData, String arcData,String arrSepData,String depSepData){
		this.earliestEndTime = 0;
		this.fltData = flightData;
		this.arcData = arcData;
		this.arrSepData = arrSepData;
		this.depSepData = depSepData;
	}
	public void readArc(){
		nodeList = new ArrayList<>();
		nodeMap = new HashMap<>();
		arcList = new ArrayList<>();
		arcMap = new HashMap<>();

		exitList = new ArrayList<>();
		entryList = new ArrayList<>();
		gateList = new ArrayList<>();
		spotList = new ArrayList<>();

		Node dummyOrigin = new Node();
		dummyOrigin.setCallsign("o");
		Node dummyDestin = new Node();
		dummyDestin.setCallsign("d");
		nodeMap.put("o", dummyOrigin);
		nodeList.add(dummyOrigin);
		nodeMap.put("d", dummyDestin);
		nodeList.add(dummyDestin);

		try {
			Scanner sc = new Scanner(new File(arcData));

			while(sc.hasNextLine()){
				String nextLine = sc.nextLine();
				if(nextLine.trim().equals("")){
					break;
				}
				Scanner innerSc = new Scanner(nextLine);
				innerSc.useDelimiter(",");
				String sNodeCallsign = innerSc.next();
				String eNodeCallsign = innerSc.next();
				int length = innerSc.nextInt();
				Node startNode;
				Node endNode;
				if(!nodeMap.containsKey(eNodeCallsign)){
					endNode = new Node();
					endNode.setCallsign(eNodeCallsign);
					nodeMap.put(eNodeCallsign, endNode);
					nodeList.add(endNode);
					if(eNodeCallsign.contains("e")){
						RunwayEntry entry = new RunwayEntry();
						entry.setNode(endNode);
						entryList.add(entry);
					}
				}else{
					endNode = nodeMap.get(eNodeCallsign);
				}
				if(!nodeMap.containsKey(sNodeCallsign)){
					startNode = new Node();
					startNode.setCallsign(sNodeCallsign);
					nodeMap.put(sNodeCallsign, startNode);
					nodeList.add(startNode);
					if(sNodeCallsign.contains("e")){
						RunwayExit exit = new RunwayExit();
						exit.setNode(startNode);
						exitList.add(exit);
					}
					/*if(sNodeCallsign.contains("g")){
						gateList.add(startNode);
						startNode.setSpot(endNode);
						if(!spotList.contains(endNode))spotList.add(endNode);
					}*/
					if(!isNumeric(sNodeCallsign)){  //contains character
						if(sNodeCallsign.contains("ex")||sNodeCallsign.contains("ey")){
							//do nothing
						}else{
							gateList.add(startNode);
							startNode.setSpot(endNode);
							if(!spotList.contains(endNode))spotList.add(endNode);
						}
						
					}else if(Integer.parseInt(sNodeCallsign)>199){
						gateList.add(startNode);
						startNode.setSpot(endNode);
						if(!spotList.contains(endNode))spotList.add(endNode);
					}
				}else{
					startNode = nodeMap.get(sNodeCallsign);
				}

				Arc arc = new Arc();
				arc.setStartNode(startNode);
				arc.setEndNode(endNode);
				arc.setLength(length);
				arcMap.put(sNodeCallsign+"_"+eNodeCallsign, arc);
				arcList.add(arc);
				startNode.getOutArcList().add(arc);
				endNode.getInArcList().add(arc);
				if(eNodeCallsign.contains("e")||sNodeCallsign.contains("e")){
					arc.setOnTaxiway(false);
					continue;  //entry and exit are uni-directional, don't create reverse arc
				}
				Arc reverseArc = new Arc();
				reverseArc.setStartNode(endNode);
				reverseArc.setEndNode(startNode);
				reverseArc.setLength(length);
				arcMap.put(eNodeCallsign+"_"+sNodeCallsign, reverseArc);
				arcList.add(reverseArc);
				endNode.getOutArcList().add(reverseArc);
				startNode.getInArcList().add(reverseArc);

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void readFlight(){		
		flightList = new ArrayList<Flight>();
		arrList = new ArrayList<>();
		depList = new ArrayList<>();
		int id = 0;
		try {
			Scanner sc = new Scanner(new File(fltData));

			while(sc.hasNextLine()){
				String nextLine = sc.nextLine();
				if(nextLine.trim().equals("")){
					break;
				}
				Scanner innerSc = new Scanner(nextLine);
				innerSc.useDelimiter(",");
				String callsign = innerSc.next();
				String originCallsign = innerSc.next();
				String destinCallsign = innerSc.next();
				int startTime = innerSc.nextInt();
				String weightCategory = innerSc.next();
				int pushbackDuration = innerSc.nextInt();

				Flight flt = new Flight();
				flt.setId(id++);
				flt.setCallsign(callsign);
				flt.setOriginNode(nodeMap.get(originCallsign));
				flt.setDestinNode(nodeMap.get(destinCallsign));
				flt.setStartTime(startTime/compactDenominator);
				flt.setWeightCategory(weightCategory);
				flt.setPushbackDuration(pushbackDuration);

				flightList.add(flt);
				if(callsign.contains("a"))arrList.add(flt);
				if(callsign.contains("d")){
					flt.setArr(false);
					depList.add(flt);
				}

				if(arcMap.get("o_"+originCallsign)==null){
					Arc pseudoOriginArc = new Arc();
					pseudoOriginArc.setLength(0);
					pseudoOriginArc.setStartNode(nodeMap.get("o"));
					pseudoOriginArc.setEndNode(nodeMap.get(originCallsign));
					pseudoOriginArc.getStartNode().getOutArcList().add(pseudoOriginArc);
					pseudoOriginArc.getEndNode().getInArcList().add(pseudoOriginArc);
					pseudoOriginArc.setOnTaxiway(false);
					arcMap.put("o_"+originCallsign, pseudoOriginArc);
					arcList.add(pseudoOriginArc);
				}
				if(arcMap.get(destinCallsign+"_d")==null){
					Arc pseudoDestinArc = new Arc();
					pseudoDestinArc.setLength(0);
					pseudoDestinArc.setStartNode(nodeMap.get(destinCallsign));
					pseudoDestinArc.setEndNode(nodeMap.get("d"));
					pseudoDestinArc.getStartNode().getOutArcList().add(pseudoDestinArc);
					pseudoDestinArc.getEndNode().getInArcList().add(pseudoDestinArc);
					pseudoDestinArc.setOnTaxiway(false);
					arcMap.put(destinCallsign+"_d", pseudoDestinArc);		
					arcList.add(pseudoDestinArc);
				}		
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readSeparation(){
		typeSprDepMap = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(new File(depSepData));
			while(sc.hasNextLine()){
				String nextLine = sc.nextLine();
				if(nextLine.trim().equals("")){
					break;
				}
				Scanner innerSc = new Scanner(nextLine);
				innerSc.useDelimiter(",");
				String leadingType = innerSc.next();
				String trailingType = innerSc.next();
				int sprTime = innerSc.nextInt();
				typeSprDepMap.put(leadingType+"_"+trailingType, sprTime);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		typeSprArrMap = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(new File(arrSepData));
			while(sc.hasNextLine()){
				String nextLine = sc.nextLine();
				if(nextLine.trim().equals("")){
					break;
				}
				Scanner innerSc = new Scanner(nextLine);
				innerSc.useDelimiter(",");
				String leadingType = innerSc.next();
				String trailingType = innerSc.next();
				int sprTime = innerSc.nextInt();
				typeSprArrMap.put(leadingType+"_"+trailingType, sprTime);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void allInOne(){
		readArc();
		readFlight();
		readSeparation();
		//assign node id
		int nodeId = 0;
		for(Node n:nodeList){
			n.setId(nodeId++);
		}

		//create pseudoArcs, circle arc, and assign arc id
		pseudoOriginArcs = new ArrayList<>();
		pseudoDestinArcs = new ArrayList<>();
		Arc circleArc = new Arc();
		circleArc.setStartNode(nodeMap.get("d"));
		circleArc.setEndNode(nodeMap.get("o"));
		circleArc.getStartNode().getOutArcList().add(circleArc);
		circleArc.getEndNode().getInArcList().add(circleArc);
		circleArc.setOnTaxiway(false);
		arcList.add(circleArc);
		arcMap.put("d_o", circleArc);	

		int arcId = 0;
		for(Arc a:arcList){
			a.setId(arcId++);
			if(a.getStartNode().getCallsign().contains("o")){
				pseudoOriginArcs.add(a);
			}
			if(a.getEndNode().getCallsign().contains("d")){
				pseudoDestinArcs.add(a);
			}
		}	
		//assign separation between each pair of departure
		sprMap = new HashMap<String, Integer>();
		for(int i=0;i<flightList.size();i++){
			for(int j=0;j<flightList.size();j++){
				if(i==j)continue;
				Flight fi = flightList.get(i);
				Flight fj = flightList.get(j);
				String typeI = fi.getWeightCategory();
				String typeJ = fj.getWeightCategory();
				if(fi.isArr()&&fj.isArr()){
					int sprTime = typeSprArrMap.get(typeI+"_"+typeJ);
					sprMap.put(fi.getCallsign()+"_"+fj.getCallsign(), sprTime);
				}else if(!fi.isArr()&&!fj.isArr()){
					int sprTime = typeSprDepMap.get(typeI+"_"+typeJ);
					sprMap.put(fi.getCallsign()+"_"+fj.getCallsign(), sprTime);
				}

			}
		}

		//determine the FCFS position
		DijkstraShortestPath dsp = new DijkstraShortestPath();
		
		for(Flight f:flightList){
			dsp.findShortestPath(f);
			earliestEndTime += f.getEstimatedDestinTime();
			if(f.isArr()){
				f.setRunwayTime(f.getStartTime());
			}else{
				f.setRunwayTime(f.getEstimatedDestinTime());
			}
		}
		
		List<Flight> FCFSdepList = new ArrayList<>();
		FCFSdepList.addAll(depList); //sort by ETT
		Collections.sort(FCFSdepList,new EstimatedtakeofftimeFlightComparator());
		//Collections.sort(FCFSdepList,new StarttimeFlightComparator());
		for(int i=0;i<FCFSdepList.size();i++){
			FCFSdepList.get(i).setFCFSposition(i);
			System.out.println(FCFSdepList.get(i)+" "+i);
		}

		List<Flight> FCFSarrList = new ArrayList<>();
		FCFSarrList.addAll(arrList); //sort by start time
		Collections.sort(FCFSarrList,new StarttimeFlightComparator());
		for(int i=0;i<FCFSarrList.size();i++){
			FCFSarrList.get(i).setFCFSposition(i);
		}

		//set latest time for flights
		for(Flight f:flightList){
			f.setLatestTime(f.getStartTime()+timewindowDuration);  //maximum delay: 15 minutes
		}

		//set candidate gates for arrivals of same class
		for(Flight f:arrList){
			for(Flight f2:arrList){
				if(f2.getWeightCategory().equals(f.getWeightCategory())){
					f.getCandidateDestinNodes().add(f2.getDestinNode());
				}
			}
		}

		//group flights according to the gate assigned
		for(Flight f:flightList){
			if(f.isArr()){
				f.getDestinNode().getSpot().getFlightList().add(f);
			}else{
				f.getOriginNode().getSpot().getFlightList().add(f);
			}
		}
	}
	
	public boolean isNumeric(String str){  
		for (int i = str.length();--i>=0;){    
			if (!Character.isDigit(str.charAt(i))){  
				return false;  
			}  
		}  
		return true;  
	}  
	public int getEarliestEndTime(){
		return this.earliestEndTime;
	}
}
