package biomine.bmvis2.pipeline;

public class GraphOperationSerializationException extends Exception {
	public GraphOperationSerializationException(Exception e) {
		super(e);
	}

	public GraphOperationSerializationException(String msg) {
		super(msg);
	}
}
