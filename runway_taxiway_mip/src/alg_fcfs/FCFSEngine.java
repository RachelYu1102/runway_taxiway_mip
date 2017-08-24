package alg_fcfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alg_decompos_fixpath.FindPathModel;
import alg_decompos_fixpath.FixPathModel;
import comparator.*;
import data.ReadData;
import entity.Flight;
import entity.Node;

public class FCFSEngine {
	
	public double solve(int mps, int cpu){
		OneByOneModel mip = new OneByOneModel();
		List<Flight> FCFSflight = new ArrayList<>();
		FCFSflight.addAll(ReadData.flightList);
		Collections.sort(FCFSflight,new RunwaytimeFlightComparator());
		
		double returnValue = 0;
		for(int i=0;i<FCFSflight.size();i++){
			FCFSflight.get(i).setId(i);  //assign a new id
			List<Flight> subFlightList = new ArrayList<>();
			List<Flight> subArrList = new ArrayList<>();
			List<Flight> subDepList = new ArrayList<>();
			int j = 0;
			while(j<=i){
				Flight f = FCFSflight.get(j);
				subFlightList.add(f);
				if(f.isArr()){
					subArrList.add(f);
				}else subDepList.add(f);
				j++;
			}
			returnValue = mip.integerSolve(mps, cpu, subFlightList, subDepList, subArrList);
			System.out.println("solve flights "+(i+1)+"/"+FCFSflight.size());
		}
		
		return returnValue;		
	}
}
