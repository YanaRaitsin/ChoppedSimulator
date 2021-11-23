package simulator;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.persistence.Query;

import org.joml.Vector3f;

import algorithm.DecisionTree;
import algorithm.ID3;
import data.Allergy;
import data.Dish;
import data.Foodtype;
import data.Ingredient;
import data.Measure;
import data.Recipe;
import data.Recipeandingredient;
import data.Type;
import data.User;
import engine.Scene;
import entities.Bowl;
import entities.Cookware;
import entities.FoodProcessor;
import entities.IngredientData;
import entities.Tray;
import graph.Material;
import graph.Mesh;
import graph.Texture;
import items.GameItem;
import loaders.OBJLoader;
import utils.Logs;
import utils.ScoreCalculator;

public class SimulationService {
	private static List<String> notEdible;
	private static List<Recipe> saveRecipes;
	private static List<String> instructions;
	private static Vector<GameItem> gameItems;
	private static Map<Ingredient, Measure> saveIngredientsData;
	private static float reflectance = 5f;
	private static long totalIngredients;
	//ingredients - save the game item for the simulation will save the data about the ingredient.
	private static Vector<IngredientData> ingredients;
	private static Recipe recipeChosen;
	
	private static Query query;
	private static User user;
	private static Vector3f savePosition = new Vector3f();
	private static int score;
	
	private static boolean startSimlation;
	private static boolean showHudScore;
	//If the player clicked on the continue button (if the simulation crashed) -> the simulation will load the last session.
	private static boolean loadLog;
	//check on every add which ingredient is added on each entity to calculate the score.
	private static List<ScoreCalculator> ingredientsInBowl;
	private static List<ScoreCalculator> ingredientsInCookingPot;
	private static List<ScoreCalculator> ingredientsInCookingPan;
	private static List<ScoreCalculator> ingredientsInBlender;
	private static List<ScoreCalculator> ingredientsInMixer;
	private static List<ScoreCalculator> ingredientsOnTray;
	private static List<ScoreCalculator> ingredientsOnDish;
	private static Tray checkTray;
	private static Cookware checkCookingPot;
	private static Cookware checkCookingPan;
	
	private static float xSubstitute;
	
	/*
	 * checkUserData - 
	 * Checking data of the user, if the user doesn't have any allergy/type/preference,
	 * We will insert all the recipes.
	 * If the user have allergy/type/preference - the simulation will build a decision tree.
	 */
	@SuppressWarnings("unchecked")
	public static void checkUserData() {
		List<Recipe> recipes = new Vector<Recipe>();
		boolean hasAllegy = true;
		boolean hasType = true;
		boolean hasPreference = false;
		
		List<Allergy> checkUserAllergy = user.getAllergies();
		for(Allergy userAllergy : checkUserAllergy) {
			if(userAllergy.getAllergy().equals("none"))
				hasAllegy = false;
		}
		
		List<Type> checkUserType = user.getTypes();
		for(Type userType : checkUserType) {
			if(userType.getType().equals("none"))
				hasType = false;
		}
		
		List<Ingredient> checkUserPreference = user.getIngredients();
		for(Ingredient userPreference : checkUserPreference) {
			if(!CsvDataService.substituteForIngredients(userPreference))
				hasPreference = true;
		}
		
		if(!hasPreference && !hasAllegy && !hasType && user.getIngredients().size()==0) {
			JpaConnection.initEntityManager();
			JpaConnection.getEntityManager().getTransaction().begin();
			try {
			recipes = JpaConnection.getEntityManager().createQuery("Select r from Recipe r").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				JpaConnection.closeEntityManager();
			}
			/*Becuase in the admin panel it only creates empty folder - The simulation will check if there is any empty folder.
			If there is an empty folder - the recipe will not show up in the simulatuon.*/
			List<Recipe> checkFiles = new Vector<Recipe>(recipes);
			saveRecipes = new Vector<Recipe>();
			for(Recipe checkRecipe : checkFiles) {
				File f= new File("src/main/java/resources/recipes/"+checkRecipe.getName());
				if(f.exists()) {
					File[] listOfFiles = f.listFiles(); 
					if(listOfFiles.length > 0){
					//if not empty - add to the saveRecipes Vector.
						saveRecipes.add(checkRecipe);
					}
				}
			}
		}
		else 
			buildRecipes();
	}
	
