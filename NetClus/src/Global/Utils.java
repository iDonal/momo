package Global;

import java.util.Comparator;


import Type.Pair;

public class Utils {

	static public int findMax(double[] data){
		int maxindex = 0;
		double maxvalue = 0;
		
		for (int i = 0; i < data.length; i++) {
			if (data[i]>maxvalue) {
				maxindex=i;
				maxvalue=data[i];
			}
		}
		return maxindex;
	}
	
	
	public static Comparator<Pair<Integer, Double>> SortPairByValueComparator = new Comparator<Pair<Integer,Double>>() {
		@Override
		public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
			if(o2.second - o1.second > 0){
				return 1;
			}else if(o2.second - o1.second < 0){
				return -1;
			}else {
				return 0;
			}
		}
	};
}
