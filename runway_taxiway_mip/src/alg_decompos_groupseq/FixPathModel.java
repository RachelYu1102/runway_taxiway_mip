package alg_decompos_groupseq;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import data.ReadData;
import entity.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;

public class FixPathModel {

	public List<RunwayExit> exitList = ReadData.exitList;
	public List<RunwayEntry> entryList = ReadData.entryList;
	public List<Flight> arrList = ReadData.arrList;
	public List<Flight> depList = ReadData.depList;
	public List<Flight> flightList = ReadData.flightList;
	private List<Node> nodeList = ReadData.nodeList;
	private Map<String,Node> nodeMap = ReadData.nodeMap;
	private Map<String,Arc> arcMap = ReadData.arcMap;
	private List<Arc> arcList = ReadData.arcList;

	//private List<Arc> pseudoOriginArcs = ReadData.pseudoOriginArcs;
	//private List<Arc> pseudoDestinArcs = ReadData.pseudoDestinArcs;
	private Map<String, Integer> sprMap = ReadData.sprMap;


	//private int pushBack = 60;  //buffer included
	private int taxiSep = 10;
	private int bigM = 10000;
	private boolean taxiwayConflictCheck = true;
	private boolean gateConflictCheck = true;
	private boolean runwayConflictCheck = true;
	private int mps = 1; //maximumPositionShifting

