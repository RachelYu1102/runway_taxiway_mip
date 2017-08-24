package alg_decompos_groupseq;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MaskFormatter;

import alg_constraintgeneration.ArcModel;
import data.ReadData;
import entity.*;

public class DecompGroupSeqEngine {
	public List<Flight> flightList = ReadData.flightList;
	public List<Flight> arrList = ReadData.arrList;
	public List<Flight> depList = ReadData.depList;
	public List<Node> spotList = ReadData.spotList;

	public double solve(int mps, int cpu){
		FindPathModel mip = new FindPathModel();
		List<Flight> allFlight = new ArrayList<>();
		List<Flight> tempFix = new ArrayList<>();

		for(Node spot:spotList){
			System.out.println("spot:"+spot+" f size:"+spot.getFlightList().size());
			if(spot.getFlightList().size()<1)continue;
			allFlight.addAll(spot.getFlightList());
			int id = 0;
			List<Flight> subArrList = new ArrayList<>();
			List<Flight> subDepList = new ArrayList<>();
			for(Flight f:allFlight){
				f.setId(id++);
				if(f.isArr()){
					subArrList.add(f);
				}else subDepList.add(f);
			}
			mip.integerSolve(mps, cpu, allFlight, tempFix, subDepList, subArrList);
			tempFix.addAll(spot.getFlightList());
		}
		int id = 0;
		for(Flight f:flightList){
			f.setId(id++);
		}
		
		FixPathModel masterModel = new FixPathModel();
		return(masterModel.integerSolve(mps, cpu));
		
	}
}
