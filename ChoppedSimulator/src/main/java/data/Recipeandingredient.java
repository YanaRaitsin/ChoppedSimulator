package data;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the recipeandingredient database table.
 * 
 */
@Entity
@NamedQuery(name="Recipeandingredient.findAll", query="SELECT r FROM Recipeandingredient r")
public class Recipeandingredient implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private BigDecimal amount;

	//bi-directional many-to-one association to Ingredient
	@ManyToOne
	private Ingredient ingredient;

	//bi-directional many-to-one association to Measure
	@ManyToOne
	private Measure measure;

	//bi-directional many-to-one association to Recipe
	@ManyToOne
	private Recipe recipe;

	public Recipeandingredient() {
	}
	
	public Recipeandingredient(BigDecimal amount, Ingredient ingredient, Measure measure, Recipe recipe) {
		setAmount(amount);
		setIngredient(ingredient);
		setMeasure(measure);
		setRecipe(recipe);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Measure getMeasure() {
		return this.measure;
	}

	public void setMeasure(Measure measure) {
		this.measure = measure;
	}

	public Recipe getRecipe() {
		return this.recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

}