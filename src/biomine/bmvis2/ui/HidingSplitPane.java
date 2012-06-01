package biomine.bmvis2.ui;

import javax.jws.Oneway;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class HidingSplitPane extends JSplitPane {
	private JButton leftButton;
	private JButton rightButton;

	public class HidingSplitPaneUI extends BasicSplitPaneUI {
		public HidingSplitPaneUI() {
		}

		public BasicSplitPaneDivider createDefaultDivider() {
			return new BasicSplitPaneDivider(this) {
				@Override
				public void prepareForDragging() {
					super.prepareForDragging();
					// HidingSplitPane.this.getLeftComponent().setVisible(true);
					// HidingSplitPane.this.getRightComponent().setVisible(true);
				}

				@Override
				protected JButton createLeftOneTouchButton() {
					JButton ret = super.createLeftOneTouchButton();
					HidingSplitPane.this.leftButton = ret;
					return ret;
				}

				@Override
				protected JButton createRightOneTouchButton() {
					JButton ret = super.createRightOneTouchButton();
					HidingSplitPane.this.rightButton = ret;
					
					return ret;
				}

				@Override
				public void dragDividerTo(int pos) {

					// if(pos<getLeftComponent().getMinimumSize().width)
					// getLeftComponent().setVisible(false);
					// else
					// getLeftComponent().setVisible(true);
					//					
					// System.out.println("pos = "+pos);
					// if(pos>getWidth()-getRightComponent().getMinimumSize().width)
					// super.dragDividerTo(getWidth());
					// getRightComponent().setVisible(false);
					// else
					// getRightComponent().setVisible(true);

					super.dragDividerTo(pos);
				}
			};
		}
	}

	public void hideLeft() {
		leftButton.doClick();
		leftButton.doClick();
	}

	public void hideRight() {
		rightButton.doClick();
		rightButton.doClick();
	}
	
	public void clickLeft(){
		leftButton.doClick();
	}
	public void clickRight(){
		rightButton.doClick();
	}
	
	public HidingSplitPane(final boolean leftHidden,final boolean rightHidden) {
		super.setUI(new HidingSplitPaneUI());
		// super.setUI(
		// });
		super.setOneTouchExpandable(true);
		final Runnable hideThem = new Runnable(){
			
			private boolean firstTime=true;
			public void run() {
				if(firstTime && isVisible()){
					firstTime = false;
					if(rightHidden){
						getRightComponent().setVisible(false);
						SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRightComponent().setVisible(true);
							hideRight();
						}});
					}
					if(leftHidden){
						getLeftComponent().setVisible(false);
						SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getLeftComponent().setVisible(true);
							hideLeft();
						}});
					}
				}
			}
		};
		//SwingUtilities.invokeLater(hideThem);
		this.addAncestorListener(
				new AncestorListener() {
					@Override
					public void ancestorRemoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
					}
					@Override
					public void ancestorMoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
					}
					boolean firstTime = true;
					@Override
					public void ancestorAdded(AncestorEvent arg0) {
						hideThem.run();
					}
				});
	}
}
