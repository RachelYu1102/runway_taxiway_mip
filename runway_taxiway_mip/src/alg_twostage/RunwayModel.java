package alg_twostage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ReadData;
import entity.Arc;
import entity.Flight;
import entity.Node;
import entity.RunwayEntry;
import entity.RunwayExit;
import entity.SequencePair;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class RunwayModel {
	public List<Flight> flightList = ReadData.flightList;
	public List<RunwayExit> exitList = ReadData.exitList;
	public List<RunwayEntry> entryList = ReadData.entryList;
	private Map<String, Integer> sprMap = ReadData.sprMap;
	private int bigM = 10000;
	
	public void integerSolve(int mps, int cpuTime, List<SequencePair> spList){
		//aircraft
		int flightSize = flightList.size();
		int[] fId = new int[flightSize];
		int[] sTime = new int[flightSize];
		int[] eTime = new int[flightSize];
		List<Flight> depList = new ArrayList<>();
		List<Flight> arrList = new ArrayList<>();
		
		for(int i=0;i<flightSize;i++){
			Flight flight = flightList.get(i);
			fId[i] = flight.getId();
			sTime[i] = flight.getStartTime();
			eTime[i] = flight.getLatestTime();
			if(flight.isArr()){
				arrList.add(flight);
			}else depList.add(flight);
		}
	
		
		int[][] sprTime = new int[flightSize][flightSize];
		for(int i=0;i<flightSize;i++){
			for(int j=0;j<flightSize;j++){
				if(i==j)continue;
				Flight fi = flightList.get(i);
				Flight fj = flightList.get(j);
				String typeI = fi.getWeightCategory();
				String typeJ = fj.getWeightCategory();
				if(fi.isArr()==fj.isArr()){
					sprTime[i][j] = sprMap.get(fi.getCallsign()+"_"+fj.getCallsign());
				}
			}
		}
		
		double returnValue = Double.MAX_VALUE;
		try{
			IloCplex cplex = new IloCplex();
			//##### variables #####
			IloNumVar[][] y = new IloNumVar[flightSize][flightSize];//separation on runway
			for(int i=0;i<flightSize;i++){	
				y[i]= cplex.boolVarArray(flightSize);
			}	
			IloNumVar[] t = cplex.numVarArray(flightSize, 0, Double.MAX_VALUE);
		

			//###### objective ######
			IloLinearNumExpr objective = cplex.linearNumExpr();

			for(int i=0;i<flightSize;i++){
				objective.addTerm(1,t[i]);
			}
			cplex.addMinimize(objective);

			//###### constraints ######

			for(int i=0;i<flightSize;i++){
				Flight f = flightList.get(i);
				cplex.addGe(t[i],sTime[i]);
				cplex.addLe(t[i],eTime[i]);			
			}

			for(Flight depi:depList){
				for(Flight depj:depList){
					if(depi.equals(depj))continue;
					IloLinearNumExpr runwaySeparationExpr = cplex.linearNumExpr();
					runwaySeparationExpr.addTerm(1.0,t[depj.getId()]);
					runwaySeparationExpr.addTerm(-1.0,t[depi.getId()]);
					runwaySeparationExpr.addTerm(-bigM,y[depi.getId()][depj.getId()]);
					cplex.addGe(runwaySeparationExpr,sprTime[depi.getId()][depj.getId()]-bigM);
					//order precedence: y(i,j) + y(j,i) = 1
					if(depj.getId()>depi.getId()){
						IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
						orderPrecedenceExpr.addTerm(1.0,y[depi.getId()][depj.getId()]);
						orderPrecedenceExpr.addTerm(1.0,y[depj.getId()][depi.getId()]);
						cplex.addEq(orderPrecedenceExpr,1);
					}	
				}
			}
			for(Flight fi:flightList){
				for(Flight fj:flightList){
					if(fi.equals(fj))continue;
					if(fi.isArr()!=fi.isArr())continue;
					IloLinearNumExpr runwaySeparationExpr = cplex.linearNumExpr();
					runwaySeparationExpr.addTerm(1.0,t[fj.getId()]);
					runwaySeparationExpr.addTerm(-1.0,t[fi.getId()]);
					runwaySeparationExpr.addTerm(-bigM,y[fi.getId()][fj.getId()]);
					cplex.addGe(runwaySeparationExpr,sprTime[fi.getId()][fj.getId()]-bigM);
					//order precedence: y(i,j) + y(j,i) = 1
					if(fj.getId()>fi.getId()){
						IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
						orderPrecedenceExpr.addTerm(1.0,y[fi.getId()][fj.getId()]);
						orderPrecedenceExpr.addTerm(1.0,y[fj.getId()][fi.getId()]);
						cplex.addEq(orderPrecedenceExpr,1);
					}	
				}
			}

			//constrained position shifting
			for(Flight f:depList){
				IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
				for(Flight f2:depList){
					if(f.equals(f2))continue;
					cpsExpr.addTerm(1, y[f2.getId()][f.getId()]);
				}
				cplex.addLe(cpsExpr,mps+f.getFCFSposition());
				cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
			}
			for(Flight f:arrList){
				IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
				for(Flight f2:arrList){
					if(f.equals(f2))continue;
					cpsExpr.addTerm(1, y[f2.getId()][f.getId()]);
				}
				cplex.addLe(cpsExpr,mps+f.getFCFSposition());
				cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
			}
			
			//variable fixing by position and mps
			for(Flight f1:depList){
				for(Flight f2:depList){
					if(f1.equals(f2))continue;
					if(f1.getFCFSposition()-f2.getFCFSposition()>=2*mps){
						cplex.addEq(y[f2.getId()][f1.getId()],1);
					}
				}
			}
			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					if(f1.getFCFSposition()-f2.getFCFSposition()>=2*mps){
						cplex.addEq(y[f2.getId()][f1.getId()],1);
					}
				}
			}
			//variable fixing by time window overlap
			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					if(f2.getStartTime()+sprTime[f2.getId()][f1.getId()]>f1.getLatestTime()){
						cplex.addEq(y[f1.getId()][f2.getId()],1);
					}
				}
			}
			
			//variable fixing by same category arrival
			for(Flight fi:arrList){
				for(Flight fj:arrList){
					if(fi.equals(fj))continue;
					if(fi.getWeightCategory().equals(fj.getWeightCategory())){
						if(fi.getStartTime()<fj.getStartTime()){
							cplex.addEq(y[fi.getId()][fj.getId()], 1);
						}
					}
				}
			}
			
			//valid inequalities by time window ratio
			for(Flight fi:arrList){
				for(Flight fj:arrList){
					if(fi.equals(fj))continue;
					double l_e = 1.0*(fj.getLatestTime()-fi.getStartTime());
					
					if(l_e>0){
						System.out.println(fi+" "+fj+" "+l_e);
						IloLinearNumExpr ratioExpr = cplex.linearNumExpr();
						ratioExpr.addTerm(1, y[fi.getId()][fj.getId()]);
						ratioExpr.addTerm(-1/l_e, t[fj.getId()]);
						ratioExpr.addTerm(1/l_e, t[fi.getId()]);
						cplex.addGe(ratioExpr,0);
					}
				}
			}
			
			//valid inequalities by node sequence sum
			int sumSequenceDep = depList.size()*(depList.size()-1)/2;
			int sumSequenceArr = arrList.size()*(arrList.size()-1)/2;
			//System.out.println(sumSequenceDep+" "+sumSequenceArr);
			IloLinearNumExpr depSumExpr = cplex.linearNumExpr();
			IloLinearNumExpr arrSumExpr = cplex.linearNumExpr();
			
			for(Flight f1:depList){
				for(Flight f2:depList){
					if(f1.equals(f2))continue;
					depSumExpr.addTerm(1, y[f1.getId()][f2.getId()]);
				}
			}
			cplex.addEq(depSumExpr, sumSequenceDep);
			
			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					arrSumExpr.addTerm(1, y[f1.getId()][f2.getId()]);
				}
			}
			cplex.addEq(arrSumExpr, sumSequenceArr);
			
			//valid inequalities by transitive 3-tuple (i,j,k)
			for(int i=0;i<depList.size();i++){
				for(int j=i+1;j<depList.size();j++){
					for(int k=j+1;k<depList.size();k++){
						int iId = depList.get(i).getId();
						int jId = depList.get(j).getId();
						int kId = depList.get(k).getId();
						IloLinearNumExpr transitiveExpr = cplex.linearNumExpr();
						transitiveExpr.addTerm(1,y[iId][kId]);
						transitiveExpr.addTerm(-1,y[iId][jId]);
						transitiveExpr.addTerm(-1,y[jId][kId]);
						cplex.addGe(transitiveExpr,-1);
						
						IloLinearNumExpr transitiveExpr2 = cplex.linearNumExpr();
						transitiveExpr2.addTerm(1,y[iId][kId]);
						transitiveExpr2.addTerm(-1,y[iId][jId]);
						transitiveExpr2.addTerm(-1,y[jId][kId]);
						cplex.addLe(transitiveExpr2,0);
					}	
				}
			}
			for(int i=0;i<arrList.size();i++){
				for(int j=i+1;j<arrList.size();j++){
					for(int k=j+1;k<arrList.size();k++){
						int iId = arrList.get(i).getId();
						int jId = arrList.get(j).getId();
						int kId = arrList.get(k).getId();
						IloLinearNumExpr transitiveExpr = cplex.linearNumExpr();
						transitiveExpr.addTerm(1,y[iId][kId]);
						transitiveExpr.addTerm(-1,y[iId][jId]);
						transitiveExpr.addTerm(-1,y[jId][kId]);
						cplex.addGe(transitiveExpr,-1);
						
						IloLinearNumExpr transitiveExpr2 = cplex.linearNumExpr();
						transitiveExpr2.addTerm(1,y[iId][kId]);
						transitiveExpr2.addTerm(-1,y[iId][jId]);
						transitiveExpr2.addTerm(-1,y[jId][kId]);
						cplex.addLe(transitiveExpr2,0);
					}	
				}
			}			
		
			
			
			//Cplex Parameters
			//cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			cplex.setParam(IloCplex.DoubleParam.TiLim,cpuTime); // set TiLim

			//solve
			if(cplex.solve()){
				System.out.print("CPS ARC obj = "+String.format("%.1f",cplex.getObjValue()));

				//show results
				
				System.out.println();
				for(int i=0;i<flightSize-1;i++){
					Flight fi = flightList.get(i);
					System.out.print(fi.getCallsign()+":");
					for(int j=i+1;j<flightSize;j++){
						Flight fj = flightList.get(j);
						if(i==j)continue;
						if(fi.isArr()!=fj.isArr())continue;
						if(Math.abs(cplex.getValue(y[i][j])-1)<0.1){
							System.out.println(fj.getCallsign()+",");
							SequencePair sp = new SequencePair();
							sp.setHeadF(fi);
							sp.setTailF(fj);
							spList.add(sp);
						}
						
					}
				}
			
			}else{
				System.out.println("No feasible MIP solution obtained!");
			}

			cplex.end();


		}catch(IloException exc){
			exc.printStackTrace();
		}
	}
}
