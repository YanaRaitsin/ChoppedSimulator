package entities;

import hud.HudTimer;
import items.GameItem;

public class FoodProcessor {
	
	private GameItem foodProcessor;
	
	private boolean on;
	
	private boolean empty;
	
	private boolean processedIngredients;
	
	private boolean inSimulation;
	
	private int saveSecondsLeft;
	
	public FoodProcessor() {
		this.inSimulation = false;
	}

	public FoodProcessor(GameItem foodProcessor) {
		this.foodProcessor = foodProcessor;
		this.on = false;
		this.empty = true;
		this.processedIngredients = false;
		this.inSimulation = false;
		this.saveSecondsLeft = 0;
	}
	
	public void processIngredients(int saveStartingTime) {
		int proccessTime = 60; //the proccess time is set to one minute.
		int checkTimeLeft = 0;
		int checkNegtiveTime;
		checkTimeLeft = saveStartingTime - proccessTime;
		//if the food processor started after 30 seconds -> positive number
		if(on && !empty) {
			if(checkTimeLeft > 0) {
				if(checkTimeLeft == HudTimer.getSeconds()) {
					on = false;
					processedIngredients = true;
					checkTimeLeft=0;
				}
			}
			checkNegtiveTime = -checkTimeLeft;
			//if the food processor started before 30 seconds -> negative number
			if(checkTimeLeft < 0 && saveSecondsLeft == 0) { 
				if(HudTimer.getSeconds()==0)
					saveSecondsLeft = 59 - (--checkNegtiveTime);
			}
			if(checkTimeLeft < 0) {
				if(HudTimer.getSeconds() == saveSecondsLeft) {
					on = false;
					processedIngredients = true;
					checkTimeLeft=0;
					saveSecondsLeft=0;
				}
			}
		}
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public GameItem getFoodProcessor() {
		return foodProcessor;
	}

	public boolean isProcessedIngredients() {
		return processedIngredients;
	}

	public void setProcessedIngredients(boolean processedIngredients) {
		this.processedIngredients = processedIngredients;
	}

	public boolean isInSimulation() {
		return inSimulation;
	}

	public void setInSimulation(boolean inSimulation) {
		this.inSimulation = inSimulation;
	}

}
