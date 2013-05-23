package RankFunc;


import java.util.Map;



import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;
import Global.Global;
import Main.NetClus;
import Type.Entity;
import Type.RankVec;
import Type.Relation;

public class RankFunc {

	public static RankVec Rank(Map<String, Relation> RelationSet,  SparseVector memberShip,  RankVec globalRank, double lambda_D, double lambda_C){
		
		
		double totalAttrNodeNum = 0;
		double[] attributePro=new double[Global.attributeTypeList.length];
		RankVec result =new RankVec(Global.typeList.length);
		
		
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
			totalAttrNodeNum+=NetClus.EntitySet.get(Global.attributeTypeList[i]).size();
		}
		
		//compute attribute ranking
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
			
			Relation attrTargetRelation = RelationSet.get(Global.relationList[i]);
			Entity attrEntity = NetClus.EntitySet.get(Global.attributeTypeList[i]);
			
			attributePro[i] =  attrEntity.size() / totalAttrNodeNum;//p(Tx|G)
			DenseVector rankVector =  SimpleRank(attrTargetRelation,attrEntity,memberShip);
			
			result.setAttributeRankVec(i, rankVector);
		}
		
		//compute target ranking
		DenseVector targetRankVector =  new DenseVector(Global.targetTypeNum);

		
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
		    DenseVector curRankvec = result.getAttribueRankVec(i);
		    Relation curRelation = RelationSet.get(Global.relationList[i]);
		    
		    for (int j = 0; j < Global.targetTypeNum; j++) {
		    	SparseVector neiborvector = curRelation.getCol(j);
		    	double score = 0;

		    	for(VectorEntry e:neiborvector){
		    		double pxk=0;
		    		if (globalRank==null) 
		    			pxk = curRankvec.get(e.index());
						
		    		else {
						double newp = globalRank.getAttribueRankVec(i).get(e.index());
						pxk=curRankvec.get(e.index())*Global.lambda_C+(1-Global.lambda_C)*newp;
						
					}
		    		if (pxk==0) {
						score = Double.NEGATIVE_INFINITY;
						break;
					}
		    		else{
		    			//double norm = Math.log(curRelation.getRow(e.index()).norm(Norm.One)+1);
		    			score += e.get() * (Math.log(pxk)+Math.log(attributePro[i]));
		    			//
		    		}
		    	}
		    	
		    	targetRankVector.add(j, score);
		    	
			}
		}

		double sum= 0;
		for (int i = 0; i < targetRankVector.size(); i++) {
			if(targetRankVector.get(i)!=Double.NEGATIVE_INFINITY)
				sum += targetRankVector.get(i);
		}
		sum = Math.abs(sum)/targetRankVector.size();
		System.out.println(sum);
//		
//		
		for (int i = 0; i < targetRankVector.size(); i++) {
			double logvalue = targetRankVector.get(i)/sum;
			//System.out.println(logvalue);
			if (logvalue < -100) {
				logvalue = Double.NEGATIVE_INFINITY;
			}
			targetRankVector.set(i,Math.exp(logvalue));
		}
		
		nomalize(targetRankVector);
		
		result.setTargetRankVec(targetRankVector);
		
		return result;
	}
	
public static RankVec Rank2(Map<String, Relation> RelationSet,  SparseVector memberShip,  RankVec globalRank, double lambda_D, double lambda_C){
		
		
		double totalAttrNodeNum = 0;
		double[] attributePro=new double[Global.attributeTypeList.length];
		RankVec result =new RankVec(Global.typeList.length);
		
		
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
			totalAttrNodeNum+=NetClus.EntitySet.get(Global.attributeTypeList[i]).size();
		}
		
		//compute attribute ranking
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
			
			Relation attrTargetRelation = RelationSet.get(Global.relationList[i]);
			Entity attrEntity = NetClus.EntitySet.get(Global.attributeTypeList[i]);
			
			attributePro[i] =  attrEntity.size() / totalAttrNodeNum;//p(Tx|G)
			DenseVector rankVector =  SimpleRank(attrTargetRelation,attrEntity,memberShip);
			
			result.setAttributeRankVec(i, rankVector);
		}
		
		//compute target ranking
		DenseVector targetRankVector =  new DenseVector(Global.targetTypeNum);

		
		
		for (int i = 0; i < Global.attributeTypeList.length; i++) {
		    DenseVector curRankvec = result.getAttribueRankVec(i);
		    Relation curRelation = RelationSet.get(Global.relationList[i]);
		    
		    for (int j = 0; j < Global.targetTypeNum; j++) {
		    	SparseVector neiborvector = curRelation.getCol(j);
		    	double score = 0;
		    	double links = 0;
		    	for(VectorEntry e:neiborvector){
		    		double pxk=0;
		    		if (globalRank==null) 
		    			pxk = curRankvec.get(e.index());
						
		    		else {
						double newp = globalRank.getAttribueRankVec(i).get(e.index());
						pxk=curRankvec.get(e.index())*Global.lambda_C+(1-Global.lambda_C)*newp;
						
					}
		    			//double norm = Math.log(curRelation.getRow(e.index()).norm(Norm.One)+1);
		    			score += e.get()*pxk*attributePro[i];
		    			links += 1;
		    	}
		   
		    	if (links!=0) {
		    		targetRankVector.add(j, score);
				}
		    	
		    	
			}
		}

		nomalize(targetRankVector);
		
		result.setTargetRankVec(targetRankVector);
		
		return result;
	}
	
	public static DenseVector SimpleRank(Relation relation, Entity entity,SparseVector memberShip) {
		
		 DenseVector rv = new DenseVector(entity.size());
		 
		 double sum =0;
		 
		 for (int i = 0; i < entity.size(); i++) {
			 double neiborSum = relation.getRow(i).dot(memberShip);
			 rv.set(i, neiborSum);
			 sum+= neiborSum;
		 }
		 if (sum!= 0) {
			 for (int i = 0; i < entity.size(); i++) {
				 rv.set(i, rv.get(i)/sum);
		 	  }
		 }
		return rv;
	}

	public static Vector nomalize(Vector v) {
		
		
		double sum=0;
		for (VectorEntry e : v) {
			
			sum+=e.get();
		}
		if (sum != 0) {
			for (VectorEntry e : v) {
				e.set(e.get()/sum);
			}
		}
		return v;
		
	}
}
