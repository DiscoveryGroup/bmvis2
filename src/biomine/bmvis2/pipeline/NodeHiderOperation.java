package biomine.bmvis2.pipeline;

public interface NodeHiderOperation extends GraphOperation {
    public long getTargetHiddenNodes();
    public void setTargetHiddenNodes(long target);
}
