package alg_decompos_fixpath;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MaskFormatter;

import alg_constraintgeneration.ArcModel;
import data.ReadData;
import entity.*;

public class DecompFixPathEngine {
	public List<Flight> flightList = ReadData.flightList;
	public List<Flight> arrList = ReadData.arrList;
	public List<Flight> depList = ReadData.depList;
	public List<Node> spotList = ReadData.spotList;

	public double solve(int mps, int cpu){
		FindPathModel mip = new FindPathModel();
	
		for(Node spot:spotList){
			System.out.println("spot:"+spot+" f size:"+spot.getFlightList().size());
			if(spot.getFlightList().size()<1)continue;
			int id = 0;
			List<Flight> subArrList = new ArrayList<>();
			List<Flight> subDepList = new ArrayList<>();
			for(Flight f:spot.getFlightList()){
				f.setId(id++);
				if(f.isArr()){
					subArrList.add(f);
				}else subDepList.add(f);
			}
			mip.integerSolve(mps, cpu, spot.getFlightList(), subDepList, subArrList);
			
		}
		int id = 0;
		for(Flight f:flightList){
			f.setId(id++);
		}
		
		FixPathModel masterModel = new FixPathModel();
		return(masterModel.integerSolve(mps, cpu));
		
	}
}
