package comparator;

import java.util.Comparator;

import entity.*;


public class EstimatedtakeofftimeFlightComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		Flight f0 = (Flight)arg0;
		Flight f1 = (Flight)arg1;
		if(f0.getEstimatedDestinTime()>f1.getEstimatedDestinTime()){
			return 1;
		}else if(f0.getEstimatedDestinTime()<f1.getEstimatedDestinTime()){
			return -1;
		}
		return 0;
	}

}
