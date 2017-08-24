package alg_twostage;
import java.util.ArrayList;
import java.util.List;

import entity.SequencePair;
public class TwostageEngine {
	public double solve(int mps, int cpu){
		List<SequencePair> spList = new ArrayList<>();
		RunwayModel rm = new RunwayModel();
		rm.integerSolve(mps, cpu, spList);
		TaxiwayModel tm = new TaxiwayModel();
		return tm.integerSolve(mps, cpu, spList);
	}
}
