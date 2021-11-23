package entities;

import java.util.List;

import data.Foodtype;
import items.GameItem;

public class IngredientData {
	
	private GameItem ingredient;
	
	private List<Foodtype> foodType;
	
	private int amount;
	
	private String measureType;

	public IngredientData(GameItem ingredient, List<Foodtype> foodType, String measureType) {
		this.ingredient = ingredient;
		this.foodType = foodType;
		this.amount = 0;
		this.measureType = measureType;
	}

	public GameItem getIngredient() {
		return ingredient;
	}

	public void setIngredient(GameItem ingredient) {
		this.ingredient = ingredient;
	}

	public List<Foodtype> getFoodType() {
		return foodType;
	}

	public void setFoodType(List<Foodtype> foodType) {
		this.foodType = foodType;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getMeasureType() {
		return measureType;
	}

}
