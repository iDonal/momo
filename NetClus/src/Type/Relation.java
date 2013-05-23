package Type;

import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

public class Relation {
	private FlexCompRowMatrix matrix = null;
	private FlexCompColMatrix matrixTranposed = null;
	
	public Relation(int row,int col){
		
		matrix = new FlexCompRowMatrix(row, col);
		matrixTranposed = new FlexCompColMatrix(row,col);
	}
	
	
	public FlexCompRowMatrix getMatrix() {
		return matrix;
	}
	public void set (int row,int column, double value) {
		matrix.set(row, column, value);
		matrixTranposed.set(row, column, value);
	}
	
	public SparseVector getRow(int i){
		return matrix.getRow(i);
	}
	public SparseVector getCol(int i) {
		return matrixTranposed.getColumn(i);
	}
	
}
