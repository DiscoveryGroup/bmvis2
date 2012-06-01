package biomine.bmvis2.group;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

/**
 * Abstract Superclass for grouping operations. 
 * @author alhartik
 *
 */
public abstract class Grouper {

	private VisualGroupNode currentGroup;
	private JProgressBar progressBar;

	/**
	 * Progress bars were partly implemented at some point. Now 
	 * with new pipeline-stuff they they are not used, though some
	 * groupers still use setProgress to indicate progress. 
	 * 
	 * Progress bars could be later added to pipeline interface.
	 * @param d
	 */
	protected void setProgress(final double d) {
		if (progressBar == null)
			return;
		progressBar.setValue((int) (100 * d));
		progressBar.paintImmediately(progressBar.getBounds());

	}

	JDialog dlg;

//	public void runGrouper(final VisualGroupNode n, Component parent) {
//		long startedAt = System.currentTimeMillis();
//		boolean ok = settingsDialog(parent, n);
//
//		if (!ok) {
//			return;
//		}
//		if(parent==null)
//		{
//			makeGroups(n);
//			return;
//		}
//		// progressBar = new JProgressBar(0,100);
//		Component frame = SwingUtilities.getRootNode(parent);
//		// System.out.println("frame = " + frame);'
//
//		dlg = null;
//		if (frame instanceof JFrame) {
//			progressBar = new JProgressBar(0, 100);
//			JFrame jf = (JFrame) frame;
//			dlg = new JDialog(jf, "Progress", false);
//			dlg.add(progressBar);
//			// jf.add(progressBar);
//		} else {
//			dlg = null;
//		}
//
//		setProgress(0);
//		currentGroup = n;
//		// work.addPropertyChangeListener(new PropertyChangeListener() {
//		// public void propertyChange(PropertyChangeEvent e) {
//		// if(e.getNewValue()==SwingWorker.StateValue.DONE){
//		// System.out.println("YEAH");
//		//
//		// if(dlg!=null){
//		// dlg.setVisible(false);
//		// dlg.dispose();
//		// }
//		// progressBar=null;
//		// }
//		// }
//		// });
//
//		dlg.pack();
//		// dlg.setModalityType(ModalityType.DOCUMENT_MODAL);
//
//		dlg.setVisible(true);
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				long t = System.currentTimeMillis();
//				makeGroups(n);
//				double secs = (System.currentTimeMillis()-t)*0.001;
//				System.err.println("");
//				dlg.dispose();
//				dlg = null;
//			}
//		});
//		System.err.println("Grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
//	}

	public abstract String makeGroups(VisualGroupNode n);

	/**
	 * Get settings dialog for grouper. Return null if there is none
	 * Overloading this method is considered deprecated, overload getSettingsComponent instead
	 * 
	 * @return
	 */
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		return true;
	}
	
	/**
	 * Get settings dialog for grouper. Return null if there is none
	 * 
	 * @return
	 */
	public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, final VisualGroupNode groupNode) {
		final JButton ret = new JButton();
		ret.setAction(new AbstractAction("Alter settings"){
			public void actionPerformed(ActionEvent arg0) {
				boolean ok = settingsDialog(ret, groupNode);
				if(ok){
					changeCallback.settingsChanged(true);
				}
			}
		});
		return ret;
	}

    public String getByName() {
        return null;
    }
}
