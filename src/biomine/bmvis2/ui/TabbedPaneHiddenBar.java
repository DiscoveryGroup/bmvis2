package biomine.bmvis2.ui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class TabbedPaneHiddenBar extends JPanel {
	JTabbedPane pane;
	private int tabCount = 0 ;
	public TabbedPaneHiddenBar(){
		
		pane = new JTabbedPane();
		this.setLayout(new GridLayout(1,1));

	}
	Component soleComponent= null;
	String soleTitle = "";
	public void addTab(String assignTabTitle, Component nt) {
		// TODO Auto-generated method stub
		if(tabCount==0){
			this.add(nt);
			soleComponent = nt;
			soleTitle = assignTabTitle;
		}
		else if(tabCount==1){
			super.remove(soleComponent);
			pane.addTab(soleTitle,soleComponent);
			pane.addTab(assignTabTitle,nt);
			this.add(pane);
			pane.setSelectedIndex(tabCount);

		}else{
			pane.addTab(assignTabTitle, nt);
			pane.setSelectedIndex(tabCount);

		}
		tabCount++;
		revalidate();
		nt.setVisible(true);
		nt.repaint();
		repaint();		
	}
	public void setSelectedComponent(Component nt) {
		// TODO Auto-generated method stub
		if(tabCount>2)
		pane.setSelectedComponent(nt);
	}
	public Component getSelectedComponent() {
		// TODO Auto-generated method stub
		if(tabCount==0)return null;
		if(tabCount==1)return soleComponent;
		return pane.getSelectedComponent();
	}
	public void addChangeListener(ChangeListener changeListener) {
		pane.addChangeListener(changeListener);
		
	}
	@Override
	public void remove(Component c ){
		if(tabCount==0)
			throw new RuntimeException("Tried to remove from empty tabbedpanehiddenbar");
		else if(tabCount==1){
			if(c==soleComponent)
				super.remove(c);
		}else if(tabCount==2){
			pane.remove(c);
			if(pane.getComponents().length==1){
				soleComponent = pane.getComponents()[0];
				soleTitle = pane.getTitleAt(0);
				super.remove(pane);
				super.add(soleComponent);
			}
		}else{
			pane.remove(c);
		}
		tabCount--;
		revalidate();
		repaint();
	}
	
}
