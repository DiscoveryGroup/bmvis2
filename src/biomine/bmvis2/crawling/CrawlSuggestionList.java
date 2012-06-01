package biomine.bmvis2.crawling;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.crawling.SuggestionQuery.NodeItem;
import biomine.bmvis2.crawling.SuggestionQuery.NodeType;


/**
 * 
 * Component for selecting nodes for queries. Uses suggest.cgi web-interface and
 * so requires internet access.
 * 
 * @author alhartik
 * 
 */
public class CrawlSuggestionList extends JPanel {

	JTextField query = new JTextField();

	JButton back = new JButton();
	JList list = new JList();
	JList selectedList = new JList();

	ArrayList<NodeItem> selectedNodes = new ArrayList<NodeItem>();

	JScrollPane listScroller = new JScrollPane();
	final SuggestionQuery suggestionQuery = new SuggestionQuery();
	
	HashMap<String,String> nameMap = new HashMap<String, String>();
	public String getName(String queryid) {
		return nameMap.get(queryid);
	}

	class SelectedListModel extends AbstractListModel {
		public Object getElementAt(int i) {
			return selectedNodes.get(i);
		}

		public int getSize() {
			return selectedNodes.size();
		}
	}

	class SuggestListModel extends AbstractListModel {
		public Object getElementAt(int i) {
			return itemList.get(i);
		}

		public int getSize() {
			return itemList.size();
		}
	}


	public void addSelection(NodeItem item) {
		System.out.println("adding " + item.getName());
		if (selectedNodes.contains(item))
			return;
		selectedNodes.add(item);
		selectedList.updateUI();

	}

	public void removeSelection(NodeItem item) {
		selectedNodes.remove(item);
		System.out.println("removing " + item.getName());
		selectedList.updateUI();
	}

	Object hoveredObject = null;

	class SuggestCellRenderer implements ListCellRenderer {

		class NodeComp extends JPanel {

			public NodeComp(final NodeItem item, boolean selected) {
				JLabel text = new JLabel(item.getName());

				this.setLayout(new GridLayout());

				this.add(text);
				boolean hover = hoveredObject != null
						&& item.equals(hoveredObject);
				this.setBackground(new Color(62, 200, 64));
				if (hover)
					this.setBackground(new Color(128, 255, 128));

			}
		}

