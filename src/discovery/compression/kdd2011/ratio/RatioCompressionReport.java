package discovery.compression.kdd2011.ratio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import biomine.bmvis2.VisualGraph.GraphReadingException;
import biomine.bmvis2.algorithms.KMedoids;
import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.algorithms.KMedoids.KMedoidsResult;
import biomine.bmvis2.algoutils.DefaultGraph;
import discovery.compression.kdd2011.ratio.RatioCompression.ResultGraph;


/**
 * Commandline tool for compression testing. Used for new compression (in
 * compression.ratio package) with compression ratio controlled compression.
 * 
 * @author alhartik
 * 
 */
public class RatioCompressionReport {
	
	public static final String PROGRAM_NAME="graph-compression-report";
	
	enum ConnectivityType{
		GLOBAL,LOCAL
	}
	
	static Options opts = new Options();
	
	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(PROGRAM_NAME+" [options] input-bmg-file",
				opts);
		System.exit(0);
	}

	public static void main(String[] args) throws GraphReadingException, IOException, java.text.ParseException {
		opts.addOption("r", true, "Goal compression ratio");
		
//		opts.addOption( "a",
//		 true,
//		 "Algorithm used for compression. The default and only currently available option is \"greedy\"");
		//opts.addOption("cost-output",true,"Output file for costs, default is costs.txt");
		//opts.addOption("cost-format",true,"Output format for ");
		
		opts.addOption("ctype",true,"Connectivity type: global or local, default is global.");
		opts.addOption("connectivity",false,"enables output for connectivity. Connectivity info will be written to connectivity.txt");
		opts.addOption("output_bmg",true,"Write bmg file with groups to given file.");
		opts.addOption("algorithm",true,"Algorithm to use, one of: greedy random1 random2 bruteforce slowgreedy");
		opts.addOption("hop2",false,"Only try to merge nodes that have common neighbors");
		opts.addOption("kmedoids",false,"Enables output for kmedoids clustering");
		opts.addOption("kmedoids_k",true,"Number of clusters to be used in kmedoids. Default is 3");
		opts.addOption("kmedoids_output",true,"Output file for kmedoid clusters. Default is clusters.txt. This file will be overwritten.");
		opts.addOption("norefresh",false,"Use old style merging: all connectivities are not refreshed when merging");
		opts.addOption("edge_attribute",true,"Attribute from bmgraph used as edge weight");
		opts.addOption("only_times",false,"Only write times.txt");
		//opts.addOption("no_metrics",false,"Exit after compression, don't calculate any metrics or produce output bmg for the compression.");
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		

		
        boolean connectivity=false;
		double ratio = 0;
    
		boolean hop2 = cmd.hasOption("hop2");
		
		RatioCompression compression = new GreedyRatioCompression(hop2);

        if(cmd.hasOption("connectivity"))
            connectivity=true;

		ConnectivityType ctype = ConnectivityType.GLOBAL;
		CompressionMergeModel mergeModel = new PathAverageMergeModel();
		if(cmd.hasOption("ctype")){
			String ctypeStr=cmd.getOptionValue("ctype");
			if(ctypeStr.equals("local")){
				ctype=ConnectivityType.LOCAL;
				mergeModel = new EdgeAverageMergeModel();
			}else if(ctypeStr.equals("global")){
				ctype=ConnectivityType.GLOBAL;
				mergeModel = new PathAverageMergeModel();
			}else{
				System.out.println(PROGRAM_NAME+": unknown connectivity type "+ctypeStr);
				printHelp();
			}
		}
		
		if(cmd.hasOption("norefresh"))
			mergeModel = new PathAverageMergeModelNorefresh();
        if(cmd.hasOption("algorithm")){
        	String alg=cmd.getOptionValue("algorithm");
        	if(alg.equals("greedy")){
        		compression = new GreedyRatioCompression(hop2);
        	}else if(alg.equals("random1")){
        		compression = new RandomRatioCompression(hop2);
        	}else if(alg.equals("random2")){
        		compression = new SmartRandomRatioCompression(hop2);
        	}else if(alg.equals("bruteforce")){
        		compression = new BruteForceCompression(hop2,ctype==ConnectivityType.LOCAL);
        	}else if(alg.equals("slowgreedy")){
        		compression = new SlowGreedyRatioCompression(hop2);
        	}else{
        		System.out.println("algorithm must be one of: greedy random1 random2 bruteforce slowgreedy");
        		printHelp();
        	}
        }
        
		compression.setMergeModel(mergeModel);

        
		if (cmd.hasOption("r")) {
			ratio = Double.parseDouble(cmd.getOptionValue("r"));
		}else {
			System.out.println(PROGRAM_NAME+": compression ratio not defined");
			printHelp();
		}
		
		if (cmd.hasOption("help")) {
			printHelp();
		}
		

		String infile = null;
		if (cmd.getArgs().length != 0) {
			infile = cmd.getArgs()[0];
		} else {
			printHelp();
		}
		
		boolean kmedoids=false;
		int kmedoidsK=3;
		String kmedoidsOutput="clusters.txt";
		if(cmd.hasOption("kmedoids"))
			kmedoids=true;
		if(cmd.hasOption("kmedoids_k"))
			kmedoidsK=Integer.parseInt(cmd.getOptionValue("kmedoids_k"));
		if(cmd.hasOption("kmedoids_output"))
			kmedoidsOutput=cmd.getOptionValue("kmedoids_output");

		String edgeAttrib = "goodness";
		if(cmd.hasOption("edge_attribute"))
			edgeAttrib = cmd.getOptionValue("edge_attribute");
		
		// This program should directly use bmgraph-java to read and
		// DefaultGraph should have a constructor that takes a BMGraph as an
		// argument.
		
		
		//VisualGraph vg = new VisualGraph(infile, edgeAttrib, false);
		//System.out.println("vg read");
		//SimpleVisualGraph origSG = new SimpleVisualGraph(vg);
		BMGraph bmg = BMGraphUtils.readBMGraph(infile);
		
		int origN = bmg.getNodes().size();
		
		//for(int i=0;i<origN;i++)
			//System.out.println(i+"="+origSG.getVisualNode(i));
		System.out.println("bmgraph read");
		
		BMNode[] i2n = new BMNode[origN];
		HashMap<BMNode,Integer> n2i = new HashMap<BMNode,Integer>();
		{
			int pi = 0;
			for(BMNode nod:bmg.getNodes()){
				n2i.put(nod,pi);
				i2n[pi++]=nod;
			}
		}
		
		DefaultGraph dg = new DefaultGraph();
		for(BMEdge e:bmg.getEdges()){
			dg.addEdge(n2i.get(e.getSource()),n2i.get(e.getTarget()),
					Double.parseDouble(e.get(edgeAttrib))
					);
		}
		
		
		
		DefaultGraph origDG = dg.copy();
		
		System.out.println("inputs read");
		RatioCompression nopCompressor = new RatioCompression.DefaultRatioCompression();
		ResultGraph nopResult = nopCompressor.compressGraph(dg,1);

		long start = System.currentTimeMillis();
		ResultGraph result = compression.compressGraph(dg, ratio);
		long timeSpent=System.currentTimeMillis()-start;
		double seconds = timeSpent*0.001;
		
		BufferedWriter timesWriter  = new BufferedWriter(new FileWriter("times.txt",true));
		timesWriter.append(""+seconds+"\n");
		timesWriter.close();

		if (cmd.hasOption("only_times")){
			System.out.println("Compression done, exiting.");
			System.exit(0);
		}
		
		BufferedWriter costsWriter  = new BufferedWriter(new FileWriter("costs.txt",true));
		costsWriter.append(""+nopResult.getCompressorCosts()+" "+result.getCompressorCosts()+"\n");
		costsWriter.close();
		
		double[][] origProb;
		double[][] compProb; 
		int[] group = new int[origN];
		
		for(int i=0;i<result.partition.size();i++)
			for(int x:result.partition.get(i))
				group[x]=i;
		
		if(ctype==ConnectivityType.LOCAL){
			origProb = new double[origN][origN];
			compProb = new double[origN][origN];
			DefaultGraph g = result.uncompressedGraph();
			for(int i=0;i<origN;i++){
				for(int j=0;j<origN;j++){
					origProb[i][j]=dg.getEdgeWeight(i,j);
					compProb[i][j]=g.getEdgeWeight(i,j);
				}
			}
			System.out.println("Writing edge-dissimilarity");
		}else{
			
			origProb = ProbDijkstra.getProbMatrix(origDG);
			
			compProb = new double[origN][origN];
			
			System.out.println("nodeCount = "+result.graph.getNodeCount());
			double[][] ccProb = ProbDijkstra.getProbMatrix(result.graph);
			System.out.println("ccProb.length = "+ccProb.length);
			
			System.out.println("ccProb[0].length = "+ccProb[0].length);
			
		
			
			for(int i=0;i<origN;i++){
				for(int j=0;j<origN;j++){
					if(group[i]==group[j])
						compProb[i][j] = result.graph.getEdgeWeight(group[i], group[j]);
					else{
						int gj = group[j];
						int gi = group[i];
						compProb[i][j]=ccProb[group[i]][group[j]];
					}
				}
			}
			
			System.out.println("Writing best-path-dissimilarity");
			//compProb = ProbDijkstra.getProbMatrix(result.uncompressedGraph());
			
		}
		
		{
            BufferedWriter connWr =null;//
            
	        if(connectivity){
	        	connWr= new BufferedWriter(new FileWriter("connectivity.txt",true));
	        }
            double totalDiff=0;
            
            for(int i=0;i<origN;i++){
                for(int j=i+1;j<origN;j++){
                	
                    double diff = Math.abs(origProb[i][j]-compProb[i][j]);
                    //VisualNode ni = origSG.getVisualNode(i);
                    //VisualNode nj = origSG.getVisualNode(j);
                    BMNode ni = i2n[i];
                    BMNode nj = i2n[j];
                    if(connectivity)
	                    connWr.append(ni+"\t"+nj+"\t"+origProb[i][j]+"\t"+compProb[i][j]+"\t"+diff+"\n");
                    totalDiff+=diff*diff;
                }
            }

            if(connectivity){
	            connWr.append("\n");
	            connWr.close();
            }
            
            totalDiff=Math.sqrt(totalDiff);
            BufferedWriter dissWr =new BufferedWriter(new FileWriter("dissimilarity.txt",true));
            dissWr.append(""+totalDiff+"\n");
            dissWr.close();
        }
        
        if(cmd.hasOption("output_bmg")){
        	BMGraph outgraph=new BMGraph();
        	
        	String outputfile = cmd.getOptionValue("output_bmg");
        	HashMap<Integer,BMNode> nodes  = new HashMap<Integer, BMNode>();
        	
        	for(int i=0;i<result.partition.size();i++){
        		ArrayList<Integer> g = result.partition.get(i);
        		if(g.size()==0)continue;
        		BMNode node = new BMNode("Supernode_"+i);
        		HashMap<String,String> attributes = new HashMap<String, String>();
        		StringBuffer contents = new StringBuffer();
        		for(int x:g)
        			contents.append(i2n[x]+",");
        		contents.delete(contents.length()-1, contents.length());
        		
        		attributes.put("nodes",contents.toString());
        		attributes.put("self-edge",""+ result.graph.getEdgeWeight(i, i));
        		node.setAttributes(attributes);
        		nodes.put(i,node);
        		outgraph.ensureHasNode(node);
        	}
        	
        	for(int i=0;i<result.partition.size();i++){
        		if(result.partition.get(i).size()==0)continue;
        		for(int x:result.graph.getNeighbors(i)){
        			if(x<i)continue;
        			BMNode from = nodes.get(i);
        			BMNode to = nodes.get(x);
        			if(from==null || to==null){
        				System.out.println(from+"->"+to);
        				System.out.println(i+"->"+x);
        				System.out.println("");
        			}
        			BMEdge e = new BMEdge(nodes.get(i), nodes.get(x), "notype");
        			
        			e.setAttributes(new HashMap<String, String>());
        			e.put("goodness", ""+result.graph.getEdgeWeight(i,x));
        			outgraph.ensureHasEdge(e);
        		}
        	}
        	BMGraphUtils.writeBMGraph(outgraph, outputfile);
        }
        
        // k medoids!
        if(kmedoids){
        	//KMedoidsResult clustersOrig=KMedoids.runKMedoids(origProb,kmedoidsK);
        	
        	
        	if(ctype==ConnectivityType.LOCAL){
				compProb = ProbDijkstra.getProbMatrix(result.uncompressedGraph());
        	}
		
        	//KMedoidsResult compClusters = KMedoids.runKMedoids(ProbDijkstra.getProbMatrix(result.graph),kmedoidsK);
        	KMedoidsResult clustersComp=KMedoids.runKMedoids(compProb,kmedoidsK);
        	
        	BufferedWriter bw=new BufferedWriter(new FileWriter(kmedoidsOutput));
        	
        	for(int i=0;i<origN;i++){
        		int g = group[i];
        		//bw.append(origSG.getVisualNode(i).getBMNode()+" "+compClusters.clusters[g]+"\n");
        		bw.append(i2n[i]+" "+clustersComp.clusters[i]+"\n");
        	}
        	bw.close();
        }
        
	
		
		System.exit(0);
	}
}
