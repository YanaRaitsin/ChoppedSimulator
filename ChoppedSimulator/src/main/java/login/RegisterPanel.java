package login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.persistence.Query;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import data.Allergy;
import data.Ingredient;
import data.Type;
import data.User;
import simulator.JpaConnection;
import utils.PasswordUtils;

@SuppressWarnings("serial")
public class RegisterPanel extends JPanel{
	
	@SuppressWarnings("unused")
	private RegisterFrame registerFrame;
	private static JTextField email;
	private static JPasswordField password;
	private static JCheckBox[] allergiesCheckBox;
	private static JCheckBox[] preferenceCheckBox;
	private static JRadioButton[] typesRadio;
	private static JRadioButton[] diabetesRadio;
	private ButtonGroup typesGroup;
	private ButtonGroup diabetesGroup;
	
	private List<String> saveAllergies;
	private String saveType;
	private List<String> savePreference;
	private long totalUsers;
	
	public RegisterPanel(RegisterFrame registerFrame) {
		super();
		this.registerFrame = registerFrame;
		this.typesGroup = new ButtonGroup();
		this.diabetesGroup = new ButtonGroup();
		this.saveAllergies = new Vector<String>();
		this.savePreference = new Vector<String>();
		setPreferredSize(new Dimension(380,750));
		setLayout(new GridLayout(4,2));
	}
	
	public void setSizePanel(int width, int height) {
		setPreferredSize(new Dimension(width,height));
	}
	
	public void initBasicInfo(JPanel basicPanel) {
		basicPanel.add(new JLabel("Enter Email:"));
		basicPanel.add(email = new JTextField(25));
		basicPanel.add(new JLabel("Enter Password:"));
		basicPanel.add(password = new JPasswordField(16));
		basicPanel.add(new JLabel("Do You Have A Diabetes?"));
		
		basicPanel.setBackground(Color.WHITE);
		
		diabetesRadio = new JRadioButton[2];
		diabetesRadio[0] = new JRadioButton("Yes");
		diabetesRadio[1] = new JRadioButton("No");
		
		diabetesGroup.add(diabetesRadio[0]);
		diabetesGroup.add(diabetesRadio[1]);
		
		diabetesRadio[0].setBackground(Color.WHITE);
		diabetesRadio[1].setBackground(Color.WHITE);
		
		basicPanel.add(diabetesRadio[0]);
		basicPanel.add(diabetesRadio[1]);
	}
	
	@SuppressWarnings("unchecked")
	public void initAllergiesCheckBox(JPanel allergiesPanel) {
		allergiesPanel.add(new JLabel("Select Which Allergies You Have:"));
		allergiesPanel.setBackground(Color.WHITE);
		JpaConnection.initEntityManager();
		long totalAllergies = (long) JpaConnection.getEntityManager().createQuery("Select count(a) from Allergy a").getSingleResult();
		allergiesCheckBox = new JCheckBox[(int) totalAllergies];
		List<Allergy> allergiesName = JpaConnection.getEntityManager().createQuery("SELECT a FROM Allergy a").getResultList();
		for(int i=0; i<allergiesCheckBox.length;i++) {
			allergiesCheckBox[i] = new JCheckBox(allergiesName.get(i).getAllergy());
			allergiesCheckBox[i].setBackground(Color.WHITE);
			allergiesPanel.add(allergiesCheckBox[i]);
		}
		JpaConnection.closeEntityManager();
	}
	
	public void initTypes(JPanel typesPanel) {
		JpaConnection.initEntityManager();
		typesPanel.add(new JLabel("Select Which Type Of Food You Like:"));
		typesPanel.setBackground(Color.WHITE);
		long totalTypes = (long) JpaConnection.getEntityManager().createQuery("Select count(t) from Type t").getSingleResult();
		typesRadio = new JRadioButton[(int) totalTypes];
		@SuppressWarnings("unchecked")
		List<Type> typesName = JpaConnection.getEntityManager().createQuery("Select t from Type t").getResultList();
		for(int i=0;i<typesRadio.length;i++) {
			typesRadio[i] = new JRadioButton(typesName.get(i).getType());
			typesRadio[i].setBackground(Color.WHITE);
			typesPanel.add(typesRadio[i]);
			typesGroup.add(typesRadio[i]);
		}
		JpaConnection.closeEntityManager();
	}
	
	public void initPreference(JPanel preferencePanel) {
		JpaConnection.initEntityManager();
		preferencePanel.add(new JLabel("Select Which Ingredients You Don't Like:"));
		preferencePanel.setBackground(Color.WHITE);
		long totalIngredients = (long) JpaConnection.getEntityManager().createQuery("Select count(i) from Ingredient i").getSingleResult();
		preferenceCheckBox = new JCheckBox[(int) totalIngredients];
		@SuppressWarnings("unchecked")
		List<Ingredient> ingredientsName = JpaConnection.getEntityManager().createQuery("select i from Ingredient i").getResultList();
		for(int i=0;i<preferenceCheckBox.length;i++) {
			preferenceCheckBox[i] = new JCheckBox(ingredientsName.get(i).getName());
			preferenceCheckBox[i].setBackground(Color.WHITE);
			preferencePanel.add(preferenceCheckBox[i]);
		}
		JpaConnection.closeEntityManager();
	}
	
