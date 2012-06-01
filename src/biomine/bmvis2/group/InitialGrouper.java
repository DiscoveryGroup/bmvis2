package biomine.bmvis2.group;

import biomine.bmvis2.VisualGroupNode;

public class InitialGrouper extends Grouper {
	public static String NAME = "Initial grouper";

	@Override
	public String makeGroups(VisualGroupNode n) {
		long startedAt = System.currentTimeMillis();
		
		PathGrouper path = new PathGrouper();
		path.makeGroups(n);
		setProgress(0.2);
		
		ParallelGrouper par = new ParallelGrouper();
		par.makeGroups(n);
		
		setProgress(0.5);
		LabelPropagationCommunity lpc = new  LabelPropagationCommunity();
		lpc.setSubdivide(true);
		lpc.makeGroups(n);
		setProgress(1.0);
		System.err.println("Label propagation grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		
		System.err.println("Grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		return null;
	}
}
