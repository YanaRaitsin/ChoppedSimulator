package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import data.Recipe;
import entities.Bowl;
import entities.Cookware;
import entities.FoodProcessor;
import entities.Tray;
import items.GameItem;
import simulator.JpaConnection;
import simulator.SimulationService;

public class Logs {
	
	public static final String LOGS_FOLDER = "SavedSimulationFiles";
	private static FileHandler fileHandler;
	public static Logger log;
	
	public void createLog() {
		File file = new File(LOGS_FOLDER);
		if (!file.exists()) file.mkdir();
		
		log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		log.setLevel(Level.INFO);
		
		try { 
			fileHandler = new FileHandler(LOGS_FOLDER + File.separator + "saved recipe.log");
			fileHandler.setFormatter(new SimpleFormatter());
			log.addHandler(fileHandler);
		}
		catch (Exception ex) {
			ex.printStackTrace(); 
		}
	}
	
	/*
	 * currentRecipe(Recipe recipe) - init the chosen recipe by the user.
	 */
	public void currentRecipe(Recipe recipe) {
		log.info(recipe.getName() + " id:" + recipe.getId());
	}
	
	/*
	 * changedTextures(String gameItem, String action, String textureFile, int minutes, int seconds) - 
	 * saves the path of the changed texture and which kitchen entity changed the texture.
	 */
	public void changedTextures(String gameItem, String action, String textureFile, int minutes, int seconds) {
		log.info(gameItem + " changed texture: " + action + " file path: " + textureFile + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * addedIngredientsKitchenTools(String gameItem, String currentIngredient, int currentAmount, int minutes, int seconds) -
	 * When the user adding ingredient in kitchen tools, it will save on which kitchen tool the ingredient added and it will save the name of the ingredient and the amount.
	 */
	public void addedIngredientsKitchenTools(String gameItem, String currentIngredient, int currentAmount, int minutes, int seconds) {
		log.info(gameItem + " ingredient added: " + currentIngredient + " amount: " + currentAmount + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * checkCookware(Cookware cookware, int minutes, int seconds) - check all the options of the cookware in the simulation.
	 */
	public void checkCookware(Cookware cookware, int minutes, int seconds) {
		String isCooked="";
		if(cookware.isBurned()) isCooked = "burned";
		else if(cookware.isCooked()) isCooked = "cooked";
		else if(!cookware.isCooked() && !cookware.isBurned()) isCooked = "not cooked";
		log.info("current "+cookware.getKitchenTool().getNameGameItem()+": " + isCooked + " is empty: " + cookware.isEmpty() + " is time checked: "+cookware.isTimeChecked()+" ingredients bowl: "+ cookware.isIngredientsBowl() + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * checkTray(Tray tray, int minutes, int seconds) - check all the options of the tray in the simulation.
	 */
	public void checkTray(Tray tray, int minutes, int seconds) {
		String isBaked="";
		if(tray.isBurned()) isBaked = "burned";
		else if(tray.isBaked()) isBaked = "baked";
		else if(tray.isNotBaked()) isBaked = "not baked";
		else if(!tray.isBaked() && !tray.isBurned() && !tray.isNotBaked()) isBaked = "to bake";
		log.info("current tray: " + isBaked + " is empty: " + tray.isEmpty() + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * checkBowl(Bowl bowl, int minutes, int seconds) - 
	 * Check all the options of the bowl in the simulation (while cooking/baking the ingredients or adding the ingredients).
	 */
	public void checkBowl(Bowl bowl, int minutes, int seconds) {
		log.info("current " + bowl.getKitchenTool().getNameGameItem() + ": is empty: " + bowl.isEmpty() + " is combined: " + bowl.isCombined() + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * checkBowl(Bowl bowl, int minutes, int seconds) - 
	 * Check all the options of the food processor (if there is processed ingredients or the food processor is empty/or not).
	 */
	public void checkFoodProcessor(FoodProcessor foodProcessor, int minutes, int seconds) {
		log.info("current " + foodProcessor.getFoodProcessor().getNameGameItem() + ": is empty: " + foodProcessor.isEmpty() + " is processed: " + foodProcessor.isProcessedIngredients() + " current simulation time: " + minutes + " : " + seconds);
	}
	
	/*
	 * If the simulation crashed and the user want to continue the last session after the crash, the simulation will read the log file
	 * and will init each kitchen entities that changed.
	 */
	public static Bowl initBowl(String currentLine, Bowl bowl) throws Exception {
		if(currentLine.contains("bowl: is empty: false is combined: true")) {
			bowl.setEmpty(false);
			bowl.setCombined(true);
		}
		else if(currentLine.contains("bowl: is empty: true is combined: false")) {
			bowl.setEmpty(true);
			bowl.setCombined(false);
			bowl.setTexture("empty", "");
		}
		else if(currentLine.contains("bowl: is empty: false is combined: false")) {
			bowl.setEmpty(false);
			bowl.setCombined(false);
		}
		else if(currentLine.contains("changed texture: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String path = currentLine.substring(currentLine.lastIndexOf(" /") + 1,iend); //take from the line the texture path.
	    	if(path.contains("empty")) {
	    		bowl.setTexture("empty", "");
	    		bowl.setEmpty(true);
	    		bowl.setCombined(false);
	    	}
	    	else if(path.contains("combinedingredients")) {
	    		bowl.setTexture("not empty", path);
	    		bowl.setCombined(true);
	    		bowl.setEmpty(false);
	    	}
	    	else if(path.contains("ingredients bowl")) {
	    		bowl.setTexture("not empty", path);
	    		bowl.setCombined(false);
	    		bowl.setEmpty(false);
	    	}
		}
		//save the ingredients that added from the log file (to calculate the score after finishing the recipe).
		else if(currentLine.contains("ingredient added: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String addedIngredient = currentLine.substring(currentLine.lastIndexOf("added: ") + 7,iend); //taking the line of the added ingredient
	    	String saveAmount = addedIngredient.substring(addedIngredient.lastIndexOf(' ') + 1); //taking the number of the amount
	    	int amount = Integer.parseInt(saveAmount);
	    	String ingredient = addedIngredient.substring(0,addedIngredient.lastIndexOf(" amount")); //taking the name of the ingredient
	    	//save it into the list of the score calculation
	    	SimulationService.addIngredientsIntoBowl(ingredient, amount);
		}
		return bowl;
	}
	
	public static Tray initTray(String currentLine, Tray tray) throws Exception {
		if(currentLine.contains("tray: burned is empty: false")) {
			tray.setBurned(true);
			tray.setEmpty(false);
		}
		else if(currentLine.contains("tray: baked is empty: false")) {
			tray.setBaked(true);
			tray.setEmpty(false);
		}
		else if(currentLine.contains("tray: not baked is empty: false")) {
			tray.setNotBaked(true);
			tray.setEmpty(false);
		}
		else if(currentLine.contains("tray: to bake is empty: false")) {
			tray.setBaked(false);
			tray.setNotBaked(false);
			tray.setBurned(false);
			tray.setEmpty(false);
		}
		else if(currentLine.contains("tray: to bake is empty: true")) {
			tray.setBaked(false);
			tray.setNotBaked(false);
			tray.setBurned(false);
			tray.setEmpty(true);
		}
		else if(currentLine.contains("changed texture: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String path = currentLine.substring(currentLine.lastIndexOf(" /") + 1,iend); //take from the line the texture path.
	    	if(path.contains("burned")) 
	    		tray.setTexture("not empty",path);
	    	else if(path.contains("to bake")) 
	    		tray.setTexture("not empty",path);
	    	else if(path.contains("baked"))
	    		tray.setTexture("not empty", path);    	
	    	else if(path.contains("trayTexture"))
	    		tray.setTexture("empty", "");
		}
		return tray;
	}
	
	public static Cookware initCookware(String currentLine, Cookware cookware) throws Exception {
		//if it's burned
		if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": burned is empty: false is time checked: true ingredients bowl: true")) {
			cookware.setBurned(true);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(true);
		}
		//if it's burned (burger buns).
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": burned is empty: false is time checked: true ingredients bowl: false")) {
			cookware.setBurned(true);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(false);
		}
		//if it's fully cooked.
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": cooked is empty: false is time checked: true ingredients bowl: true")) {
			cookware.setBurned(false);
			cookware.setCooked(true);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(true);
		}
		//if it's fully cooked (burger buns).
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": cooked is empty: false is time checked: true ingredients bowl: false")) {
			cookware.setBurned(false);
			cookware.setCooked(true);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(false);
		}
		//if it's not fully cooked.
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": not cooked is empty: false is time checked: true ingredients bowl: true")) {
			cookware.setBurned(false);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(true);
		}
		//if the player didn't turn on the gas for cooking.
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": not cooked is empty: false is time checked: false ingredients bowl: true")) {
			cookware.setBurned(false);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(false);
			cookware.setIngredientsBowl(true);
		}
		//if not ingredients from the bowl - so the player want to griddle the burger buns (when the player didn't turn on the gas).
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": not cooked is empty: false is time checked: false ingredients bowl: false")) {
			cookware.setBurned(false);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(false);
			cookware.setIngredientsBowl(false);
		}
		//if the player did turn on the gas and it's not fully cooked (burger buns).
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": not cooked is empty: false is time checked: true ingredients bowl: false")) {
			cookware.setBurned(false);
			cookware.setCooked(false);
			cookware.setEmpty(false);
			cookware.setTimeChecked(true);
			cookware.setIngredientsBowl(true);
		}
		//if it's empty
		else if(currentLine.contains(cookware.getKitchenTool().getNameGameItem()+": not cooked is empty: true is time checked: false ingredients bowl: false")) {
			cookware.setBurned(false);
			cookware.setCooked(false);
			cookware.setEmpty(true);
			cookware.setTexture("empty", "");
    		cookware.setTimeChecked(false);
			cookware.setIngredientsBowl(false);
		}
		else if(currentLine.contains("changed texture: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String path = currentLine.substring(currentLine.lastIndexOf(" /") + 1,iend); //take from the line the texture path.
	    	if(path.contains("burned")) {
	    		cookware.setTexture("not empty",path);
	    		cookware.setBurned(true);
	    		cookware.setCooked(false);
	    		cookware.setEmpty(false);
	    		cookware.setTimeChecked(true);
	    	}
	    	else if(path.contains("to cook")) {
	    		cookware.setTexture("not empty",path);
	    		cookware.setBurned(false);
	    		cookware.setCooked(false);
	    		cookware.setEmpty(false);
	    		cookware.setTimeChecked(true);
	    	}
	    	else if(path.contains("cooked")) {
	    		cookware.setTexture("not empty", path);
	    		cookware.setBurned(false);
	    		cookware.setCooked(true);
	    		cookware.setEmpty(false);
	    		cookware.setTimeChecked(true);
	    	}	    	
	    	else if(path.contains("frypanTexture")) {
	    		cookware.setTexture("empty", "");
	    		cookware.setBurned(false);
	    		cookware.setCooked(false);
	    		cookware.setEmpty(true);
	    		cookware.setTimeChecked(false);
	    		cookware.setIngredientsBowl(false);
	    	}
		}
		else if(currentLine.contains("ingredient added: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String addedIngredient = currentLine.substring(currentLine.lastIndexOf("added: ") + 7,iend); //taking the line of the added ingredient
	    	String saveAmount = addedIngredient.substring(addedIngredient.lastIndexOf(' ') + 1); //taking the number of the amount
	    	int amount = Integer.parseInt(saveAmount);
	    	String ingredient = addedIngredient.substring(0,addedIngredient.lastIndexOf(" amount")); //taking the name of the ingredient
	    	//save it into the list of the score calculation
	    	if(cookware.getKitchenTool().getNameGameItem().equals("cooking pan"))
	    		SimulationService.addIngredientsIntoCookingPan(ingredient, amount);
	    	else if(cookware.getKitchenTool().getNameGameItem().equals("cooking pot"))
	    		SimulationService.addIngredientsIntoCookingPot(ingredient, amount);
		}
		return cookware;
	}
	
	public static FoodProcessor initFoodProcessor(String currentLine, FoodProcessor foodProcessor) {
		if(currentLine.contains(foodProcessor.getFoodProcessor().getNameGameItem() + ": is empty: false is processed: true")) {
			foodProcessor.setEmpty(false);
			foodProcessor.setProcessedIngredients(true);
		}
		else if(currentLine.contains(foodProcessor.getFoodProcessor().getNameGameItem() + ": is empty: false is processed: false")) {
			foodProcessor.setEmpty(false);
			foodProcessor.setProcessedIngredients(false);
		}
		else if(currentLine.contains(foodProcessor.getFoodProcessor().getNameGameItem() + ": is empty: true is processed: false")) {
			foodProcessor.setEmpty(true);
			foodProcessor.setProcessedIngredients(false);
		}
		return foodProcessor;
	}
	
	public static GameItem initDish(String currentLine, GameItem dish) throws Exception {
		if(currentLine.contains("changed texture: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String path = currentLine.substring(currentLine.lastIndexOf(" /") + 1,iend); //take from the line the texture path.
	    	if(path.contains("burned")) {
	    		dish.changeTexture(dish,path);
	    	}
	    	else if(path.contains("notcooked")) {
	    		dish.changeTexture(dish,path);
	    	}
	    	else if(path.contains("cooked")) {
	    		dish.changeTexture(dish,path);
	    	}
	    	else if(path.contains("baked")) {
	    		dish.changeTexture(dish,path);
	    	}
	    	else if(path.contains("not baked")) {
	    		dish.changeTexture(dish,path);
	    	}
		}
		else if(currentLine.contains("ingredient added: ")) {
	    	int iend = currentLine.indexOf(" current");
	    	String addedIngredient = currentLine.substring(currentLine.lastIndexOf("added: ") + 7,iend); //taking the line of the added ingredient
	    	String saveAmount = addedIngredient.substring(addedIngredient.lastIndexOf(' ') + 1); //taking the number of the amount
	    	int amount = Integer.parseInt(saveAmount);
	    	String ingredient = addedIngredient.substring(0,addedIngredient.lastIndexOf(" amount")); //taking the name of the ingredient
	    	//save it into the list of the score calculation
	    	SimulationService.addIngredientsOnDish(ingredient, amount);
		}
		return dish;
	}
	
	/*
	 * initLastTime() - If the simulation chrashed - in the hudTimer class the simulation will init the last time (that saved) in the session.
	 */
	public static long initHudTimer() throws IOException {
		List<String> logLines = Files.readAllLines(new File("SavedSimulationFiles/saved recipe.log").toPath());
    	String subTimeLine = logLines.get(logLines.size()-1).substring(logLines.get(logLines.size()-1).lastIndexOf("time: ") + 6); //take the last line of the time.
    	int iend = subTimeLine.indexOf(" :");
    	String subMinutes = subTimeLine.substring(0,iend); //substring the minutes
    	Long time = Long.parseLong(subMinutes);
    	System.out.println(time);
    	return time;
	}
	
	/*
	 * initRecipe(String secondLine) - usually in each log file the second line will be the name of the recipe - it will init the recipe by taking the id of the recipe.
	 */
	public static void initRecipe(String secondLine) {
		String subRecipe = secondLine.substring(secondLine.lastIndexOf(":") + 1);
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
    		Recipe recipe = JpaConnection.getEntityManager().find(Recipe.class, Integer.parseInt(subRecipe));
    		SimulationService.setRecipeChosen(recipe);
    		JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception ex) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
	}
	
	/*
	 * closeFileHandler() - closing the file handler after pressing 'Q' in the simulation to delete the log file because the simulation didn't crashed.
	 */
	public static void closeFileHandler() {
		fileHandler.close();
	}
	
}
