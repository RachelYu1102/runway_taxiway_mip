package experiment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.ReadData;
import entity.*;

public class TestRD {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReadData rd = new ReadData();	
		rd.setData("data/flight_6.csv", "data/arc_6.csv");
		//rd.setData("data/aircraft_AIAA_A.csv", "data/arc_AIAA_A.csv",);
		rd.allInOne();
		
		/*for(Arc a:rd.arcList){
			System.out.println(a.getId()+" "+a);
		}*/
		for(Node n:rd.nodeList){
			System.out.println(n.getId()+" "+n);
			for(Arc a:n.getOutArcList()){
				System.out.print(a+" ");
			}
			System.out.println();
			for(Arc a:n.getInArcList()){
				System.out.print(a+" ");
			}
			System.out.println();
		}
		/*Map map = rd.sprMap;
		Iterator ite = map.entrySet().iterator(); 
		while (ite.hasNext()) { 
			Map.Entry entry = (Map.Entry) ite.next(); 
		    String key = (String)entry.getKey(); 
		    Integer val = (Integer)entry.getValue(); 
		    //System.out.println(key+" "+val);
		}
		
		for(RunwayExit rex:rd.exitList){
			System.out.println(rex);
		}
		for(RunwayEntry ren:rd.entryList){
			System.out.println(ren);
		}*/
		
		int a1 = 1;
		int b1 = 2;
		System.out.println(a1/b1);
		System.out.println(1.0*a1/b1);
		double c1 = 2;
		System.out.println(a1/c1);
	}

}
