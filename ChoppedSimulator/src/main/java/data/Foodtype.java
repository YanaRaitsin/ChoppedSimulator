package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the foodtype database table.
 * 
 */
@Entity
@NamedQuery(name="Foodtype.findAll", query="SELECT f FROM Foodtype f")
public class Foodtype implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String name;

	//bi-directional many-to-many association to Ingredient
	@ManyToMany
	@JoinTable(
		name="ingredienttype"
		, joinColumns={
			@JoinColumn(name="TypeId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="IngredientId")
			}
		)
	private List<Ingredient> ingredients;

	public Foodtype() {
	}

	public Foodtype(int id, String name) {
		super();
		this.id = id;
		this.name = name;
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

	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

}