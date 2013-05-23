package ShopRelation;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;


import Global.Global;
import Type.Pair;

public class ShopRelation {

	int topN = 10;
	int topicNum = 20;
	int shopNum = 20001;
	int queueLen = 30;
	double alpha = 0.9; //衰减系数
	double[][] matrix = new double[shopNum][topicNum];
	
	String[] shopname = new String[shopNum];
	String[] shopid = new String[shopNum];
	
	Map<Integer, Set<Long>> shopuser = new HashMap<Integer, Set<Long>>(210000,0.7f);
	
	Map<Integer, Double> weakenMap = new HashMap<Integer, Double>();
	Queue<Integer> weakenQueue = new LinkedList<Integer>();
	
	
	public void loadData() throws IOException{
		
		//TODO /model-final.theta
		//BufferedReader br = new BufferedReader(new FileReader(Global.BASEPATH+"/shopvector.txt"));
		BufferedReader br = new BufferedReader(new FileReader(Global.BASEPATH+"/model-final.theta"));
		String line = null;
		
		int l = 0;
		while((line = br.readLine())!=null){
			String[] arr = line.split(" ");
			
			if (arr.length!=topicNum) {
				throw new IOException();
			}
			for (int i = 0; i < topicNum; i++) {
				matrix[l][i] = Double.parseDouble(arr[i]);
			}
			l++; 
			if (l >=shopNum) {
				break;
			}
		}
		br.close();
		
		//TODO /index.txt
		br = new BufferedReader(new FileReader(Global.BASEPATH+"/index.txt"));
		while ((line = br.readLine())!=null){
			String[] arr = line.split("\t");
		    shopid[Integer.parseInt(arr[0])] =arr[2];
			shopname[Integer.parseInt(arr[0])] = arr[1];
		}
		br.close();
		
		
	}
	
	public void loadShopuser()throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(Global.BASEPATH+"/totalshop/shop-user-coll2.txt"));
		String line = null;
		
		int l = 0;
		while((line = br.readLine())!=null){
			String[] arr = line.split(" ");
			
			Set<Long> oneshop = new HashSet<Long>((int)(arr.length*1.5),0.7f);
			for (int i = 1; i < arr.length; i++) {
				oneshop.add(Long.parseLong(arr[i]));
			}
			shopuser.put(Integer.parseInt(arr[0]), oneshop);
			l++; 
//			if (l >=shopNum) {
//				break;
//			}
			if (l%10000==0) {
				System.out.println(l);
			}
	
		}
		br.close();
		
	}
	
	
	public Pair[] findTopN(int index){
		
		PriorityQueue<Pair<Integer, Double>> pq = new PriorityQueue<Pair<Integer, Double>>(topN,Pair.SortPairByValueComparator);
		
		for (int i = 0; i < shopNum; i++) {
			if(i ==index)continue;
			
			
			//double gain = cosin(matrix[index], matrix[i]);
			double gain = similar(shopuser.get(shopid[index]), shopuser.get(shopid[i]));
			
			if (weakenMap.containsKey(i)) {
				gain = gain*weakenMap.get(i);
				weakenMap.put(i, weakenMap.get(i)*alpha);
			}
			else{
				if (weakenQueue.size() >= queueLen) { //remove first
					int removedindex = weakenQueue.poll();
					weakenMap.remove(removedindex);
				}	
				//add to queue
				weakenQueue.offer(i);
				weakenMap.put(i, alpha);		
			}
			Pair<Integer, Double> pair =new Pair<Integer, Double>(i, gain);
			if (pq.size()< topN) {
				pq.offer(pair);
			}
			else if (gain > pq.peek().second) {
				pq.poll();
				pq.offer(pair);
			}
			
		}
		Pair[] result = pq.toArray(new Pair[topN]);
		
		
//		System.out.println(shopname[index]);
//		for (int i = 0; i < result.length; i++) {
//			
//			System.out.println(shopname[(Integer)result[i].first]);
//			System.out.println(result[i].second);
//		}
		
		return result;

	}
	private double similar(Set<Long> a, Set<Long> b) {
		if (a.size() > 0 && b.size() > 0 ) {
			int count = 0;
			
			if (a.size() > b.size()) {
				for (Long id : b) {
					if(a.contains(id))
						count++;
				}
			}
			else{
				for (Long id : a) {
					if(b.contains(id))
						count++;
				}	
				
		     }
		
			return count/(Math.sqrt(a.size()*b.size()));
		}
		else {
			return 0;
		}
		
	}
	
	private double cosin(double[] a, double[] b) {
		double as = 0,bs = 0,ab = 0;
		
		for (int i = 0; i < a.length; i++) {
			as += a[i]*a[i];
			bs += b[i]*b[i];
			ab += a[i]*b[i];
		}
		
		if (ab!= 0 ) {
			return ab/(Math.sqrt(as)*Math.sqrt(bs)); 
		}
		else {
			return 0;
		}
	}
	public void run(){
		
		Pair[] result = findTopN(0);
		
		 Scanner scanner = new Scanner(System.in);
		 
		 while(true){
			 for (int i = 0; i < result.length; i++) {
				System.out.print(i);
				System.out.print('\t');
				System.out.println(shopname[(Integer)result[i].first]);
				System.out.print('\t');
				System.out.println(result[i].second);
			 }
			 int choice =  scanner.nextInt();
			 
			 if (choice >=0 && choice < topN) {
				result = findTopN((Integer)result[choice].first);
			}
		 }
	}
	public void writeSimi() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(Global.BASEPATH+"/totalshop/top100.dat"));
		int K  = 50;
		int n  = 0;
		for (Integer shopid : shopuser.keySet()) {
			StringBuffer sb = new StringBuffer();
			sb.append(shopid);
			
			PriorityQueue<Pair<Integer, Double>> pq = new PriorityQueue<Pair<Integer, Double>>(topN,Pair.SortPairByValueComparator);
			
			for (Integer j : shopuser.keySet()){
				
				if (shopid==j) {
					continue;
				}
				double gain = similar(shopuser.get(shopid), shopuser.get(j));
				Pair<Integer, Double> pair =new Pair<Integer, Double>(j, gain);
				if (pq.size()< K) {
					pq.offer(pair);
				}
				else if (gain > pq.peek().second) {
					pq.poll();
					pq.offer(pair);
				}
				
			}
			
			for (Pair<Integer, Double> pair : pq) {
				if (pair.second > 0)
					sb.append(" "+pair.first+":"+pair.second);
			}
			sb.append("\n");
			
			bw.write(sb.toString());
			
			n++;
			if (n%100==0)
				System.out.println(n);
		
		}
	
	}
	public static void main(String[] args) {
		ShopRelation app = new ShopRelation();
		
		try {
			//app.loadData();
			app.loadShopuser();
			app.writeSimi();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	app.run();
	}
}
