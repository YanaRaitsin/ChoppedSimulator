package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name="users")
@NamedQuery(name="User.findAll", query="SELECT u FROM User u")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Lob
	private String diabetes;

	private String email;

	private String password;

	private String salt;

	//bi-directional many-to-one association to Dish
	@OneToMany(mappedBy="user")
	private List<Dish> dishs;

	//bi-directional many-to-many association to Allergy
	@ManyToMany
	@JoinTable(
		name="userallergies"
		, joinColumns={
			@JoinColumn(name="userId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="allergiesId")
			}
		)
	private List<Allergy> allergies;

	//bi-directional many-to-many association to Ingredient
	@ManyToMany
	@JoinTable(
		name="userpreference"
		, joinColumns={
			@JoinColumn(name="userId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="IngredientId")
			}
		)
	private List<Ingredient> ingredients;

	//bi-directional many-to-many association to Type
	@ManyToMany
	@JoinTable(
		name="usertype"
		, joinColumns={
			@JoinColumn(name="userId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="typeId")
			}
		)
	private List<Type> types;

	public User() {
	}
	
	public User(int id,String email, String password, String diabetes, String salt, List<Allergy> allergies,
			List<Ingredient> ingredients, List<Type> types) {
		setId(id);
		setEmail(email);
		setPassword(password);
		setDiabetes(diabetes);
		setAllergies(allergies);
		setIngredients(ingredients);
		setTypes(types);
		setSalt(salt);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDiabetes() {
		return this.diabetes;
	}

	public void setDiabetes(String diabetes) {
		this.diabetes = diabetes;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return this.salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public List<Dish> getDishs() {
		return this.dishs;
	}

	public void setDishs(List<Dish> dishs) {
		this.dishs = dishs;
	}

	public Dish addDish(Dish dish) {
		getDishs().add(dish);
		dish.setUser(this);

		return dish;
	}

	public Dish removeDish(Dish dish) {
		getDishs().remove(dish);
		dish.setUser(null);

		return dish;
	}

	public List<Allergy> getAllergies() {
		return this.allergies;
	}

	public void setAllergies(List<Allergy> allergies) {
		this.allergies = allergies;
	}

	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	public List<Type> getTypes() {
		return this.types;
	}

	public void setTypes(List<Type> types) {
		this.types = types;
	}

}