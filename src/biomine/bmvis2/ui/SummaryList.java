package biomine.bmvis2.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


public class SummaryList extends JPanel{
	
	private LinkedHashMap<String, CollapsePane> panes;
	//private JPanel ipane;
	
	JPanel glue = new JPanel(){
		public Dimension getPreferredSize(){
			Dimension ret = super.getPreferredSize();
			ret.height=Short.MAX_VALUE;
			return ret;
		}
		
	};
	public SummaryList(){
		panes = new LinkedHashMap<String, CollapsePane>();
		
		//ipane = new JPanel();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
	//	this.add(glue);
//	//this.add(ipane);
//		scroller = new JScrollPane(ipane);
//		this.add(scroller);
//		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		
	}
	
	
	public void addItem(String title,JComponent comp,Action removeAct){
		if(panes.get(title)!=null)
			throw new RuntimeException("component with title "+title+" is already added to summarylist");
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		JPanel titleBag = new JPanel();
		titleBag.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill=c.HORIZONTAL;
		c.weightx=1;
		
		titleBag.add(new JLabel(title),c);
	
		JButton b = new JButton(removeAct);
		b.setMargin(new Insets(0,0,0,0));
		b.setAlignmentX(RIGHT_ALIGNMENT);
		
		c.gridx=1;
		c.fill=c.NONE;
		c.weightx=0;
		c.anchor=c.EAST;
		titleBag.add(b,c);
		
		CollapsePane p = new CollapsePane(titleBag, comp);

		panes.put(title,p);
	//	this.remove(glue);
		this.add(p);
		//this.add(glue);
	}
	
	public void addItem(String title,JComponent comp){
		CollapsePane p = new CollapsePane(title, comp);

		panes.put(title,p);
	//	this.remove(glue);
		this.add(p);
		//this.add(glue);
	}
	
	public void removeItem(String title){
		CollapsePane p = panes.get(title);
		if(p!=null){
			this.remove(p);
			panes.remove(title);
		}
		if(panes.size()==0)
			setBorder(BorderFactory.createEmptyBorder());
	}
	public void clearItems(){
		List<String> s = new ArrayList<String>(itemTitles());
		for(String str:s)
			removeItem(str);
	}
	public Set<String> itemTitles(){
		return new LinkedHashSet<String>(panes.keySet());
	}
	
	public boolean hasItem(String title){
		return panes.containsKey(title);
	}
	public JComponent getPane(String title){
		return panes.get(title);
	}
	
}
