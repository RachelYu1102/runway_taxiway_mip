package experiment;

import alg_constraintgeneration.IterationEngine;
import alg_twostage.TwostageEngine;
import data.ReadData;
import mip.RefinedArcModel;
import mip.RefinedArcModelS;

public class TestTwostage {

	public static void main(String[] args) {
		ReadData rd = new ReadData();
		rd.setData("data/flight_8.csv", "data/arc_T2T3_Sim.csv");
		rd.allInOne();
		long startTime = System.currentTimeMillis();  //timing-start
		TwostageEngine controller = new TwostageEngine();
		int cpu = 10800;
		int totalIter = 3;
		int mps = 1;
		controller.solve(mps,cpu);
		long endTime = System.currentTimeMillis();  //timing-end
		System.out.println("cpu "+(endTime-startTime)/1000.0);
	}

}
