package data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the allergies database table.
 * 
 */
@Entity
@Table(name="allergies")
@NamedQuery(name="Allergy.findAll", query="SELECT a FROM Allergy a")
public class Allergy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String allergy;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="allergies")
	private List<User> users;

	public Allergy() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAllergy() {
		return this.allergy;
	}

	public void setAllergy(String allergy) {
		this.allergy = allergy;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}