package biomine.bmvis2.pipeline;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;

/**
 * Due to the intricate use of reflection in constructing the JSON operations
 * list and its serialization to text and de-serialization of it, all
 * GraphOperations in addition to the fromJSON and toJSON methods NEED
 * CONSTRUCTORS WITHOUT PARAMETERS.
 */
public interface GraphOperation {
    class GraphOperationException extends Exception {
        public GraphOperationException(Throwable cause) {
            super(cause);
        }

        public GraphOperationException(String messages) {
            super(messages);
        }
    }

    public String getTitle();

    public String getToolTip();

    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph);

    public void doOperation(VisualGraph g) throws GraphOperationException;

    public JSONObject toJSON();

    public void fromJSON(JSONObject o) throws Exception;
}

