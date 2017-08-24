package experiment;

import java.io.IOException;

import alg_constraintgeneration.IterationEngine;
import alg_decompos_fixpath.DecompFixPathEngine;
import alg_fcfs.FCFSEngine;
import alg_twostage.TwostageEngine;
import data.ReadData;
import mip.BasicArcModel;
import mip.RefinedArcModel;
import mip.RefinedArcModelS;
import mip.RefinedArcModel_fuelcost;

public class TestRefinedArcModel {

	public static void main(String[] args) {
		try {
			
			String[] period = {"Evening"};
			int beginData = 2;
			int endDate = 2;
			int minFlightNum = 8;
			int maxFlightNum = 8;
			int mps = 1;
			int cpu = 7200;
			boolean refineArc = true;
					
			if(refineArc){
				MyFile.creatTxtFile("result/RefinedArc.txt");			
				for(int i=beginData;i<=endDate;i++){
					for(int j=0;j<period.length;j++){
						for(int fNum = minFlightNum; fNum<=maxFlightNum;fNum = fNum + 2){
							String flightData = "smalldata/"+i+"_"+period[j]+"_T3South_"+fNum+".csv";
							ReadData rd = new ReadData();
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start
							
							RefinedArcModel mip = new RefinedArcModel();			
							double obj = mip.integerSolve(mps, cpu) - rd.getEarliestEndTime();						
							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("Refined arc cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps+",gap "+ReadData.gap);
							System.exit(1);
						}
					}		
				}	
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
