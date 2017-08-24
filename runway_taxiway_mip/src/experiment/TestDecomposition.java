package experiment;

import java.io.IOException;

import alg_constraintgeneration.IterationEngine;
import alg_decompos_fixpath.DecompFixPathEngine;
import alg_decompos_fixsequence.DecompEngine;
import data.ReadData;
import mip.RefinedArcModel;
import mip.RefinedArcModelS;

public class TestDecomposition {

	public static void main(String[] args) {
		try {
			MyFile.creatTxtFile("result/path_fix.txt");
			//rd.setData("data/flight_40.csv", "data/arc_T2T3_Sim.csv");
			String[] period = {"Morning","Noon","Evening"};
			for(int i=1;i<=31;i++){
				for(int j=0;j<period.length;j++){
					String flightData = "changidata/"+i+"_2014_Flight_"+period[j]+".csv";
					ReadData rd = new ReadData();
					rd.setData(flightData, "changidata/changi_arc_T2T3.csv",
							"changidata/changi_arrSeparation.csv","changidata/changi_depSeparation.csv");
					rd.allInOne();		
					//DecompEngine controller = new DecompEngine();
					long startTime = System.currentTimeMillis();  //timing-start
					
					DecompFixPathEngine controller = new DecompFixPathEngine();
					int cpu = 10800;
					int mps = 1;
					double obj = controller.solve(mps,cpu) - rd.getEarliestEndTime();
					
					long endTime = System.currentTimeMillis();  //timing-end
					double totalCPU = (endTime-startTime)/1000.0;
					System.out.println("Path fix cpu "+totalCPU);
					MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", flightNum"+ReadData.flightList.size());
					
				}
				
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
