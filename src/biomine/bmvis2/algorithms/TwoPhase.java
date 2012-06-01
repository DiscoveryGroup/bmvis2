package biomine.bmvis2.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Map.Entry;

import biomine.bmvis2.*;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.algorithms.TwoPhase.TPGraph.TPEdge;
import biomine.bmvis2.utils.OrderedPair;

public class TwoPhase {
	public static class TPGraph {
		private long version = 0;

		public class TPEdge {
			int id;
			int from;
			int to;
			long lastVersion;
			boolean lastExists;

			/**
			 * Var to be used in algorithms to associate path sets with edge.
			 */
			HashSet<Path> truePaths;

			boolean exists() {
				if (lastVersion == version)
					return lastExists;
				lastExists = Math.random() < p;
				lastVersion = version;
				return lastExists;
			}

			void forceExists(boolean value) {
				lastExists = value;
				lastVersion = version;
			}

			double p;
			VisualEdge visualEdge;

			public int other(int x) {
				assert (x == from || x == to);
				if (x == from)
					return to;
				return from;
			}

			public double w() {
				return -Math.log(p);
			}
			
			@Override
			public String toString(){
				return from+"->"+to;
			}
		}

		Collection<TPEdge> edges() {
			LinkedHashSet<TPEdge> ret = new LinkedHashSet<TPEdge>();
			for (int i = 0; i < size(); i++)
				for (int j = 0; j < edges[i].length; j++)
					ret.add(edges[i][j]);
			return ret;
		}

		VisualNode[] visualNodes;
		TPEdge[][] edges;

		public int size() {
			return visualNodes.length;
		}

		public TPGraph(SimpleVisualGraph sg) {
			visualNodes = new VisualNode[sg.n];
			for (int i = 0; i < sg.n; i++)
				visualNodes[i] = sg.getVisualNode(i);

			edges = new TPEdge[sg.n][];
			HashMap<OrderedPair<Integer>, TPEdge> emap = new HashMap<OrderedPair<Integer>, TPEdge>();

			int eid = 0;
			for (int i = 0; i < sg.n; i++) {
				edges[i] = new TPEdge[sg.getEdges(i).size()];
				int j = 0;
				for (SimpleEdge se : sg.getEdges(i)) {
					OrderedPair<Integer> p = new OrderedPair<Integer>(i, se.to);
					TPEdge te = emap.get(p);
					if (te == null) {
						te = new TPEdge();
						te.from = i;
						te.id = eid++;
						te.to = se.to;
						te.visualEdge = se.visualEdge;
						te.p = se.weight;
						te.lastExists = true;
						emap.put(p, te);
					}
					edges[i][j] = te;
					j++;
				}
			}
		}

		public void undecide() {
			version++;
		}

		/**
		 * 
		 * @param paths
		 *            paths to take in account.
		 * @param query
		 *            query nodes.
		 * @param cutSampler
		 *            whether we force edges to fail instead of monte-carloing
		 *            them until this happens. Will save some time.
		 * @return first path that was not cut.
		 */
		public Path resetEdges(Collection<Path> paths,
				Collection<Integer> query, boolean cutSampler) {

			if (!cutSampler) {
				boolean retry = false;
				Path ret;
				do {
					ret = null;
					undecide();

					for (Path p : paths) {
						boolean exists = p.isTrue();
						if (exists && p.getNodes().containsAll(query)) {
							retry = true;
							break;
						} else if (exists) {
							ret = p;
						}
					}
				} while (retry);
				return ret;
			} else {
				// DA CUT SAMPLAR
				undecide();

				// f contains finished and existing paths
				ArrayList<Path> f = new ArrayList<Path>();
				
				//list of unfinished yet true paths
				ArrayList<Path> unfinished =  new ArrayList<Path>();
				for (Path p : paths) {
					// only add finished paths to f
					boolean exists = p.isTrue();
					if (exists && p.getNodes().containsAll(query)) {
						f.add(p);
					}else if(exists){
						unfinished.add(p);
					}
				}

				while (!f.isEmpty()) {
					HashSet<TPEdge> ae = new HashSet<TPEdge>();
					for (Path p : f)
						ae.addAll(p);

					for (TPEdge e : ae)
						e.truePaths = new HashSet<Path>();

					for (Path p : f)
						for (TPEdge e : p)
							e.truePaths.add(p);

					double maxScore = 0;
					TPEdge bestCut = null;

					for (TPEdge e : ae) {
						double sc = e.truePaths.size() + (1 - e.p);
						if (sc > maxScore) {
							maxScore = sc;
							bestCut = e;
						}
					}
					bestCut.forceExists(false);
					f.removeAll(bestCut.truePaths);
				}
				for (Path p : unfinished){
					// only add finished paths to f
					if (p.isTrue()){
						if(query.size()==2){
                            Logging.debug("twophase", p.toString());
                            Logging.debug("twophase", p.getNodes().toString());
                            Logging.debug("twophase", query.toString());
                            Logging.debug("twophase", "" + p.getNodes().containsAll(query));
                            Logging.debug("twophase", "paths = "+paths);
							assert false;
						}
						return p;
					}
				}
			}
			return null;
		}
	}