		class TypeComp extends JPanel {
			public TypeComp(NodeType t, boolean selected) {
				this.setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.gridy = 0;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;

				this.add(new JLabel(t.name), c);
				JLabel text = new JLabel(t.more);
				// + " -- type=" + t.tcode + " organism=" + t.ocode);
				text.setAlignmentX(RIGHT_ALIGNMENT);

				c.fill = c.NONE;
				c.anchor = GridBagConstraints.EAST;
				c.gridx = 1;
				this.add(text, c);
				this.setBackground(new Color(52, 125, 53));
				if (t.ocode == 0 && t.tcode == 0)
					setBackground(Color.gray);

				boolean hover = hoveredObject != null
						&& t.equals(hoveredObject);
				if (hover)
					this.setBackground(new Color(128, 255, 128));

			}
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof NodeItem)
				return new NodeComp((NodeItem) value, isSelected);
			if (value instanceof NodeType)
				return new TypeComp((NodeType) value, isSelected);
			return null;
		}

	}

	class SuggestMouseListener implements MouseListener, MouseMotionListener {
		JList mylist;

		public SuggestMouseListener(JList l) {
			mylist = l;
		}

		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int cell = mylist.locationToIndex(e.getPoint());
			System.out.println(e.getPoint());
			System.out.println("mylist="+mylist);
			if(mylist.getCellBounds(cell,cell)!=null)
			if (mylist.getCellBounds(cell, cell).contains(e.getPoint())) {
				if (e.getClickCount() >= 1) {
					// DOUBLECLICK ON CELL!!
					Object value = mylist.getModel().getElementAt(cell);
					if (value instanceof NodeItem) {
						NodeItem ni = (NodeItem) value;
						if (mylist == selectedList)
							removeSelection(ni);
						else
							addSelection((NodeItem) value);
					}
					if (value instanceof NodeType) {
						NodeType nt = (NodeType) value;
						suggestionQuery.focusType(nt);
						updateList();
					}
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent e) {
			hoveredObject = null;
			redrawLists();
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseMoved(MouseEvent e) {
			int cell = mylist.locationToIndex(e.getPoint());
			Rectangle bounds = mylist.getCellBounds(cell, cell);
			if (bounds == null)
				return;
			if (bounds.contains(e.getPoint())) {
				hoveredObject = mylist.getModel().getElementAt(cell);

			}
			redrawLists();
		}
	}
	
	private String db=null;
	
	private JComboBox dbSelect=null;
	public CrawlSuggestionList() {
		// setMinimumSize(new Dimension(600,200));

		// initialize layout
		GridBagLayout layout = new GridBagLayout();

		this.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

	
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridheight= 1;
		
				
		dbSelect = new JComboBox();
		for(String db:Databases.getDatabases()){
			dbSelect.addItem(db);
		}
		
		dbSelect.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				suggestionQuery.setDb(dbSelect.getSelectedItem().toString());
			}
		});
		
		this.add(dbSelect,c);
		c.gridy++;
		this.add(query, c);

		c.gridwidth = 1;
		c.weighty = 3;
		c.weightx = 10;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy ++;
		// listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScroller = new JScrollPane(list);
		this.add(listScroller, c);

		c.weighty = 0;
		c.weightx = 10;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy  ++;
		this.add(new JLabel("Selected nodes:"), c);

		c.weighty = 3;
		c.weightx = 10;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		JScrollPane selectedScroller = new JScrollPane(selectedList);
		this.add(selectedScroller, c);

		// initialize components
		back.setAction(new AbstractAction("< back to unfiltered results") {

			public void actionPerformed(ActionEvent arg0) {
				suggestionQuery.back();
				updateList();
			}
		});
		query.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent arg0) {
				updateList();
			}

		});

		list.setModel(new SuggestListModel());
		list.addMouseListener(new SuggestMouseListener(list));
		list.addMouseMotionListener(new SuggestMouseListener(list));

		list.setCellRenderer(new SuggestCellRenderer());

		selectedList.setModel(new SelectedListModel());
		selectedList.addMouseListener(new SuggestMouseListener(selectedList));
		selectedList.addMouseMotionListener(new SuggestMouseListener(
				selectedList));
		selectedList.setCellRenderer(new SuggestCellRenderer());
	}

	public boolean isSelected(NodeItem ni) {
		return selectedNodes.contains(ni);
	}

	public void redrawLists() {
		list.updateUI();
		selectedList.updateUI();
	}
	/**
	 * Tries to add node by name
	 * @param name
	 * @return true if we find the node from db
	 */
	public void addNode(VisualNode vn){
		BMNode bmn =vn.getBMNode();
		System.out.println("adding to query:"+vn+" shortname="+bmn.get("ShortName"));
		
		if(bmn==null)
			throw new RuntimeException("Only proper nodes can be queried");
		String shortName = bmn.get("ShortName");
		if(shortName==null)shortName=bmn.getId();
		this.selectedNodes.add(new SuggestionQuery.NodeItem(bmn.getId(), shortName));
	}

	ArrayList<Object> itemList = new ArrayList<Object>();

	private int curVersion = 0;

	private void updateList() {
		final int myVersion = ++curVersion;
		SwingWorker<Void, Void> work = new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {

				try {
					suggestionQuery.setQuery(query.getText());
					suggestionQuery.updateResult();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(
									CrawlSuggestionList.this, e.getMessage());
						}
					});
					return null;
				}
				// update list in event thread
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// this thread finished after new thread was started
						if (myVersion < curVersion)
							return;
						itemList.clear();
						for (NodeType n : suggestionQuery.getResult()) {
							itemList.add(n);
							for (NodeItem ni : n.items){
								nameMap.put(ni.getCode(), ni.getName());
								itemList.add(ni);
							}
						}
						list.updateUI();
					}
				});
				return null;
			}
		};

		work.execute();

	}
	public String getDatabase(){
		return suggestionQuery.getDb();
	}
	public void setDatabase(String str){
		dbSelect.setSelectedItem(str);
	}
	public CrawlQuery getQueryTerms() {
		ArrayList<String> ret = new ArrayList<String>();
		for (NodeItem it : selectedNodes)
			ret.add(it.getCode());
		return new CrawlQuery(ret);
	}
}

