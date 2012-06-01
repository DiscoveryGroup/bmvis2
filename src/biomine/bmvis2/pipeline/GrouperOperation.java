package biomine.bmvis2.pipeline;

import javax.swing.JComponent;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.group.GrouperList;

public class GrouperOperation extends StructuralOperation implements GraphOperation {
    private Grouper grouper;
    private String toolTipText;

    public GrouperOperation() {

    }

    public GrouperOperation(Grouper g) {
        this.grouper = g;
    }

    public void doOperation(VisualGraph g) {
        this.toolTipText = grouper.makeGroups(g.getRootNode());
    }

    public String getTitle() {
        return GrouperList.grouperName(grouper);
    }

    public String getToolTip() {
        return this.toolTipText;
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback v, final VisualGraph graph) {
        return grouper.getSettingsComponent(v, graph.getRootNode());
    }

    public void fromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class grouperClass;
        String grouperClassName = o.get("grouper").toString();
        grouperClass = getClass().getClassLoader().loadClass(
                grouperClassName);

        this.grouper = (Grouper) grouperClass.newInstance();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("grouper", this.grouper.getClass().getName());
        return ret;
    }

    public Grouper getGrouper() {
        return grouper;
    }

    public String toString() {
        return "GrouperOperation: " + this.getTitle();
    }
}
