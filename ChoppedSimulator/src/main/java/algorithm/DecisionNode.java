package algorithm;
public class DecisionNode {
	
	public int attribute; // id
	public DecisionNode[] nodes; // list of child nodes
	public String[] attributeValues; // values for the attribute

	public boolean isClassNode;
	public String className; // name if its a ClassNode (node corresponding to the target class)
	
	public String getClassName() {
		return className;
	}

	public boolean isClassNode() {
		return isClassNode;
	}
	
	
}