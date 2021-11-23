package entities;

import graph.Material;
import graph.Texture;
import hud.HudTimer;
import items.GameItem;
import utils.Logs;

public class Cookware extends KitchenTool{

	private int startToCook; //because it's a simulation, the starting time in every ingredient always be 4 minutes
	
	private boolean gasIsOn;
	
	private boolean cooked;
	
	private boolean burned;
	
	private boolean timeChecked;
	
	private int saveSecondsLeft;
	
	private int checkCookTime;
	
	private boolean ingredientsBowl;
	
	public Cookware() {
		super();
	}
	
	public Cookware(Cookware cookware) {
		this.cooked = cookware.cooked;
		this.burned = cookware.burned;
	}

	public Cookware(GameItem kitchenTool) {
		super(kitchenTool);
		this.gasIsOn = false;
		this.cooked = false;
		this.burned = false;
		this.timeChecked = false;
		this.saveSecondsLeft = 0; //if the cookware is cooking and the player turned on the gas and it's negative number - save it into a variable.
		this.startToCook = 240; //set seconds to cook the ingredients. (4 minutes = 240 seconds).
		this.checkCookTime = 1;
		this.ingredientsBowl = false;
	}
	
	/*
	 * checkCookingTime(int saveStartingTime) - 
	 * The function getting the starting time in seconds (in the simulation) when the user want to cook the ingredients.
	 * When the gas is still on and the user didn't turned it off and the time is over, checkCookTime is decreasing because the ingredient may overcooked.
	 * If the user will turn off the gas on time, so the ingredient will be fully cooked or before the time is over, the ingredients will not be fully cooked.
	 */
	public void checkCookingTime(int saveStartingTime) throws Exception {
		timeChecked = true;
		int checkTimeLeft = 0;
		int checkNegtiveTime;
		checkTimeLeft = saveStartingTime - startToCook;
		//if started after 30 seconds -> positive number
		if(gasIsOn && !this.isEmpty()) {
			if(checkTimeLeft > 0) {
				if(checkTimeLeft == HudTimer.getSeconds()) {
					checkCookTime--;
				}
			}
			checkNegtiveTime = -checkTimeLeft;
			//if started before 30 seconds -> negative number
			if(checkTimeLeft < 0 && saveSecondsLeft == 0) { 
				if(HudTimer.getSeconds()==0)
					saveSecondsLeft = 59 - (--checkNegtiveTime);
			}
			if(checkTimeLeft < 0) {
				if(HudTimer.getSeconds() == saveSecondsLeft) {
					checkCookTime--;
				}
			}
		}
	}
	
	public void changeCookWareTexture(Logs log,String directoryPath, String textureName) throws Exception {
		//change the textures after the player turned off the gas.
		if(timeChecked && !gasIsOn && !this.isEmpty()) {
			/* when the player turned off the gas, check the cooking time.
			 * if less than 4 minutes, the ingredient not fully cooked.
			 * if equals to 4 minutes, the ingredient in the cooking pot is fully cooked.
			 * if after the 4 minutes, the ingredient in the cooking pot is burned.
			*/
			if(checkCookTime < 1 && checkCookTime > -10) { //fully cooked
				if(this.getKitchenTool().getNameGameItem().equals("cooking pot")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" cooked cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "cooked fully", "/resources/recipes/"+directoryPath+"/"+textureName+" cooked cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
				else if(this.getKitchenTool().getNameGameItem().equals("cooking pan")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" cooked cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "cooked fully", "/resources/recipes/"+directoryPath+"/"+textureName+" cooked cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
				cooked = true;
			}
			else if(checkCookTime < 4 && checkCookTime > 0) { //raw cooked
				if(this.getKitchenTool().getNameGameItem().equals("cooking pot")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" to cook cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "not cooked fully", "/resources/recipes/"+directoryPath+"/"+textureName+" to cook cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
				else if(this.getKitchenTool().getNameGameItem().equals("cooking pan")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" to cook cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "not cooked fully", "/resources/recipes/"+directoryPath+"/"+textureName+" to cook cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
			}
			else if(checkCookTime < -1 && checkCookTime < -10) { //burned
				if(this.getKitchenTool().getNameGameItem().equals("cooking pot")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" burned cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "burned", "/resources/recipes/"+directoryPath+"/"+textureName+" burned cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
				else if(this.getKitchenTool().getNameGameItem().equals("cooking pan")) {
					this.setTexture("not empty", "/resources/recipes/"+directoryPath+"/"+textureName+" burned cookware.png");
					log.changedTextures(this.getKitchenTool().getNameGameItem(), "burned", "/resources/recipes/"+directoryPath+"/"+textureName+" burned cookware.png",HudTimer.getMinutes(),HudTimer.getSeconds());
				}
				burned = true;
			}
			log.checkCookware(this,HudTimer.getMinutes(),HudTimer.getSeconds());
		}
	}

	public int getStartingTime() {
		return startToCook;
	}

	@Override
	public void setTexture(String action, String textureName) throws Exception {
		float reflectance = 5f;
		if(action.equals("empty")) {
			Texture emptyCookingPotTexture = new Texture("/resources/textures/frypanTexture.png");
			Material emptyCookingPot = new Material(emptyCookingPotTexture,reflectance);
			this.getKitchenTool().getMesh().setMaterial(emptyCookingPot);
		}
		else if(action.equals("not empty")) {
			Texture notFullyCookedTexture = new Texture(textureName);
			Material notFullyCooked = new Material(notFullyCookedTexture,reflectance);
			this.getKitchenTool().getMesh().setMaterial(notFullyCooked); 
		}
	}

	public boolean isCooked() {
		return cooked;
	}

	public void setCooked(boolean cooked) {
		this.cooked = cooked;
	}

	public boolean isBurned() {
		return burned;
	}

	public void setBurned(boolean burned) {
		this.burned = burned;
	}

	public boolean isGasIsOn() {
		return gasIsOn;
	}

	public void setGasIsOn(boolean gasIsOn) {
		this.gasIsOn = gasIsOn;
	}

	public boolean isTimeChecked() {
		return timeChecked;
	}

	public int getCheckCookTime() {
		return checkCookTime;
	}

	public void setCheckCookTime(int checkCookTime) {
		this.checkCookTime = checkCookTime;
	}

	public boolean isIngredientsBowl() {
		return ingredientsBowl;
	}

	public void setIngredientsBowl(boolean ingredientsBowl) {
		this.ingredientsBowl = ingredientsBowl;
	}

	public void setTimeChecked(boolean timeChecked) {
		this.timeChecked = timeChecked;
	}
	
}
