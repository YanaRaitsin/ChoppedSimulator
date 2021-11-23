package admin;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.persistence.Query;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.Foodtype;
import data.Ingredient;
import login.CustomButton;
import simulator.JpaConnection;

@SuppressWarnings("serial")
public class IngredientPanel extends JPanel implements ActionListener {

	private JTextField ingredientField;
	private JTextField foodTypeField;
	private JButton insertIngredientButton;
	
	private long totalFoodtypes;
	private long totalIngredients;
	
	public IngredientPanel(IngredientFrame ingredientFrame) {
		super();
		setBackground(Color.WHITE);
	}
	
	//init the panel with all the components (JTextField and JButton).
	public void initInsertNewIngredientPanel() {
		setLayout(new GridLayout(3, 2));
		add(new JLabel("Insert New Ingredient Name:"));
		add(ingredientField = new JTextField(35));
		add(new JLabel("Insert Food Type Of The Ingredient:"));
		add(foodTypeField = new JTextField(35));
		add(insertIngredientButton = new CustomButton("Insert New Ingredient"));
		insertIngredientButton.addActionListener(this);
	}
	
	/*
	 * checkNewIngredientValuesPanel() - 
	 * Checks if the text fields are empty - if empty, throw exception.
	 * if the text field contains digits and characters, it will throw exception.
	 */
	public void checkNewIngredientValuesPanel() throws Exception {
    	boolean checkIngredient = ingredientField.getText().matches("^[ A-Za-z]+$");
    	boolean checkFoodType = foodTypeField.getText().matches("^[ A-Za-z]+$");
		if(ingredientField.getText().equals("") || foodTypeField.getText().equals(""))
			throw new Exception("Please insert new ingredient name or food type");
		else if(!checkIngredient)
			throw new Exception("Please check the value of the new ingredient");
		else if(!checkFoodType)
			throw new Exception("Please check the value of the food type");
	}
	
	/*
	 * insertNewIngredientType(String insertedIngredient, String foodType) - 
	 * First, the function save all the food type. After saving all the food types, the function check if the food type exists.
	 * If exists - it will save the food type into list and than it will insert the new ingredient with the food type.
	 * If it's not exists - it creates new food type and than it creates the new ingredient that inserted with the new food type.
	 */
	@SuppressWarnings("unchecked")
	private void insertNewIngredientType(String insertedIngredient, String foodType) {
		List<Foodtype> foodTypes = new Vector<Foodtype>();
		List<Foodtype> insertedFoodTypeList = new Vector<Foodtype>();
		List<Ingredient> newIngredientsList = new Vector<Ingredient>();;
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			foodTypes = JpaConnection.getEntityManager().createQuery("SELECT ft FROM Foodtype ft").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		//if the food type exists - save the the food type.
		for(Foodtype checkFoodType : foodTypes) {
			if(checkFoodType.getName().equals(foodType))
				insertedFoodTypeList.add(checkFoodType);
		}
		
		//if the size of the list isn't zero - create the new ingredient with the existing food type.
		if(insertedFoodTypeList.size()!=0) {
			JpaConnection.initEntityManager();
			JpaConnection.getEntityManager().getTransaction().begin();
			try {
				Query query = JpaConnection.getEntityManager().createQuery("SELECT count(i) FROM Ingredient i");
				totalIngredients = (long) query.getSingleResult();
				totalIngredients++;
				Ingredient insertNewIngredient = new Ingredient((int) totalIngredients, insertedIngredient, insertedFoodTypeList);
				JpaConnection.getEntityManager().persist(insertNewIngredient);
				
				Foodtype insertedFoodType = JpaConnection.getEntityManager().find(Foodtype.class, insertedFoodTypeList.get(0).getId());
				newIngredientsList.add(insertNewIngredient);
				insertedFoodType.setIngredients(newIngredientsList);
				
				JpaConnection.getEntityManager().getTransaction().commit();
			} catch(Exception ex) {
				JpaConnection.getEntityManager().getTransaction().rollback();
			} finally {
				JpaConnection.closeEntityManager();
			}
		}
		//else - it will create the new food type and than creates the ingredient with the new food type.
		else {
			JpaConnection.initEntityManager();
			JpaConnection.getEntityManager().getTransaction().begin();
			try {
				Query query = JpaConnection.getEntityManager().createQuery("SELECT count(ft) FROM Foodtype ft");
				totalFoodtypes = (long) query.getSingleResult();
				totalFoodtypes++;
				Foodtype newFoodType = new Foodtype((int) totalFoodtypes, foodType);
				insertedFoodTypeList.add(newFoodType);
				query = JpaConnection.getEntityManager().createQuery("SELECT count(i) FROM Ingredient i");
				totalIngredients = (long) query.getSingleResult();
				totalIngredients++;
				Ingredient insertNewIngredient = new Ingredient((int) totalIngredients, insertedIngredient, insertedFoodTypeList);
				newIngredientsList.add(insertNewIngredient);
				newFoodType.setIngredients(newIngredientsList);
				JpaConnection.getEntityManager().persist(insertNewIngredient);
				JpaConnection.getEntityManager().persist(newFoodType);
				JpaConnection.getEntityManager().getTransaction().commit();
			} catch(Exception ex) {
				JpaConnection.getEntityManager().getTransaction().rollback();
			} finally {
				JpaConnection.closeEntityManager();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Insert New Ingredient")) {
			try {
				checkNewIngredientValuesPanel();
				insertNewIngredientType(ingredientField.getText(),foodTypeField.getText());
				JOptionPane.showMessageDialog(null, "New Ingredient Inserted");
				ingredientField.setText("");
				foodTypeField.setText("");
			} catch (Exception showMessage) {
				JOptionPane.showMessageDialog(null, showMessage.getMessage());
			}
		}
	}
	
}
