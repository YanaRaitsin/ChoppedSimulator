package entities;

import hud.HudTimer;
import items.GameItem;

public class Oven {
	
	private GameItem oven;
	
	private boolean inUse;
	
	private int startToBaked;
	
	private int secondsLeft;
	
	private boolean timeChecked;
	
	private int totalCookingTime;

	public Oven(GameItem oven) {
		this.oven = oven;
		this.inUse = false;
		this.startToBaked = 120; //for each dessert, the total baking time is 2 minutes.
		this.secondsLeft = 0;
		this.timeChecked = false;
		this.totalCookingTime = 5;
	}
	
	public void checkTimeBaking(int saveStartingTime) {
		timeChecked = true;
		int checkTimeLeft = 0;
		int checkNegtiveTime;
		checkTimeLeft = saveStartingTime - startToBaked;
		//if started after 30 seconds -> positive number
		if(inUse) {
			if(checkTimeLeft > 0) {
				if(checkTimeLeft == HudTimer.getSeconds()) {
					totalCookingTime--;
				}
			}
			checkNegtiveTime = -checkTimeLeft;
			//if started before 30 seconds -> negative number
			if(checkTimeLeft < 0 && secondsLeft == 0) { 
				if(HudTimer.getSeconds()==0)
					secondsLeft = 59 - (--checkNegtiveTime);
			}
			if(checkTimeLeft < 0) {
				if(HudTimer.getSeconds() == secondsLeft) {
					totalCookingTime--;
				}
			}
		}
	}
	
	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public GameItem getOven() {
		return oven;
	}

	public boolean isTimeChecked() {
		return timeChecked;
	}

	public void setTimeChecked(boolean timeChecked) {
		this.timeChecked = timeChecked;
	}

	public int getTotalCookingTime() {
		return totalCookingTime;
	}

	public void setTotalCookingTime(int totalCookingTime) {
		this.totalCookingTime = totalCookingTime;
	}

}
