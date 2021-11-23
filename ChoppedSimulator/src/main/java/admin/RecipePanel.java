package admin;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.Foodtype;
import data.Ingredient;
import data.Measure;
import data.Recipe;
import data.Recipeandingredient;
import game.ChoppedSettings;
import login.CustomButton;
import simulator.JpaConnection;
import simulator.SimulationService;

@SuppressWarnings("serial")
public class RecipePanel extends JPanel implements ActionListener {
	
	private RecipeFrame recipeFrame;
	
	private JPanel optionsPanel;
	private JButton adminPanelButton;
	private JButton startSimulationButton;
	
	private JPanel selectRecipePanel;
	private JButton dinnerButton;
	private JButton dessertButton;
	private JButton goBackMainButton;
	
	private JPanel recipePanel;
	private JPanel dinnerPanel;
	private JButton addDinnerRecipeButton;
	private JButton insertDinnerButton;
	private JButton addDinnerIngredientsButton;
	private JButton insertNewIngredientsDinnerButton;
	
	private JPanel dessertPanel;
	private JButton addDesertIngredientsButton;
	private JButton insertDessertButton;
	private JButton addDessertRecipeButton;
	private JButton goBackToAdminPanelButton;
	private JButton insertNewIngredientsDessertButton;
	
	private JTextField recipeName;
	private JTextField recipeType;
	private JComboBox<String> ingredientBox;
	private JComboBox<String> amountBox;
	private JTextField amountField;
	
	private List<Ingredient> ingredients;
	private List<Measure> measures;
	private long totalRecipes;
	private static Recipe insertedRecipe;
	
	public RecipePanel(RecipeFrame recipeFrame) {
		super();
		this.recipeFrame = recipeFrame;
		setBackground(Color.WHITE);
	}
	
	public void initOptionsPanel() {
		setLayout(new GridLayout(1, 2));
		optionsPanel = new JPanel();
		optionsPanel.setBackground(Color.WHITE);
		optionsPanel.setLayout(new GridLayout(1, 2));
		optionsPanel.add(adminPanelButton = new CustomButton("Admin Panel"));
		optionsPanel.add(startSimulationButton = new CustomButton("Start Simulation"));
		add(optionsPanel);
		startSimulationButton.addActionListener(this);
		adminPanelButton.addActionListener(this);
	}
	
	public void initSelectRecipePanel() {
		selectRecipePanel = new JPanel();
		selectRecipePanel.setBackground(Color.WHITE);
		selectRecipePanel.setLayout(new GridLayout(1, 2));
		selectRecipePanel.add(dinnerButton = new CustomButton("Insert Dinner"));
		selectRecipePanel.add(dessertButton = new CustomButton("Insert Dessert"));
		selectRecipePanel.add(goBackMainButton = new CustomButton("Go Back To Admin Panel"));
		add(selectRecipePanel);
		dinnerButton.addActionListener(this);
		dessertButton.addActionListener(this);
		goBackMainButton.addActionListener(this);
	}
	
	public void initRecipePanel() {
		recipePanel = new JPanel();
		recipePanel.setBackground(Color.WHITE);
		recipePanel.setLayout(new GridLayout(3, 2));
		recipePanel.add(new JLabel("Insert Name:"));
		recipePanel.add(recipeName = new JTextField(35));
		recipePanel.add(new JLabel("Insert Recipe Type:"));
		recipePanel.add(recipeType = new JTextField(35));
		recipePanel.add(goBackToAdminPanelButton = new CustomButton("Go Back To Select Recipe"));
		add(recipePanel);
		goBackToAdminPanelButton.addActionListener(this);
	}
	
	public void initDinnerPanel() {
		setLayout(new GridLayout(1, 10));
		dinnerPanel = new JPanel();
		dinnerPanel.setBackground(Color.WHITE);
		dinnerPanel.setLayout(new GridLayout(5, 2));
		initDinnerComboBox();
		dinnerPanel.add(new JLabel("Select Ingredient:"));
		dinnerPanel.add(ingredientBox);
		dinnerPanel.add(new JLabel("Select Measure:"));
		dinnerPanel.add(amountBox);
		dinnerPanel.add(new JLabel("Insert Amount:"));
		dinnerPanel.add(amountField = new JTextField(35));
		dinnerPanel.add(addDinnerIngredientsButton = new CustomButton("Add Dinner Ingredient"));
		dinnerPanel.add(insertDinnerButton = new CustomButton("Insert Dinner Into Database"));
		dinnerPanel.add(insertNewIngredientsDinnerButton = new CustomButton("Insert New Dinner Ingredient"));
		add(dinnerPanel);
		addDinnerIngredientsButton.addActionListener(this);
		insertDinnerButton.addActionListener(this);
		insertNewIngredientsDinnerButton.addActionListener(this);
	}
	
