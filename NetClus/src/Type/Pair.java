package Type;

import java.util.Comparator;


public class Pair<T1, T2> {
	public T1 first;
	public T2 second;

	public Pair(T1 f, T2 s) {
	 first = f;
	 second =s;
	 
	}
	 
	// TODO Auto-generated constructor stub
	 @Override
	 public int hashCode() {
			// TODO Auto-generated method stub
			return first.hashCode()+second.hashCode();
		}
	 
	@SuppressWarnings("unchecked")
	@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			if (!(obj instanceof Pair<?,?>)) {
				return false;
			}
			Pair<T1, T2> pair = (Pair<T1, T2>)obj;
			
			return (pair.first.equals(first)&&pair.second.equals(this.second));
		}





	 public static Comparator<Pair<?, ?>> SortPairByValueComparator = new Comparator<Pair<?,?>>() {
			@Override
			public int compare(Pair<?, ?> o1, Pair<?, ?> o2) {
				if((Double)o2.second - (Double)o1.second < 0){
					return 1;
				}else if((Double)o2.second - (Double)o1.second > 0){
					return -1;
				}else {
					return 0;
				}
			}
	 };
 
}

