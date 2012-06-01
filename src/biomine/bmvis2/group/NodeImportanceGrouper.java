package biomine.bmvis2.group;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.Logging;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleTwoDirEdge;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import biomine.nodeimportancecompression.BruteForceCompression;
import biomine.nodeimportancecompression.BruteForceCompressionOnlyEdges;
import biomine.nodeimportancecompression.BruteForceCompressionOnlyMerges;
import biomine.nodeimportancecompression.CompressionAlgorithm;
import biomine.nodeimportancecompression.FastBruteForceCompression;
import biomine.nodeimportancecompression.ImportanceGraph;
import biomine.nodeimportancecompression.ImportanceMerger;
import biomine.nodeimportancecompression.ImportanceMergerEdges;
import biomine.nodeimportancecompression.ImportanceMergerPaths;
import biomine.nodeimportancecompression.QueryImportance;
import biomine.nodeimportancecompression.RandomizedCompression;
import biomine.nodeimportancecompression.RandomizedCompressionOnlyEdges;
import biomine.nodeimportancecompression.RandomizedCompressionOnlyMerges;

public class NodeImportanceGrouper extends Grouper {

	double ratio = 0.5;
	double oldratio;
	boolean paths = false;
	boolean oldpaths;
	boolean brute = true;
	boolean oldbrute;
	boolean keepedge = true;
	boolean oldkeepedge;
	boolean queryimp= true;
	boolean oldqueryimp;
	boolean merge = true;
	boolean edge = true;
	boolean oldmerge,oldedge;
	
