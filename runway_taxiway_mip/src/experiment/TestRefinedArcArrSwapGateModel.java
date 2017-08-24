package experiment;

import data.ReadData;
import mip.ArrSwapGateModel;
import mip.RefinedArcModel;

public class TestRefinedArcArrSwapGateModel {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReadData rd = new ReadData();
		rd.setData("data/flight_5.csv", "data/arc_6.csv");
		//rd.setData("data/flight_T3SouthApron_4.csv", "data/arc_T3SouthApron_roupGates.csv");
		//rd.setData("data/flight_test.csv", "data/arc_test.csv");
		rd.allInOne();
	
		ArrSwapGateModel mip = new ArrSwapGateModel();
		int mps = 1;
		int cpu = 10800;
		System.out.println("maximum position shifting "+mps);
		mip.integerSolve(mps, cpu);
	}

}