	/*
	 * checkInsertedRecipe() - check the values of the recipe and the recipe type.
	 * if the values contains characters and digits - it will throw exception.
	 * if the values empty - the admin have to insert values into the text fields.
	 */
	public void checkInsertedRecipe() throws Exception {
    	boolean checkName = recipeName.getText().matches("^[ A-Za-z]+$");
		if(recipeType.getText().equals(""))
			throw new Exception("Please insert type");
		else if(recipeName.getText().equals(""))
			throw new Exception("Please insert recipe name");
		else if(!checkName) {
	    	throw new Exception("Please check the inserted recipe name");
		}
	}
	
	/*
	 * checkDinnerTypeTextField() - if the admin selected insert dinner - the recipe type have to be dinner.
	 * else - it will throw exception.
	 */
	public void checkDinnerTypeTextField() throws Exception {
		if(!recipeType.getText().equals("dinner"))
			throw new Exception("The type have to be dinner!");
	}
	
	/*
	 * checkDessertTypeTextField() - if the admin selected insert dessert - the recipe type have to be dessert.
	 * else - it will throw exception.
	 */
	public void checkDessertTypeTextField() throws Exception {
		if(!recipeType.getText().equals("dessert"))
			throw new Exception("The type have to be dessert!");
	}
	
	public void initDessertPanel() {
		setLayout(new GridLayout(1, 10));
		dessertPanel = new JPanel();
		dessertPanel.setBackground(Color.WHITE);
		dessertPanel.setLayout(new GridLayout(5, 2));
		initDessertComboBox();
		dessertPanel.add(new JLabel("Select Ingredient:"));
		dessertPanel.add(ingredientBox);
		dessertPanel.add(new JLabel("Select Measure:"));
		dessertPanel.add(amountBox);
		dessertPanel.add(new JLabel("Insert Amount:"));
		dessertPanel.add(amountField = new JTextField(35));
		dessertPanel.add(addDesertIngredientsButton = new CustomButton("Add Dessert Ingredient"));
		dessertPanel.add(insertDessertButton = new CustomButton("Insert Dessert Into Database"));
		dessertPanel.add(insertNewIngredientsDessertButton = new CustomButton("Insert New Dessert Ingredient"));
		add(dessertPanel);
		addDesertIngredientsButton.addActionListener(this);
		insertDessertButton.addActionListener(this);
		insertNewIngredientsDessertButton.addActionListener(this);
	}
	
	/*
	 * checkAmountValue() - the function checks the data that the admin inserted.
	 * if the field is empty - the admin have to insert data.
	 * if the text field have characters with digits - it will throw exception because the amount have to be digits only.
	 */
	public void checkAmountValue() throws Exception {
    	boolean checkAmount = amountField.getText().matches("[0-9]+");
		if(amountField.getText().equals(""))
			throw new Exception("Please insert amount");
		else if(!checkAmount)
			throw new Exception("The amount must be digits only.");
	}
	