class SuggestionQuery {

	private String query;
	private int type;
	private int organism;
	private String db = null;
	
	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	private ArrayList<NodeType> resultList = new ArrayList<NodeType>();

	private ArrayList<NodeType> fetchList(String q, int type, int organism)
			throws IOException {

		ArrayList<NodeType> ret = new ArrayList<NodeType>();
		if (q.length() == 0)
			return ret;
		String typePar = type <= 0 ? "" : ("type=" + type + "__");
		String orgPar = organism <= 0 ? "" : ("organism=" + organism + "__");
		String dbPar = db==null ? "" : ("db="+URLEncoder.encode(db,"UTF-8")+"__");
		
		URL u = new URL(WebConstants.DEFAULT_BASEURL + "suggest.cgi?" + orgPar
				+ typePar + dbPar + "term=" + URLEncoder.encode(q, "UTF-8"));
		
		System.out.println("querying " + u + " organism=" + organism + " type="
				+ type);
		
		
		String jsonStr = URLUtils.getURLContents(u);
		if (type != 0) {
			NodeType back = new NodeType();
			back.name = "back to unfiltered results";
			back.tcode = 0;
			back.ocode = 0;
			ret.add(back);
		}
		try {
			JSONArray ja = (JSONArray) JSONValue.parse(jsonStr);

			for (Object orgz : ja) {

				JSONObject orgtype = (JSONObject) orgz;
				Long tcodeL = (Long) orgtype.get("type");
				int tcode = tcodeL.intValue();
				Long ocodeL = (Long) orgtype.get("organism");
				int ocode = ocodeL.intValue();
				

				NodeType curType = null;
				curType = new NodeType();
				curType.tcode = tcode;
				curType.ocode = ocode;
				curType.name = orgtype.get("text").toString();
				curType.more = orgtype.get("unshown").toString();
				ret.add(curType);

				JSONArray match = (JSONArray) orgtype.get("matches");
				for (Object o : match) {
					JSONObject node = (JSONObject) o;

					String code = node.get("term").toString();
					String name = node.get("title").toString();
					
					NodeItem it = new NodeItem(code, name);
					curType.items.add(it);
				}

			}
		} catch (ClassCastException e) {
			throw new IOException("Invalid JSON from server", e);
		}
		return ret;
	}

	public static class NodeItem {
		private String code;
		private String name;

		public NodeItem(String c, String n) {
			code = c;
			name = n;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public boolean equals(Object o) {
			if (o.getClass() == getClass()) {
				NodeItem oi = (NodeItem) o;
				return oi.name.equals(name) && oi.code.equals(code);
			}
			return false;
		}

		public int hashCode() {
			return code.hashCode() + Integer.rotateLeft(name.hashCode(), 12);
		}
	}

	class NodeType {
		int tcode;
		int ocode;
		String name;
		ArrayList<NodeItem> items = new ArrayList<NodeItem>();
		String more;
	}

	/**
	 * Resets state to show unfiltered results (needs updating before getResult
	 * changes)
	 */
	void back() {
		type = 0;
		organism = 0;
	}

	/**
	 * Sets query to show only results of given type (needs updating before
	 * getResult changes)
	 */
	void focusType(NodeType t) {
		type = t.tcode;
		organism = t.ocode;
	}

	void setQuery(String q) {
		query = q;
	}

	/**
	 * Updates suggestion list
	 * 
	 * @throws IOException
	 *             when something goes bad in networkstuff
	 */
	void updateResult() throws IOException {
		resultList = fetchList(query, type, organism);
		System.out.println("updateResult");

	}

	ArrayList<NodeType> getResult() {
		return resultList;
	}

}
