package alg_decompos_fixsequence;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MaskFormatter;

import alg_constraintgeneration.ArcModel;
import data.ReadData;
import entity.*;

public class DecompEngine {
	public List<Flight> flightList = ReadData.flightList;
	public List<Flight> arrList = ReadData.arrList;
	public List<Flight> depList = ReadData.depList;
	public List<Node> spotList = ReadData.spotList;
	private int mps = 1;
	public void solve(int maxIter, int cpu){
		SubgroupModel mip = new SubgroupModel();
		List<SequencePair> sequPairList = new ArrayList<>();
		for(Node spot:spotList){
			//System.out.println("spot:"+spot+" f size:"+spot.getFlightList().size());
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
			mip.integerSolve(mps, cpu, spot.getFlightList(), subDepList, subArrList,sequPairList);
			
		}
		int id = 0;
		for(Flight f:flightList){
			f.setId(id++);
		}
		
		MasterModel masterModel = new MasterModel();
		masterModel.integerSolve(mps, cpu, flightList, depList, arrList, sequPairList);
		
	}
}