	/*
	 * insertRecipe(String recipeName, String type) -
	 * The function create new recipe object with the data that inserted and than the function insert the object into database.
	 */
	private void insertRecipe(String recipeName, String type) {
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
	        Query query = JpaConnection.getEntityManager().createQuery("SELECT count(r) FROM Recipe r");
	        totalRecipes = (long) query.getSingleResult();
	        totalRecipes++;
	        insertedRecipe = new Recipe((int) totalRecipes, recipeName.toLowerCase(), type.toLowerCase());
			JpaConnection.getEntityManager().persist(insertedRecipe);
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * initIngredientsInRecipe(String ingredient, String measure, BigDecimal amount) - 
	 * The function first save the ingredient that selected and the measure.
	 * Than it creates new recipeandingredient object and with the new object, the function insert the new data into database.
	 */
	private void initIngredientsInRecipe(String ingredient, String measure, BigDecimal amount) {
		Ingredient addIngredient = new Ingredient();
		Measure addMeasure = new Measure();
		for(Ingredient saveIngredient: ingredients) {
			if(ingredient.equals(saveIngredient.getName()))
				addIngredient = saveIngredient;
		}
		for(Measure saveMeasure: measures) {
			if(measure.equals(saveMeasure.getName()))
				addMeasure = saveMeasure;
		}
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			Recipeandingredient addIngredients = new Recipeandingredient(amount,addIngredient,addMeasure,insertedRecipe);
			JpaConnection.getEntityManager().persist(addIngredients);
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * getSelectedIngredientCombo() and getSelectedAmountCombo() - it saves the value of the selected item in the comboBox.
	 */
	private String getSelectedIngredientCombo() {
		int n = ingredientBox.getItemCount();
		for(int i=0;i<n;i++) {
			if(ingredientBox.getSelectedIndex() == i)
				return ingredientBox.getItemAt(i);
		}
		return null;
	}
	
	private String getSelectedAmountCombo() {
		int n = amountBox.getItemCount();
		for(int i=0;i<n;i++) {
			if(amountBox.getSelectedIndex() == i)
				return amountBox.getItemAt(i);
		}
		return null;
	}
	
	/*
	 * initDinnerComboBox() - the function init the ComboBox with the values from the database (it init the ingredient of dinner).
	 * the function check if the ingredient can use for dinner recipe, if we can use this ingredient for dinner recipe, the function will add this ingredient into the comboBox.
	 */
	private void initDinnerComboBox() {
		boolean addToCombo = true;
		initLists();
		
		//init ingredients for dinner
		ingredientBox = new JComboBox<String>();
		for(Ingredient addIngredient : ingredients) {
			List<Foodtype> ingredientType = addIngredient.getFoodtypes();
			for(Foodtype currentType : ingredientType) {
				if(dessertIngredients(currentType.getName(),addIngredient.getName()))
					addToCombo = false;
			}
			if(addToCombo)
				ingredientBox.addItem(addIngredient.getName());
			addToCombo = true;
		}
	}
	
	/*
	 * initDessertComboBox() - the function init the ComboBox with the values from the database (it init the ingredient of dessert).
	 * the function check if the ingredient can use for dessert recipe, if we can use this ingredient for dessert recipe, the function will add this ingredient into the comboBox.
	 */
	private void initDessertComboBox() {
		boolean addToCombo = true;
		initLists();
		
		//init ingredients for dinner
		ingredientBox = new JComboBox<String>();
		for(Ingredient addIngredient : ingredients) {
			List<Foodtype> ingredientType = addIngredient.getFoodtypes();
			for(Foodtype currentType : ingredientType) {
				if(dinnerIngredients(currentType.getName(),addIngredient.getName()))
					addToCombo = false;
			}
			if(addToCombo)
				ingredientBox.addItem(addIngredient.getName());
			addToCombo = true;
		}
	}
	
	/*
	 * initLists - the function init all the ingredients into the list and the measures into the comboBox after getting the result list from db.
	 */
	@SuppressWarnings("unchecked")
	private void initLists() {
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			ingredients = JpaConnection.getEntityManager().createQuery("Select i from Ingredient i").getResultList();
			measures = JpaConnection.getEntityManager().createQuery("Select m from Measure m").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		
		//init all the measures into the comboBox.
		amountBox = new JComboBox<String>();
		for(Measure addMeasure : measures)
			amountBox.addItem(addMeasure.getName());
	}
	
	/*
	 * dessertIngredients(String currentType, String ingredientName) - check which ingredient used for dessert.
	 */
	private boolean dessertIngredients(String currentType, String ingredientName) {
		if(currentType.contains("cacao"))
			return true;
		else if(currentType.contains("vanilla bean"))
			return true;
		else if(currentType.contains("coconut"))
			return true;
		else if(currentType.contains("sugar"))
			return true;
		else if(currentType.contains("steviol glycosides"))
			return true;
		else if(ingredientName.contains("whipped cream"))
			return true;
		else if(ingredientName.contains("oats"))
			return true;
		else if(ingredientName.contains("maple syrup"))
			return true;
		else if(ingredientName.contains("cream cheese"))
			return true;
		else if(currentType.contains("fruit"))
			return true;
		else if(currentType.contains("coffee"))
			return true;
		else if(ingredientName.contains("yellow cake mix"))
			return true;
		return false;
	}
	
	/*
	 * dinnerIngredients(String currentType, String ingredientName) - check which ingredient used for dinner.
	 */
	private boolean dinnerIngredients(String currentType, String ingredientName) {
		if(currentType.equals("fish"))
			return true;
		else if(currentType.contains("meat"))
			return true;
		else if(currentType.contains("soy"))
			return true;
		else if(currentType.contains("poultry"))
			return true;
		else if(currentType.contains("beans"))
			return true;
		else if(currentType.contains("sesame"))
			return true;
		else if(currentType.contains("vegetable"))
			return true;
		else if(ingredientName.contains("coriander"))
			return true;
		else if(ingredientName.contains("cumin"))
			return true;
		else if(ingredientName.contains("olive"))
			return true;
		else if(ingredientName.contains("burger"))
			return true;
		else if(ingredientName.contains("spaghetti"))
			return true;
		else if(ingredientName.contains("parmesan"))
			return true;
		else if(ingredientName.contains("red wine"))
			return true;
		else if(ingredientName.contains("bay"))
			return true;
		else if(ingredientName.contains("garlic"))
			return true;
		else if(ingredientName.contains("oregano"))
			return true;
		else if(ingredientName.contains("chili"))
			return true;
		else if(ingredientName.contains("feta cheese"))
			return true;
		else if(ingredientName.contains("pepper"))
			return true;
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Start Simulation")) {
			try {
				recipeFrame.dispose();
				SimulationService.checkUserData();
				ChoppedSettings.startMenu();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Admin Panel")) {
			remove(optionsPanel);
			recipeFrame.getContentPane().remove(this);
			initSelectRecipePanel();
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Insert Dessert")) {
			remove(selectRecipePanel);
			recipeFrame.getContentPane().remove(this);
			initRecipePanel();
			recipePanel.add(addDessertRecipeButton = new CustomButton("Insert Dessert Ingredients"));
			addDessertRecipeButton.addActionListener(this);
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Go Back To Admin Panel")) {
			remove(selectRecipePanel);
			initOptionsPanel();
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Insert Dessert Ingredients")) {
			try {
				checkInsertedRecipe();
				checkDessertTypeTextField();
				insertRecipe(recipeName.getText(),recipeType.getText());
				remove(recipePanel);
				recipeFrame.getContentPane().remove(this);
				initDessertPanel();
				recipeFrame.getContentPane().add(this);
				recipeFrame.getContentPane().validate();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Go Back To Select Recipe")) {
			remove(recipePanel);
			recipeFrame.getContentPane().remove(this);
			initSelectRecipePanel();
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Add Dessert Ingredient")) {
			try {
				checkAmountValue();
				BigDecimal amountValue = new BigDecimal(amountField.getText());
				initIngredientsInRecipe(getSelectedIngredientCombo(),getSelectedAmountCombo(),amountValue);
				amountField.setText("");
			} catch(Exception addMessage) {
				JOptionPane.showMessageDialog(null, addMessage.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Insert Dessert Into Database")) {
		    String createFolder = "src/main/java/resources/recipes/"+insertedRecipe.getName();
			File file = new File(createFolder);
			if (!file.exists()) file.mkdir();
			JOptionPane.showMessageDialog(null, "Folder created. Please insert textures and gui's in the folder to run it in the simulation.");
			remove(dessertPanel);
			recipeFrame.getContentPane().remove(this);
			initOptionsPanel();
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Insert Dinner")) {
			remove(selectRecipePanel);
			recipeFrame.getContentPane().remove(this);
			initRecipePanel();
			recipePanel.add(addDinnerRecipeButton = new CustomButton("Insert Dinner Ingredients"));
			addDinnerRecipeButton.addActionListener(this);
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Insert Dinner Ingredients")) {
			try {
				checkInsertedRecipe();
				checkDinnerTypeTextField();
				insertRecipe(recipeName.getText(),recipeType.getText());
				remove(recipePanel);
				recipeFrame.getContentPane().remove(this);
				initDinnerPanel();
				recipeFrame.getContentPane().add(this);
				recipeFrame.getContentPane().validate();
			} catch (Exception recipeMessage) {
				JOptionPane.showMessageDialog(null, recipeMessage.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Add Dinner Ingredient")) {
			try {
				checkAmountValue();
				BigDecimal amountValue = new BigDecimal(amountField.getText());
				initIngredientsInRecipe(getSelectedIngredientCombo(),getSelectedAmountCombo(),amountValue);
				amountField.setText("");
			} catch(Exception showMessage) {
				JOptionPane.showMessageDialog(null, showMessage.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Insert Dinner Into Database")) {
		    String createFolder = "src/main/java/resources/recipes/"+insertedRecipe.getName();
			File file = new File(createFolder);
			if (!file.exists()) file.mkdir();
			JOptionPane.showMessageDialog(null, "Folder created. Please insert textures and gui's in the folder to run it in the simulation.");
			remove(dinnerPanel);
			recipeFrame.getContentPane().remove(this);
			initOptionsPanel();
			recipeFrame.getContentPane().add(this);
			recipeFrame.getContentPane().validate();
		}
		else if(e.getActionCommand().equals("Insert New Dinner Ingredient")) {
			try {
				@SuppressWarnings("unused")
				IngredientFrame ingredientFrame = new IngredientFrame();
			} catch (Exception initFrameException) {
				JOptionPane.showMessageDialog(null, initFrameException.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Insert New Dessert Ingredient")) {
			try {
				@SuppressWarnings("unused")
				IngredientFrame ingredientFrame = new IngredientFrame();
			} catch (Exception initFrameException) {
				JOptionPane.showMessageDialog(null, initFrameException.getMessage());
			}
		}
	}
}
