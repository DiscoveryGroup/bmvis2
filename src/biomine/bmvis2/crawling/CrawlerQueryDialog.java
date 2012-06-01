//package biomine.bmvis2.crawling;
//
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.event.ActionEvent;
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import javax.swing.AbstractAction;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextArea;
//import javax.swing.SwingUtilities;
//import javax.swing.SwingWorker;
//
//import biomine.bmgraph.BMGraph;
//import biomine.bmvis2.VisualGraph;
//
//
//public class CrawlerQueryDialog extends JPanel {
//
//	
//	CrawlSuggestionList selector;
//	JLabel stateLabel;
//	JTextArea textArea;
//
//	JButton b;
//	VisualGraph visualGraph;
//	HashSet<CrawlerCallback> callbacks = new HashSet<CrawlerCallback>();
//	
//	
//	public void addCallback(CrawlerCallback b){
//		callbacks.add(b);
//	}
//
//	public void removeCallback(CrawlerCallback b){
//		callbacks.remove(b);
//	}
//	public CrawlerQueryDialog(VisualGraph graph) {
//		visualGraph = graph;
//		
//		b = new JButton("Query");
//		textArea = new JTextArea();
//		stateLabel = new JLabel("");
//		textArea.setEditable(false);
//		selector = new CrawlSuggestionList();
//
//		this.setLayout(new GridBagLayout());
//		GridBagConstraints c = new GridBagConstraints();
//		c.ipadx = 5;
//		c.ipady = 5;
//		c.gridx = 0;
//		c.gridy = 0;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.gridwidth = 2;
//		c.fill = c.BOTH;
//
//		this.add(selector, c);
//
//		c.gridy = 1;
//		c.fill = c.HORIZONTAL;
//		c.weighty=0;
//		this.add(b, c);
//
//		// Box box = new Box(BoxLayout.X_AXIS);
//		// box.add(query);
//		// box.add(b);
//		// box.setMaximumSize(new Dimension(Short.MAX_VALUE, box
//		// .getPreferredSize().width));
//		// this.add(box);
//		c.gridy = 2;
//		
//		this.add(stateLabel, c);
//		c.gridy = 3;
//		c.weighty=1;
//		c.fill = c.BOTH;
//		this.add(textArea, c);
//		//
//		b.setAction(new AbstractAction("Discover connections and add to graph") {
//			public void actionPerformed(ActionEvent arg0) {
//				
//				start();
//			}
//		});
//	}
//
//	private void start() {
//		b.setEnabled(false);
//		final ArrayList<String> qs = new ArrayList<String>(selector
//				.getQueryTerms());
//		for(CrawlerCallback b:callbacks){
//			b.newQuery(selector.getQueryTerms());
//		}
//		SwingWorker<Void, Void> work = new SwingWorker<Void, Void>() {
//
//			@Override
//			protected Void doInBackground() throws Exception {
//				final CrawlerFetch c = new CrawlerFetch(qs,false);
//				while (c.isDone() == false) {
//					c.update();
//					SwingUtilities.invokeAndWait(new Runnable() {
//						public void run() {
//		
//							textArea.setText(c.getMessages());
//							stateLabel.setText("Status: "+c.getState());
//						}
//					});
//					Thread.sleep(500);
//				}
//				if (c.isReady()) {
//					final BMGraph graph = c.getBMGraph();
//					System.out.println(graph.getNodes());
//					SwingUtilities.invokeAndWait(new Runnable() {
//						public void run() {
//				
//							textArea.setText(c.getMessages() + "\nDone");
//							visualGraph.addBMGraph(graph);
//						}
//					});
//
//				}
//				SwingUtilities.invokeAndWait(new Runnable() {
//					public void run() {
//						b.setEnabled(true);
//					}
//				});
//				return null;
//			}
//
//		};
//		work.execute();
//	}
//
//}
