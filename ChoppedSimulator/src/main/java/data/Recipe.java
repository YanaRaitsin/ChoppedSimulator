package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the recipe database table.
 * 
 */
@Entity
@NamedQuery(name="Recipe.findAll", query="SELECT r FROM Recipe r")
public class Recipe implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Lob
	private String name;

	@Lob
	private String type;

	//bi-directional many-to-one association to Dish
	@OneToMany(mappedBy="recipe")
	private List<Dish> dishs;

	//bi-directional many-to-one association to Recipeandingredient
	@OneToMany(mappedBy="recipe")
	private List<Recipeandingredient> recipeandingredients;

	public Recipe() {
	}
	
	public Recipe(int id, String recipeName, String type) {
		setId(id);
		setName(recipeName);
		setType(type);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Dish> getDishs() {
		return this.dishs;
	}

	public void setDishs(List<Dish> dishs) {
		this.dishs = dishs;
	}

	public Dish addDish(Dish dish) {
		getDishs().add(dish);
		dish.setRecipe(this);

		return dish;
	}

	public Dish removeDish(Dish dish) {
		getDishs().remove(dish);
		dish.setRecipe(null);

		return dish;
	}

	public List<Recipeandingredient> getRecipeandingredients() {
		return this.recipeandingredients;
	}

	public void setRecipeandingredients(List<Recipeandingredient> recipeandingredients) {
		this.recipeandingredients = recipeandingredients;
	}

	public Recipeandingredient addRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().add(recipeandingredient);
		recipeandingredient.setRecipe(this);

		return recipeandingredient;
	}

	public Recipeandingredient removeRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().remove(recipeandingredient);
		recipeandingredient.setRecipe(null);

		return recipeandingredient;
	}

}