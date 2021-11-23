package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the ingredient database table.
 * 
 */
@Entity
@NamedQuery(name="Ingredient.findAll", query="SELECT i FROM Ingredient i")
public class Ingredient implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Lob
	private String name;

	//bi-directional many-to-many association to Foodtype
	@ManyToMany(mappedBy="ingredients")
	private List<Foodtype> foodtypes;

	//bi-directional many-to-one association to Recipeandingredient
	@OneToMany(mappedBy="ingredient")
	private List<Recipeandingredient> recipeandingredients;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="ingredients")
	private List<User> users;

	public Ingredient() {
	}

	public Ingredient(int id, String name, List<Foodtype> foodtypes) {
		super();
		this.id = id;
		this.name = name;
		setFoodtypes(foodtypes);
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

	public List<Foodtype> getFoodtypes() {
		return this.foodtypes;
	}

	public void setFoodtypes(List<Foodtype> foodtypes) {
		this.foodtypes = foodtypes;
	}

	public List<Recipeandingredient> getRecipeandingredients() {
		return this.recipeandingredients;
	}

	public void setRecipeandingredients(List<Recipeandingredient> recipeandingredients) {
		this.recipeandingredients = recipeandingredients;
	}

	public Recipeandingredient addRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().add(recipeandingredient);
		recipeandingredient.setIngredient(this);

		return recipeandingredient;
	}

	public Recipeandingredient removeRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().remove(recipeandingredient);
		recipeandingredient.setIngredient(null);

		return recipeandingredient;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}