	/*
	 * saveDataFromDecisionTree:
	 * The function creates the csv file and from the csv file it builds the decision tree.
	 * From the decision tree, the simulation saves all the ingredients that the player can't eat (or the player prefer not to eat).
	 */
	private static void saveDataFromDecisionTree() {
		CsvFileCreator.createDatasetFile();
		ID3 id3 = new ID3();
		try {
			notEdible = new Vector<String>();
			DecisionTree resultingTree = id3.runAlgorithm("resTree/dataset.csv", "eat");
			//resultingTree.print();
			notEdible = resultingTree.searchNode(resultingTree.root, "", "no");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/*
	 * buildRecipes:
	 * The function saves all the recipes that the player can make in the simulation.
	 * First, it loads the data from the decision tree and than the function checks in each recipe, which ingredients the player can't eat.
	 * What the player can't eat - the simulation will not save the recipe into list, it will save only what the player can.
	 */
	@SuppressWarnings("unchecked")
	private static void buildRecipes() {
		List<Recipe> checkRecipes = new Vector<Recipe>();
		List<Recipeandingredient> checkIngredients = new Vector<Recipeandingredient>();
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
		checkRecipes = JpaConnection.getEntityManager().createQuery("Select r from Recipe r").getResultList();
		JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		saveDataFromDecisionTree();
		boolean canBeAdded;
		
		saveRecipes = new Vector<Recipe>();
		for(Recipe checkRecipe : checkRecipes) {
			canBeAdded = true;
			checkIngredients = checkRecipe.getRecipeandingredients();
			for(Recipeandingredient checkIngredient : checkIngredients) {
				for(String checkEdible : notEdible) {
					if(checkIngredient.getIngredient().getName().equals(checkEdible))
						canBeAdded = false;
				}
			}
			/*Becuase in the admin panel it only creates empty folder - The simulation will check if there is any empty folder.
			If there is an empty folder - the recipe will not show up in the simulatuon. (The same after the tree builder)*/
			File f= new File("src/main/java/resources/recipes/"+checkRecipe.getName());
			File[] listOfFiles = f.listFiles(); 
			if(canBeAdded && listOfFiles.length > 0) //if there is files and the user can eat the ingredient -> add into the Vector the recipe.
				saveRecipes.add(checkRecipe);
		}
	}
	
	/*
	 * countIngredientsInRecipe - 
	 * the function count all the ingredients in the recipe so later in the simulation, it will check if the player used all the ingredients as in the recipe (to check the score after the simulation).
	*/
	public static int countIngredientsInRecipe() {
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			query = JpaConnection.getEntityManager().createNativeQuery("select count(r.ingredient_id) from recipeandingredient r where r.recipe_id= ?recipeId");
			query.setParameter("recipeId", recipeChosen.getId());
			totalIngredients = (long) query.getSingleResult();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		return (int) totalIngredients;
	}
	/*
	 * loadIngredientsFromRecipe - 
	 * The function will save all the ingredients from the recipe the player choose so we can init the ingredients in the simulation.
	 */	
	@SuppressWarnings("unchecked")
	public static void loadIngredientsFromRecipe() {
		saveIngredientsData = new LinkedHashMap<Ingredient, Measure>();
		List<Recipeandingredient> recipeData = new Vector<Recipeandingredient>();
		List<Recipeandingredient> recipes = new Vector<Recipeandingredient>();
		JpaConnection.initEntityManager();
		try {
			JpaConnection.getEntityManager().getTransaction().begin();
			recipes = JpaConnection.getEntityManager().createQuery("Select r from Recipeandingredient r").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		for(Recipeandingredient loadRecipe : recipes) {
			if(recipeChosen.getId() == loadRecipe.getRecipe().getId())
				recipeData.add(loadRecipe);
		}
		for(Recipeandingredient ingredients : recipeData)
			saveIngredientsData.put(ingredients.getIngredient(), ingredients.getMeasure());
	}
	
	/*
	 * saveInstructions - 
	 * The function saves into vector of string the instructions from the txt file so the simulaction will check if the player follow the instructions of the recipe.
	 */
	public static void saveInstructions() throws Exception {
		instructions = new ArrayList<String>();
		File recipeFile = new File("src/main/java/resources/recipesTextFiles/"+recipeChosen.getName()+".txt");
		if(!recipeFile.exists())
			throw new Exception("Can't load recipe, file not found");
		Scanner s = new Scanner(recipeFile);
		while(s.hasNext()) 
			instructions.add(s.nextLine());
		s.close();
	}
	
	/*
	 * All the setters of the Kitchen Tool:
	 * The simulation will only init the kitchen tools that the player need for the recipe.
	 */
	private static void initIngredients() throws Exception {
		loadIngredientsFromRecipe();
		ingredients = new Vector<IngredientData>();
		xSubstitute = 16f;
		float x = 50;
		for(Map.Entry<Ingredient, Measure> loadIngredient : saveIngredientsData.entrySet()) {
	        Mesh ingredientMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture ingredientTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/" + loadIngredient.getKey().getName() + ".png");
	        Material ingredientMaterial = new Material(ingredientTexture, reflectance);
	        ingredientMesh.setMaterial(ingredientMaterial);
	        GameItem ingredient= new GameItem(loadIngredient.getKey().getName(),ingredientMesh);
	        ingredient.setPosition(x, 7.25f, 5f);
	        gameItems.add(ingredient);
	        ingredients.add(new IngredientData(ingredient, loadIngredient.getKey().getFoodtypes(), loadIngredient.getValue().getName()));
	        x-=1.8f;
	        loadSubstituteForIngredients(loadIngredient.getKey(),loadIngredient.getValue());
		}
	}
	
	private static void setBowl() throws Exception {
		Mesh bowl = OBJLoader.loadMesh("/resources/models/bowl.obj");
		Texture bowlTexture = new Texture("/resources/textures/bowlTexture.png",2,1);
		Material bowlMaterial = new Material(bowlTexture, reflectance);
		bowl.setMaterial(bowlMaterial);
		GameItem gameItem = new GameItem("bowl",bowl);
		gameItem.setPosition(10f, 7f, 6f);
		gameItems.add(gameItem);
	}
	
	private static void setFryPan() throws Exception {
		Mesh fryPan = OBJLoader.loadMesh("/resources/models/fryPan.obj");
		Texture fryPanTexture = new Texture("/resources/textures/frypanTexture.png");
		Material fryPanMaterial = new Material(fryPanTexture,reflectance);
		fryPan.setMaterial(fryPanMaterial);
		GameItem gameItem = new GameItem("cooking pan",fryPan);
		gameItem.setPosition(67.5f, 10.1f, 17f);
		gameItems.add(gameItem);
	}
	
	private static void setCookingPot() throws Exception {
		Mesh cookingPot = OBJLoader.loadMesh("/resources/models/cookingPot.obj");
		Texture cookingPotTexture = new Texture("/resources/textures/cookingpotTexture.png", 2, 1);
		Material cookingPotMaterial = new Material(cookingPotTexture,reflectance);
		cookingPot.setMaterial(cookingPotMaterial);
		GameItem gameItem = new GameItem("cooking pot",cookingPot);
		gameItem.setPosition(67.5f, 10.06f, 22f);
		gameItems.add(gameItem);
	}
	
	private static void setSpoon() throws Exception {
		Mesh spoon = OBJLoader.loadMesh("/resources/models/spoon.obj");
		Texture spoonTexture = new Texture("/resources/textures/cookingpotTexture.png", 2, 1);
		Material spoonMaterial = new Material(spoonTexture,reflectance);
		spoon.setMaterial(spoonMaterial);
		GameItem gameItem = new GameItem("spoon",spoon);
		gameItem.setPosition(12f, 7.14f, 7f);
		gameItems.add(gameItem);
	}
	
	private static void setSpatula() throws Exception {
		Mesh spatula = OBJLoader.loadMesh("/resources/models/spatula.obj");
		Texture spatulaTexture = new Texture("/resources/textures/cookingpotTexture.png", 2, 1);
		Material spatulaMaterial = new Material(spatulaTexture,reflectance);
		spatula.setMaterial(spatulaMaterial);
		GameItem gameItem = new GameItem("spatula",spatula);
		gameItem.setPosition(14.5f, 7.14f, 7f);
		gameItems.add(gameItem);
	}
	
	private static void setTeaSpoon() throws Exception {
		Mesh teaSpoon = OBJLoader.loadMesh("/resources/models/teaspoon.obj");
		Texture teaSpoonTexture = new Texture("/resources/textures/cookingpotTexture.png");
		Material teaSpoonMaterial = new Material(teaSpoonTexture,reflectance);
		teaSpoon.setMaterial(teaSpoonMaterial); 
		GameItem gameItem = new GameItem("tea spoon",teaSpoon);
		gameItem.setPosition(13f, 7.12f, 7f);
		gameItems.add(gameItem);
	}
	
	private static void setTray() throws Exception {
		Mesh tray = OBJLoader.loadMesh("/resources/models/tray.obj");
		Texture trayTexture = new Texture("/resources/textures/trayTexture.png");
		Material trayMaterial = new Material(trayTexture,reflectance);
		tray.setMaterial(trayMaterial);
		GameItem gameItem = new GameItem("tray",tray);
		gameItem.setPosition(61f, 7.2f, 6f);
		gameItems.add(gameItem);
	}
	
	private static void setMixer() throws Exception {
		Mesh mixerMesh = OBJLoader.loadMesh("/resources/models/mixer.obj");
		Texture mixerTexture = new Texture("/resources/textures/blenderTexture.png", 2, 1);
		Material mixerMaterial = new Material(mixerTexture,reflectance);
		mixerMesh.setMaterial(mixerMaterial);
		GameItem mixer = new GameItem("mixer",mixerMesh);
		mixer.setPosition(70f, 7f, 6f);
		gameItems.add(mixer);
	}
	
	private static void setBlender() throws Exception {
		Mesh blenderMesh = OBJLoader.loadMesh("/resources/models/blender.obj");
		Texture blenderTexture = new Texture("/resources/textures/blenderTexture.png", 2, 1);
		Material blenderMaterial = new Material(blenderTexture,reflectance);
		blenderMesh.setMaterial(blenderMaterial);
		GameItem blender = new GameItem("blender",blenderMesh);
		blender.setPosition(70f, 7f, 12f);
		gameItems.add(blender);
	}
	
	public static void initKitchenEntities() throws Exception {
		saveInstructions();
		gameItems = new Vector<GameItem>();
		
		gameItems = new Vector<GameItem>();
		Mesh wallRightMesh = OBJLoader.loadMesh("/resources/models/wall.obj");
		Texture wallRightTexture = new Texture("/resources/textures/walltexture.png");
		Material wallRightMaterial = new Material(wallRightTexture, reflectance);
		wallRightMesh.setMaterial(wallRightMaterial);
		GameItem wallRight = new GameItem("wall right",wallRightMesh);
		wallRight.setPosition(37.5f, 0, 0);
		gameItems.add(wallRight); //0
		
		Mesh wallLeftMesh = OBJLoader.loadMesh("/resources/models/wall.obj");
		Texture wallLeftTexture = new Texture("/resources/textures/walltexture.png");
		Material wallLeftMaterial = new Material(wallLeftTexture, reflectance);
		wallLeftMesh.setMaterial(wallLeftMaterial);
		GameItem wallLeft = new GameItem("wall left",wallLeftMesh);
		wallLeft.setPosition(37.5f, 0, 75f);
		gameItems.add(wallLeft); //1
		
		Mesh wallFrontMesh = OBJLoader.loadMesh("/resources/models/wallfront.obj");
		Texture wallFrontTexture = new Texture("/resources/textures/walltexture.png");
		Material wallFrontMaterial = new Material(wallFrontTexture, reflectance);
		wallFrontMesh.setMaterial(wallFrontMaterial);
		GameItem wallFront = new GameItem("wall front",wallFrontMesh);
		wallFront.setPosition(75f, 0, 37.5f);
		gameItems.add(wallFront); //2
		
		Mesh wallBackMesh = OBJLoader.loadMesh("/resources/models/wallfront.obj");
		Texture wallBackTexture = new Texture("/resources/textures/walltexture.png",2,1);
		Material wallBackMaterial = new Material(wallBackTexture, reflectance);
		wallBackMesh.setMaterial(wallBackMaterial);
		GameItem wallBack = new GameItem("wall back",wallBackMesh);
		wallBack.setPosition(0, 0, 37.5f);
		gameItems.add(wallBack); //3
		
		Mesh kitchenMesh = OBJLoader.loadMesh("/resources/models/kitchen.obj");
		Texture kitchenTexture = new Texture("/resources/textures/kitchentexture.png",2,1);
		Material kitchenMaterial = new Material(kitchenTexture, reflectance);
		kitchenMesh.setMaterial(kitchenMaterial);
		GameItem kitchen = new GameItem("kitchen",kitchenMesh);
		kitchen.setPosition(68f, 0.5f, -3f);
		gameItems.add(kitchen); //4
		
		Mesh fridgeMesh = OBJLoader.loadMesh("/resources/models/fridge.obj");
		Texture fridgeTexture = new Texture("/resources/textures/kitchentexture.png",2,1);
		Material fridgeMaterial = new Material(fridgeTexture, reflectance);
		fridgeMesh.setMaterial(fridgeMaterial);
		GameItem fridge = new GameItem("fridge",fridgeMesh);
		fridge.setPosition(70f, 0.5f, 28.5f);
		gameItems.add(fridge); //5
		
		Mesh ovenMesh = OBJLoader.loadMesh("/resources/models/oven.obj");
		Texture ovenTexture = new Texture("/resources/textures/frypanTexture.png", 2, 1);
		Material ovenMaterial = new Material(ovenTexture, reflectance);
		ovenMesh.setMaterial(ovenMaterial);
		GameItem oven = new GameItem("oven",ovenMesh);
		oven.setPosition(70f, 5f, 20f);
		gameItems.add(oven); //6
	    
		Mesh plateMesh = OBJLoader.loadMesh("/resources/models/plate.obj");
		Texture plateTexture = new Texture("/resources/textures/platetexture.png", 2, 1);
		Material plateMaterial = new Material(plateTexture,reflectance);
		plateMesh.setMaterial(plateMaterial);
		GameItem plate = new GameItem("dish",plateMesh);
		plate.setPosition(55f, 7.3f, 5f);
		gameItems.add(plate);
		
		initIngredients();
		if(instructions.get(0).contains("bowl"))
			setBowl();
		if(instructions.get(0).contains("cooking pan"))
			setFryPan();
		if(instructions.get(0).contains("spoon"))
			setSpoon();
		if(instructions.get(0).contains("tea spoon"))
			setTeaSpoon();
		if(instructions.get(0).contains("spatula"))
			setSpatula();
		if(instructions.get(0).contains("cooking pot"))
			setCookingPot();
		if(instructions.get(0).contains("tray"))
			setTray();
		if(instructions.get(0).contains("blender"))
			setBlender();
		if(instructions.get(0).contains("mixer"))
			setMixer();
	}
	
	/*
	 * loadSubstituteForIngredients -
	 * If the user have preference (or even the user don't any - the user can choose substitute if he want) and there is substitute for the ingredient - add the substitute and the original ingredient.
	 * If there is no substitute - the recipe will not show up in the menu.
	 */
	private static void loadSubstituteForIngredients(Ingredient checkIngredient, Measure mesure) throws Exception {
		if(checkIngredient.getName().equals("eggs")) {
			if(recipeChosen.getType().equals("dessert")) {
				Mesh bananaMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
				Texture bananaTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/banana.png");
				Material bananaMaterial = new Material(bananaTexture, reflectance);
				bananaMesh.setMaterial(bananaMaterial);
				GameItem bananaIngredient= new GameItem("banana",bananaMesh);
				bananaIngredient.setPosition(xSubstitute, 7.2f, 5); 
				gameItems.add(bananaIngredient);
				ingredients.add(new IngredientData(bananaIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
		        xSubstitute+=1.8f;
			}
	        Mesh yogurtMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture yogurtTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/greek yogurt.png");
	        Material yogurtMaterial = new Material(yogurtTexture, reflectance);
	        yogurtMesh.setMaterial(yogurtMaterial);
	        GameItem yogurtIngredient= new GameItem("greek yogurt",yogurtMesh);
	        yogurtIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(yogurtIngredient);
	        ingredients.add(new IngredientData(yogurtIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
	        Mesh groundFlaxseedMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture groundFlaxseedTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/ground flaxseed.png");
	        Material groundFlaxseedMaterial = new Material(groundFlaxseedTexture, reflectance);
	        groundFlaxseedMesh.setMaterial(groundFlaxseedMaterial);
	        GameItem flexSeedIngredient= new GameItem("ground flaxseed",groundFlaxseedMesh);
	        flexSeedIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(flexSeedIngredient);
	        ingredients.add(new IngredientData(flexSeedIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
		}
		else if(checkIngredient.getName().equals("organic palm sugar") || checkIngredient.getName().equals("golden caster sugar") || checkIngredient.getName().equals("coconut sugar") || checkIngredient.getName().equals("powdered sugar") || checkIngredient.getName().equals("sugar") || checkIngredient.getName().equals("brown sugar")) {
	        Mesh steviaMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture steviaTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/stevia.png");
	        Material steviaMaterial = new Material(steviaTexture, reflectance);
	        steviaMesh.setMaterial(steviaMaterial);
	        GameItem steviaIngredient= new GameItem("stevia",steviaMesh);
	        steviaIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(steviaIngredient);
	        ingredients.add(new IngredientData(steviaIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
		}
		else if(checkIngredient.getName().equals("milk")) {
	        Mesh cashewMilkMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture cashewMilkTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/cashew milk.png");
	        Material cashewMilkMaterial = new Material(cashewMilkTexture, reflectance);
	        cashewMilkMesh.setMaterial(cashewMilkMaterial);
	        GameItem cashewMilkIngredient= new GameItem("cashew milk",cashewMilkMesh);
	        cashewMilkIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(cashewMilkIngredient);
	        ingredients.add(new IngredientData(cashewMilkIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
	        Mesh almondMilkMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture almondMilkTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/almond milk.png");
	        Material almondMilkMaterial = new Material(almondMilkTexture, reflectance);
	        almondMilkMesh.setMaterial(almondMilkMaterial);
	        GameItem almondMilkIngredient= new GameItem("almond milk",almondMilkMesh);
	        almondMilkIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(almondMilkIngredient);
	        ingredients.add(new IngredientData(almondMilkIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
		}
		else if(checkIngredient.getName().equals("all purpose flour") || checkIngredient.getName().equals("plain flour")) {
	        Mesh coconutFlourMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture coconutFlourTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/coconut flour.png");
	        Material coconutFlourMaterial = new Material(coconutFlourTexture, reflectance);
	        coconutFlourMesh.setMaterial(coconutFlourMaterial);
	        GameItem coconutFlourIngredient= new GameItem("coconut flour",coconutFlourMesh);
	        coconutFlourIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(coconutFlourIngredient);
	        ingredients.add(new IngredientData(coconutFlourIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
	        Mesh speltFlourMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture speltFlourTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/spelt flour.png");
	        Material speltFlourMaterial = new Material(speltFlourTexture, reflectance);
	        speltFlourMesh.setMaterial(speltFlourMaterial);
	        GameItem speltFlourIngredient= new GameItem("spelt flour",speltFlourMesh);
	        speltFlourIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(speltFlourIngredient);
	        ingredients.add(new IngredientData(speltFlourIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
		}
		else if(checkIngredient.getName().equals("vegetable oil")) {
	        Mesh coconutOilMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture coconutOilTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/coconut oil.png");
	        Material coconutOilMaterial = new Material(coconutOilTexture, reflectance);
	        coconutOilMesh.setMaterial(coconutOilMaterial);
	        GameItem coconutOilIngredient= new GameItem("coconut oil",coconutOilMesh);
	        coconutOilIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(coconutOilIngredient);
	        ingredients.add(new IngredientData(coconutOilIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
	        Mesh oliveOilMesh = OBJLoader.loadMesh("/resources/models/ingredient.obj");
	        Texture oliveOilTexture = new Texture("/resources/recipes/"+ recipeChosen.getName() + "/olive oil.png");
	        Material oliveOilMaterial = new Material(oliveOilTexture, reflectance);
	        oliveOilMesh.setMaterial(oliveOilMaterial);
	        GameItem oliveOilIngredient= new GameItem("olive oil",oliveOilMesh);
	        oliveOilIngredient.setPosition(xSubstitute, 7.2f, 5); 
	        gameItems.add(oliveOilIngredient);
	        ingredients.add(new IngredientData(oliveOilIngredient, checkIngredient.getFoodtypes(), mesure.getName()));
	        xSubstitute+=1.8f;
		}
	}
	
	/*
	 * setCurrentScene:
	 * When we removing or adding back the items, we will always update the scene so in the simulation we will see the changes we make when we pressing 'T', 'P' or
	 * 'B'.
	 */
	public static void setScene(Scene scene) {
		scene.setGameItems(gameItems);
	}
	
	/*
	 * putBackGameItemToScene(GameItem addGameItem) - If the user pressed 'P' to put back the kitchen tool, it will set for the current kitchen tool to position that the item had before.
	 */
	public static void putBackGameItemToScene(GameItem addGameItem) {
		addGameItem.setPosition(savePosition.x, savePosition.y, savePosition.z);
		savePosition.x = 0;
		savePosition.y = 0;
		savePosition.z = 0;
	}
	
	/*
	 * removeGameItemFromScene(GameItem removeGameItem) - if the user pressed 'T', it will remove the kitchen tool of the kitchen room.
	 */
	public static void removeGameItemFromScene(GameItem removeGameItem) {
		savePosition.x = removeGameItem.getPosition().x;
		savePosition.y = removeGameItem.getPosition().y;
		savePosition.z = removeGameItem.getPosition().z;
		removeGameItem.setPosition(0, -1000, 0);
	}
	
	/*
	 * isOil(IngredientData checkIngredient) - in the cookware, the user can add on spoon/tea spoon oil to cook the ingredients.
	 */
	public static boolean isOil(IngredientData checkIngredient) {
		List<Foodtype> ingredientFoodType = checkIngredient.getFoodType();
		for(Foodtype checkType : ingredientFoodType) {
			if(checkType.getName().contains("oil"))
				return true;
		}
		return false;
	}
	
	/*
	 * isSpice(IngredientData checkIngredient) - Check if the food type of the ingredient is spice, if it's spice, the user can't add spice on empty cookware.
	 */
	public static boolean isSpice(IngredientData checkIngredient) {
		List<Foodtype> ingredientFoodType = checkIngredient.getFoodType();
		for(Foodtype checkType : ingredientFoodType) {
			if(checkType.getName().contains("spice"))
				return true;
		}
		return false;
	}
	
	//if spoon selected check the measure of the ingredient (if the player can take it with this kitchen tool).
	public static boolean isMeasureSoon(IngredientData checkIngredient) {
		return checkIngredient.getMeasureType().contains("tbsp") && checkIngredient.getMeasureType().contains("tbsp");
	}
	
	//if tea spoon selected check the measure of the ingredient (if the player can take it with this kitchen tool).
	public static boolean isMeasureTeaSoon(IngredientData checkIngredient) {
		return checkIngredient.getMeasureType().contains("tsp") && checkIngredient.getMeasureType().contains("tsp");
	}
	
	//Check if the measure type is tbsp/tsp - if true and the selected kitchen tool is bowl (for example), the player can't add the ingredient (becuase he need to use spoon/tea spoon).
	public static boolean isMeasureSpoonOrTeaSpoon(IngredientData checkIngredient) {
		if(checkIngredient.getMeasureType().contains("tbsp") || checkIngredient.getMeasureType().contains("tsp"))
			return true;
		else
			return false;
	}
	
	/*
	 * For the recipe "Falafel burgers" - the functions checks if the user took burger buns to render the right gui after taking the ingredient.
	 */
	public static boolean isBurgerBuns(String checkIngredient) {
		return checkIngredient.contains("burger buns");
	}
	
	/*
	 * The user can add optional ingredients on the dish for extra points, the function checks if there is an optional ingredients that the user can put on the dish.
	 */
	public static boolean optionalIngredientsOnDish(String ingredient) {
		String OptionalIngredients = instructions.get(instructions.size()-1);
		String currentIngredient = "";
		String checkOptional = OptionalIngredients.substring(OptionalIngredients.lastIndexOf(":") + 1);
		StringTokenizer stOptional = new StringTokenizer(checkOptional,",");
		while(stOptional.hasMoreTokens()) {
			currentIngredient = stOptional.nextToken();
			if(currentIngredient.contains(ingredient))
				return true;
		}
		return false;
	}
	
	/*
	 * There is a recipe where the user can griddle the bruger, if the user griddle the bruger buns or there is burger buns on the dish, the user can't griddle them again.
	 */
	public static boolean ingredientOnKitchenTools(String selectedKitchenTool) {
		if(selectedKitchenTool.equals("dish")) {
			for(ScoreCalculator checkIngredient : ingredientsOnDish) {
				if(checkIngredient.getName().contains("burger buns"))
					return true;
			}
		}
		else if(selectedKitchenTool.equals("cooking pan")) {
			for(ScoreCalculator checkIngredient : ingredientsOnDish) {
				if(checkIngredient.getName().contains("burger buns"))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * checkDuplicates() - the simulation checks if the player took the ingredient before, if the player took the same ingredient that already in the list,
	 * it will return true (and after it return true, it will init the new amount in ChoppedSimulation Class).
	 */
	public static boolean checkDuplicates(String ingredient, int amount, String listName) {
		int i=0;
		if(listName.equals("dish")) {
			for(i=0; i<ingredientsOnDish.size(); i++) {
				if(ingredientsOnDish.get(i).getName().equals(ingredient) && ingredientsOnDish.get(i).getAmount() != amount)
					return true;
			}
		}
		else if(listName.equals("bowl")) {
			for(i=0; i<ingredientsInBowl.size(); i++) {
				if(ingredientsInBowl.get(i).getName().equals(ingredient) && ingredientsInBowl.get(i).getAmount() != amount)
					return true;
			}
		}
		else if(listName.equals("cooking pot")) {
			for(i=0; i<ingredientsInCookingPot.size(); i++) {
				if(ingredientsInCookingPot.get(i).getName().equals(ingredient) && ingredientsInCookingPot.get(i).getAmount() != amount)
					return true;
			}
		}
		else if(listName.equals("cooking pan")) {
			for(i=0; i<ingredientsInCookingPan.size(); i++) {
				if(ingredientsInCookingPan.get(i).getName().equals(ingredient) && ingredientsInCookingPan.get(i).getAmount() != amount) 
					return true;
			}
		}
		else if(listName.equals("blender")) {
			for(i=0; i<ingredientsInBlender.size(); i++) {
				if(ingredientsInBlender.get(i).getName().equals(ingredient) && ingredientsInBlender.get(i).getAmount() != amount)
					return true;
			}
		}
		else if(listName.equals("mixer")) {
			for(i=0; i<ingredientsInMixer.size(); i++) {
				if(ingredientsInMixer.get(i).getName().equals(ingredient) && ingredientsInMixer.get(i).getAmount() != amount)
					return true;
			}
		}
		else if(listName.equals("tray")) {
			for(i=0; i<ingredientsOnTray.size(); i++) {
				if(ingredientsOnTray.get(i).getName().equals(ingredient) && ingredientsOnTray.get(i).getAmount() != amount)
					return true;
			}
		}
		return false;
	}
	
	/*
	 * If the player took before the ingredient, the simulation will check if there is the same ingredient that the player took,
	 * if returned true -> the simulation will add the new amount to the same ingredient that in the list.
	 */
	public static void addDuplicates(String ingredient, int amount, String listName) {
		int i=0;
		if(listName.equals("dish")) {
			for(i=0; i<ingredientsOnDish.size(); i++) {
				if(ingredientsOnDish.get(i).getName().equals(ingredient) && ingredientsOnDish.get(i).getAmount() != amount) {
					int newAmount = ingredientsOnDish.get(i).getAmount() + amount;
					ingredientsOnDish.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("bowl")) {
			for(i=0; i<ingredientsInBowl.size(); i++) {
				if(ingredientsInBowl.get(i).getName().equals(ingredient) && ingredientsInBowl.get(i).getAmount() != amount) {
					int newAmount = ingredientsInBowl.get(i).getAmount() + amount;
					ingredientsInBowl.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("cooking pot")) {
			for(i=0; i<ingredientsInCookingPot.size(); i++) {
				if(ingredientsInCookingPot.get(i).getName().equals(ingredient) && ingredientsInCookingPot.get(i).getAmount() != amount) {
					int newAmount = ingredientsInCookingPot.get(i).getAmount() + amount;
					ingredientsInCookingPot.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("cooking pan")) {
			for(i=0; i<ingredientsInCookingPan.size(); i++) {
				if(ingredientsInCookingPan.get(i).getName().equals(ingredient) && ingredientsInCookingPan.get(i).getAmount() != amount) {
					int newAmount = ingredientsInCookingPan.get(i).getAmount() + amount;
					ingredientsInCookingPan.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("blender")) {
			for(i=0; i<ingredientsInBlender.size(); i++) {
				if(ingredientsInBlender.get(i).getName().equals(ingredient) && ingredientsInBlender.get(i).getAmount() != amount) {
					int newAmount = ingredientsInBlender.get(i).getAmount() + amount;
					ingredientsInBlender.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("mixer")) {
			for(i=0; i<ingredientsInMixer.size(); i++) {
				if(ingredientsInMixer.get(i).getName().equals(ingredient) && ingredientsInMixer.get(i).getAmount() != amount) {
					int newAmount = ingredientsInMixer.get(i).getAmount() + amount;
					ingredientsInMixer.get(i).setAmount(newAmount);
				}
			}
		}
		else if(listName.equals("tray")) {
			for(i=0; i<ingredientsOnTray.size(); i++) {
				if(ingredientsOnTray.get(i).getName().equals(ingredient) && ingredientsOnTray.get(i).getAmount() != amount) {
					int newAmount = ingredientsOnTray.get(i).getAmount() + amount;
					ingredientsOnTray.get(i).setAmount(newAmount);
				}
			}
		}
	}
	
	/*
	 * The functions checks if there is substitute for the ingredients in the chosen recipe by the user (for the score calculation).
	 */
	public static boolean haveSubstitute() {
		for(Map.Entry<Ingredient, Measure> ingredient : saveIngredientsData.entrySet()) {
			if(checkSubstitute(ingredient.getKey()))
				return true;
		}
		return false;
	}
	
	/*
	 * checkSubstitute(Ingredient currentIngredient) - the function checks if the inredient have substitute.
	 */
	public static boolean checkSubstitute(Ingredient currentIngredient) {
		if(currentIngredient.getName().equals("eggs"))
			return true;
		else if(currentIngredient.getName().equals("milk"))
			return true;
		else if(currentIngredient.getName().equals("vegetable oil"))
			return true;
		else if(currentIngredient.getName().equals("all purpose flour"))
			return true;
		else if(currentIngredient.getName().equals("plain flour"))
			return true;
		else if(currentIngredient.getName().contains("sugar"))
			return true;
		return false;
	}
	
	/*
	 * usedSubstitute(ScoreCalculator substitute) - for the score calculation, checks which substitute the user used in the chosen recipe.
	 */
	public static boolean usedSubstitute(ScoreCalculator substitute) {
		if(substitute.getName().equals("coconut oil"))
			return true;
		else if(substitute.getName().equals("olive oil"))
			return true;
		else if(substitute.getName().equals("spelt flour"))
			return true;
		else if(substitute.getName().equals("coconut flour"))
			return true;
		else if(substitute.getName().equals("almond milk"))
			return true;
		else if(substitute.getName().equals("cashew milk"))
			return true;
		else if(substitute.getName().equals("stevia"))
			return true;
		else if(substitute.getName().equals("ground flaxseed"))
			return true;
		else if(substitute.getName().equals("greek yogurt"))
			return true;
		else if(substitute.getName().equals("banana"))
			return true;
		return false;
	}
	
	/*
	 * initRecipeFromLog() - If the player pressed on the continue button (because the simulation crashed), first it will init the recipe.
	 */
	public static void initRecipeFromLog() throws IOException {
		List<String> logLines = Files.readAllLines(new File("SavedSimulationFiles/saved recipe.log").toPath());
		Logs.initRecipe(logLines.get(1));
	}
	
	/*
	 * loadLog() - if the simulation crashed and the player pressed on continue button - it will init all kitchen tools from the last session.
	 * The functions reads all the lines from the log file.
	 */
	public static void loadLog(GameItem dish, Bowl bowl, Cookware cookingPot, Cookware cookingPan, Tray tray, FoodProcessor blender, FoodProcessor mixer) throws Exception {
		List<String> logLines = Files.readAllLines(new File("SavedSimulationFiles/saved recipe.log").toPath());
		for(int i=0; i<logLines.size(); i++) {
			if(logLines.get(i).contains("bowl"))
				Logs.initBowl(logLines.get(i), bowl);
			else if(logLines.get(i).contains("tray"))
				Logs.initTray(logLines.get(i), tray);
			else if(logLines.get(i).contains("cooking pan"))
				Logs.initCookware(logLines.get(i), cookingPan);
			else if(logLines.get(i).contains("cooking pot"))
				Logs.initCookware(logLines.get(i), cookingPot);
			else if(logLines.get(i).contains("blender"))
				Logs.initFoodProcessor(logLines.get(i), blender);
			else if(logLines.get(i).contains("mixer"))
				Logs.initFoodProcessor(logLines.get(i), mixer);
			else if(logLines.get(i).contains("dish"))
				Logs.initDish(logLines.get(i), dish);
		}
	}
	
	/*
	 * addDish function - 
	 * after the simulation calculate the score, the simulation will add the recipe that the player played with the score.
	 */
	public static void addDish() {
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
	    	java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
			Dish dish = new Dish(SimulationService.getUser(),recipeChosen,date,score);
			JpaConnection.getEntityManager().persist(dish);
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * deleteLogFolder() - After pressing the 'Q' button and the player see the score screen, it will delete the log file.
	 */
	public static void deleteLogFolder() {
		Logs.closeFileHandler();
    	File logFolder = new File("SavedSimulationFiles");
    	String[]entries = logFolder.list();
    	for(String s: entries){
    	    File currentFile = new File(logFolder.getPath(),s);
    	    currentFile.delete();
    	}
    	logFolder.delete();
	}
	
	/*
	 * cloneList - 
	 * The simulation will clone all the data from the lists (from the ChoppedSimulation class) to calculate the score after the player pressed 'Q' on the dish.
	 */
	@SuppressWarnings("unused")
	private void cloneList(List<ScoreCalculator> target, List<ScoreCalculator> copy) {
		for(ScoreCalculator clone : copy) {
			target.add(clone);
		}
	}
	
	/*
	 * Before the simulation starts - init the lists to calculate the score.
	 */
	public static void initScoreCalculatorLists() {
		ingredientsInBowl = new Vector<ScoreCalculator>();
		ingredientsInCookingPot = new Vector<ScoreCalculator>();
		ingredientsInCookingPan = new Vector<ScoreCalculator>();
		ingredientsInBlender = new Vector<ScoreCalculator>();
		ingredientsInMixer = new Vector<ScoreCalculator>();
		ingredientsOnTray = new Vector<ScoreCalculator>();
		ingredientsOnDish = new Vector<ScoreCalculator>();
	}
	
	/*
	 * Add functions for all the lists - in the simulation the player will add ingredients.
	 * Each list will save the name of the ingredient and the amount.
	 */
	public static void addIngredientsIntoBowl(String name, int amount) {
		ingredientsInBowl.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsIntoCookingPot(String name, int amount) {
		ingredientsInCookingPot.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsIntoCookingPan(String name, int amount) {
		ingredientsInCookingPan.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsIntoBlender(String name, int amount) {
		ingredientsInBlender.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsIntoMixer(String name, int amount) {
		ingredientsInMixer.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsOnTray(String name, int amount) {
		ingredientsOnTray.add(new ScoreCalculator(name,amount));
	}
	
	public static void addIngredientsOnDish(String name, int amount) {
		ingredientsOnDish.add(new ScoreCalculator(name,amount));
	}
	
	/*
	 * init each kitchen tool to calculate the score.
	 * If the tray burned / cooked - it will add or sub the score - the same for others.
	 */
	public static void initTray(Tray tray) {
		checkTray = new Tray(tray);
	}
	
	public static void initCookingPot(Cookware cookingPot) {
		checkCookingPot = new Cookware(cookingPot);
	}
	
	public static void initCookingPan(Cookware cookingPan) {
		checkCookingPan = new Cookware(cookingPan);
	}
	
	/*
	 * calculateScore() - 
	 * The calculation of the score, first the function checks instructions which kitchen tools in the recipe.
	 * than it checks in every list (of the ScoreCalculator) the ingredients that the user added and the amount of the ingredients.
	 * if it follows the recipe - the simulation will add points into the score, if not - it will not add the points.
	 */
	public static void calculateScore() {
		boolean useCookingPot = false;
		boolean useCookingPan = false;
		boolean useOven = false;
		int countIngredients = 0;
		int totalIngredients = (int) countIngredientsInRecipe();
		
		//check in the recipe which kitchen entities the player need to use
		for(String instruction : instructions) {
			if(instruction.contains("fry on the cooking pan"))
				useCookingPan = true;
			else if(instruction.contains("cook in the cooking pot"))
				useCookingPot = true;
			else if(instruction.contains("bake in the oven"))
				useOven = true;
		}
		
		//check list of the ingredients in the bowl if it follows as in the recipe
		if(ingredientsInBowl.size()!=0) {
			for(ScoreCalculator checkIngredientsInBowl : ingredientsInBowl) {
				for(Map.Entry<Ingredient,Measure> checkIngredientData : saveIngredientsData.entrySet()) {
					if(checkIngredientData.getKey().getName().contains(checkIngredientsInBowl.getName())) {
						countIngredients++;
					}
				}
				if(haveSubstitute() && usedSubstitute(checkIngredientsInBowl)) {
					countIngredients++;
				}
			}
			checkIngredientsAmount(ingredientsInBowl,saveIngredientsData);
			checkSubstituteAmount(ingredientsInBowl,saveIngredientsData);
		}
		
		if(useOven && ingredientsOnTray.size()!=0) {
			for(ScoreCalculator checkIngredient : ingredientsOnTray) {
				for(Map.Entry<Ingredient,Measure> checkIngredientData : saveIngredientsData.entrySet()) {
					if(checkIngredientData.getKey().getName().contains(checkIngredient.getName()))
						countIngredients++;
				}
				if(haveSubstitute() && usedSubstitute(checkIngredient))
					countIngredients++;
			}
			checkIngredientsAmount(ingredientsOnTray, saveIngredientsData);
			checkSubstituteAmount(ingredientsOnTray,saveIngredientsData);
			
			//if the tray is baked - add 10 points into the score
			if(checkTray.isBaked())
				score+=10;
		}
		
		if(useCookingPot && ingredientsInCookingPot.size()!=0) {
			for(ScoreCalculator checkIngredient : ingredientsInCookingPot) {
				for(Map.Entry<Ingredient,Measure> checkIngredientData : saveIngredientsData.entrySet()) {
					if(checkIngredientData.getKey().getName().contains(checkIngredient.getName()))
						countIngredients++;
				}
				if(haveSubstitute() && usedSubstitute(checkIngredient))
					countIngredients++;
			}
			
			checkIngredientsAmount(ingredientsInCookingPot, saveIngredientsData);
			checkSubstituteAmount(ingredientsInCookingPot,saveIngredientsData);
			
			//if cooking pot is cooked - add 10 points into the score
			if(checkCookingPot.isCooked())
				score+=10;
		}
		
		if(useCookingPan && ingredientsInCookingPan.size()!=0) { 
			for(ScoreCalculator checkIngredient : ingredientsInCookingPan) {
				for(Map.Entry<Ingredient,Measure> checkIngredientData : saveIngredientsData.entrySet()) {
					if(checkIngredientData.getKey().getName().contains(checkIngredient.getName()))
						countIngredients++;
				}
				if(haveSubstitute() && usedSubstitute(checkIngredient))
					countIngredients++;
			}
			
			checkIngredientsAmount(ingredientsInCookingPan, saveIngredientsData);
			checkSubstituteAmount(ingredientsInCookingPan,saveIngredientsData);
			
			if(checkCookingPan.isCooked())
				score+=10;
		}
		
		/*after counting all the ingredients that the player used, check if it's equals to the total ingredients from the recipe.
		 * if the player used ingredients but not all of them - the player will get half of the points into the score.
		 */
		if(countIngredients == totalIngredients)
			score+=40;
		if(countIngredients < totalIngredients && countIngredients!=0)
			score+=20;
		
		//check if the player used optional ingredients (bonus score):
		if(ingredientsOnDish.size()!=0) {
			for(@SuppressWarnings("unused") ScoreCalculator optional : ingredientsOnDish) {
				score+=5;
			}
		}
	}
	
	/*check if it follows the amount of the ingredients as in the recipe
	if it follows as the amount in the recipe - add 3 points*/
	private static void checkIngredientsAmount(List<ScoreCalculator> ingredientsList, Map<Ingredient,Measure> ingredientsData) {
		for(ScoreCalculator checkIngredient : ingredientsList) {
			for(Map.Entry<Ingredient,Measure> checkIngredientData : ingredientsData.entrySet()) {
				List<Recipeandingredient> amountData = checkIngredientData.getValue().getRecipeandingredients();
				for(Recipeandingredient checkAmount : amountData) {
					BigDecimal amount = checkAmount.getAmount();
					if(amount.intValue() == checkIngredient.getAmount() && checkIngredient.getName().equals(checkAmount.getIngredient().getName()))
						score+=3;
				}
			}
		}
	}
	
	/*check if the substitute follows the amount of the original ingredient that the player replaced.
	if it follows as the amount in the recipe - add 3 points */
	private static void checkSubstituteAmount (List<ScoreCalculator> ingredientsList, Map<Ingredient,Measure> ingredientsData) {
		for(ScoreCalculator checkIngredient : ingredientsList) {
			if(usedSubstitute(checkIngredient)) {
				for(Map.Entry<Ingredient,Measure> checkIngredientData : ingredientsData.entrySet()) {
					if(checkIngredientData.getKey().getName().equals("vegetable oil") || checkIngredientData.getKey().getName().equals("all purpose flour") || checkIngredientData.getKey().getName().equals("white flour") || checkIngredientData.getKey().getName().equals("milk") || checkIngredientData.getKey().getName().equals("sugar") || checkIngredientData.getKey().getName().equals("eggs")) {
						List<Recipeandingredient> amountData = checkIngredientData.getValue().getRecipeandingredients();
						for(Recipeandingredient checkAmount : amountData) {
							BigDecimal amount = checkAmount.getAmount();
							if(checkIngredient.getAmount()==amount.intValue())
								score+=3;
						}
					}
				}
			}
		}
	}

	public static Vector<GameItem> getGameItems() {
		return gameItems;
	}
	
	public static void setUser(User user) {
		SimulationService.user = user;
	}

	public static User getUser() {
		return user;
	}

	public static List<Recipe> getSaveRecipes() {
		return saveRecipes;
	}

	public static Vector<IngredientData> getIngredients() {
		return ingredients;
	}

	public static Recipe getRecipeChosen() {
		return recipeChosen;
	}

	public static void setRecipeChosen(Recipe recipeChosen) {
		SimulationService.recipeChosen = recipeChosen;
	}

	public static Map<Ingredient, Measure> getSaveIngredientsData() {
		return saveIngredientsData;
	}

	public static int getScore() {
		return score;
	}

	public static boolean isStartSimlation() {
		return startSimlation;
	}

	public static void setStartSimlation(boolean startSimlation) {
		SimulationService.startSimlation = startSimlation;
	}

	public static boolean isShowHudScore() {
		return showHudScore;
	}

	public static void setShowHudScore(boolean showHudScore) {
		SimulationService.showHudScore = showHudScore;
	}

	public static boolean isLoadLog() {
		return loadLog;
	}

	public static void setLoadLog(boolean loadLog) {
		SimulationService.loadLog = loadLog;
	}

}
