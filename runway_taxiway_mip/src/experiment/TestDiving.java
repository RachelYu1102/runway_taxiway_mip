package experiment;

import alg_diving.DivingEngine;
import data.ReadData;

public class TestDiving {

	public static void main(String[] args) {
		ReadData rd = new ReadData();
		//rd.setData("data/flight_5.csv", "data/arc_6.csv");
		//rd.setData("data/flight_T3SouthApron_8.csv", "data/arc_T3SouthApron_roupGates.csv");
		rd.setData("data/flight_8.csv", "data/arc_T3_groupGates_reduce.csv");
		//rd.setData("data/flight_6.csv", "data/arc_T3_groupGates.csv");
		//rd.setData("data/flight_test.csv", "data/arc_test.csv");
		rd.allInOne();
		
		long startTime = System.currentTimeMillis();  //timing-start
	
		DivingEngine controller = new DivingEngine();
		int cpu = 10800;

		controller.solve(1,cpu);
		
		long endTime = System.currentTimeMillis();  //timing-end
		System.out.println("diving cpu:"+(endTime-startTime)/1000.0);
	}

}