	/*
	 * checkRegistration() - check the registration input and throw exception if:
	 * If nothing is selected or the text fields are empty.
	 * If the email is already used.
	 * If the email that given is not in the right pattern.
	 */
	public void checkRegistration() throws Exception {
		if(email.getText().equals("") && password.getPassword().length==0 && !diabetesRadio[0].isSelected() && !diabetesRadio[1].isSelected() && !checkTypeSelection() && !checkAllergiesSelection())
			throw new Exception("Please Insert Data");	
		else if(!isValid(email.getText())) 
			throw new Exception("Invalid Email! Please Check Your Email");
		else if(password.getPassword().length==0)
			throw new Exception("Please Insert Password");
		else if(!diabetesRadio[0].isSelected() && !diabetesRadio[1].isSelected())
			throw new Exception("Please Select Yes/No In The Diabetes Section");
		else if(!checkTypeSelection())
			throw new Exception("Please Choose Which Type Of Food Do You Like");
		else if(!checkAllergiesSelection())
			throw new Exception("Please Choose Which Allergies Do You Have");
		else if(checkAllergiesInvalidSelection())
			throw new Exception("You Cannot Select Allergies When None Selected! Please Check Your Selection");
		else if(isEmailUsed(email.getText()))
			throw new Exception("Email Already Registered");
	}
	
	private boolean checkTypeSelection() {
		for(JRadioButton type : typesRadio) {
			if(type.isSelected()) {
				saveType = type.getText();
				return true;
			}
		}
		return false;
	}
	
	private boolean checkAllergiesSelection() {
		for(JCheckBox allergy : allergiesCheckBox) {
			if(allergy.isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkAllergiesInvalidSelection() {
		boolean valid = false;
		for(int i=0; i<8; i++) {
			if(allergiesCheckBox[i].isSelected())
				valid = true;
		}
		if(allergiesCheckBox[9].isSelected() && valid)
			return true;
		return false;
	}
	
	/*
	 * isEmailUsed(String email) - if the email is already exists in the database.
	 */
	@SuppressWarnings("unchecked")
	private boolean isEmailUsed(String email) {
		List<User> users = null;
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			users = JpaConnection.getEntityManager().createQuery("SELECT u from User u").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e2) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		
		for(User user : users) {
			if(user.getEmail().equals(email))
				return true;
		}
		return false;
	}
	
	private void checkPreferenceSelection() {
		for(JCheckBox preference : preferenceCheckBox) {
			if(preference.isSelected())
				savePreference.add(preference.getText());
		}
	}
	
	/*
	 * isValid(String email) - check the email that given by the user if it's valid.
	 */
	public boolean isValid(String email) { 
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
	                            "[a-zA-Z0-9_+&*-]+)*@" + 
	                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
	                            "A-Z]{2,7}$"; 
		Pattern pat = Pattern.compile(emailRegex); 
		if (email == null) 
			return false; 
		return pat.matcher(email).matches(); 
	} 
	
	/*
	 * checkDiabetes() - if diabetes is selected or not.
	 */
	private String checkDiabetes() {
		if(diabetesRadio[0].isSelected())
			return diabetesRadio[0].getText();
		else if(diabetesRadio[1].isSelected())
			return diabetesRadio[1].getText();
		return "";
	}
	
	/*
	 * insertNewUser() - insert all the allergies, email, password, preference and type that given by the user in the registration screen.
	 */
	@SuppressWarnings("unchecked")
	public void insertNewUser() {
		try {
			checkRegistration();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String lowerCaseEamil = email.getText().toLowerCase();
		String savePassword = new String(password.getPassword());
		JpaConnection.initEntityManager();
		List<Allergy> allergies = JpaConnection.getEntityManager().createQuery("SELECT a FROM Allergy a").getResultList();
		List<Type> types = JpaConnection.getEntityManager().createQuery("Select t from Type t").getResultList();
		List<Ingredient> ingredients = JpaConnection.getEntityManager().createQuery("select i from Ingredient i").getResultList();
		
		List<Allergy> addAllergy = new Vector<Allergy>();
		List<Type> addType = new Vector<Type>();
		List<Ingredient> addPreference = new Vector<Ingredient>();
		
		for(JCheckBox allergy : allergiesCheckBox) {
			if(allergy.isSelected()) {
				saveAllergies.add(allergy.getText());
			}
		}
		
		for(Allergy findAllergy : allergies) {
			for(String allergy : saveAllergies) {
				if(findAllergy.getAllergy().equals(allergy)) {
					addAllergy.add(findAllergy);
				}
			}
		}
		for(Type findType : types) {
			if(findType.getType().equals(saveType)) {
				addType.add(findType);
			}
		}
		
		checkPreferenceSelection();
		if(savePreference.size()!=0) {
			for(Ingredient findIngredient : ingredients) {
				for(String preference : savePreference) {
					if(findIngredient.getName().equals(preference)) {
						addPreference.add(findIngredient);
					}
				}
			}
		}
		String salt = PasswordUtils.getSalt(30);
		String securePassword = PasswordUtils.generateSecurePassword(savePassword, salt);
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
	        Query query = JpaConnection.getEntityManager().createQuery("SELECT count(u) FROM User u");
	        totalUsers = (long) query.getSingleResult();
	        totalUsers++;
			User user = new User((int) totalUsers,lowerCaseEamil,securePassword,checkDiabetes(),salt,addAllergy,addPreference,addType);
			JpaConnection.getEntityManager().persist(user);
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception insertMessage) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
}
