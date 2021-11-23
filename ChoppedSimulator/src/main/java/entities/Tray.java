package entities;

import graph.Material;
import graph.Texture;
import items.GameItem;

public class Tray extends KitchenTool {
	
	private boolean inOven;
	
	private boolean notBaked;
	
	private boolean baked;
	
	private boolean burned;
	
	public Tray() {
		super();
	}
	
	public Tray(GameItem kitchenTool) {
		super(kitchenTool);
		this.inOven = false;
		this.notBaked = false;
		this.baked = false;
		this.burned = false;
	}
	
	public Tray(Tray tray) {
		this.notBaked = tray.notBaked;
		this.baked = tray.baked;
		this.burned = tray.burned;
	}
	
	
	public void setTexture(String action, String directoryPath) throws Exception {
		if(action.equals("not empty")) {
			Texture ingredientOnTrayTexture = new Texture(directoryPath);
			Material ingredientOnTrayMaterial = new Material(ingredientOnTrayTexture,5f);
			this.getKitchenTool().getMesh().setMaterial(ingredientOnTrayMaterial);
		}
		else if(action.equals("empty")) {
			Texture ingredientOnTrayTexture = new Texture("/resources/textures/trayTexture.png");
			Material ingredientOnTrayMaterial = new Material(ingredientOnTrayTexture,5f);
			this.getKitchenTool().getMesh().setMaterial(ingredientOnTrayMaterial);
		}
	}

	public boolean isInOven() {
		return inOven;
	}

	public void setInOven(boolean inOven) {
		this.inOven = inOven;
	}

	public boolean isBaked() {
		return baked;
	}

	public void setBaked(boolean baked) {
		this.baked = baked;
	}

	public boolean isBurned() {
		return burned;
	}

	public void setBurned(boolean burned) {
		this.burned = burned;
	}

	public boolean isNotBaked() {
		return notBaked;
	}

	public void setNotBaked(boolean notBaked) {
		this.notBaked = notBaked;
	}
	
}
