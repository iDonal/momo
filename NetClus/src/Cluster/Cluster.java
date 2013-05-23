package Cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import java.util.Map;
import java.util.Random;



import Global.Global;
import Global.Utils;
import Main.NetClus;
import RankFunc.RankFunc;
import Type.Entity;
import Type.Pair;
import Type.RankVec;
import Type.Relation;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.sparse.SparseVector;

public class Cluster {
	
    static double epsi_cluster = 1;
    static double last_epsi = 0;
    static  int MAX_ITER_C = 30;
    static int MAX_ITER_PK = 150;
    static int iter_c = 0;
    
    static double epsi_pk = 1;
    static int iter_pk = 0;
   
    
    
	static public void cluster(SparseVector[] subNetWork, int clusterNum ) {  
        double[] PK = new double[clusterNum+1]; // p(k)
        double[] newPK = new double[clusterNum+1];
        DenseVector[] clusterCenter= new DenseVector[clusterNum];
        DenseVector[] PK_D = new DenseVector[Global.targetTypeNum];//p(k|d) 1--k
        
        for (int i = 0; i < PK_D.length; i++) {
			PK_D[i] = new DenseVector(clusterNum);
		}
        
        //global only compute once
        RankVec globalRank = RankFunc.Rank2(NetClus.RelationSet, subNetWork[clusterNum], null, Global.lambda_D, Global.lambda_C); //TODO rank
        
        
        
        SparseVector[] curSubNetWork = new SparseVector[clusterNum];
        RankVec[] curRankInCluster = new RankVec[clusterNum];
        for (int i = 0; i < clusterNum; i++) {
			curSubNetWork[i]= new SparseVector(subNetWork[i]);
		}
        //
        while (epsi_cluster > 0.001 && iter_c < MAX_ITER_C)
        {

        	// compute p(d|Gk)  p(X|Tx,Gk)
        	for (int i = 0; i < clusterNum; i++) {
				curRankInCluster[i] =  RankFunc.Rank2(NetClus.RelationSet, curSubNetWork[i], globalRank, Global.lambda_D, Global.lambda_C);
			}
        	
        	for (int k = 0; k < clusterNum; k++)
            {
                PK[k] = (double)1 / (clusterNum);
            }
        	
        	
            //using EM algorithm to iteratively improve the probability
            epsi_pk = 1;
            iter_pk = 0;
            
            while (iter_pk < MAX_ITER_PK && epsi_pk > 0.000001)
            {
                //double[] newPK = new double[clusterNum+1];
            	for (int i = 0; i < newPK.length; i++) {
 					newPK[i]=0;
 				}

                for(int di = 0 ; di < Global.targetTypeNum;di++) //D(1,2,...i)
                {
                    //double[] PK_Di = new double[clusterNum];
                	DenseVector PK_Di = PK_D[di];
                	
                    double sumM = 0;
                    double globalPk_d = 0;
                    for (int k = 0; k < clusterNum; k++)
                    {
                    	//p(di|k)*p(k)
                    	PK_Di.set(k,curRankInCluster[k].getTargetRankVec().get(di)*PK[k]);
                        sumM += PK_Di.get(k);

                    }
                    
                    globalPk_d = globalRank.getTargetRankVec().get(di)*PK[clusterNum];
                    sumM += globalPk_d;


                    for (int k = 0; k < clusterNum; k++)
                    {
                        if (sumM == 0)
                        {
                            PK_Di.set(k, 0);
                        }
                        else
                        {
                        	PK_Di.set(k,PK_Di.get(k) / sumM);  // p(k|d)
                        }
                        newPK[k] += PK_Di.get(k); //////  sum of p(k|d) 
                        
                    }
                    newPK[clusterNum]+=((sumM==0)? 0:globalPk_d/sumM);
                    
                  //  PK_D[di] = new DenseVector(PK_Di);

                }
                epsi_pk = 0;
                
                //update pk
                for (int k = 0; k < clusterNum+1; k++)
                {
                    newPK[k] = newPK[k] / Global.targetTypeNum;    
                    epsi_pk += Math.abs(newPK[k] - PK[k]);
                    //PK[k] = newPK[k];
                }
                double[] tmp = PK;
                PK = newPK;
                newPK = tmp;
   
                if (iter_pk % 10 == 0)
                {
                    System.out.println("iteration "+iter_pk+" of P(Z=k), epsi: "+epsi_pk);
                }
                iter_pk++;
            }
            
            
            
            //get new center for the original cluster for target type
           
            for (int k = 0; k < clusterNum; k++) {
            	clusterCenter[k] = MeanOfCluster(PK_D,curSubNetWork[k],clusterNum);
			}
            
            
            
            //k-means readjust clusters, get new cluster
            int[] indicator=new int[Global.targetTypeNum];
            for (int i = 0; i < Global.targetTypeNum; i++) {				
         
                double maxDistance = 0;
               int indexOfMax = 0;
               
               for (int k = 0; k < clusterNum; k++) {
            	   //cos simi
				  double distance = PK_D[i].dot(clusterCenter[k])/(PK_D[i].norm(Norm.Two)*clusterCenter[k].norm(Norm.Two));
                  
				  //
				  //distance *= (1-1.0*curSubNetWork[k].getUsed()/Global.targetTypeNum );
				  if (distance > maxDistance) {
					maxDistance = distance;
					indexOfMax = k;
				}
               }     
               indicator[i]=indexOfMax;
            }
            SparseVector[] oldSubNetWork = curSubNetWork;  
            curSubNetWork = new SparseVector[clusterNum];
            for (int i = 0; i < curSubNetWork.length; i++) {
				curSubNetWork[i] = new SparseVector(Global.targetTypeNum);
			}
            
            //reassing
            for (int i = 0; i < Global.targetTypeNum; i++) {
				curSubNetWork[indicator[i]].set(i, 1);
			
			}
            
            iter_c++;
            epsi_cluster = diff(oldSubNetWork, curSubNetWork);
            System.out.println("Iteration "+iter_c+" in clustering: epsi_clustering is "+epsi_cluster);
            //get new cluster size
            if (iter_c>Global.MIN_ITER_CLUSTER&& epsi_cluster > last_epsi) {
				break;
			}
            last_epsi = epsi_cluster;
        }
       //last time,output result and  calculate cluster membership for attribute objects as well
        for (int i = 0; i < clusterNum; i++) {
			curRankInCluster[i] =  RankFunc.Rank2(NetClus.RelationSet, curSubNetWork[i], globalRank, Global.lambda_D, Global.lambda_C);
		}
        
        
        try {
			printDistribution(PK_D,curSubNetWork);
			
			BufferedWriter bw =  new BufferedWriter(new FileWriter(Global.BASEPATH+"pk.txt"));
			for (int i = 0; i < PK.length; i++) {
				bw.write(PK[i]+" ");
			}
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        for (int i = 0; i < clusterNum; i++) {
			SparseVector oneclus = curSubNetWork[i];
			
			for(VectorEntry e:oneclus){
				double distance = PK_D[e.index()].dot(clusterCenter[i])/(PK_D[e.index()].norm(Norm.Two)*clusterCenter[i].norm(Norm.Two));
			    curSubNetWork[i].set(e.index(),distance);
			}
		}
        
        
        
        printClusterResult(curSubNetWork,curRankInCluster,NetClus.EntitySet.get("shop"));
        
        Map<String, DenseVector[]> PK_XMap = calculateDistribution(PK_D,clusterNum);
      
        attributeTypeCluster(PK_XMap.get("tag"), clusterNum, NetClus.EntitySet.get("tag"),clusterCenter);
    	attributeTypeCluster(PK_XMap.get("user"),clusterNum, NetClus.EntitySet.get("user"),clusterCenter);
       
        
        
        
//        for (int i = 0; i < Global.targetTypeNum; i++) {
//        	
//        	System.out.print(i+"\t");
//			for (int j = 0; j < clusterNum; j++) {
//				System.out.print(PK_D[i].get(j)+" ");
//			}
//			System.out.println();
//		}
		
	}
	private static void printDistribution(DenseVector[] pk_d,SparseVector[] network) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(Global.BASEPATH+"/Distribution.txt"));
		
