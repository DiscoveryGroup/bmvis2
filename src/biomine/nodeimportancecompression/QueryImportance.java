package biomine.nodeimportancecompression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMNode;


public class QueryImportance {
	public static void setImportance(ImportanceGraph ig,Map<Integer,Double> query){
		double[] up = new double[query.size()];
		int[] queryInts = new int[query.size()];
		
		HashSet<Integer> queryIntSet = new HashSet<Integer>();
		{
			int i =0;
			for(int x:query.keySet()){
				queryIntSet.add(x);
				queryInts[i]=x;
				up[i]=query.get(x);
				i++;
			}
		}
		
		
		int n = ig.getNodeCount();
		//direct access
		double[][] da = new double[n][queryInts.length];
		//balanced proportion 
		double[] bp = new double[queryInts.length];
		
		//compute direct access
		for(int i=0;i<queryInts.length;i++){
			int m = queryInts[i];
			ImportanceGraph vm = ig.copy();
			for(int j=0;j<queryInts.length;j++)
				if(j!=i)
					vm.removeNode(queryInts[j]);
				
			ProbDijkstra pdTop = new ProbDijkstra(vm,m);
			ProbDijkstra pdBottom = new ProbDijkstra(ig,m);
			double sum =  0;
			for(int j=0;j<n;j++){
				if(!queryIntSet.contains(j)){
					double t = pdTop.getProbTo(j);
					double b = pdBottom.getProbTo(j);
					if(b!=0)
						da[j][i] = t/b;
					else
						da[j][i] = 0;
                                   //     System.out.println(t+"\t"+b+"\t"+da[j][i]);
					sum+=da[j][i];
				}
			}
			bp[i] = (n-query.size())/sum;
		}
		
		for(int i=0;i<queryInts.length;i++){
			int m = queryInts[i];
			ig.setImportance(m, bp[i]*up[i]);
                       // double s = bp[i]*up[i];
			//System.out.println("importance "+queryArr.get(i)+"\t\t"+s);
                      //  System.out.println("importance "+queryArr.get(i)+"\t\t"+m);
		}
		for(int i=0;i<n;i++){
			double s = 0;
			if(!queryIntSet.contains(i)){
                               
				for(int j=0;j<queryInts.length;j++){
					s+=da[i][j]*bp[j]*up[j];
                                
				}

				s/=queryInts.length;
				ig.setImportance(i,s);
				if(Double.isNaN(s))
					ig.setImportance(i, 0);
			//	System.out.println("importance "+wrap.intToNode(i)+"\t\t"+s);
			}
		}
		
	}
	
	public static ImportanceGraphWrapper queryImportanceGraph(BMGraph bm,Map<BMNode,Double> query){
		ImportanceGraphWrapper wrap = new ImportanceGraphWrapper(bm);
		ArrayList<BMNode> queryArr  = new ArrayList<BMNode>(query.keySet());
		
		Map<Integer,Double> queryI = new HashMap<Integer, Double>();
		for(Entry<BMNode,Double> e:query.entrySet()){
			queryI.put(wrap.nodeToInt(e.getKey()), e.getValue());
		}
		
		setImportance(wrap.getImportanceGraph(), queryI);
		return wrap;
	}
}