	@Override
	public String makeGroups(VisualGroupNode n) {
		SimpleVisualGraph svg = new SimpleVisualGraph(n.getChildren());
		ImportanceGraph ig = new ImportanceGraph();
		VisualGraph vg = n.getGraph();
		for(SimpleTwoDirEdge se:svg.getAllEdges()){
			ig.addEdge(se.from, se.to, se.weight);
		}
		if(queryimp){
			QueryImportance qi = new QueryImportance();
			HashMap<Integer,Double> qr  = new HashMap<Integer, Double>();
			for(VisualNode vn:n.getChildren()){
				int x = svg.getInt(vn);

				if(vg.getInterestingness(vn)>0){
					qr.put(x,1.0);
				}
				//System.out.println("Q N "+vn.getName()+" "+vg.getInterestingness(vn));
				//System.out.println("selected "+vn.getName()+" "+vn.isSelected());
			}
			QueryImportance.setImportance(ig, qr);
		}else{
			for(VisualNode x:n.getChildren()){
				int xi = svg.getInt(x);
				double imp = -1;
				if(x.getBMNode()!=null){
					try{
						imp = Double.parseDouble(x.getBMNode().get("importance"));
					}catch (Exception e) {
						Logging.error("grouping", "Trying to parse importance:");
						Logging.error("grouping", e.toString());
					}
				}
				if(imp < 0 ){
					Logging.warning("grouping", "Importance not set for "+x+", using importance = 1");
					imp = 1;
				}
				
				ig.setImportance(xi, imp);
			}
		};
		oldratio = ratio;
		oldbrute = brute;
		oldpaths = paths;
		oldkeepedge =  keepedge;
		oldqueryimp = queryimp;
		oldmerge = merge;
		oldedge = edge;
		CompressionAlgorithm ca;
		if(brute){
			if(paths){
				if(merge && edge)
					ca = new BruteForceCompression();
				else if(merge){
					ca = new BruteForceCompressionOnlyMerges();
				}else{
					ca = new BruteForceCompressionOnlyEdges();
				}
			}else{
				ca = new FastBruteForceCompression(merge,edge);
			}
		}else{
			if(merge && edge)
				ca = new RandomizedCompression();
			else if(merge){
				ca = new RandomizedCompressionOnlyMerges();
			}else{
				ca = new RandomizedCompressionOnlyEdges();
			}
		}
		//System.out.println("Using "+ca.getClass());
		ImportanceMerger im;
		if(paths){
			im = new ImportanceMergerPaths(ig);
		}else{
			im = new ImportanceMergerEdges(ig);
		}
		im.setKeepEdges(keepedge);
		ca.compress(im, ratio);
		VisualNode[] gns  = new VisualNode[im.getGroups().size()];
		int ix = -1;
		for (ArrayList<Integer> a : im.getGroups()) {
			ix++;
			//if (a.size()==1)
			//	gns[ix] = svg.getVisualNode(a.get(0));
            if (a.size() < 1) continue;
            VisualGroupNode ng = new VisualGroupNode(n);
            for (int i : a) {
                	svg.getVisualNode(i).setParent(ng);
            }
            gns[ix]=ng;
        }
		
		for(ImportanceGraph.Edge e:im.getCurrentGraph().getEdges()){
			VisualNode from = gns[e.from];
			VisualNode to = gns[e.to];
			if(from==null || to ==null)continue;
			if(from instanceof VisualGroupNode){
				((VisualGroupNode) from).addConnection(to, e.weight);
			}
			if(to instanceof VisualGroupNode){
				((VisualGroupNode) to).addConnection(from, e.weight);
			}
		}
		for(VisualNode nx:gns){
			if(nx !=null && nx instanceof VisualGroupNode)
				((VisualGroupNode) nx).createEdges();
		}
		
		return "";
	}
	boolean changed(){
		return queryimp || ratio != oldratio ||
			brute != oldbrute ||
			paths != oldpaths || 
			keepedge != oldkeepedge ||
			queryimp != oldqueryimp ||
			edge != oldedge ||
			merge != oldmerge;
			
	}
	public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, final VisualGroupNode gn) {
		final JPanel ret = new JPanel();
		final JRadioButton edge = new JRadioButton("Edge-oriented");
		final JRadioButton path = new JRadioButton("Path-oriented");
		ButtonGroup bg = new ButtonGroup();
		bg.add(edge);
		bg.add(path);

		final JButton recompute = new JButton();

		edge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(edge.isSelected())
					paths = false;
			//	recompute.setEnabled(changed());
			}
		});
		path.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(path.isSelected())
					paths = true;
			//	recompute.setEnabled(changed());
			}
		});
		edge.setSelected(!paths);
		path.setSelected(paths);

		final JCheckBox keepe = new JCheckBox("Keep edges",keepedge);
		keepe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				keepedge = keepe.isSelected();
			//	recompute.setEnabled(changed());
			}
		});
		
		final JRadioButton attr = new JRadioButton("BMGraph");
		final JRadioButton qry = new JRadioButton("Query");
		bg = new ButtonGroup();
		bg.add(attr);
		bg.add(qry);
		
		qry.setSelected(queryimp);
		attr.setSelected(!queryimp);
		qry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(qry.isSelected())
					queryimp = true;
				recompute.setEnabled(changed());
			}
		});
		attr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(attr.isSelected())
					queryimp = false;
				recompute.setEnabled((changed()));
			}
		});
		
		final JRadioButton bf = new JRadioButton("Brute force");
		final JRadioButton rand = new  JRadioButton("Random");
		bg = new ButtonGroup();
		bg.add(bf);
		bg.add(rand);
		bf.setSelected(brute);
		rand.setSelected(!brute);
		bf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(bf.isSelected())
					brute = true;
				recompute.setEnabled(changed());
			}
		});
		
		rand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(rand.isSelected())
					brute = false;
				recompute.setEnabled(changed());
			}
		});
		
		final JCheckBox merges = new JCheckBox("Merge");
		final JCheckBox edges = new JCheckBox("Edge deletion"); 
		merges.setSelected(merge);
		edges.setSelected(this.edge);
		merges.setEnabled(this.edge);
		edges.setEnabled(merge);
		merges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				merge = merges.isSelected();
				edges.setEnabled(merge);
				recompute.setEnabled(changed());
			}
		});
		edges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				NodeImportanceGrouper.this.edge = edges.isSelected();
				merges.setEnabled(NodeImportanceGrouper.this.edge);
				recompute.setEnabled(changed());
			}
		});
		
		final JSpinner rati = new  JSpinner(new SpinnerNumberModel(ratio,0,1, 0.05));
		rati.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				ratio = (Double)rati.getValue();
				recompute.setEnabled(changed());
			}
		});
		
        recompute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	changeCallback.settingsChanged(true);
            }
        });
        recompute.setText("Recompute");
        recompute.setEnabled(changed());

    	GridBagLayout lay = new GridBagLayout();
		ret.setLayout(lay);
		
		GridBagConstraints c = new GridBagConstraints();
	
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL ;
        
        ret.add(edge, c);
        c.gridy++;
        ret.add(path, c);
        
        c.gridy++;
        ret.add(keepe,c);
        c.gridy++;
        c.gridwidth=1;
        c.gridx=0;
        ret.add(new JLabel("Importance"),c);
        c.gridx++;
        ret.add(qry,c);
        c.gridy++;
        ret.add(attr,c);
        c.gridy++;
        
        c.gridx=0;
        c.gridy++;
        c.gridwidth=1;
        ret.add(new JLabel("Algorithm"),c);
        c.gridx=1;
        ret.add(rand,c);
        c.gridx=1;
       
        //c.gridx = 1;
        c.gridy++;
        ret.add(bf,c);
        c.gridy++;
        c.gridx=0;
        
        ret.add(new JLabel("Operations"),c);
        
        c.gridx++;
        ret.add(merges,c);
        c.gridy++;
        ret.add(edges,c);
        c.gridx=0;
        c.gridy++;
        
        
        ret.add(new JLabel("Ratio"),c);
        c.gridx=1;

        c.gridy++;
        ret.add(rati,c);
        c.gridx=0;
        c.gridy++;
        c.gridwidth=2;
        ret.add(recompute,c);
		return ret;
	}

}
