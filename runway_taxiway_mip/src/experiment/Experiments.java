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

public class Experiments {

	public static void main(String[] args) {
		try {
			//String[] period = {"Morning","Noon","Evening"};
			String[] period = {"Evening"};
			int[] dateRange = {1,1};
			int[] flightNumRange = {6,6};
			int mps = 2;
			int cpu = 7200;
			boolean RefineArc = true;
			boolean BasicArc = false;
			boolean Constraint_Generation = false;
			boolean Decomposition = false;
			boolean Fcfs = false;
			boolean TwoStage = false;
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
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps+", gap "+ReadData.gap);
						}
					}		
				}	
			}

			if(BasicArc){
				/*dateRange[0] = 3; dateRange[1] = 5;
				flightNumRange[0] = 8;flightNumRange[1] = 8;*/
				MyFile.creatTxtFile("result/BasicArc.txt");			
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){
							String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
							//rd.timewindowDuration = 450;
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							BasicArcModel mip = new BasicArcModel();
							double obj = mip.integerSolve(mps, cpu) - rd.getEarliestEndTime();						
							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("BasicArc cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps+", gap "+ReadData.gap);
						}
					}		
				}	
			}
			if(TwoStage){
				/*dateRange[0] = 8; dateRange[1] = 8;
				flightNumRange[0] = 10;flightNumRange[1] = 10;*/
				MyFile.creatTxtFile("result/TwoStage.txt");
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){
							String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
							//rd.timewindowDuration = 450;
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							TwostageEngine controller = new TwostageEngine();		
							double obj = controller.solve(mps,cpu)- rd.getEarliestEndTime();

							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("TwoStage cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps);
						}
					}		
				}	
			}
			if(Constraint_Generation){
				/*dateRange[0] = 1; dateRange[1] = 10;
				flightNumRange[0] = 10;flightNumRange[1] = 10;*/
				MyFile.creatTxtFile("result/Constraint_Generation.txt");		
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){
							String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
							rd.timewindowDuration = 450;
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							IterationEngine controller = new IterationEngine();							
							double[] returnVal = controller.solve(mps,cpu); 
							double obj = returnVal[0] - rd.getEarliestEndTime();
							double iteration = returnVal[1];
							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("Constraint_Generation cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps +", totalIter "+iteration);
						}
					}		
				}
			}

			if(Decomposition){
				MyFile.creatTxtFile("result/Decomposition-large.txt");
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){
							String flightData = "changidata/"+i+"_2014_flight_"+period[j]+".csv";
							//String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							//rd.compactDenominator = 2;
							
							rd.setData(flightData, "changidata/arc_T2T3.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");rd.timewindowDuration = 450;
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							DecompFixPathEngine controller = new DecompFixPathEngine();						
							//DecompGroupSeqEngine controller = new DecompGroupSeqEngine();						
							double obj = controller.solve(mps,cpu) - rd.getEarliestEndTime();	

							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("Decomposition cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps);
						}
					}		
				}	
			}
			if(Fcfs){
				MyFile.creatTxtFile("result/FCFS.txt");
				for(int fNum = flightNumRange[0]; fNum<=flightNumRange[1];fNum = fNum + 2){
					for(int i=dateRange[0];i<=dateRange[1];i++){
						for(int j=0;j<period.length;j++){
							//String flightData = "changidata/"+i+"_2014_flight_"+period[j]+".csv";
							String flightData = "compact/"+i+"_"+period[j]+"_T3South_"+fNum+"_compact.csv";
							ReadData rd = new ReadData();
							rd.setData(flightData, "smalldata/arc_T3South.csv",
									"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");rd.timewindowDuration = 450;
							rd.allInOne();		
							long startTime = System.currentTimeMillis();  //timing-start

							FCFSEngine controller = new FCFSEngine();				
							double obj = controller.solve(mps,cpu)- rd.getEarliestEndTime();

							long endTime = System.currentTimeMillis();  //timing-end
							double totalCPU = (endTime-startTime)/1000.0;
							System.out.println("FCFS cpu "+totalCPU);
							MyFile.writeTxtFile(flightData+", cpu "+totalCPU+", obj "+obj+", mps "+mps);
						}
					}		
				}	
			}
			
			/*	if(fuel_consumption){
			MyFile.creatTxtFile("result/RefinedArc_fuel.txt");
			for(int i=beginData;i<=endDate;i++){
				for(int j=0;j<period.length;j++){
					for(int fNum =8; fNum<=10;fNum = fNum + 2){
						String flightData = "smalldata/"+i+"_"+period[j]+"_T3South_"+fNum+".csv";
						ReadData rd = new ReadData();
						rd.setData(flightData, "smalldata/arc_T3South.csv",
								"smalldata/changi_arrSeparation.csv","smalldata/changi_depSeparation.csv");
						rd.allInOne();		
						long startTime = System.currentTimeMillis();  //timing-start

						RefinedArcModel_fuelcost mip = new RefinedArcModel_fuelcost();			
						double obj = mip.integerSolve(mps, cpu);						
						long endTime = System.currentTimeMillis();  //timing-end
						double totalCPU = (endTime-startTime)/1000.0;
						System.out.println("RefinedArc_fuel cpu "+totalCPU);
						MyFile.writeTxtFile(flightData+" cpu "+totalCPU+" obj "+obj+" mps "+mps);
					}
				}		
			}	
		}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
