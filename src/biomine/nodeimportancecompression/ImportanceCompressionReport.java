package biomine.nodeimportancecompression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmgraph.write.BMGraphWriter;

public class ImportanceCompressionReport {

	public static final String PROGRAM_NAME = "importance-compression";
	static Options opts = new Options();

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(PROGRAM_NAME + " [options] input-bmg-file", opts);
		System.exit(0);
	}
	
	public static Map<String,Double> readMap(BufferedReader read) throws IOException{
		ArrayList<String> lines = new ArrayList<String>();

		String line;
		while ((line = read.readLine()) != null) {
			lines.add(line);
		}

		HashMap<String,Double> ret = new HashMap<String, Double>();
		for (int i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			String[] s = line.split("[,\\s]+");
			String id = null;
			double d = 1;
			if (s.length == 0) {
			} else if (s.length == 1) {
				id = s[0];
			} else if (s.length == 2) {
				id = s[0];
				d = Double.parseDouble(s[1]);
			} else if (s.length > 2) {
				System.out
						.println("Error parsing queryfile: Too much whitespace:"
								+ line);
				System.exit(1);
			}
			if (id != null && id.length() == 0)
				id = null;

			if(id!=null)
				ret.put(id, d);
		}
		return ret;
		
	}

	public static void main(String[] args) throws IOException,
			java.text.ParseException {
		opts
				.addOption("algorithm", true,
						"Used algorithm for compression. Possible values are 'brute-force', " +
						"'brute-force-edges','brute-force-merges','randomized','randomized-merges',"+
                        "'randomized-edges',"+
                        "'fast-brute-force',"+
                        "'fast-brute-force-merges','fast-brute-force-merge-edges'. Default is 'brute-force'.");
		opts.addOption("query", true, "Query nodes ids, separated by comma.");
		opts.addOption("queryfile", true, "Read query nodes from file.");
		opts.addOption("ratio", true, "Goal ratio");
		opts.addOption("importancefile",true,"Read importances straight from file");
		opts.addOption("keepedges",false,"Don't remove edges during merges");
		opts.addOption("connectivity",false,"Compute and output connectivities in edge oriented case");
		opts.addOption("paths",false,"Do path oriented compression");
		opts.addOption("edges",false,"Do edge oriented compression");
		// opts.addOption( "a",

		double sigma = 1.0;
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}

		String queryStr = cmd.getOptionValue("query");
		String[] queryNodeIDs = {};
		double[] queryNodeIMP = {};
		if (queryStr != null) {
			queryNodeIDs = queryStr.split(",");
			queryNodeIMP = new double[queryNodeIDs.length];
			for (int i = 0; i < queryNodeIDs.length; i++) {
				String s = queryNodeIDs[i];
				String[] es = s.split("=");
				queryNodeIMP[i] = 1;
				if (es.length == 2) {
					queryNodeIDs[i] = es[0];
					queryNodeIMP[i] = Double.parseDouble(es[1]);
				} else if (es.length > 2) {
					System.out
							.println("Too many '=' in querynode specification: "
									+ s);
				}
			}
		}

		String queryFile = cmd.getOptionValue("queryfile");
		Map<String,Double> queryNodes = Collections.EMPTY_MAP;
		if (queryFile != null) {
			File in = new File(queryFile);
			BufferedReader read = new BufferedReader(new FileReader(in));

			queryNodes = readMap(read);
			read.close();
		}
		
		String impfile = cmd.getOptionValue("importancefile");
		Map<String,Double> importances = null;
		if(impfile!=null){
			File in = new File(impfile);
			BufferedReader read = new BufferedReader(new FileReader(in));

			importances = readMap(read);
			read.close();
		}
		
		
		String algoStr = cmd.getOptionValue("algorithm");
		CompressionAlgorithm algo = null;

		if (algoStr == null || algoStr.equals("brute-force")) {
			algo = new BruteForceCompression();
		} else if(algoStr.equals("brute-force-edges")){
			algo = new BruteForceCompressionOnlyEdges();
		} else if(algoStr.equals("brute-force-merges")){
			algo = new BruteForceCompressionOnlyMerges();
		} else if(algoStr.equals("fast-brute-force-merges")){
			//algo = new FastBruteForceCompressionOnlyMerges();
            algo = new FastBruteForceCompression(true,false);
		} else if(algoStr.equals("fast-brute-force-edges")){
            algo = new FastBruteForceCompression(false,true);
            //algo = new FastBruteForceCompressionOnlyEdges();
		} else if(algoStr.equals("fast-brute-force")){
			algo = new FastBruteForceCompression(true,true);
		} else if(algoStr.equals("randomized-edges")){
			algo = new RandomizedCompressionOnlyEdges();  //modified
		} else if(algoStr.equals("randomized")){
			algo = new RandomizedCompression();
		} else if(algoStr.equals("randomized-merges")){
			algo = new RandomizedCompressionOnlyMerges();
		} else {
			System.out.println("Unsupported algorithm: " + algoStr);
			printHelp();
		}

		String ratioStr = cmd.getOptionValue("ratio");
		double ratio = 0;
		if (ratioStr != null) {
			ratio = Double.parseDouble(ratioStr);
		} else {
			System.out.println("Goal ratio not specified");
			printHelp();
		}

		String infile = null;
		if (cmd.getArgs().length != 0) {
			infile = cmd.getArgs()[0];
		} else {
			printHelp();
		}

		BMGraph bmg = BMGraphUtils.readBMGraph(new File(infile));
		HashMap<BMNode, Double> queryBMNodes = new HashMap<BMNode, Double>();
		for (String id:queryNodes.keySet()) {
			queryBMNodes.put(bmg.getNode(id), queryNodes.get(id));
		}

		long startMillis = System.currentTimeMillis();
		ImportanceGraphWrapper wrap = QueryImportance.queryImportanceGraph(bmg,
				queryBMNodes);
		
		if(importances!=null){
			for(String id:importances.keySet()){
				wrap.setImportance(bmg.getNode(id), importances.get(id));
			}
		}

		ImportanceMerger merger=null;
		if(cmd.hasOption("edges")){
			merger = new ImportanceMergerEdges(wrap.getImportanceGraph());
		}else if(cmd.hasOption("paths")){
			merger = new ImportanceMergerPaths(wrap.getImportanceGraph());
		}else{
			System.out.println("Specify either 'paths' or 'edges'.");
			System.exit(1);
		}
		
		if(cmd.hasOption("keepedges")){
			merger.setKeepEdges(true);
		}
		
		algo.compress(merger, ratio);
		long endMillis = System.currentTimeMillis();

		// write importance

		{
			BufferedWriter wr = new BufferedWriter(new FileWriter(
					"importance.txt", false));
			for (BMNode nod : bmg.getNodes()) {
				wr.write(nod + " " + wrap.getImportance(nod) + "\n");
			}
			wr.close();
		}

                // write sum of all pairs of node importance    added by Fang
     /*   {
            BufferedWriter wr = new BufferedWriter(new FileWriter("sum_of_all_pairs_importance.txt", true));
            ImportanceGraph orig = wrap.getImportanceGraph();
            double sum = 0;

            for (int i = 0; i <= orig.getMaxNodeId(); i++) {
                for (int j = i+1; j <= orig.getMaxNodeId(); j++) {
                    sum = sum+ wrap.getImportance(i)* wrap.getImportance(j);
                }
            }

            wr.write(""+sum);
            wr.write("\n");
            wr.close();
        }

*/

		// write uncompressed edges
		{
			BufferedWriter wr = new BufferedWriter(new FileWriter("edges.txt",
					false));
			ImportanceGraph orig = wrap.getImportanceGraph();
			ImportanceGraph ucom = merger.getUncompressedGraph();
			for (int i = 0; i <= orig.getMaxNodeId(); i++) {
				String iname = wrap.intToNode(i).toString();
				HashSet<Integer> ne = new HashSet<Integer>();
				ne.addAll(orig.getNeighbors(i));
				ne.addAll(ucom.getNeighbors(i));
				for (int j : ne) {
					if (i < j)
						continue;
					String jname = wrap.intToNode(j).toString();
					double a = orig.getEdgeWeight(i, j);
					double b = ucom.getEdgeWeight(i, j);
					wr.write(iname + " " + jname + " " + a + " " + b + " "
							+ Math.abs(a - b));
					wr.write("\n");
				}
			}
			wr.close();
		}
		// write distance
		{
			// BufferedWriter wr = new BufferedWriter(new
			// FileWriter("distance.txt",false));
			BufferedWriter wr = new BufferedWriter(new FileWriter(
					"distance.txt", true));  //modified by Fang

			ImportanceGraph orig = wrap.getImportanceGraph();
			ImportanceGraph ucom = merger.getUncompressedGraph();
			double error = 0;
			for (int i = 0; i <= orig.getMaxNodeId(); i++) {
				HashSet<Integer> ne = new HashSet<Integer>();
				ne.addAll(orig.getNeighbors(i));
				ne.addAll(ucom.getNeighbors(i));
				for (int j : ne) {
					if (i <= j)
						continue;
					double a = orig.getEdgeWeight(i, j);
					double b = ucom.getEdgeWeight(i, j);
					error += (a - b) * (a - b) * wrap.getImportance(i)
							* wrap.getImportance(j);
					// modify by Fang: multiply imp(u)imp(v)

				}
			}
			 error = Math.sqrt(error);
			//////////error = Math.sqrt(error / 2); // modified by Fang: the error of each
											// edge is counted twice
			wr.write("" + error);
			wr.write("\n");
			wr.close();
		}
		// write sizes
		{
			ImportanceGraph orig = wrap.getImportanceGraph();
			ImportanceGraph comp = merger.getCurrentGraph();
			// BufferedWriter wr = new BufferedWriter(new
			// FileWriter("sizes.txt",false));
			BufferedWriter wr = new BufferedWriter(new FileWriter("sizes.txt", true));   //modified by Fang

			wr.write(orig.getNodeCount() + " " + orig.getEdgeCount() + " "
					+ comp.getNodeCount() + " " + comp.getEdgeCount());
			wr.write("\n");
			wr.close();
		}
		//write time
		{
			System.out.println("writing time");
			BufferedWriter wr = new BufferedWriter(new FileWriter("time.txt", true)); //modified by Fang
			double secs = (endMillis-startMillis)*0.001;
			wr.write(""+secs+"\n");
			wr.close();
		}

               //write change of connectivity for edge-oriented case       // added by Fang
              	{
                if(cmd.hasOption("connectivity")){

                   BufferedWriter wr = new BufferedWriter(new FileWriter("connectivity.txt", true));
                   ImportanceGraph orig = wrap.getImportanceGraph();
                   ImportanceGraph ucom = merger.getUncompressedGraph();
             
                   double diff = 0;

                   for (int i = 0; i <= orig.getMaxNodeId(); i++) {
                       ProbDijkstra pdori = new ProbDijkstra(orig,i);
                       ProbDijkstra pducom = new ProbDijkstra(ucom,i);

                       for (int j = i+1; j <= orig.getMaxNodeId(); j++) {
                                double oriconn = pdori.getProbTo(j);
                                double ucomconn = pducom.getProbTo(j);

                               diff = diff + (oriconn - ucomconn)*(oriconn - ucomconn)*wrap.getImportance(i)*wrap.getImportance(j);

                          }
                   }

                   diff = Math.sqrt(diff);
                   wr.write(""+diff);
                   wr.write("\n");
                   wr.close();

                 } 
              }
                

		//write output graph
		{
			BMGraph output = bmg;//new BMGraph(bmg);
			
			int no=0;
			BMNode[] nodes = new BMNode[merger.getGroups().size()];
			for(ArrayList<Integer> gr:merger.getGroups()){
				BMNode bmgroup = new BMNode("Group",""+(no+1));
				bmgroup.setAttributes(new HashMap<String, String>());
				bmgroup.put("autoedges", "0");
				nodes[no]=bmgroup;
				no++;
				if(gr.size()==0)continue;
				for(int x:gr){
					BMNode nod = output.getNode(wrap.intToNode(x).toString());
					BMEdge belongs = new BMEdge(nod,bmgroup,"belongs_to");
					output.ensureHasEdge(belongs);
				}
				output.ensureHasNode(bmgroup);
			}
			for(int i=0;i<nodes.length;i++){
				for(int x:merger.getCurrentGraph().getNeighbors(i)){
					if(x==i){
						nodes[x].put("selfedge",""+merger.getCurrentGraph().getEdgeWeight(i,x));
						//ge.put("goodness", ""+merger.getCurrentGraph().getEdgeWeight(i, x));
						continue;
					}
					BMEdge ge = new BMEdge(nodes[x],nodes[i],"groupedge");
					ge.setAttributes(new HashMap<String,String>());
					ge.put("goodness", ""+merger.getCurrentGraph().getEdgeWeight(i, x));
					output.ensureHasEdge(ge);
				}
			}
			System.out.println(output.getGroupNodes());
			
			BMGraphUtils.writeBMGraph(output, "output.bmg");
		}
	}
}
