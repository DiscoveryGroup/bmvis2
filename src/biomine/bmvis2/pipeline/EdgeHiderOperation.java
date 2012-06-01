package biomine.bmvis2.pipeline;

import biomine.bmvis2.VisualGraph;

public interface EdgeHiderOperation extends GraphOperation {
    public long getTargetHiddenEdges();
    public void setTargetHiddenEdges(long target);
    public void setCurrentGraph(VisualGraph graph);
}
