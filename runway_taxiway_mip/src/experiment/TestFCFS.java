package experiment;

import java.io.IOException;

import alg_constraintgeneration.IterationEngine;
import alg_decompos_fixpath.DecompFixPathEngine;
import alg_fcfs.FCFSEngine;
import data.ReadData;
import mip.RefinedArcModel;
import mip.RefinedArcModelS;

public class TestFCFS {

	public static void main(String[] args) {
		try {
			MyFile.creatTxtFile("result/FCFS.txt");
			//rd.setData("data/flight_40.csv", "data/arc_T2T3_Sim.csv");
			String[] period = {"Morning","Noon","Evening"};
			for(int i=1;i<=29;i++){
				for(int j=0;j<period.length;j++){
					String flightData = "changidata/"+i+"_2014_Flight_"+period[j]+".csv";
					ReadData rd = new ReadData();
					rd.setData(flightData, "changidata/changi_arc_T2T3.csv",
							"changidata/changi_arrSeparation.csv","changidata/changi_depSeparation.csv");
					rd.allInOne();		
					long startTime = System.currentTimeMillis();  //timing-start
					
					FCFSEngine controller = new FCFSEngine();
					int cpu = 10800;
					int mps = 1;
					double obj = controller.solve(mps,cpu) - rd.getEarliestEndTime();;
					
					long endTime = System.currentTimeMillis();  //timing-end
					double totalCPU = (endTime-startTime)/1000.0;
					System.out.println("FCFS cpu "+totalCPU);
					MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", flightNum"+ReadData.flightList.size());
					
				}
				
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
