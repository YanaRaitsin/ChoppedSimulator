package entities;

import graph.Material;
import graph.Texture;
import items.GameItem;

public class Bowl extends KitchenTool{
	
	private boolean combined;

	public Bowl() {
		super();
	}

	public Bowl(GameItem kitchenTool) {
		super(kitchenTool);
		this.combined = false;
	}

	@Override
	public void setTexture(String action, String textureName) throws Exception {
		float reflectance = 5f;
		if(action.equals("empty")) {
			Texture emptyBowlTexture = new Texture("/resources/textures/bowlTexture.png");
			Material emptyBowl = new Material(emptyBowlTexture,reflectance);
			this.getKitchenTool().getMesh().setMaterial(emptyBowl); 
		}
		else if(action.equals("not empty")) {
			Texture ingredientBowlTexture = new Texture(textureName);
			Material ingredientBowl = new Material(ingredientBowlTexture,reflectance);
			this.getKitchenTool().getMesh().setMaterial(ingredientBowl); 
		}
	}

	public boolean isCombined() {
		return combined;
	}

	public void setCombined(boolean combined) {
		this.combined = combined;
	}

}
