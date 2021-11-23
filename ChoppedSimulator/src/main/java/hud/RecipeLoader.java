package hud;

import java.util.Vector;

import data.Ingredient;
import data.Recipe;

public class RecipeLoader {
	
	//Recipe Loader saves all the data to show in the simulation the recipes in each page if there is more than 5 recipes recommend for the user.
	
	private boolean hover;
	
	private Recipe recipe;
	
	private Vector<Ingredient> ingredientData;
	
	private String chooseRecipe;
	
	public RecipeLoader(boolean hover, Recipe recipe, Vector<Ingredient> ingredientData) {
		this.hover = hover;
		this.recipe = recipe;
		this.ingredientData = ingredientData;
		this.chooseRecipe = "Choose Recipe";
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public Vector<Ingredient> getRecipeData() {
		return ingredientData;
	}

	public void setRecipeData(Vector<Ingredient> ingredientData) {
		this.ingredientData = ingredientData;
	}

	public String getChooseRecipe() {
		return chooseRecipe;
	}

	public void setChooseRecipe(String chooseRecipe) {
		this.chooseRecipe = chooseRecipe;
	}

}
