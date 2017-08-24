package alg_fcfs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import data.ReadData;
import entity.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;

public class OneByOneModel {
	public List<RunwayExit> exitList = ReadData.exitList;
	public List<RunwayEntry> entryList = ReadData.entryList;
	private List<Node> nodeList = ReadData.nodeList;
	private Map<String,Node> nodeMap = ReadData.nodeMap;
	private Map<String,Arc> arcMap = ReadData.arcMap;
	private List<Arc> arcList = ReadData.arcList;
	private Map<String, Integer> sprMap = ReadData.sprMap;
	private List<Arc> psudoOArc = ReadData.pseudoOriginArcs;
	private List<Arc> psudoDArc = ReadData.pseudoDestinArcs;
	//private int pushBack = 60;  //buffer included
	private int taxiSep = ReadData.taxiSep;
	private int bigM = 10000;
	private boolean taxiwayConflictCheck = true;
	private boolean gateConflictCheck = true;
	private boolean runwayConflictCheck = true;
	
	public double integerSolve(int mps, int cpuTime, List<Flight> flightList, 
			List<Flight> depList, List<Flight> arrList){
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
			IloNumVar[][] x = new IloNumVar[flightSize][nodeSize]; //node cover
			for(int i=0;i<flightSize;i++){
				x[i] = cplex.boolVarArray(nodeSize);
				//x[i] = cplex.numVarArray(nodeSize,0,1);
			}
			
			IloNumVar[][][] y = new IloNumVar[flightSize][flightSize][nodeSize];//separation on node
			for(int i=0;i<flightSize;i++){
				for(int j=0;j<flightSize;j++){
					y[i][j] = cplex.boolVarArray(nodeSize);
					//y[i][j] = cplex.numVarArray(nodeSize,0,1);
				}
			}
			
			IloNumVar[][] t = new IloNumVar[flightSize][nodeSize]; //time on node
			for(int i=0;i<flightSize;i++){
				//t[i] = cplex.numVarArray(nodeSize, 0, Double.MAX_VALUE);
				t[i] = cplex.numVarArray(nodeSize, 0, 20000);
			}			
			IloNumVar[][] omega = new IloNumVar[flightSize][arcSize]; //arc selection
			for(int i=0;i<flightSize;i++){
				omega[i] = cplex.boolVarArray(arcSize);
				//omega[i] = cplex.numVarArray(arcSize,0,1);
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
					//originNode
					cplex.addEq(x[i][oNode[i]],1);
					//destinNode 
					cplex.addEq(x[i][dNode[i]],1);
					//destinArc
					for(Arc a:psudoDArc){
						if(a.getStartNode().getId()==dNode[i]){
							cplex.addEq(omega[i][a.getId()],1);
						}
					}
					//originArc
					for(Arc a:psudoOArc){
						if(a.getEndNode().getId()==oNode[i]){
							cplex.addEq(omega[i][a.getId()],1);
						}
					}
					//runway exit time >= pseudoOrigin node time (runway time)
					cplex.addGe(t[i][pseudoOriginId],sTime[i]);
					//runway time <= latest time
					cplex.addLe(t[i][pseudoOriginId],eTime[i]);
					//runway exit time = runway touch time (cannot stop on runway)
					IloLinearNumExpr OccupyExpr = cplex.linearNumExpr(); 
					OccupyExpr.addTerm(1, t[i][oNode[i]]);
					OccupyExpr.addTerm(-1, t[i][pseudoOriginId]);
					cplex.addEq(OccupyExpr,0);  //o could be substituted by a constant occupation time
					//gate arrival time = gate touch time
					IloLinearNumExpr gateTouchExpr = cplex.linearNumExpr(); 
					gateTouchExpr.addTerm(1, t[i][pseudoDestinId]);
					gateTouchExpr.addTerm(-1, t[i][dNode[i]]);
					cplex.addEq(gateTouchExpr,0); 
				}else{
					//destinNode
					cplex.addEq(x[i][dNode[i]],1);
					//originNode
					cplex.addEq(x[i][oNode[i]],1);
					//destinArc
					for(Arc a:psudoDArc){
						if(a.getStartNode().getId()==dNode[i]){
							cplex.addEq(omega[i][a.getId()],1);
						}
					}
					//originArc
					for(Arc a:psudoOArc){
						if(a.getEndNode().getId()==oNode[i]){
							cplex.addEq(omega[i][a.getId()],1);
						}
					}
					//ready to depart from gate
					//cplex.addEq(t[i][pseudoOriginId],sTime[i]);
					cplex.addGe(t[i][pseudoOriginId],sTime[i]);
					//gate pushback begin time <= latest time
					cplex.addLe(t[i][pseudoOriginId],eTime[i]);
					/**DEPARTURE gate leave time (gate node time) >= pseudoOrigin node time(gate ready time)
					 * gate leave time >= pseudoOrigin node time + pushbackDuration**/
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
			}
			
			
			//arc time continuity
			for(Arc arc:arcList){
				if(arc.getStartNode().getCallsign().contains("d"))continue;
				for(int i=0;i<flightSize;i++){
					
					IloLinearNumExpr timeContinuityExpr = cplex.linearNumExpr();
					timeContinuityExpr.addTerm(1.0, t[i][arc.getEndNode().getId()]);
					timeContinuityExpr.addTerm(-1.0, t[i][arc.getStartNode().getId()]);
					timeContinuityExpr.addTerm(-bigM, omega[i][arc.getId()]);
					//cplex.addGe(timeContinuityExpr,arc.getLength()/speed[i]-bigM);
					cplex.addGe(timeContinuityExpr,arc.getLength()-bigM);
									
				}
			}
		
			//flow balance
			for(Node n:nodeList){
				for(int i=0;i<flightSize;i++){
					IloLinearNumExpr arcCoverExpr = cplex.linearNumExpr();  //omega = x
					IloLinearNumExpr flowBalanceExpr = cplex.linearNumExpr(); //omega-in = omega-out
					//System.out.println(n+" in arc ");
					for(Arc arc:n.getInArcList()){
						//System.out.println(arc);
						arcCoverExpr.addTerm(1.0, omega[i][arc.getId()]);
						flowBalanceExpr.addTerm(1.0, omega[i][arc.getId()]);
					}
					arcCoverExpr.addTerm(-1.0, x[i][n.getId()]);
					//System.out.println(n+" out arc ");
					for(Arc arc:n.getOutArcList()){
						//System.out.println(arc);
						flowBalanceExpr.addTerm(-1.0, omega[i][arc.getId()]);
					}
					cplex.addEq(flowBalanceExpr,0);
					cplex.addEq(arcCoverExpr,0);
				}		
			}
			//total circle arc select = flight number
			//IloLinearNumExpr circleArcCoverExpr = cplex.linearNumExpr();  
			for(int i=0;i<flightSize;i++){
				//circleArcCoverExpr.addTerm(1, omega[i][arcMap.get("d_o").getId()]);
				cplex.addEq(omega[i][arcMap.get("d_o").getId()],1);
			}
			//cplex.addEq(circleArcCoverExpr,flightSize);
			
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
			
			
			
			//taxiway conflict
			if(taxiwayConflictCheck){
				for(Node n:nodeList){
					if(n.getCallsign().contains("o")||n.getCallsign().contains("d"))continue;
					for(int i=0;i<flightSize;i++){
						for(int j=i+1;j<flightSize;j++){
							IloLinearNumExpr taxiNodeExpr = cplex.linearNumExpr();
							taxiNodeExpr.addTerm(1.0,t[i][n.getId()]);
							taxiNodeExpr.addTerm(-1.0,t[j][n.getId()]);
							taxiNodeExpr.addTerm(-bigM,x[i][n.getId()]);
							taxiNodeExpr.addTerm(-bigM,x[j][n.getId()]);
							taxiNodeExpr.addTerm(-bigM,y[j][i][n.getId()]);
							//cplex.addGe(taxiNodeExpr,traSep[j]-3*bigM);
							cplex.addGe(taxiNodeExpr,taxiSep-3*bigM);
							
							IloLinearNumExpr taxiNodeEpr2 = cplex.linearNumExpr();
							taxiNodeEpr2.addTerm(1.0,t[j][n.getId()]);
							taxiNodeEpr2.addTerm(-1.0,t[i][n.getId()]);
							taxiNodeEpr2.addTerm(-bigM,x[i][n.getId()]);
							taxiNodeEpr2.addTerm(-bigM,x[j][n.getId()]);
							taxiNodeEpr2.addTerm(-bigM,y[i][j][n.getId()]);
							//cplex.addGe(taxiNodeEpr2,traSep[i]-3*bigM);
							cplex.addGe(taxiNodeEpr2,taxiSep-3*bigM);
							
				/*			//order that pass the node: x(i,j)*x(j,n)*[y(i,j)+y(j,i)-1]=0
							IloLinearNumExpr orderPrecedenceExprG0 = cplex.linearNumExpr();
							orderPrecedenceExprG0.addTerm(1.0,y[i][j][n.getId()]);
							orderPrecedenceExprG0.addTerm(1.0,y[j][i][n.getId()]);
							orderPrecedenceExprG0.addTerm(-1,x[i][n.getId()]);
							orderPrecedenceExprG0.addTerm(-1,x[j][n.getId()]);
							cplex.addGe(orderPrecedenceExprG0,-1);
							
							IloLinearNumExpr orderPrecedenceExprL0 = cplex.linearNumExpr();
							orderPrecedenceExprL0.addTerm(1.0,y[i][j][n.getId()]);
							orderPrecedenceExprL0.addTerm(1.0,y[j][i][n.getId()]);
							orderPrecedenceExprL0.addTerm(1,x[i][n.getId()]);
							orderPrecedenceExprL0.addTerm(1,x[j][n.getId()]);
							cplex.addLe(orderPrecedenceExprL0,3);
							
							//relationship of x and y
							IloLinearNumExpr yijxiExpr = cplex.linearNumExpr();
							yijxiExpr.addTerm(1, y[i][j][n.getId()]);
							yijxiExpr.addTerm(-1, x[i][n.getId()]);
							cplex.addLe(yijxiExpr,0);
							
							IloLinearNumExpr yijxjExpr = cplex.linearNumExpr();
							yijxjExpr.addTerm(1, y[i][j][n.getId()]);
							yijxjExpr.addTerm(-1, x[j][n.getId()]);
							cplex.addLe(yijxjExpr,0);
							
							IloLinearNumExpr yjixiExpr = cplex.linearNumExpr();
							yjixiExpr.addTerm(1, y[j][i][n.getId()]);
							yjixiExpr.addTerm(-1, x[i][n.getId()]);
							cplex.addLe(yjixiExpr,0);
							
							IloLinearNumExpr yjixjExpr = cplex.linearNumExpr();
							yjixjExpr.addTerm(1, y[j][i][n.getId()]);
							yjixjExpr.addTerm(-1, x[j][n.getId()]);
							cplex.addLe(yjixjExpr,0);
							
							IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
							orderPrecedenceExpr.addTerm(1.0,y[i][j][n.getId()]);
							orderPrecedenceExpr.addTerm(1.0,y[j][i][n.getId()]);
							cplex.addLe(orderPrecedenceExpr,1);*/
							
							IloLinearNumExpr orderPrecedenceExpr = cplex.linearNumExpr();
							orderPrecedenceExpr.addTerm(1.0,y[i][j][n.getId()]);
							orderPrecedenceExpr.addTerm(1.0,y[j][i][n.getId()]);
							cplex.addEq(orderPrecedenceExpr,1);
						}
					}				
				}
	
				
				for(Arc arc:arcList){
					if(!arc.isOnTaxiway())continue;
					Node sNode = arc.getStartNode();
					Node eNode = arc.getEndNode();
					//take-over conflict
					for(int i=0;i<flightSize;i++){
						for(int j=i+1;j<flightSize;j++){						
							//order preserve on n and l
							IloLinearNumExpr arcOrderExprG0 = cplex.linearNumExpr();
							arcOrderExprG0.addTerm(1.0,y[i][j][eNode.getId()]);
							arcOrderExprG0.addTerm(-1.0,y[i][j][sNode.getId()]);
							arcOrderExprG0.addTerm(-1,omega[i][arc.getId()]);
							arcOrderExprG0.addTerm(-1,omega[j][arc.getId()]);
							cplex.addGe(arcOrderExprG0,-2);
							
							IloLinearNumExpr arcOrderExprL0 = cplex.linearNumExpr();
							arcOrderExprL0.addTerm(1.0,y[i][j][eNode.getId()]);
							arcOrderExprL0.addTerm(-1.0,y[i][j][sNode.getId()]);
							arcOrderExprL0.addTerm(1,omega[i][arc.getId()]);
							arcOrderExprL0.addTerm(1,omega[j][arc.getId()]);
							cplex.addLe(arcOrderExprL0,2);

						}
					}
					
					//head-on conflict
					Arc reverseArc = arcMap.get(eNode.getCallsign()+"_"+sNode.getCallsign());
					//System.out.println(arc);
					for(int i=0;i<flightSize;i++){
						for(int j=i+1;j<flightSize;j++){		
							/*IloLinearNumExpr taxiArcExpr = cplex.linearNumExpr();
							taxiArcExpr.addTerm(1.0,t[i][sNode.getId()]);
							taxiArcExpr.addTerm(-1.0,t[j][sNode.getId()]);
							taxiArcExpr.addTerm(-bigM,omega[i][arc.getId()]);
							taxiArcExpr.addTerm(-bigM,omega[j][reverseArc.getId()]);
							taxiArcExpr.addTerm(-bigM,y[j][i][sNode.getId()]);
							//cplex.addGe(taxiArcExpr,traSep[j]-3*bigM);
							cplex.addGe(taxiArcExpr,taxiSep-3*bigM);

							IloLinearNumExpr taxiArcEpr2 = cplex.linearNumExpr();
							taxiArcEpr2.addTerm(1.0,t[j][eNode.getId()]);
							taxiArcEpr2.addTerm(-1.0,t[i][eNode.getId()]);
							taxiArcEpr2.addTerm(-bigM,omega[i][arc.getId()]);
							taxiArcEpr2.addTerm(-bigM,omega[j][reverseArc.getId()]);
							taxiArcEpr2.addTerm(-bigM,y[i][j][eNode.getId()]);
							//cplex.addGe(taxiArcEpr2,traSep[i]-3*bigM);
							cplex.addGe(taxiArcEpr2,taxiSep-3*bigM);*/
							
							//order preserve on n and l
							IloLinearNumExpr arcOrderExprG0 = cplex.linearNumExpr();
							arcOrderExprG0.addTerm(1.0,y[i][j][eNode.getId()]);
							arcOrderExprG0.addTerm(-1.0,y[i][j][sNode.getId()]);
							arcOrderExprG0.addTerm(-1,omega[i][arc.getId()]);
							arcOrderExprG0.addTerm(-1,omega[j][reverseArc.getId()]);
							cplex.addGe(arcOrderExprG0,-2);
							
							IloLinearNumExpr arcOrderExprL0 = cplex.linearNumExpr();
							arcOrderExprL0.addTerm(1.0,y[i][j][eNode.getId()]);
							arcOrderExprL0.addTerm(-1.0,y[i][j][sNode.getId()]);
							arcOrderExprL0.addTerm(1,omega[i][arc.getId()]);
							arcOrderExprL0.addTerm(1,omega[j][reverseArc.getId()]);
							cplex.addLe(arcOrderExprL0,2);

						}
					}
				}
			}
			//constrained position shifting
			if(flightList.get(flightSize-1).isArr()){
				for(Flight f:arrList){
					IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
					for(Flight f2:arrList){
						if(f.equals(f2))continue;
						cpsExpr.addTerm(1, y[f2.getId()][f.getId()][pseudoOriginId]);
					}
					cplex.addLe(cpsExpr,mps+f.getFCFSposition());
					cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
				}
			}else{
				for(Flight f:depList){
					IloLinearNumExpr cpsExpr = cplex.linearNumExpr();
					for(Flight f2:depList){
						if(f.equals(f2))continue;
						cpsExpr.addTerm(1, y[f2.getId()][f.getId()][pseudoDestinId]);
						//System.out.println("["+f2.getId()+"]["+f.getId());
					}
					cplex.addLe(cpsExpr,mps+f.getFCFSposition());
					//System.out.println("less than "+(mps+f.getFCFSposition()));
					//System.out.println("greater than "+(f.getFCFSposition()-mps));
					cplex.addGe(cpsExpr,f.getFCFSposition()-mps);
				}
			}
		
			for(int j=0;j<flightSize-1;j++){  //first F-1 flights
				Flight f = flightList.get(j);
				System.out.println(f.getRoute());
				for(int i=0;i<f.getRoute().getNodeList().size();i++){
					cplex.addEq(x[f.getId()][f.getRoute().getNodeList().get(i).getId()], 1);
					cplex.addEq(t[f.getId()][f.getRoute().getNodeList().get(i).getId()], f.getRoute().getTimeList().get(i));
				}
			}
			
			//Cplex Parameters
			//cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			cplex.setParam(IloCplex.DoubleParam.TiLim,cpuTime); // set TiLim
			//cplex.setParam(IloCplex.IntParam.RootAlg,IloCplex.Algorithm.Dual);
			
			//solve
			if(cplex.solve()){
				System.out.println("FCFS obj = "+String.format("%.1f",cplex.getObjValue()));
				returnValue = Math.round(cplex.getObjValue());
				//show results
				for(int i=flightSize-1;i<flightSize;i++){
					Map<Node,Arc> showMap = new HashMap<>();
					System.out.print(flightList.get(i).getCallsign()+" t(dNode)"+ String.format("%.1f",cplex.getValue(t[i][dNode[i]]))+" ");

					for(Arc arc:arcList){
						if(Math.abs(cplex.getValue(omega[i][arc.getId()])-1)<0.001){
							showMap.put(arc.getStartNode(), arc);
						}
					}
					Node sNode = nodeMap.get("o");
					Route route = new Route();
					route.setF(flightList.get(i));
					flightList.get(i).setRoute(route);
					route.getNodeList().add(sNode);
					route.getTimeList().add((int)Math.round(cplex.getValue(t[i][sNode.getId()])));
					while(!sNode.getCallsign().contains("d")){			
						System.out.print(sNode+"("+String.format("%.1f",cplex.getValue(t[i][sNode.getId()]))+")-");
						Node eNode = showMap.get(sNode).getEndNode();
						sNode = eNode;
						route.getNodeList().add(sNode);
						route.getTimeList().add((int)Math.round(cplex.getValue(t[i][sNode.getId()])));
					
					}
					System.out.println(sNode+"("+String.format("%.1f",cplex.getValue(t[i][sNode.getId()]))+")-");				
		
				}
			
			}else{
				System.out.println(flightList.get(flightSize-1));
				System.out.println("No feasible MIP solution obtained!  "+cplex.getStatus());
				System.out.println(cplex.getObjValue()+" ");
				
				returnValue = Math.round(cplex.getObjValue());
				//show results
				for(int i=flightSize-1;i<flightSize;i++){
					Map<Node,Arc> showMap = new HashMap<>();
					System.out.print(flightList.get(i).getCallsign()+" t(dNode)"+ String.format("%.1f",cplex.getValue(t[i][dNode[i]]))+" ");

					for(Arc arc:arcList){
						if(Math.abs(cplex.getValue(omega[i][arc.getId()])-1)<0.001){
							showMap.put(arc.getStartNode(), arc);
						}
					}
					Node sNode = nodeMap.get("o");
					Route route = new Route();
					route.setF(flightList.get(i));
					flightList.get(i).setRoute(route);
					route.getNodeList().add(sNode);
					route.getTimeList().add((int)Math.round(cplex.getValue(t[i][sNode.getId()])));
					while(!sNode.getCallsign().contains("d")){			
						System.out.print(sNode+"("+String.format("%.1f",cplex.getValue(t[i][sNode.getId()]))+")-");
						Node eNode = showMap.get(sNode).getEndNode();
						sNode = eNode;
						route.getNodeList().add(sNode);
						route.getTimeList().add((int)Math.round(cplex.getValue(t[i][sNode.getId()])));
					
					}
					System.out.println(sNode+"("+String.format("%.1f",cplex.getValue(t[i][sNode.getId()]))+")-");				
		
				}
				
				
			}

			cplex.end();


		}catch(IloException exc){
			exc.printStackTrace();
		}
		return returnValue;
	}
}
