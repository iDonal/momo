package Main;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

import Cluster.Cluster;
import Global.Global;
import Type.Entity;
import Type.Relation;



public class NetClus {
	
	public static Map<String, Entity> EntitySet = new HashMap<String, Entity>(3);
	public static Map<String, Relation> RelationSet =new HashMap<String, Relation>(2);
	
	
	public void loadData() throws IOException {
		
		for (int i = 0; i < Global.typeList.length; i++) {
			Entity entity = new Entity();
			entity.setTypeName(Global.typeList[i]);
			
			BufferedReader br = new BufferedReader(new FileReader(Global.BASEPATH+Global.typeFileList[i]));
		    String car = "";
		    
		    while ((car = br.readLine())!= null) {
		    	if (entity.getTypeName().equals("tag")) {
					entity.set(car.split("\t")[0]);
				}
		    	else if (entity.getTypeName().equals("shop")) {
		    		String[] terms = car.split("\t");
		    		entity.set(Long.parseLong(terms[0]), terms[1]);
		    		if (entity.size()>20000) {
						break;
					}
				}
		    	else {
					entity.set(Long.parseLong(car.split("\t")[0]));
					if (entity.size()>200000) {
						break;
					}
				}
			}
		    br.close();
		    
		    EntitySet.put(entity.getTypeName(), entity);
		    System.out.println("Entity load done : "+entity.getTypeName());
		}
		
		
		
	    //tag - shop relation
		Entity shop = EntitySet.get("shop");
		Entity tag = EntitySet.get("tag");
		
		

	    Relation tag_shop = new Relation(tag.size(), shop.size());
	    BufferedReader br1 = new BufferedReader(new FileReader(Global.BASEPATH+Global.relFileList[1]));
	    String car1 = "";
	    
	    long count = 0;
	    while ((car1 = br1.readLine())!= null) {
			String[] terms = car1.split("\t");
			long shopid = Long.parseLong(terms[0]);
			count++;
			
			if (count%100000==0) {
				System.out.println(count);
			}
			
			if(!shop.contains(shopid))
				continue;
			
			String[] tags = terms[1].split(" ");
			for (int i = 0; i < tags.length; i++) {
				String[] pair = tags[i].split(":");
				if (!tag.contains(pair[0])) {
					continue;
				}
				tag_shop.set(tag.getIndexByname(pair[0]), shop.getIndexByID(shopid), Integer.parseInt(pair[1]));
			    
			}
	    }
	    br1.close();
		
	    RelationSet.put("tag-shop", tag_shop);
	    System.out.println("Relation load done : tag-shop");
		
		
		
		//user - shop relation
		Entity user = EntitySet.get("user");

		Relation user_shop = new Relation(user.size(), shop.size()); 
		
		BufferedReader br = new BufferedReader(new FileReader(Global.BASEPATH+Global.relFileList[0]));
	    String car = "";
	    
	    
	    count = 0;
	    while ((car = br.readLine())!= null) {
	    	try{
	    		count++;
				String[] terms = car.split("\t");
				long uid = Long.parseLong(terms[0]);
				long shopid = Long.parseLong(terms[3]); //TODO 1 for test 3 for real
				if (user.contains(uid)&&shop.contains(shopid)) 
				 user_shop.set(user.getIndexByID(uid), shop.getIndexByID(shopid), 1);
	    	}
	    	catch (NumberFormatException e) {
				continue;
	    		// TODO: handle exception
			}
	    	if (count%100000==0) {
				System.out.println(count);
			}
	    }
	    br.close();
	    RelationSet.put("user-shop", user_shop);
	    System.out.println("Relation load done : user-shop");
	    
	    
	    //normalize
	    
//	    for (int i = 0; i < tag.size(); i++) {
//			SparseVector v = tag_shop.getRow(i);
//			double norm = v.norm(Norm.One);
//			
//			for (VectorEntry e : v) {
//				tag_shop.set(i,e.index(),10*e.get()/norm);
//			}
//			
//		}
//	    

	    

	    
	    Global.targetTypeNum = shop.size();
	    
	    
	}
	
	public void  run(int clusterNum) {
	   SparseVector[] subNetWork = new SparseVector[clusterNum+1];
	   
	   
	   //each subNetwork
	   for (int i = 0; i <= clusterNum; i++) {
		 subNetWork[i] = new SparseVector(Global.targetTypeNum);  
	   }
	   
	   Random random= new Random();
	   for (int i = 0; i < Global.targetTypeNum; i++) {
		 
		  int k = random.nextInt(clusterNum);
		  subNetWork[k].set(i, 1);
		   
		  subNetWork[clusterNum].set(i, 1);
	   }
	   Cluster.cluster(subNetWork, clusterNum);
		
	}

	
	public static void main(String[] args) {
	
		NetClus netClus = new NetClus();
		try {
			netClus.loadData();
		} catch (IOException e) {
			
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		netClus.run(20);
		
		
	}

}