	private static class PQNode implements Comparable<PQNode> {
		int node;
		double w;
		TPEdge lastEdge;

		public int compareTo(PQNode o) {
			return Double.compare(w, o.w);
		}
	}
	private static void sortPath(Path p){
		//sort path by increasing probability, so that existance testing
		//is faster
		Collections.sort(p, new Comparator<TPEdge>() {
			public int compare(TPEdge a,TPEdge b){
				return Double.compare(a.p,b.p);
			}
		});
	}

	private static Path getBestPath(TPGraph tg, int from, Collection<Integer> to) {

		// little dijkstra here
		PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
		TPEdge[] lastEdge = new TPEdge[tg.size()];
		boolean[] visited = new boolean[tg.size()];
		Arrays.fill(visited, false);
		PQNode start = new PQNode();
		start.node = from;
		start.w = 0;
		start.lastEdge = null;
		pq.add(start);

		while (!pq.isEmpty()) {
			PQNode p = pq.poll();
			int n = p.node;
			if (visited[n])
				continue;
			
			visited[n] = true;
			lastEdge[n] = p.lastEdge;
			if (to.contains(p.node)) {
				int t = p.node;
				// backtrack and get the path
				Path ret = new Path();
				while (t != from) {
					ret.add(lastEdge[t]);
					t = lastEdge[t].other(t);
				}
				Collections.reverse(ret);
				//sortPath(ret);
				return ret;
			}
			
			// System.out.println(Arrays.asList(tg.edges[p.node]));
			for (TPEdge e : tg.edges[p.node]) {
				int o = e.other(n);
				if (visited[o])
					continue;
				if (!e.exists())
					continue;

				PQNode nn = new PQNode();
				nn.node = o;
				nn.w = p.w + e.w();
				nn.lastEdge = e;
				pq.add(nn);
			}
		}
		return null;
	}

	public static class Path extends ArrayList<TPEdge> {
		public boolean isTrue() {
			for (TPEdge t : this) {
				if (t.exists() == false)
					return false;
			}
			return true;
		}

		public Collection<Integer> getNodes() {
			HashSet<Integer> ret = new HashSet<Integer>();
			for(TPEdge t:this){
				ret.add(t.from);
				ret.add(t.to);
			}
			return ret;
		}
	}

	public static List<Path> samplePaths(TPGraph tg,
			Collection<Integer> endPoints, int n) {

		long version = 0;

		ArrayList<Path> ret = new ArrayList<Path>();

		for (TPEdge e : tg.edges())
			e.forceExists(true);

		// first add all best paths
		for (int a : endPoints) {
			for (int b : endPoints) {
				if (b <= a)
					continue;
				Path p = getBestPath(tg, a, Collections.singleton(b));
				ret.add(p);
			}
		}
		
		//continue either adding new paths
		//or extending existing paths

		int fails = 0;

		Random rand = new Random();
		int pathCount=0;
		
		while(pathCount<n){
			
			ArrayList<Integer> epArr = new ArrayList<Integer>(endPoints);
			
			Path extended = tg.resetEdges(ret,endPoints,true);
			
			if(extended==null){
				//select randomly start and end
				int u=0,v=0;
				while(u==v){
					u = epArr.get(rand.nextInt(endPoints.size()));
					v = epArr.get(rand.nextInt(endPoints.size()));
				}
				Path p = getBestPath(tg, u,Collections.singleton(v));
				if (p == null) {
					fails++;
					if (fails >= 300)
						break;
					continue;
				}
				assert(p.getNodes().contains(u));
				assert(p.getNodes().contains(v));
				ret.add(p);
				extended = p ;
			}else{
				assert(endPoints.size()>2);
				epArr.removeAll(extended);
				int r = rand.nextInt(epArr.size());
				int u = epArr.get(r);
				Path p = getBestPath(tg, u,extended.getNodes());
				if(p==null){
					continue;
				}
				extended.addAll(p);
			}
			
			fails = 0;
			if(extended.getNodes().containsAll(endPoints))
				pathCount++;
			// System.out.println(p);
		}
		ArrayList<Path> ret2 = new ArrayList<Path>();
		for(Path p:ret)
			if(p.getNodes().containsAll(endPoints))
				ret2.add(p);
		Logging.debug("twophase", "r1.size() = "+ret.size());
		Logging.debug("twophase", "r2.size() = "+ret2.size());
		return ret2;
	}

