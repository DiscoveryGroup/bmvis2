package biomine.nodeimportancecompression;

import java.util.PriorityQueue;
import java.util.Random;

public class HeapMapTest {
	
	static final int ITERATIONS = 100000;
	
	static HeapMap<Integer,Integer> hm;
	static PriorityQueue<Integer> pq;
	static Random rand;
	
	static int a=0;
	static int[] adds;
	static int iter;
	static void checkSize(){
		if(pq.size()!=hm.size()){
			System.out.println("sizes differ");
			System.exit(1);
		}
	}
	static void push(){
		checkSize();
		int v = a++;
		int k = rand.nextInt();
		adds[v] = k;
		hm.put(v, k);
		pq.add(k);
		hm.checkIntegrity();
	}
	
	static void popCheck(){
		checkSize();
		if(pq.isEmpty())return;
		int a = hm.topValue();
		int b = pq.peek();
		if(a!=b){
			System.out.println("Error popping");
			System.out.println(a+" "+b);
			System.exit(1);
		}
		hm.pop();
		pq.poll();
		hm.checkIntegrity();
	}
	static void putCheck(){
		checkSize();
		int v = rand.nextInt(a);
		int k = rand.nextInt();
		if(hm.containsKey(v)){
			if(hm.get(v)!=adds[v]){
				System.out.println("adds not proper");
				System.exit(1);
			}
			if(pq.remove(adds[v])==false){
				System.out.println(adds[v]+" was not found from pq");
				System.exit(1);
			}
			pq.add(k);
			adds[v] = k;
			hm.put(v,k);
			System.out.println("putting");
		}
		hm.checkIntegrity();
	}
	static void removeCheck(){
		checkSize();
		if(pq.isEmpty()){
			return;
		}
		int r = rand.nextInt(a);
		if(hm.containsKey(r)){
			hm.remove(r);
			pq.remove(adds[r]);
			System.out.println("removing");
		}
	}
	public static void main(String[] a){
		hm = new HeapMap<Integer, Integer>();
		pq = new PriorityQueue<Integer>();
		rand = new Random();
		adds = new int[ITERATIONS];
		
		for(int i=0;i<ITERATIONS;i++){
			iter = i;
			System.out.println("iter "+i+" size "+hm.size());
			int op = rand.nextInt(11);
			if(op<7){
				push();
			}else if(op<9){
				popCheck();
			}else if(op<10){
				removeCheck();
			}else{
				putCheck();
			}
		}
		System.out.println("success");
		
	}
}
