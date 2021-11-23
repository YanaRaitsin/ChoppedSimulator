package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the dish database table.
 * 
 */
@Entity
@NamedQuery(name="Dish.findAll", query="SELECT d FROM Dish d")
public class Dish implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private int score;

	//bi-directional many-to-one association to Recipe
	@ManyToOne
	@JoinColumn(name="recipeid")
	private Recipe recipe;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="userid")
	private User user;

	public Dish() {
	}
	
	public Dish(User user, Recipe recipe, Date date, int score) {
		setUser(user);
		setRecipe(recipe);
		setDate(date);
		setScore(score);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Recipe getRecipe() {
		return this.recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}