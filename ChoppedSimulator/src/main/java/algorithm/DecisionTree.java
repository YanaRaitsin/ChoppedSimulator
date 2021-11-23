package algorithm;
import java.util.ArrayList;
import java.util.List;

public class DecisionTree {

	public String[] allAttributes;
	public DecisionNode root = null;
	private List<String> saveIngrediants = new ArrayList<String>();
	/**
	 * Print the tree to console
	 */
	public void print() {
		System.out.println("Decision Tree for given csv File:");
		System.out.println();
		print(root, "");
	}

	/**
	 * Print a sub-tree to console
	 *
	 * @param nodeToPrint
	 *            the root note
	 * @param indent
	 *            the current indentation
	 * @param value
	 *            a string that should be used to increase the indentation
	 */
	private void print(DecisionNode nodeToPrint, String value) {
		if (value.isEmpty() == false) {
		// if it is a class node print it directly
		if (nodeToPrint.isClassNode) {
			System.out.println(value + " " + nodeToPrint.className);
		} 
		}
		else {
			// if it is a decision node, print it and recursivly call {@code print()}
			DecisionNode node = nodeToPrint;
			for (int i = 0; i < node.nodes.length; i++) {
				print(node.nodes[i],node.attributeValues[i]);
			}
		}
	}
	
	public List<String> searchNode(DecisionNode node, String value, String check)
	{
		if (value.isEmpty() == false) {
		if (node.isClassNode) {
			if(node.getClassName().equals(check))
				saveIngrediants.add(value);
		} 
		}
		else {
			DecisionNode checkNext = node;
			for (int i = 0; i < checkNext.nodes.length; i++) {
				searchNode(checkNext.nodes[i],checkNext.attributeValues[i],check);
			}
		}
		return saveIngrediants;
	}

}
