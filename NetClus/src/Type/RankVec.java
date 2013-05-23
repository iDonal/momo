package Type;

import no.uib.cipr.matrix.DenseVector;

public class RankVec {
	private DenseVector[] rankvectors;
	
	public RankVec(int typeNum) {
		rankvectors = new DenseVector[typeNum];
		// TODO Auto-generated constructor stub
	}
	
	public void setTargetRankVec(DenseVector v){
		rankvectors[0] = v;
	}
	public void setAttributeRankVec(int index,DenseVector v){
		rankvectors[index+1]= v;
	}
	
	public void setAttributeRankVecs(DenseVector[] vs){
		for (int i = 1; i < rankvectors.length; i++) {
			rankvectors[i]=vs[i-1];
		}
	}
	public DenseVector getTargetRankVec(){
		return rankvectors[0];
	} 
	public DenseVector getAttribueRankVec(int i) {
		return rankvectors[i+1];
	}
	
	
}
