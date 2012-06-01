package biomine.bmvis2.crawling;

import java.util.Collection;
import java.util.HashSet;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;

/**
 * Set of node-ids used for web queries.
 * @author alhartik
 */
public class CrawlQuery extends HashSet<String>{
	public CrawlQuery(){
		
	}
	public CrawlQuery(Collection<String> codes){
		super(codes);
	}
	public void addVisualNode(VisualNode n){
		BMNode bmn = n.getBMNode();
		addBMNode(bmn);
		
	}
	public void addBMNode(BMNode bmn) {
		add(bmn.getId());
	}
	public void removeVisualNode(VisualNode n){
		BMNode bmn = n.getBMNode();
		removeBMNode(bmn);
	}
	public void removeBMNode(BMNode bmn) {
		remove(bmn.getId());
	}
}
