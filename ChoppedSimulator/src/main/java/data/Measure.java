package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the measure database table.
 * 
 */
@Entity
@NamedQuery(name="Measure.findAll", query="SELECT m FROM Measure m")
public class Measure implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Lob
	private String name;

	//bi-directional many-to-one association to Recipeandingredient
	@OneToMany(mappedBy="measure")
	private List<Recipeandingredient> recipeandingredients;

	public Measure() {
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

	public List<Recipeandingredient> getRecipeandingredients() {
		return this.recipeandingredients;
	}

	public void setRecipeandingredients(List<Recipeandingredient> recipeandingredients) {
		this.recipeandingredients = recipeandingredients;
	}

	public Recipeandingredient addRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().add(recipeandingredient);
		recipeandingredient.setMeasure(this);

		return recipeandingredient;
	}

	public Recipeandingredient removeRecipeandingredient(Recipeandingredient recipeandingredient) {
		getRecipeandingredients().remove(recipeandingredient);
		recipeandingredient.setMeasure(null);

		return recipeandingredient;
	}

}