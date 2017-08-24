package comparator;

import java.util.Comparator;

import entity.*;


public class LabelNodeComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		Node n0 = (Node)arg0;
		Node n1 = (Node)arg1;
		if(n0.getLabel()>n1.getLabel()){
			return 1;
		}else if(n0.getLabel()<n1.getLabel()){
			return -1;
		}
		return 0;
	}

}
