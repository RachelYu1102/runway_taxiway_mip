package alg_diving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comparator.StarttimeFlightComparator;
import data.ReadData;
import entity.ArcFlight;
import entity.Flight;

public class DivingEngine {
	public List<Flight> flightList = ReadData.flightList;
	public List<Flight> arrList = ReadData.arrList;
	public List<Flight> depList = ReadData.depList;
	
	public void solve(int mps, int cpu){
		List<ArcFlight> afList = new ArrayList<>();
		List<Flight> inLoopFlightList = new ArrayList<>();
		inLoopFlightList.addAll(flightList);
		Collections.sort(inLoopFlightList,new StarttimeFlightComparator());
		LinearModel lm = new LinearModel();
		int iter = 0;
		while(inLoopFlightList.size()>0){
			Flight f = inLoopFlightList.get(iter%inLoopFlightList.size());
			lm.linearSolve(mps, cpu, afList,f);
			if(f.getPath()!=null){
				inLoopFlightList.remove(f);
			}else {
				System.out.println("iter "+iter);
				iter++;
			}
		}
		FixPathModel fpm = new FixPathModel();
		fpm.integerSolve(mps, cpu);
	}
}	