		for (int i = 0; i < network.length; i++) {
			
			bw.write("Group :"+i+"\n");
			SparseVector v= network[i];
			for (VectorEntry e : v) {
				DenseVector distr = pk_d[e.index()];
				for (int j = 0; j < distr.size(); j++) {
					bw.write(distr.get(j)+" ");
				}
				bw.write("\n");
			}
			bw.write("\n");
			/////////
			System.out.println("Group :"+i+" "+v.getUsed());
		}
		bw.close();
		
		//TODO for similarity
		
		bw = new BufferedWriter(new FileWriter(Global.BASEPATH+"/shopvector.txt"));
		for (int i = 0; i < network.length; i++) {
			
			SparseVector v= network[i];
			for (VectorEntry e : v) {
				DenseVector distr = pk_d[e.index()];
				for (int j = 0; j < distr.size(); j++) {
					bw.write(distr.get(j)+" ");
				}
				bw.write("\n");
			}
		}
		bw.close();
		
		
		Entity shop = NetClus.EntitySet.get("shop");
		bw = new BufferedWriter(new FileWriter(Global.BASEPATH+"/nameindex.txt"));
		for (int i = 0; i < network.length; i++) {
			
			SparseVector v= network[i];
			for (VectorEntry e : v) {
				bw.write(e.index()+"\t"+shop.getNameByIndex(e.index())+"\n");
			}
		}
		bw.close();
		
	}

	private static void printClusterResult(SparseVector[] curSubNetWork,RankVec[] rank,Entity entity) {
//		for (int i = 0; i < curSubNetWork.length; i++) {
//			System.out.println("group :"+i);
//			SparseVector v = curSubNetWork[i];
//			for (VectorEntry e : v) {
//				 System.out.print(entity.getNameByIndex(e.index())+"\n");
//			}
//			
//			System.out.println();
//		}
		try {
			if (entity.getTypeName().equals("user")) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(Global.BASEPATH + entity.getTypeName()+"Result.txt"));
				for (int i = 0; i < curSubNetWork.length; i++) {
					bw.write("Group :"+i+"\n");
					SparseVector v = curSubNetWork[i];
					for (VectorEntry e : v) {
							bw.write(entity.getIDByIndex(e.index())+"\t");
					}
					bw.write("\n");
				}
				bw.close();
			}
			
			else{
			
				BufferedWriter bw = new BufferedWriter(new FileWriter(Global.BASEPATH + entity.getTypeName()+"Result.txt"));
				for (int i = 0; i < curSubNetWork.length; i++) {
					bw.write("Group :"+i+"\n");
					SparseVector v = curSubNetWork[i];
					if (rank == null)
						for (VectorEntry e : v) {
							bw.write(entity.getNameByIndex(e.index())+"\t");
						}
				
					else{
						DenseVector rankVector = rank[i].getTargetRankVec();
						ArrayList<Pair<Integer,Double>> list = new ArrayList<Pair<Integer,Double>>();
						for (VectorEntry e : v) {
							list.add(new Pair<Integer,Double>(e.index(),e.get()*rankVector.get(e.index())));
						}
						Collections.sort(list,Utils.SortPairByValueComparator);
						
						for (Pair<Integer, Double> pair : list) {
							bw.write(entity.getNameByIndex(pair.first)+"\t"+pair.second+"\n");
						}
						
					}
					bw.write("\n");
				}
				bw.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
    static private Map<String, DenseVector[]> calculateDistribution(DenseVector[] PK_D,int K){
    	
    	Map<String, DenseVector[]> PK_XMap = new HashMap<String, DenseVector[]>(Global.attributeTypeList.length);
    	
    	
    	for (int type = 0; type < Global.attributeTypeList.length; type++) {
    		
    		Entity entity= NetClus.EntitySet.get(Global.attributeTypeList[type]);
    		Relation relation = NetClus.RelationSet.get(Global.relationList[type]);
			DenseVector[] PK_X = new DenseVector[entity.size()];
			
			for (int x = 0; x < entity.size(); x++) {
				
				SparseVector neiborbvector = relation.getRow(x);
				PK_X[x] = new DenseVector(K);
				int Ngx = neiborbvector.getUsed();
				
				
				for (VectorEntry e : neiborbvector) {
					PK_X[x].add(PK_D[e.index()]);
				}
				
				if (Ngx > 0) {
					for (VectorEntry e : PK_X[x]) {
						e.set(e.get()/Ngx);
					}
				}
			}
			
			PK_XMap.put(Global.attributeTypeList[type], PK_X);
    		
 
		}
    	return PK_XMap;
    	
        
    }
    
    static private void attributeTypeCluster(DenseVector[] PK_X, int K, Entity entity, DenseVector[] clusterCenter){
    	SparseVector[] subNetWork = new SparseVector[K];
    	for (int i = 0; i < subNetWork.length; i++) {
			subNetWork[i] = new SparseVector(entity.size());
		}
    	
    	int[] indicator=new int[entity.size()];
         for (int i = 0; i < entity.size(); i++) {				
      
             double maxDistance = 0;
            int indexOfMax = 0;
            
            for (int k = 0; k < K; k++) {
         	   //cos simi
				  double distance = PK_X[i].dot(clusterCenter[k])/(PK_X[i].norm(Norm.Two)*clusterCenter[k].norm(Norm.Two));
               
				  //
				  //distance *= (1-1.0*curSubNetWork[k].getUsed()/Global.targetTypeNum );
				  if (distance > maxDistance) {
					maxDistance = distance;
					indexOfMax = k;
				}
            }     
            indicator[i]=indexOfMax;
         }
         
         //reassing
         for (int i = 0; i < entity.size(); i++) {
				subNetWork[indicator[i]].set(i, 1);
			}
         printClusterResult(subNetWork,null, entity);
    	
    }
    
    
    
    static private void attributeTypeCluster(DenseVector[] PK_X, int K, Entity entity){
    	
    	SparseVector[] subNetWork = new SparseVector[K];
    	for (int i = 0; i < subNetWork.length; i++) {
			subNetWork[i] = new SparseVector(entity.size());
		}
    	Random random= new Random();
  	   	for (int i = 0; i < entity.size(); i++) {
  		 
  		  int k = random.nextInt(K);
  		  subNetWork[k].set(i, 1);
  	   	}
  	   	
  	   	
  	  iter_c = 0;
  	  epsi_cluster = 1;
  	  while (epsi_cluster > 0.0001 && iter_c < MAX_ITER_C){ 	
  	   	
        //get new center for the original cluster for target type
        DenseVector[] clusterCenter= new DenseVector[K];
        for (int k = 0; k < K; k++) {
        	clusterCenter[k] = MeanOfCluster(PK_X,subNetWork[k],K);
		}
        
        //k-means readjust clusters, get new cluster
        int[] indicator=new int[entity.size()];
        for (int i = 0; i < entity.size(); i++) {				
     
           double maxDistance = 0;
           int indexOfMax = 0;
           
           for (int k = 0; k < K; k++) {
        	   //cos simi
			  double distance = PK_X[i].dot(clusterCenter[k])/(PK_X[i].norm(Norm.Two)*clusterCenter[k].norm(Norm.Two));
              
			  //
			  //distance *= (1-1.0*curSubNetWork[k].getUsed()/Global.targetTypeNum );
			  if (distance > maxDistance) {
				maxDistance = distance;
				indexOfMax = k;
			}
           }     
           indicator[i]=indexOfMax;
        }
        SparseVector[] oldSubNetWork = subNetWork;
        
        subNetWork = new SparseVector[K];
        for (int i = 0; i < subNetWork.length; i++) {
			subNetWork[i] = new SparseVector(entity.size());
		}
        
        //reassing
        for (int i = 0; i < entity.size(); i++) {
			subNetWork[indicator[i]].set(i, 1);
		
		}
        iter_c++;
        epsi_cluster = diff(oldSubNetWork, subNetWork);
        System.out.println("Attribute Iteration "+iter_c+" in clustering: epsi_clustering is "+epsi_cluster);
        //get new cluster size
        
    }
    		
    	printClusterResult(subNetWork,null, entity);
    	
    	
    }
    
    
    
	static private double diff(SparseVector[] oldSubNetWork,SparseVector[] newSubNetWork){
		
		double sum_old = 0;
		double difference = 0;

		for (int i = 0; i < oldSubNetWork.length; i++) {
			
			for (int j = 0; j < oldSubNetWork[0].size(); j++) {
				difference += Math.abs(oldSubNetWork[i].get(j)-newSubNetWork[i].get(j));
				sum_old += oldSubNetWork[i].get(j);
			}
			
		}
		return difference/sum_old;
	}
	static private DenseVector MeanOfCluster(DenseVector[]matrix,SparseVector ownership,int K){
		
		DenseVector mean = new DenseVector(K);
		double count = 0;
		
		for (VectorEntry e : ownership) {
			double norm = matrix[e.index()].norm(Norm.Two);
			for (int i = 0; i < mean.size(); i++) {
				if (norm > 0) {
					mean.add(i, matrix[e.index()].get(i)/norm);
				}
				else {
					mean.add(i,0);
				}
			}
			//mean.add(matrix[e.index()]);
			count++;
		}
		
		if (count > 0) {
			for (int i = 0; i < mean.size(); i++) {
				mean.set(i,	mean.get(i)/count);
			}
		}
		return mean;
	}
}
