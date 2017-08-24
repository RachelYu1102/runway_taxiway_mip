package experiment;

import java.io.IOException;

import alg_constraintgeneration.IterationEngine;
import alg_decompos_fixpath.DecompFixPathEngine;
import alg_decompos_groupseq.DecompGroupSeqEngine;
import alg_fcfs.FCFSEngine;
import alg_twostage.TwostageEngine;
import data.ReadData;
import mip.BasicArcModel;
import mip.RefinedArcModel;
import mip.RefinedArcModelS;
import mip.RefinedArcModel_fuelcost;

public class OutputSchedule {

	public static void main(String[] args) {
		try {
			//String[] period = {"Morning","Noon","Evening"};
			String[] period = {"Evening"};
			int[] dateRange = {1,1};
			int[] flightNumRange = {6,6};
			int mps = 2;
			int cpu = 7200;
			boolean RefineArc = true;
			//boolean fuel_consumption = false;
			
			
			if(RefineArc){
				/*dateRange[0] = 6; dateRange[1] = 10;
				flightNumRange[0] = 6;flightNumRange[1] = 8;*/
				MyFile.creatTxtFile("result/RefineArc.txt");			
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){

							String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							//rd.timewindowDuration = 450;
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							RefinedArcModel mip = new RefinedArcModel();			
							double obj = mip.integerSolve(mps, cpu) - rd.getEarliestEndTime();						
							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = Double.parseDouble(String.format("%.2f",(endTime-startTime)/1000.0));
							System.out.println("RefineArc cpu "+totalCPU);
							//MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps+", gap "+ReadData.gap);
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