	public static Collection<Path> selectPaths(TPGraph tg,
			Collection<Integer> endPoints, Collection<Path> pathsC, int rounds,
			int edges) {

		HashSet<Path> ret = new HashSet<Path>();
		ArrayList<Path> paths = new ArrayList<Path>(pathsC);
		HashSet<TPEdge> es = new HashSet<TPEdge>();
		for (Path p : paths)
			es.addAll(p);
		Logging.debug("twophase", es.size() + "/" + tg.edges().size());

		boolean[][] sampleMatrix = new boolean[rounds][paths.size()];

		LinkedHashSet<Integer> activePaths = new LinkedHashSet<Integer>();

		for (int i = 0; i < paths.size(); i++)
			activePaths.add(i);

		Logging.debug("twophase", "path count: " + paths.size());
		HashSet<TPEdge> taken = new HashSet<TPEdge>();
		do {
			for (int i = 0; i < rounds; i++) {
				tg.undecide();
				for (int j = 0; j < paths.size(); j++) {
					Path p = paths.get(j);
					sampleMatrix[i][j] = p.isTrue();
					j++;
				}
			}
			LinkedList<Integer> activeRounds = new LinkedList<Integer>();
			for (int i = 0; i < rounds; i++)
				activeRounds.add(i);
			while (!activeRounds.isEmpty()) {
				double bestScore = -1;
				int bestPath = 0;
				int bestLen = -1;
				double prob = 0;
				for (int i : activePaths) {
					// count how many new edges we must add
					int len = 0;
					for (TPEdge t : paths.get(i)) {
						if (!taken.contains(t))
							len++;
					}
					// if it is free, we might as well take it now.
					if (len == 0) {
						bestPath = i;
						bestLen = 0;
						break;
					}

					if (len > edges)
						continue;

					int count = 0;
					for (int j : activeRounds) {
						if (sampleMatrix[j][i]) {
							count++;
						}
					}
					double score = count / (double) len;

					if (score > bestScore) {
						prob += (double) count / (double) rounds;
						bestScore = score;
						bestPath = i;
						bestLen = len;
					}
				}
				Logging.debug("twophase", "prob increase " + prob);

				if (bestLen < 0) {
					break;
				}

				ret.add(paths.get(bestPath));

				taken.addAll(paths.get(bestPath));
				activePaths.remove(bestPath);

				edges -= bestLen;
				Logging.debug("twophase", "e=" + edges + " len=" + bestLen + " score="
						+ bestScore);

				// remove rounds that contain true for selected path
				Iterator<Integer> it = activeRounds.iterator();
				while (it.hasNext()) {
					int j = it.next();
					if (sampleMatrix[j][bestPath]) {
						// remove this round from activeRounds
						it.remove();
					}
				}
			}
		} while (edges > 0 && activePaths.size() != 0);

		return ret;
	}

	ArrayList<Path> pathsInOrder;
	VisualGraph vg;

	public TwoPhase(VisualGraph g) {
		vg = g;
		SimpleVisualGraph sg = new SimpleVisualGraph(g);
		HashSet<Integer> endPoints = new HashSet<Integer>();
		for (Entry<VisualNode, Double> ent : g.getNodesOfInterest().entrySet()) {
			if (ent.getValue() > 0)// only see positive querynodes
				endPoints.add(sg.getInt(ent.getKey()));

		}
		long t = System.currentTimeMillis();
		TPGraph tg = new TPGraph(sg);
		Collection<Path> sampled = samplePaths(tg, endPoints, (int)(4.0/endPoints.size() * sg
				.getEdgeCount()));
		Collection<Path> result = selectPaths(tg, endPoints, sampled, 1000,
				10 + sg.getEdgeCount());

		pathsInOrder = new ArrayList<Path>(result);
		t = System.currentTimeMillis()-t;
	    Logging.info("graph_operation", "TwoPhase took "+t+"ms");

	}

	public void doHiding(int targetEdges) {
		
		Collection<VisualEdge> currentHiddenEdges = vg.getHiddenEdges();
		
		HashSet<VisualNode> hiddenN = new HashSet<VisualNode>();
		hiddenN.addAll(vg.getAllNodes());
		HashSet<VisualEdge> hiddenE = new HashSet<VisualEdge>();
		hiddenE.addAll(vg.getAllEdges());
		int edgesAdded = 0;

		for (Path p : pathsInOrder) {
			LinkedHashSet<VisualNode> nn = new LinkedHashSet<VisualNode>();
			LinkedHashSet<VisualEdge> ne = new LinkedHashSet<VisualEdge>();
			for (TPEdge e : p) {
				if (hiddenE.contains(e.visualEdge) == false)
					continue;
				if (edgesAdded >= targetEdges)
					break;
				nn.addAll(e.visualEdge.getFrom().getAncestors());
				nn.addAll(e.visualEdge.getTo().getAncestors());
				ne.add(e.visualEdge);
				edgesAdded++;
			}
			if (edgesAdded >= targetEdges)
				break;
			hiddenN.removeAll(nn);
			hiddenE.removeAll(ne);
		}

		//only update if hidden edges have changed
		if(!hiddenE.equals(currentHiddenEdges)){
			vg.setHiddenEdges(hiddenE);
			vg.setHiddenNodes(hiddenN);
		}

	}

}