	public double integerSolve(int mps, int cpuTime){
		//initialize parameters
		this.mps = mps;

		//aircraft
		int flightSize = flightList.size();
		int[] fId = new int[flightSize];
		int[] sTime = new int[flightSize];
		int[] eTime = new int[flightSize];
		int[] oNode = new int[flightSize];
		int[] dNode = new int[flightSize];
		int[] pushT = new int[flightSize];

		for(int i=0;i<flightSize;i++){
			fId[i] = flightList.get(i).getId();
			sTime[i] = flightList.get(i).getStartTime();
			eTime[i] = flightList.get(i).getLatestTime();
			oNode[i] = flightList.get(i).getOriginNode().getId();
			dNode[i] = flightList.get(i).getDestinNode().getId();
			pushT[i] = flightList.get(i).getPushbackDuration();	
		}
		//node and arc
		int nodeSize = nodeList.size();
		int arcSize = arcList.size();
		int pseudoOriginId = nodeMap.get("o").getId();
		int pseudoDestinId = nodeMap.get("d").getId();


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

			IloNumVar[][][] y = new IloNumVar[flightSize][flightSize][nodeSize];//separation on node
			for(int i=0;i<flightSize;i++){
				for(int j=0;j<flightSize;j++){
					y[i][j] = cplex.boolVarArray(nodeSize);
				}
			}

			IloNumVar[][] t = new IloNumVar[flightSize][nodeSize]; //time on node
			for(int i=0;i<flightSize;i++){
				t[i] = cplex.numVarArray(nodeSize, 0, Double.MAX_VALUE);
			}			


			//###### objective ######
			IloLinearNumExpr objective = cplex.linearNumExpr();

			for(int i=0;i<flightSize;i++){
				//objective.addTerm(1,t[i][dNode[i]]);	
				objective.addTerm(1,t[i][pseudoDestinId]);
			}
			cplex.addMinimize(objective);

			//###### constraints ######

			for(int i=0;i<flightSize;i++){
				Flight f = flightList.get(i);
				if(f.isArr()){
					//runway exit time >= pseudoOrigin node time (runway time)
					cplex.addGe(t[i][pseudoOriginId],sTime[i]);
					//runway time <= latest time
					cplex.addLe(t[i][pseudoOriginId],eTime[i]);
					//runway exit time = runway touch time (cannot stop on runway)
					IloLinearNumExpr OccupyExpr = cplex.linearNumExpr(); 
					OccupyExpr.addTerm(1, t[i][oNode[i]]);
					OccupyExpr.addTerm(-1, t[i][pseudoOriginId]);
					cplex.addEq(OccupyExpr,0);  //o could be substituted by a constant occupation time
					//pseudoDestin node time = end node time
					IloLinearNumExpr pseudoDestinExpr = cplex.linearNumExpr(); 
					pseudoDestinExpr.addTerm(1, t[i][pseudoDestinId]);
					pseudoDestinExpr.addTerm(-1, t[i][dNode[i]]);
					cplex.addGe(pseudoDestinExpr,0);
				}else{
					//cplex.addEq(t[i][pseudoOriginId],sTime[i]);
					cplex.addGe(t[i][pseudoOriginId],sTime[i]);
					//gate pushback begin time <= latest time
					cplex.addLe(t[i][pseudoOriginId],eTime[i]);

					IloLinearNumExpr pushbackExpr = cplex.linearNumExpr(); 
					pushbackExpr.addTerm(1, t[i][oNode[i]]);
					pushbackExpr.addTerm(-1, t[i][pseudoOriginId]);
					cplex.addGe(pushbackExpr,pushT[i]);

					//runway entry time = runway time (pseudoDestinNode) (cannot stop on runway)
					IloLinearNumExpr runwayExpr = cplex.linearNumExpr(); 
					runwayExpr.addTerm(1, t[i][pseudoDestinId]);
					runwayExpr.addTerm(-1, t[i][dNode[i]]);
					cplex.addEq(runwayExpr,0); //0 could be substituted by a constant runway occupation time
				}

				//Constraint(1) path Select

				Path path = f.getPath();
				for(int l=0;l<path.getArcList().size();l++){
					Arc arc = path.getArcList().get(l);
					//Constraint: arc time continuity
					IloLinearNumExpr timeContinuityExpr = cplex.linearNumExpr();
					timeContinuityExpr.addTerm(1.0,t[i][arc.getEndNode().getId()]);
					timeContinuityExpr.addTerm(-1.0,t[i][arc.getStartNode().getId()]);
					cplex.addGe(timeContinuityExpr,arc.getLength());  //can stop
				}				
			}

			//gate conflict
			if(gateConflictCheck){
				for(Flight arr:arrList){
					for(Flight dep:depList){
						if(arr.getDestinNode().equals(dep.getOriginNode())){
							IloLinearNumExpr gateConflictExpr = cplex.linearNumExpr();
							gateConflictExpr.addTerm(1, t[arr.getId()][dNode[arr.getId()]]);
							gateConflictExpr.addTerm(-1, t[dep.getId()][oNode[dep.getId()]]);
							cplex.addGe(gateConflictExpr,pushT[dep.getId()]);
						}
					}
				}
			}

			//runway separation
			if(runwayConflictCheck){
				for(Flight depi:depList){
					for(Flight depj:depList){
						if(depi.equals(depj))continue;
						IloLinearNumExpr runwaySeparationExpr = cplex.linearNumExpr();
						runwaySeparationExpr.addTerm(1.0,t[depj.getId()][pseudoDestinId]);
						runwaySeparationExpr.addTerm(-1.0,t[depi.getId()][pseudoDestinId]);
						runwaySeparationExpr.addTerm(-bigM,y[depi.getId()][depj.getId()][pseudoDestinId]);
						cplex.addGe(runwaySeparationExpr,sprTime[depi.getId()][depj.getId()]-bigM);
						//order precedence: y(i,j) + y(j,i) = 1
						if(depj.getId()>depi.getId()){
							IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
							orderPrecedenceExpr.addTerm(1.0,y[depi.getId()][depj.getId()][pseudoDestinId]);
							orderPrecedenceExpr.addTerm(1.0,y[depj.getId()][depi.getId()][pseudoDestinId]);
							cplex.addEq(orderPrecedenceExpr,1);
						}	
					}
				}
				for(Flight arri:arrList){
					for(Flight arrj:arrList){
						if(arri.equals(arrj))continue;
						IloLinearNumExpr runwaySeparationExpr = cplex.linearNumExpr();
						runwaySeparationExpr.addTerm(1.0,t[arrj.getId()][pseudoOriginId]);
						runwaySeparationExpr.addTerm(-1.0,t[arri.getId()][pseudoOriginId]);
						runwaySeparationExpr.addTerm(-bigM,y[arri.getId()][arrj.getId()][pseudoOriginId]);
						cplex.addGe(runwaySeparationExpr,sprTime[arri.getId()][arrj.getId()]-bigM);
						//order precedence: y(i,j) + y(j,i) = 1
						if(arrj.getId()>arri.getId()){
							IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
							orderPrecedenceExpr.addTerm(1.0,y[arri.getId()][arrj.getId()][pseudoOriginId]);
							orderPrecedenceExpr.addTerm(1.0,y[arrj.getId()][arri.getId()][pseudoOriginId]);
							cplex.addEq(orderPrecedenceExpr,1);
						}	
					}
				}
			}

			//taxiway conflict
			if(taxiwayConflictCheck){
				for(int i=0;i<flightSize;i++){
					Path p1 = flightList.get(i).getPath();
					//System.out.println("PATH "+p1);
					for(int j=i+1;j<flightSize;j++){
						Path p2 = flightList.get(j).getPath();
						
						List<Node> commonNodes = p1.commonNode(p2);
						List<Arc> commonArcs = p1.common(p2);
						List<Arc> reverseArcs = p1.reverseArc(p2);
						if(commonNodes.size()>0){
							for(Node node:commonNodes){
								IloLinearNumExpr taxiNodeExpr = cplex.linearNumExpr();
								taxiNodeExpr.addTerm(1.0,t[i][node.getId()]);
								taxiNodeExpr.addTerm(-1.0,t[j][node.getId()]);
								taxiNodeExpr.addTerm(-bigM,y[j][i][node.getId()]);
								cplex.addGe(taxiNodeExpr,taxiSep-bigM);

								IloLinearNumExpr taxiNodeEpr2 = cplex.linearNumExpr();
								taxiNodeEpr2.addTerm(1.0,t[j][node.getId()]);
								taxiNodeEpr2.addTerm(-1.0,t[i][node.getId()]);
								taxiNodeEpr2.addTerm(-bigM,y[i][j][node.getId()]);
								cplex.addGe(taxiNodeEpr2,taxiSep-bigM);

								//order that pass the node: y(i,j) + y(j,i) = 1
								IloLinearNumExpr orderPrecedenceExpr2 = cplex.linearNumExpr();
								orderPrecedenceExpr2.addTerm(1.0,y[i][j][node.getId()]);
								orderPrecedenceExpr2.addTerm(1.0,y[j][i][node.getId()]);
								cplex.addEq(orderPrecedenceExpr2,1);

							}

						}
						if(commonArcs.size()>0){
							for(Arc arc:commonArcs){
								Node eNode = arc.getEndNode();
								Node sNode = arc.getStartNode();

								//order preserve on n and l
								IloLinearNumExpr arcOrderExprG0 = cplex.linearNumExpr();
								arcOrderExprG0.addTerm(1.0,y[i][j][eNode.getId()]);
								arcOrderExprG0.addTerm(-1.0,y[i][j][sNode.getId()]);
								cplex.addEq(arcOrderExprG0,0);

							}
						}
						if(reverseArcs.size()>0){
							for(Arc arc:reverseArcs){

								Node sNode = arc.getStartNode();
								Node eNode = arc.getEndNode();
								IloLinearNumExpr taxiArcExpr = cplex.linearNumExpr();
								taxiArcExpr.addTerm(1.0,t[i][sNode.getId()]);
								taxiArcExpr.addTerm(-1.0,t[j][sNode.getId()]);
								taxiArcExpr.addTerm(-bigM,y[j][i][sNode.getId()]);
								cplex.addGe(taxiArcExpr,taxiSep-bigM);

								IloLinearNumExpr taxiArcEpr2 = cplex.linearNumExpr();
								taxiArcEpr2.addTerm(1.0,t[j][eNode.getId()]);
								taxiArcEpr2.addTerm(-1.0,t[i][eNode.getId()]);
								taxiArcEpr2.addTerm(-bigM,y[i][j][eNode.getId()]);
								cplex.addGe(taxiArcEpr2,taxiSep-bigM);

								//order preserve on n and l
								IloLinearNumExpr arcOrderEpr2 = cplex.linearNumExpr();
								arcOrderEpr2.addTerm(1.0,y[i][j][eNode.getId()]);
								arcOrderEpr2.addTerm(-1.0,y[i][j][sNode.getId()]);
								cplex.addEq(arcOrderEpr2,0);							
							}
						}

					}
				}
			}	
			//constrained position shifting
			for(Flight f:depList){
				IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
				for(Flight f2:depList){
					if(f.equals(f2))continue;
					cpsExpr.addTerm(1, y[f2.getId()][f.getId()][pseudoDestinId]);
				}
				cplex.addLe(cpsExpr,mps+f.getFCFSposition());
				cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
			}
			for(Flight f:arrList){
				IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
				for(Flight f2:arrList){
					if(f.equals(f2))continue;
					cpsExpr.addTerm(1, y[f2.getId()][f.getId()][pseudoOriginId]);
				}
				cplex.addLe(cpsExpr,mps+f.getFCFSposition());
				cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
			}

			//variable fixing by position and mps
			for(Flight f1:depList){
				for(Flight f2:depList){
					if(f1.equals(f2))continue;
					if(f1.getFCFSposition()-f2.getFCFSposition()>=2*mps){
						cplex.addEq(y[f2.getId()][f1.getId()][pseudoDestinId],1);
					}
				}
			}
			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					if(f1.getFCFSposition()-f2.getFCFSposition()>=2*mps){
						cplex.addEq(y[f2.getId()][f1.getId()][pseudoOriginId],1);
					}
				}
			}
			//variable fixing by time window overlap
			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					if(f2.getStartTime()+sprTime[f2.getId()][f1.getId()]>f1.getLatestTime()){
						cplex.addEq(y[f1.getId()][f2.getId()][pseudoOriginId],1);
					}
				}
			}

			//variable fixing by same category arrival
			for(Flight fi:arrList){
				for(Flight fj:arrList){
					if(fi.equals(fj))continue;
					if(fi.getWeightCategory().equals(fj.getWeightCategory())){
						if(fi.getStartTime()<fj.getStartTime()){
							cplex.addEq(y[fi.getId()][fj.getId()][pseudoOriginId], 1);
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
						//System.out.println(fi+" "+fj+" "+l_e);
						IloLinearNumExpr ratioExpr = cplex.linearNumExpr();
						ratioExpr.addTerm(1, y[fi.getId()][fj.getId()][pseudoOriginId]);
						ratioExpr.addTerm(-1/l_e, t[fj.getId()][pseudoOriginId]);
						ratioExpr.addTerm(1/l_e, t[fi.getId()][pseudoOriginId]);
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
					depSumExpr.addTerm(1, y[f1.getId()][f2.getId()][pseudoDestinId]);
				}
			}
			cplex.addEq(depSumExpr, sumSequenceDep);

			for(Flight f1:arrList){
				for(Flight f2:arrList){
					if(f1.equals(f2))continue;
					arrSumExpr.addTerm(1, y[f1.getId()][f2.getId()][pseudoOriginId]);
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
						transitiveExpr.addTerm(1,y[iId][kId][pseudoDestinId]);
						transitiveExpr.addTerm(-1,y[iId][jId][pseudoDestinId]);
						transitiveExpr.addTerm(-1,y[jId][kId][pseudoDestinId]);
						cplex.addGe(transitiveExpr,-1);

						IloLinearNumExpr transitiveExpr2 = cplex.linearNumExpr();
						transitiveExpr2.addTerm(1,y[iId][kId][pseudoDestinId]);
						transitiveExpr2.addTerm(-1,y[iId][jId][pseudoDestinId]);
						transitiveExpr2.addTerm(-1,y[jId][kId][pseudoDestinId]);
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
						transitiveExpr.addTerm(1,y[iId][kId][pseudoOriginId]);
						transitiveExpr.addTerm(-1,y[iId][jId][pseudoOriginId]);
						transitiveExpr.addTerm(-1,y[jId][kId][pseudoOriginId]);
						cplex.addGe(transitiveExpr,-1);

						IloLinearNumExpr transitiveExpr2 = cplex.linearNumExpr();
						transitiveExpr2.addTerm(1,y[iId][kId][pseudoOriginId]);
						transitiveExpr2.addTerm(-1,y[iId][jId][pseudoOriginId]);
						transitiveExpr2.addTerm(-1,y[jId][kId][pseudoOriginId]);
						cplex.addLe(transitiveExpr2,0);
					}	
				}
			}			
			
			//Cplex Parameters
			//cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			cplex.setParam(IloCplex.DoubleParam.TiLim,cpuTime); // set TiLim

			//solve
			if(cplex.solve()){
				System.out.print("Fix Path obj = "+String.format("%.1f",cplex.getObjValue()));
				returnValue = Math.round(cplex.getObjValue());
				//show results

				System.out.println();
				for(int i=0;i<flightSize;i++){
					Flight f = flightList.get(i);
					System.out.print(f.getCallsign()+" t(dNode)"+ String.format("%.1f",cplex.getValue(t[i][dNode[i]]))+" ");

					for(Node n:f.getPath().getNodeList()){
						System.out.print(n.getCallsign()+"("+Math.round(cplex.getValue(t[i][n.getId()]))+")->");
					}

					System.out.print("d("+Math.round(cplex.getValue(t[i][1]))+")->");
					System.out.println();
				}
				/*System.out.println(cplex.getValue(y[0][3][nodeMap.get("g1").getId()]));
				System.out.println(cplex.getValue(y[3][0][nodeMap.get("g1").getId()]));
				System.out.println(cplex.getValue(y[0][3][nodeMap.get("n1").getId()]));
				System.out.println(cplex.getValue(y[3][0][nodeMap.get("n1").getId()]));*/
			}else{
				System.out.println("No feasible MIP solution obtained!");
			}

			cplex.end();


		}catch(IloException exc){
			exc.printStackTrace();
		}
		return returnValue;
	}
}
