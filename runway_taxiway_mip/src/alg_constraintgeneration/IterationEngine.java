package alg_constraintgeneration;

import java.util.ArrayList;
import java.util.List;

import data.ReadData;
import entity.*;
import mip.RefinedArcModel;

public class IterationEngine {
	private List<ConstraintNode> constraintNodes = new ArrayList();
	private List<ConstraintArc> constraintArcs = new ArrayList<>();
	private List<ConstraintArc> constraintReverseArcs = new ArrayList<>();
	public List<Flight> flightList = ReadData.flightList;
	private int mps = 1;
	
	public double[] solve(int mps,int maxCpu){
		this.mps = mps;
		ArcModel mip = new ArcModel();
		long startTime = System.currentTimeMillis();  //timing-start
		long endTime = System.currentTimeMillis();  //timing-end
		int iter = 0;
		double[] returnValue = new double[2];
		while((endTime-startTime)/1000.0<maxCpu){
			returnValue[0] = mip.integerSolve(mps, maxCpu,constraintNodes,constraintArcs,constraintReverseArcs);
			List<ConstraintNode> newCN = new ArrayList<>();
			List<ConstraintArc> newCA = new ArrayList<>();
			List<ConstraintArc> newRCA = new ArrayList<>();
			for(int i=0;i<flightList.size()-1;i++){
				Flight f1 = flightList.get(i);
				for(int j=i+1;j<flightList.size();j++){
					Flight f2 = flightList.get(j);
					newCN.addAll(f1.getRoute().conflictNode(f2.getRoute()));
					newCA.addAll(f1.getRoute().conflictArc(f2.getRoute()));
					newRCA.addAll(f1.getRoute().conflictReverseArc(f2.getRoute()));
					
				}
			}
			System.out.println("iter "+iter);
			if(newCN.size()==0&&newCA.size()==0&&newRCA.size()==0)break;
			
			if(newCN.size()>0){
				constraintNodes.addAll(newCN);
				System.out.println("node "+newCN);
			}
			if(newCA.size()>0){
				constraintArcs.addAll(newCA);
				System.out.println("arc "+newCA);
			}
			if(newRCA.size()>0){
				constraintReverseArcs.addAll(newRCA);
				System.out.println("reverse arc "+newRCA);
			}
			
			endTime = System.currentTimeMillis();  //timing-end
			iter++;
			
		}
		returnValue[1] = iter;
		return returnValue;
	}
}
