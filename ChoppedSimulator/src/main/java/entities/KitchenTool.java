package entities;

import items.GameItem;

public abstract class KitchenTool {
	
	//The object of the kitchen tool
	private GameItem kitchenTool;
	
	//check if the kitchen tool is empty
	private boolean empty;
	
	//if the user take the kitchen tool - than update the taken.
	private boolean taken;
	
	//if the recipe need the kitchen tool, inSimulation will be true, else, will be false.
	private boolean inSimulation;
	
	public KitchenTool() {
		this.inSimulation = false;
	}

	public KitchenTool(GameItem kitchenTool) {
		this.kitchenTool = kitchenTool;
		this.empty = true;
		this.taken = false;
		this.inSimulation = false;
	}
	
	//when the user will insert ingredients into kitchen tool, the object will update the current texture after the changes.
	public abstract void setTexture(String action, String textureName) throws Exception;

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isTaken() {
		return taken;
	}

	public void setTaken(boolean taken) {
		this.taken = taken;
	}

	public GameItem getKitchenTool() {
		return kitchenTool;
	}

	public void setKitchenTool(GameItem kitchenTool) {
		this.kitchenTool = kitchenTool;
	}

	public boolean isInSimulation() {
		return inSimulation;
	}

	public void setInSimulation(boolean inSimulation) {
		this.inSimulation = inSimulation;
	}

}
