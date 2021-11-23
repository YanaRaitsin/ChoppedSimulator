package game;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

import data.Recipe;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Scene;
import engine.SceneLight;
import engine.Window;
import entities.Bowl;
import entities.Cookware;
import entities.FoodProcessor;
import entities.IngredientData;
import entities.Oven;
import entities.Spatula;
import entities.Spoon;
import entities.Tray;
import graph.Camera;
import graph.Renderer;
import hud.HudAmount;
import hud.HudExceptions;
import hud.HudGui;
import hud.HudLoading;
import hud.HudMiniTimer;
import hud.HudTimer;
import items.GameItem;
import items.SkyBox;
import items.Terrain;
import lights.DirectionalLight;
import simulator.SimulationService;
import utils.Logs;

import static org.lwjgl.glfw.GLFW.*;

public class ChoppedSimulation implements IGameLogic {
	
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static Vector<GameItem> gameItems;
    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private Scene scene;
    private static final float CAMERA_POS_STEP = 0.10f;
    @SuppressWarnings("unused")
	private float angleInc;
    private MouseBoxSelectionDetector selectDetector;
    private IngredientData saveSelectedIngredient;
    private boolean simulationRendered;
    private HudLoading hudLoading;
    private HudExceptions hudExceptions;

	/* Kitchen Entities */
	/**
	 * Oven Entity key map:
	 * 'I' - insert a tray into the oven.
	 * 'S' - take out the tray from the oven.
	 * 'O' - turn on the gas on the oven.
	 * 'F' - turn off the gas.
	 */
	private Oven oven; //check if the recipe is dinner or desert -> if desert set 7 minutes OR if dinner set 10
	/**
	 * Blender and Mixer Entities key map:
	 * 'I' - insert an ingredients into the blender or mixer.
	 * 'O' - turn on the blender / mixer.
	 * 'T' - take out the mixed ingredients from the bleder / mixer.
	 */
	private FoodProcessor blender;
	private FoodProcessor mixer;
	/**
	 * Bowl Entity key map:
	 * 'I' - insert the ingredients/cooked ingredients into the bowl.
	 * 'T' - take the bowl.
	 * 'P' - put back the bowl.
	 * 'C' - combine the ingredient by using empty spoon ONLY!.
	 */
	private Bowl bowl;
	/**
	 * Tray Entity key map:
	 * 'I' - insert an ingredients on the tray.
	 * 'T' - take the tray to the oven ONLY.
	 * 'P' - put back the tray when it's ONLY to bake.
	 */
	private Tray tray;
	/**
	 * Cooking Pot / Pan Entity key map:
	 * 'I' - insert ingredient into the cooking pot / cooking pan / Or taking the ingredients with spatula.
	 * 'T' - take ingredients that can took by hand from the cooking pan ONLY.
	 */
	private Cookware cookingPot;
	private Cookware cookingPan;
	
	/**
	 * Spoon/Tea Spoon Entity key map:
	 * 'T' - take the spoon.
	 * 'P' - put the spoon.
	 */
	private Spoon spoon;
	private Spoon teaSpoon;
	
	/**
	 * spatula Entity key map:
	 * 'T' - take the spatula.
	 * 'P' - put the spatula.
	 */
	private Spatula spatula;
	
	private GameItem dish;
	private Vector<IngredientData> ingredients;
	
	//GUIs checker - check if rendered.
	private List<HudGui> guiList;
	
	//Tray GUIs
	private HudGui trayToBake;
	private HudGui trayNotBaked;
	private HudGui trayBaked;
	private HudGui trayBurned;
	
	//Mixer and Blender GUIs
	private HudGui ingredientProcessed;
	
	//Bowl GUIs
	private HudGui emptyBowl;
	private HudGui ingredientBowl;
	
	//Ingredient GUIs
	 /* 
	  * Key Map - 
	 * 'R' - remove the current ingredient hud when rendered.
	 */
	private HudGui ingredientTaken;
	
	//Spoon and TeaSpoon GUIs
	private HudGui emptySpoon;
	private HudGui emptyTeaSpoon;
	private HudGui ingredientOnSpoon;
	private HudGui ingredientOnTeaSpoon;
	
	//Spatula GUIs
	private HudGui emptySpatula;
	private HudGui ingredientOnSpatula;
	
	//Save ingredients that put in the cooking pot/oven/trsy/bowl and more so we can render new texture after the changes.
	private String currentIngredientGui;
	
	//save the ingredient name and check the if's in checkIngredientsActions function.
	private String saveIngredient;
	
	//if on the spoon/tea spoon there is oil - the player can add it into the cookware.
	private boolean oilOnSpoon;
	
	//save the amount of the ingredient taken and add the amount into the list (to calculate later the score).
	private int saveAmount;
	
	//Hud of the ingredient amount - to take
    private HudAmount hudAmount;
    
    //Hud of the timer
    private HudTimer hudTimer;
    
    //mini timer for the blender/mixer/baking/cooking
    private HudMiniTimer hudMiniTimer;
    
    //save in the class the choosed recipe.
    private Recipe recipeChosen;
    
    //if ingredient found check the actions of the ingredient and init the hudAmount.
    private boolean ingredientFound;
    
    //save the seconds of the huderTime for activate the timer of the processing.
    private int startToFoodProcess;
    
    //save the seconds of the huderTime for activate the timer of the cookware.
    private int startToCooked;
    
    //save the seconds of the huderTime for activate the timer of the baking.
    private int startToBake;
    
    //check if the ingredients put first in the plate (to change the texture of the dish when the player add other ingredients).
    private boolean combinedIngredientsOnDish;
    
    //init the current message of the action into hudExceptions
    private String currentMessage;
    
    //init the time of the hudExceptions (the message will apper for 3 seconds).
    private int messageTime;
    
    //saves the current changes in the kitchen tool.
    private Logs log;
    
    //When we starting the simulation - the time is setting to 0,0 - the simulation need a flag so it can init the recipe.
    private boolean startToCheckTime;
    
    public ChoppedSimulation() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        scene = new Scene();
    	hudTimer = new HudTimer();
    	hudAmount = new HudAmount();
    	hudLoading = new HudLoading();
    	hudExceptions = new HudExceptions();
		combinedIngredientsOnDish = false;
        startToCheckTime = false;
    	/* Setting up all the GUIs */
		trayToBake = new HudGui();
		trayNotBaked = new HudGui();
		trayBaked = new HudGui();
		trayBurned = new HudGui(); 
		ingredientProcessed = new HudGui();
		emptyBowl = new HudGui();
		ingredientBowl = new HudGui();
		ingredientTaken = new HudGui();
		emptySpoon = new HudGui();
		emptyTeaSpoon = new HudGui();
		ingredientOnSpoon = new HudGui();
		ingredientOnTeaSpoon = new HudGui();
		emptySpatula = new HudGui();
		ingredientOnSpatula = new HudGui();
		guiList = new Vector<HudGui>();
		hudMiniTimer = new HudMiniTimer();
		/***********/
		ingredientFound = false;
    }
	
    public void initLoadingScreen(Window window) throws Exception {
		hudLoading.init(window);
		hudLoading.setRendered(true);
    }
    
	public void initKitchenEntities() throws Exception {
		ingredients = SimulationService.getIngredients();
		tray = new Tray();
		blender = new FoodProcessor();
		mixer = new FoodProcessor();
		bowl = new Bowl();
		cookingPot = new Cookware();
		cookingPan = new Cookware();
		spoon = new Spoon();
		teaSpoon = new Spoon();
		spatula = new Spatula();
		
		for(GameItem gameItem : gameItems) {
			if(gameItem.getNameGameItem().equals("oven"))
				oven = new Oven(gameItem);
			else if(gameItem.getNameGameItem().equals("tray")) {
				tray = new Tray(gameItem);
				tray.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("blender")) {
				blender = new FoodProcessor(gameItem);
				blender.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("mixer")) {
				mixer = new FoodProcessor(gameItem);
				mixer.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("bowl")) {
				bowl = new Bowl(gameItem);
				bowl.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("cooking pot")) {
				cookingPot = new Cookware(gameItem);
				cookingPot.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("cooking pan")) {
				cookingPan = new Cookware(gameItem);
				cookingPan.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("spoon")) {
				spoon = new Spoon(gameItem);
				spoon.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("tea spoon")) {
				teaSpoon = new Spoon(gameItem);
				teaSpoon.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("spatula")) {
				spatula = new Spatula(gameItem);
				spatula.setInSimulation(true);
			}
			else if(gameItem.getNameGameItem().equals("dish"))
				dish = gameItem;
			//add whisk later.
		}
		
		guiList.add(trayToBake);
		guiList.add(trayBaked);
		guiList.add(trayBurned);
		guiList.add(ingredientProcessed);
		guiList.add(emptyBowl);
		guiList.add(ingredientBowl);
		guiList.add(ingredientTaken);
		guiList.add(emptySpoon);
		guiList.add(emptyTeaSpoon);
		guiList.add(ingredientOnSpoon);
		guiList.add(ingredientOnTeaSpoon);
		guiList.add(emptySpatula);
		guiList.add(ingredientOnSpatula);
		SimulationService.initScoreCalculatorLists();
	}
	
	public void initSimulation(Window window) throws Exception {
		initLoadingScreen(window);
		//If the player clicked on the button continue -> first it will init the recipe.
		if(SimulationService.isLoadLog()) {
			String logFolder = "SavedSimulationFiles";
			File file = new File(logFolder);
			if(file.exists())
				SimulationService.initRecipeFromLog();
		}
    	recipeChosen = SimulationService.getRecipeChosen();
		simulationRendered = false;
		hudAmount.init(window);
		hudTimer.init(window);
        renderer.init(window);
		hudExceptions.init(window);
		hudMiniTimer.init(window);

        scene = new Scene();
        
        float skyBoxScale = 100.0f;

        selectDetector = new MouseBoxSelectionDetector();

        float terrainScale = 1.5f;
        int terrainSize = 1;
        int textInc = 0;
        Terrain terrain = new Terrain(terrainSize,terrainScale, "/resources/textures/floor.png", textInc);
        scene.setGameItems(terrain.getGameItems());
        
        // Setup  SkyBox
        SkyBox skyBox = new SkyBox("/resources/models/skybox.obj", "/resources/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        camera.getPosition().x = 21f; 
        camera.getPosition().y = 15f; 
        camera.getPosition().z = 41f; 
        camera.getRotation().x = 11f;
        camera.getRotation().y = 34.6f;
        
        //Setup Kitchen GameItems
        SimulationService.initKitchenEntities();
        gameItems = SimulationService.getGameItems();
        SimulationService.setScene(scene);
        
    	ingredientTaken.init(window);
    	trayToBake.init(window);
    	trayBaked.init(window);
    	trayBurned.init(window);
    	trayNotBaked.init(window);
    	ingredientProcessed.init(window);
    	emptyBowl.init(window);
    	ingredientBowl.init(window);
    	emptySpoon.init(window);
    	emptyTeaSpoon.init(window);
    	ingredientOnSpoon.init(window);
    	ingredientOnTeaSpoon.init(window);
    	emptySpatula.init(window);
    	ingredientOnSpatula.init(window);
    	initKitchenEntities();
    	
		//If the player clicked on the button continue -> it will load the last session of kitchen tools (will init the load file).
		if(SimulationService.isLoadLog()) {
			String logFolder = "SavedSimulationFiles";
			File file = new File(logFolder);
			if(file.exists())
				SimulationService.loadLog(dish,bowl,cookingPot,cookingPan,tray,blender,mixer);
			SimulationService.setLoadLog(false);
		}
    	
		emptySpoon.setFileName("src/main/java/resources/recipes/textures/spoon empty gui.png");
		emptyTeaSpoon.setFileName("src/main/java/resources/recipes/textures/tea spoon empty gui.png");
		emptyBowl.setFileName("src/main/java/resources/recipes/textures/bowl empty gui.png");
		emptySpatula.setFileName("src/main/java/resources/recipes/textures/spatula empty gui.png");
		
		log = new Logs();
		log.createLog();
		log.currentRecipe(recipeChosen);
		
        simulationRendered = true;
	}

	@Override
	public void init(Window window) throws Exception {
	}
	
	public boolean hasCollision() {
		if(camera.getPosition().z>13f&& camera.getPosition().z<70f && camera.getPosition().x>2f&& camera.getPosition().x<70f) 
			return true;
		
		else if(camera.getPosition().z<13f ) {	
			camera.setPosition(camera.getPosition().x, 15,14);
			return true;
		}
		else if(camera.getPosition().z>70f ) {
			camera.setPosition(camera.getPosition().x, 15, 69);
			return true;
		}
		else if(camera.getPosition().x<2f) {	
			camera.setPosition(3, 15,camera.getPosition().z);
			return true;
		}
		else if(camera.getPosition().x>70f) {
			camera.setPosition(69, 15, camera.getPosition().z);
			return true;
		}
		else
			return false;
	}

	@Override
	public void input(Window window, MouseInput mouseInput) throws Exception {
        if(simulationRendered) {
        	cameraInc.set(0, 0, 0);
        	if (window.isKeyPressed(GLFW_KEY_W) && hasCollision()) {
        		cameraInc.z = -3;
        	} else if (window.isKeyPressed(GLFW_KEY_S) && hasCollision()) {
        		cameraInc.z = 3;
        	}
        	if (window.isKeyPressed(GLFW_KEY_A) && hasCollision()) {
        		cameraInc.x = -3;
        	} else if (window.isKeyPressed(GLFW_KEY_D) && hasCollision()) {
        		cameraInc.x = 3;
        	} else {
        		angleInc = 0;
        	}
        }
	}

	@SuppressWarnings("static-access")
	@Override
	public void update(float interval, MouseInput mouseInput, Window window) throws Exception {
    	if(simulationRendered) {
    		if (mouseInput.isRightButtonPressed()) {
    			// Update camera based on mouse            
    			Vector2f rotVec = mouseInput.getDisplVec();
    			camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
    		} 
    		// Update camera position
    		camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

    		// Update view matrix
    		camera.updateViewMatrix();
        
    		if(mouseInput.isRightButtonPressed())
    			this.selectDetector.selectGameItem(gameItems, window, mouseInput.getCurrentPos(), camera);
        
    		//If the hud amount of the ingredient is rendered - check the actions of the + and - actions.
    		if(mouseInput.isLeftButtonPressed()) {
    			if(hudAmount.isHoverPlus() && hudAmount.isRendered()) {
    				if(hudAmount.checkAmount())
    					hudAmount.incAmount();
    			}
            else if(hudAmount.isHoverMinus() && hudAmount.isRendered()) {
        		if(hudAmount.checkAmount() || hudAmount.checkMaxAmount())
        			hudAmount.decAmount();	
            	}
    		}
    		
    		//check ingredients actions, if one of the ingredient is selected, the simulation will check who is selected.
			saveSelectedIngredient = searchSelectedIngredient();
			if(saveSelectedIngredient!=null && ingredientFound && saveSelectedIngredient.getIngredient().isSelected()) {
				hudAmount.setRendered(true);
				checkIngredientsActions(window,saveSelectedIngredient);
			}
    		
    		//check mixer/blender actions when selected.
        	if(mixer.isInSimulation()) {
        		if(mixer.getFoodProcessor().isSelected())
        			checkFoodProcessorActions(window, mixer);
        		if(mixer.isOn())
        			mixer.processIngredients(startToFoodProcess);
        	}
        	else if(blender.isInSimulation()) {
        		if(blender.getFoodProcessor().isSelected())
        			checkFoodProcessorActions(window, blender);
        		if(blender.isOn())
        			blender.processIngredients(startToFoodProcess);
        	}
        	
        	//check bowl actions when selected
        	if(bowl.isInSimulation()) {
        		if(bowl.getKitchenTool().isSelected())
        			checkBowlActions(window, bowl);
        	}
        	
        	//check tea spoon / spoon actions when selected
        	if(spoon.isInSimulation()) {
        		if(spoon.getKitchenTool().isSelected())
        			checkSpoonActions(window, spoon);
        	}
        	if(teaSpoon.isInSimulation()) {
        		if(teaSpoon.getKitchenTool().isSelected())
        			checkSpoonActions(window, teaSpoon);
        	}
        	
        	//check spatula actions when selected
        	if(spatula.isInSimulation())
        		if(spatula.getKitchenTool().isSelected()) {
        			checkSpatulaActions(window,spatula);
        	}
        	
        	//check cooking pot / cooking pan actions when selected
        	if(cookingPot.isInSimulation()) {
        		if(cookingPot.getKitchenTool().isSelected())
        			checkCookwareActions(window,cookingPot);
        		if(cookingPot.isGasIsOn() && !cookingPot.isEmpty())
        			cookingPot.checkCookingTime(startToCooked);
        	}
        	if(cookingPan.isInSimulation()) {
        		if(cookingPan.getKitchenTool().isSelected())
        			checkCookwareActions(window,cookingPan);
        		if(cookingPan.isGasIsOn() && !cookingPan.isEmpty())
        			cookingPan.checkCookingTime(startToCooked);
        	}
        	
        	//check oven actions when selected
        	if(oven.getOven().isSelected()) {
        		checkOvenActions(window, oven);
        		if(tray.isInSimulation() && tray.isInOven() && oven.isInUse())
        			oven.checkTimeBaking(startToBake);
        	}
        	
        	//check tray actions when selected.
        	if(tray.isInSimulation()) {
        		if(tray.getKitchenTool().isSelected())
        			checkTrayActions(window, tray);
        	}
        	
        	//check dish actions when selected
        	if(dish.isSelected())
        		checkDishActions(window, dish);
        	
			if(ingredientTaken.isRendered()) {
				if(window.isKeyPressed(GLFW_KEY_R))
					ingredientTaken.setRendered(false);
			}
        	
        	//putting back kitchen tools when the guis -> bowl/spoon/tea spoon/ingredient taken is rendered
        	putBackKitchenTools(window);
        	
        	/*If the time is over - calculte all the actions that the player did and init the main*/
        	if(hudTimer.getMinutes()==0 && hudTimer.getSeconds()==0 && startToCheckTime) {
        		SimulationService.deleteLogFolder();
    			simulationRendered = false;
    			startToCheckTime = false;
    			SimulationService.setShowHudScore(true);
        		if(tray.isInSimulation())
        			SimulationService.initTray(tray);
        		else if(cookingPot.isInSimulation())
        			SimulationService.initCookingPot(cookingPot);
        		else if(cookingPan.isInSimulation())
        			SimulationService.initCookingPan(cookingPan);
    			IGameLogic menu  = new ChoppedMenu();
    			menu.init(window);
    			ChoppedSettings.startNewWindow(menu);
        	}
    	}
    	
    	if(SimulationService.isStartSimlation() && !simulationRendered) {
    		initSimulation(window);
    	    if(simulationRendered) {
    	    	hudLoading.setRendered(false);
    	    	SimulationService.setStartSimlation(false);
    	    }
    	}
	}

	@Override
	public void render(Window window) throws Exception {
    	if(hudLoading.isRendered() && !simulationRendered) {
    		hudLoading.render(window);
    	}
    	
		if(!SimulationService.isStartSimlation() && simulationRendered) {
	        startToCheckTime = true;
			renderer.render(window, camera, scene);
			hudTimer.render(window);
		}
		
		if(saveSelectedIngredient!=null && ingredientFound && saveSelectedIngredient.getIngredient().isSelected())
			hudAmount.render(window);
		
		//render the message
		if(hudExceptions.isRendered()) 
			hudExceptions.renderMessage(window, currentMessage, messageTime);
		//reset the message
		if(!hudExceptions.isRendered())
			currentMessage = "";
		
		//if some of the ingredients the user can take without the kitchen tool, it will render the gui of the ingredient.
		if(ingredientTaken.isRendered()) {
			ingredientFound = false;
			ingredientTaken.render(window);
		}
			
		//check the guis and render them
		if(ingredientProcessed.isRendered())
			ingredientProcessed.render(window);
			//bowl guis renderer
		if(emptyBowl.isRendered())
			emptyBowl.render(window);
		if(ingredientBowl.isRendered())
			ingredientBowl.render(window);
		//spoon / tea spoon guis renderer
		if(emptyTeaSpoon.isRendered())
			emptyTeaSpoon.render(window);
		if(emptySpoon.isRendered())
			emptySpoon.render(window);
		if(ingredientOnSpoon.isRendered())
			ingredientOnSpoon.render(window);
		if(ingredientOnTeaSpoon.isRendered())
			ingredientOnTeaSpoon.render(window);
		//spatula guis renderer
		if(emptySpatula.isRendered())
			emptySpatula.render(window);
		if(ingredientOnSpatula.isRendered())
			ingredientOnSpatula.render(window);
		//tray guis renderer
		if(trayToBake.isRendered())
			trayToBake.render(window);
		if(trayBaked.isRendered())
			trayBaked.render(window);
		if(trayBurned.isRendered())
			trayBurned.render(window);
		if(trayNotBaked.isRendered())
			trayNotBaked.render(window);
		//if the player started to cook/bake/process ingredients - show mini timer
		if(hudMiniTimer.isRendered())
			hudMiniTimer.render(window);
		}

	@Override
	public void cleanup() {
		if(simulationRendered) {
			renderer.cleanup();
			scene.cleanup();
			if(hudAmount!=null)
				hudAmount.cleanup();
			else if(hudTimer!=null)
				hudTimer.cleanup();
		}
        else if(hudLoading!=null)
        	hudLoading.cleanup();
	}
	
	@SuppressWarnings("static-access")
	public void checkDishActions(Window window, GameItem dish) throws Exception {
		if(window.isKeyPressed(GLFW_KEY_I)) {
			//if optional ingredient is rendered - add it into the dish.
			if(ingredientTaken.isRendered() && !combinedIngredientsOnDish) {
				if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "dish"))
					SimulationService.checkDuplicates(currentIngredientGui,saveAmount,"dish");
				else
					SimulationService.addIngredientsOnDish(currentIngredientGui, saveAmount);
				dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/burger buns dish.png");
				log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/burger buns dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
				ingredientTaken.setRendered(false);
			}
			else if(ingredientTaken.isRendered() && combinedIngredientsOnDish) {
				if((cookingPot.isInSimulation() && cookingPot.isBurned()) || (cookingPan.isInSimulation() && cookingPan.isBurned())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns burned dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns burned dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
				}
				else if((cookingPot.isInSimulation() && cookingPot.isCooked()) || (cookingPan.isInSimulation() && cookingPan.isCooked())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns cooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns cooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
				}
				else if((cookingPot.isInSimulation() && !cookingPot.isCooked()) || (cookingPan.isInSimulation() && !cookingPan.isCooked())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns notcooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns notcooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
				}
				ingredientTaken.setRendered(false);
			}
			
			//if spatula is rendered - add the ingredients into the dish.
			if(spatula.isTaken() && !spatula.isEmpty() && !SimulationService.ingredientOnKitchenTools("dish")) {
				//cookware actions
				if((cookingPot.isInSimulation() && cookingPot.isBurned()) || (cookingPan.isInSimulation() && cookingPan.isBurned())) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burned dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burned dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if((cookingPot.isInSimulation() && cookingPot.isCooked()) || (cookingPan.isInSimulation() && cookingPan.isCooked())) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient cooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient cooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if((cookingPot.isInSimulation() && !cookingPot.isCooked()) || (cookingPan.isInSimulation() && !cookingPan.isCooked())) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient notcooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient notcooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				//tray actions
				if(tray.isInSimulation() && tray.isBurned()) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burned dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burned dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if(tray.isInSimulation() && tray.isBaked()) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient baked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient baked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if(tray.isInSimulation() && tray.isNotBaked()) {
					combinedIngredientsOnDish = true;
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient not baked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient not baked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
			}
			
			else if(spatula.isTaken() && !spatula.isEmpty() && SimulationService.ingredientOnKitchenTools("dish")) {
				if((cookingPot.isInSimulation() && cookingPot.isBurned()) || (cookingPan.isInSimulation() && cookingPan.isBurned())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns burned dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns burned dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if((cookingPot.isInSimulation() && cookingPot.isCooked()) || (cookingPan.isInSimulation() && cookingPan.isCooked())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns cooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns cooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);
				}
				else if((cookingPot.isInSimulation() && !cookingPot.isCooked()) || (cookingPan.isInSimulation() && !cookingPan.isCooked())) {
					dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns notcooked dish.png");
					log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient burger buns notcooked dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					spatula.setEmpty(true);
					ingredientOnSpatula.setRendered(false);
					emptySpatula.setRendered(true);;
				}
			}
			
			if(SimulationService.optionalIngredientsOnDish(currentIngredientGui)) {
				if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "dish")) 
					SimulationService.addDuplicates(currentIngredientGui, saveAmount, "dish");
				else
					SimulationService.addIngredientsOnDish(currentIngredientGui, saveAmount);
				log.addedIngredientsKitchenTools(dish.getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				currentMessage = currentIngredientGui + " Added on the dish.";
				hudExceptions.setRendered(true);
			}
			else if(((!recipeChosen.getName().contains("salad") && bowl.isTaken()) || spoon.isTaken() || teaSpoon.isTaken()) &&  !SimulationService.optionalIngredientsOnDish(currentIngredientGui)) {
				currentMessage = "You can't add current ingredient into the dish.";
				hudExceptions.setRendered(true);
			}
			
			if(recipeChosen.getName().contains("salad") && bowl.isTaken() && ingredientBowl.isRendered()) {
				dish.changeTexture(dish, "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient salad dish.png");
				log.changedTextures(dish.getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredient salad dish.png",hudTimer.getMinutes(),hudTimer.getSeconds());
				bowl.setEmpty(true);
				bowl.setCombined(false);
				bowl.setTexture("empty", "");
				ingredientBowl.setRendered(false);
				emptyBowl.setRendered(true);
			}
		}
		
		/*If the player clicked the 'Q' to serve the dish after baking / cooking
		 * the simulation will delete the log file (because the simulation not crashed).
		 * In the simulation service, it will init all the changes of the tray and the cookware to calculate the score.
		 * And the simulation will render the score screen.
		*/
		if(window.isKeyPressed(GLFW_KEY_Q)) {
    		SimulationService.deleteLogFolder();
			simulationRendered = false;
			startToCheckTime = false;
    		if(tray.isInSimulation())
    			SimulationService.initTray(tray);
    		else if(cookingPot.isInSimulation())
    			SimulationService.initCookingPot(cookingPot);
    		else if(cookingPan.isInSimulation())
    			SimulationService.initCookingPan(cookingPan);
			SimulationService.setShowHudScore(true);
			IGameLogic menu  = new ChoppedMenu();
			menu.init(window);
			ChoppedSettings.startNewWindow(menu);
		}
			
	}
	
	public void checkSpatulaActions(Window window, Spatula spatula) throws Exception {
		if(!isGuiRendered()) {
			//if 'T' is pressed - render the gui of the spatula that selected.
			if(window.isKeyPressed(GLFW_KEY_T)) {
				spatula.setTaken(true);
				SimulationService.removeGameItemFromScene(spatula.getKitchenTool());
				emptySpatula.setRendered(true);
			}
		}
	}
	
	public void checkSpoonActions(Window window, Spoon spoon) throws Exception {
		if(!isGuiRendered()) {
			//if 'T' is pressed - render the gui of the spoon that selected.
			if(window.isKeyPressed(GLFW_KEY_T)) {
				spoon.setTaken(true);
				if(spoon.getKitchenTool().getNameGameItem().equals("spoon")) {
					emptySpoon.setFileName("src/main/java/resources/recipes/textures/spoon empty gui.png");
					emptySpoon.setRendered(true);
					SimulationService.removeGameItemFromScene(spoon.getKitchenTool());
				}
				if(spoon.getKitchenTool().getNameGameItem().equals("tea spoon")) {
					emptyTeaSpoon.setFileName("src/main/java/resources/recipes/textures/tea spoon empty gui.png");
					emptyTeaSpoon.setRendered(true);
					SimulationService.removeGameItemFromScene(spoon.getKitchenTool());
				}
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void putBackKitchenTools(Window window) throws Exception {
		//if 'P' is pressed - cleanup the current gui and set into the scene the gameitem that the player want to put back.
		if(window.isKeyPressed(GLFW_KEY_P)) {
			//putting tea spoon or the spoon back.
			if(emptySpoon.isRendered() || emptyTeaSpoon.isRendered()) {
				if(spoon.isTaken() && spoon.isEmpty()) {
					spoon.setTaken(false);
					emptySpoon.setRendered(false);
					SimulationService.putBackGameItemToScene(spoon.getKitchenTool());
				}
				else if(teaSpoon.isTaken() && teaSpoon.isEmpty()) {
					teaSpoon.setTaken(false);
					emptyTeaSpoon.setRendered(false);
					SimulationService.putBackGameItemToScene(teaSpoon.getKitchenTool());
				}
			}
			else if(ingredientOnSpoon.isRendered() || ingredientOnTeaSpoon.isRendered()) {
				currentMessage = "The spoon must be empty!";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			
			//put back the bowl and render the current texture to the bowl.
			if(emptyBowl.isRendered() && bowl.isEmpty() && bowl.isTaken()) {
				bowl.setTaken(false);
				bowl.setTexture("empty", "");
				log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
				SimulationService.putBackGameItemToScene(bowl.getKitchenTool());
				emptyBowl.setRendered(false);
			}
			else if(ingredientBowl.isRendered() && !bowl.isEmpty() && !bowl.isCombined() && bowl.isTaken()) {
				ingredientBowl.setRendered(false);
				bowl.setTaken(false);
				bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png");
				log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
				SimulationService.putBackGameItemToScene(bowl.getKitchenTool());
			}
			else if(ingredientBowl.isRendered() && !bowl.isEmpty() && bowl.isCombined() && bowl.isTaken()) {
				ingredientBowl.setRendered(false);
				bowl.setTaken(false);
				bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients bowl.png");
				log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
				SimulationService.putBackGameItemToScene(bowl.getKitchenTool());
			}
			
			//put back the spatula when it's empty only!
			if(emptySpatula.isRendered() && spatula.isTaken() && spatula.isEmpty()) {
				emptySpatula.setRendered(false);
				spatula.setTaken(false);
				SimulationService.putBackGameItemToScene(spatula.getKitchenTool());
			}
			else if(ingredientOnSpatula.isRendered() && !spatula.isEmpty() && spatula.isTaken()) {
				currentMessage = "You can't put back the spatula when it's not empty! add the ingredient on the spatula to the plate!";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			
			//putting back the tray and check every tray guis.
			if(trayToBake.isRendered() && tray.isTaken()) {
				tray.setTaken(false);
				trayToBake.setRendered(false);
				SimulationService.putBackGameItemToScene(tray.getKitchenTool());
			}
			//if tray is baked:
			else if(trayBaked.isRendered() && tray.isTaken() && tray.isBaked()) {
				tray.setTaken(false);
				trayBaked.setRendered(false);
				SimulationService.putBackGameItemToScene(tray.getKitchenTool());
			}
			//if tray is burned:
			else if(trayBurned.isRendered() && tray.isTaken() && tray.isBurned()) {
				tray.setTaken(false);
				trayBurned.setRendered(false);
				SimulationService.putBackGameItemToScene(tray.getKitchenTool());
			}
			else if(trayNotBaked.isRendered() && tray.isTaken() && tray.isNotBaked()) {
				tray.setTaken(false);
				trayNotBaked.setRendered(false);
				SimulationService.putBackGameItemToScene(tray.getKitchenTool());
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkBowlActions(Window window, Bowl bowl) throws Exception {
		//'T' - take the bowl and render the current gui bowl.
		if(window.isKeyPressed(GLFW_KEY_T) && !isGuiRendered()) {
			bowl.setTaken(true);
			if(bowl.isEmpty()) {
				emptyBowl.setRendered(true);
				SimulationService.removeGameItemFromScene(bowl.getKitchenTool());
			}
			if(!bowl.isEmpty() && bowl.isCombined()) {
				ingredientBowl.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients bowl gui.png");
				ingredientBowl.setRendered(true);
				SimulationService.removeGameItemFromScene(bowl.getKitchenTool());
			}
			if(!bowl.isEmpty() && !bowl.isCombined()) {
				ingredientBowl.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl gui.png");
				ingredientBowl.setRendered(true);
				SimulationService.removeGameItemFromScene(bowl.getKitchenTool());
			}
		}
			 
		//'I' - check which kitchen tool is rendered and set the new gui when the ingredient is inserted into the kitchen tool.
		if(window.isKeyPressed(GLFW_KEY_I)) {
				//check if tea spoon/spoon is rendered with ingredients on it to put into the bowl.
				if(!teaSpoon.isEmpty() && teaSpoon.isTaken() && ingredientOnTeaSpoon.isRendered()) {
					if(SimulationService.checkDuplicates(currentIngredientGui,saveAmount,"bowl")) 
						SimulationService.addDuplicates(currentIngredientGui,saveAmount,"bowl");
					else
						SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(bowl.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
					bowl.setEmpty(false);
					ingredientOnTeaSpoon.setRendered(false);
					teaSpoon.setEmpty(true);
					emptyTeaSpoon.setRendered(true);
					bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png");
					log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"not empty","/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					currentMessage = currentIngredientGui + "Added in the bowl";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
				}
				else if(!spoon.isEmpty() && spoon.isTaken() && ingredientOnSpoon.isRendered()) {
					if(SimulationService.checkDuplicates(currentIngredientGui,saveAmount,"bowl")) 
						SimulationService.addDuplicates(currentIngredientGui,saveAmount,"bowl");
					else
						SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(bowl.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
					bowl.setEmpty(false);
					ingredientOnSpoon.setRendered(false);
					spoon.setEmpty(true);
					emptySpoon.setRendered(true);
					bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png");
					log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"not empty","/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					currentMessage = currentIngredientGui + " Added in the bowl";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
				}
				else if(ingredientProcessed.isRendered()) {
					bowl.setEmpty(false);
					ingredientProcessed.setRendered(false);
					bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients bowl.png");
					log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"not empty","/resources/recipes/"+recipeChosen.getName()+"/combinedingredients bowl.png",hudTimer.getMinutes(),hudTimer.getSeconds());
					bowl.setCombined(true);
			}
		}
		
		//stir the ingredients with empty spoon only.
		if(window.isKeyPressed(GLFW_KEY_C)) {
			if(spoon.isEmpty() && spoon.isTaken() && emptySpoon.isRendered()) {
				if(!bowl.isEmpty()) {
					bowl.setCombined(true);
					bowl.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients bowl.png");
					currentMessage = "The ingredients are combined";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
				}
				else if(bowl.isEmpty()) {
					currentMessage = "The bowl is empty.";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
				}
			}
			else if(!spoon.isEmpty() && spoon.isTaken() && ingredientOnSpoon.isRendered()) {
				currentMessage = "The spoon must be empty.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkOvenActions(Window window, Oven oven) throws Exception {
		//'I' - When the tray gui is rendered with the ingredients on, put the tray into the oven.
		if(window.isKeyPressed(GLFW_KEY_I)) {
			if(trayToBake.isRendered() && tray.isTaken()) {
				tray.setTaken(false);
				tray.setInOven(true);
				trayToBake.setRendered(false);
				oven.setInUse(true);
				startToBake = hudTimer.getSeconds();
				hudMiniTimer.init(window);
				hudMiniTimer.setOvenInUse(true);
				hudMiniTimer.setSeconds(0);
				hudMiniTimer.setMinutes(0);
				hudMiniTimer.setTime(2);
				hudMiniTimer.setRendered(true);
			}
			else if(bowl.isTaken()) {
				currentMessage = "You can't add bowl into the oven.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
		
		//'S' - stop the baking in the oven and take out the tray and check the time that the ingredients were in the oven.
		if(window.isKeyPressed(GLFW_KEY_S)) {
			if(!isGuiRendered() && tray.isInOven()) {
				oven.setInUse(false);
				hudMiniTimer.setOvenInUse(false);
				hudMiniTimer.setRendered(false);
				//take out the tray when not fully baked (before the time).
				if(oven.getTotalCookingTime() > 0 && oven.getTotalCookingTime() <= 5) {
					tray.setTaken(true);
					tray.setInOven(false);
					tray.setTexture("not empty","/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray.png");
					trayNotBaked.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray gui.png");
					trayNotBaked.setRendered(true);
					tray.setNotBaked(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				//take out the tray when fully baked (exactly on time).
				if(oven.getTotalCookingTime() <= 0 && oven.getTotalCookingTime() > -10) {
					tray.setTaken(true);
					tray.setInOven(false);
					tray.setTexture("not empty","/resources/recipes/"+recipeChosen.getName()+"/combinedingredients baked tray.png");
					trayBaked.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients baked tray gui.png");
					trayBaked.setRendered(true);
					tray.setBaked(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients baked tray.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				//take out the tray when the ingredients burned (not on time).
				if(oven.getTotalCookingTime() < -10) {
					tray.setTaken(true);
					tray.setInOven(false);
					tray.setTexture("not empty","/resources/recipes/"+recipeChosen.getName()+"/combinedingredients burned tray.png");
					trayBurned.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients burned tray gui.png");
					trayBurned.setRendered(true);
					tray.setBurned(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients burned tray.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
			else if(tray.isInOven() && isGuiRendered()) {
				currentMessage = "You can't take out the tray! Press 'P' or 'R' to put/remove the current kitchen tool/ingredient.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
		
		//turning on the gas for the cooking pot/pan to cook ingredients.
		if(window.isKeyPressed(GLFW_KEY_O)) { 
			//cooking pot / pan actions when the gas is on - if on and it's not empty, it will start to check cooking time until the gas is off.
			if(!cookingPot.isEmpty() && !cookingPot.isTimeChecked()) {
				cookingPot.setGasIsOn(true);
				startToCooked = hudTimer.getSeconds();
				currentMessage = "Started to cook for 4 minutes. Press 'F' to stop.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
				hudMiniTimer.init(window);
				hudMiniTimer.setSeconds(0);
				hudMiniTimer.setMinutes(0);
				hudMiniTimer.setTime(4);
				hudMiniTimer.setRendered(true);
				hudMiniTimer.setCookwareInUse(true);
			}
			if(!cookingPan.isEmpty() && !cookingPan.isTimeChecked()) {
				cookingPan.setGasIsOn(true);
				startToCooked = hudTimer.getSeconds();
				currentMessage = "Started to cook for 4 minutes. Press 'F' to stop.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
				hudMiniTimer.init(window);
				hudMiniTimer.setSeconds(0);
				hudMiniTimer.setMinutes(0);
				hudMiniTimer.setTime(4);
				hudMiniTimer.setRendered(true);
				hudMiniTimer.setCookwareInUse(true);
			}
		}
		
		//turning off the gas for the cooking pot/pan.
		if(window.isKeyPressed(GLFW_KEY_F)) {
			if(cookingPan.isInSimulation() && cookingPan.isGasIsOn()) {
				cookingPan.setGasIsOn(false);
				if(!SimulationService.ingredientOnKitchenTools("cooking pan")) {
					hudMiniTimer.setRendered(false);
					hudMiniTimer.setCookwareInUse(false);
					cookingPan.changeCookWareTexture(log,recipeChosen.getName(), "combinedingredients");
					log.checkCookware(cookingPan, hudTimer.getMinutes(), hudTimer.getSeconds());
				} 
				else if(SimulationService.ingredientOnKitchenTools("cooking pan")) {
					hudMiniTimer.setRendered(false);
					hudMiniTimer.setCookwareInUse(false);
					cookingPan.changeCookWareTexture(log,recipeChosen.getName(), currentIngredientGui);
					log.checkCookware(cookingPan, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
			else if(cookingPot.isInSimulation() && cookingPot.isGasIsOn()) {
				cookingPot.setGasIsOn(false);
				if(!SimulationService.ingredientOnKitchenTools("cooking pan")) {
					hudMiniTimer.setRendered(false);
					hudMiniTimer.setCookwareInUse(false);
					cookingPot.changeCookWareTexture(log,recipeChosen.getName(), "combinedingredients");
					log.checkCookware(cookingPot, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkTrayActions(Window window, Tray tray) throws Exception {
		//if 'I' is pressed - insert ingredients on the tray or on the spatula when the tray is baked.
		if(window.isKeyPressed(GLFW_KEY_I)) {
			//if the player took the bowl to insert the ingredients on tray.
			if(ingredientBowl.isRendered() && bowl.isTaken() && bowl.isCombined()) {
		        if(tray.isEmpty())
		        	tray.setEmpty(false);
				ingredientBowl.setRendered(false);
				bowl.setEmpty(true);
				emptyBowl.setRendered(true);
				//setting the current texture to the tray after adding the ingredients on the tray.
				tray.setTexture("not empty","/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray.png");
				log.changedTextures(tray.getKitchenTool().getNameGameItem(), "not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
		        log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
				log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"empty","/resources/textures/bowlTexture.png",hudTimer.getMinutes(),hudTimer.getSeconds());
			}
			
			if(emptySpatula.isRendered() && spatula.isTaken() && !tray.isInOven()) {
				if(tray.isNotBaked()) {
					emptySpatula.setRendered(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					spatula.setEmpty(false);
					tray.setTexture("empty","");
					tray.setEmpty(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/trayTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				else if(tray.isBaked()) {
					emptySpatula.setRendered(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients baked spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					spatula.setEmpty(false);
					tray.setTexture("empty","");
					tray.setEmpty(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/trayTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				else if(tray.isBurned()) {
					emptySpatula.setRendered(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients burned spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					spatula.setEmpty(false);
					tray.setTexture("empty","");
					tray.setEmpty(true);
					log.changedTextures(tray.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/trayTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
					log.checkTray(tray, hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
			else if(ingredientOnSpatula.isRendered() && !spatula.isEmpty() && spatula.isTaken() && !tray.isInOven()) {
				currentMessage = "You can't take another ingredient on the spatula! put back on the dish the current one!";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
			
		//if 'T' pressed - take the tray when the player want to bake the ingredients - ONLY.
		if(window.isKeyPressed(GLFW_KEY_T)) {
			//if the tray is not empty:
			if(!tray.isEmpty() && !isGuiRendered() && !oven.isTimeChecked()) {
				tray.setTaken(true);
				trayToBake.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to bake tray gui.png");
				trayToBake.setRendered(true);
				SimulationService.removeGameItemFromScene(tray.getKitchenTool());
			}
			else if((bowl.isTaken() || teaSpoon.isTaken() || spoon.isTaken() || spatula.isTaken()) && window.isKeyPressed(GLFW_KEY_T) && !tray.isEmpty() && !oven.isTimeChecked()) {
				currentMessage = "You can't take the tray. Please put the current kitchen tool by pressing 'P' or remove the ingredient by pressing 'R'";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkCookwareActions(Window window, Cookware cookware) throws Exception {
		if(window.isKeyPressed(GLFW_KEY_I)) {
			if(ingredientTaken.isRendered() && !SimulationService.ingredientOnKitchenTools("dish") && cookware.isEmpty()) { 
				if(cookware.isEmpty()) //if the gas is not on and the cooking pot is empty - set to false because we added ingredient.
					cookware.setEmpty(false);
				//add the ingredients into the selected cookware.
				if(cookware.getKitchenTool().getNameGameItem().equals("cooking pan"))
					SimulationService.addIngredientsIntoCookingPan(currentIngredientGui, saveAmount);
				else if(cookware.getKitchenTool().getNameGameItem().equals("cooking pot"))
					SimulationService.addIngredientsIntoCookingPot(currentIngredientGui, saveAmount);
				log.addedIngredientsKitchenTools(cookware.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				currentMessage = currentIngredientGui + " Added in the cookware";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
				ingredientTaken.setRendered(false);
				cookware.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/"+currentIngredientGui+" to cook cookware.png"); //set new texture of the cooking pot/pan when new ingredient inserted
				log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "added ingredient", "/resources/recipes/"+recipeChosen.getName()+"/"+currentIngredientGui+" to cook cookware.png",hudTimer.getMinutes(),hudTimer.getSeconds());
			}
			else if(ingredientTaken.isRendered() && SimulationService.ingredientOnKitchenTools("dish")) {
				currentMessage = "You can't insert the " + currentIngredientGui + " again! please remove it by pressing 'R'";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			else if(ingredientTaken.isRendered() && !SimulationService.ingredientOnKitchenTools("dish") && !cookware.isEmpty()) {
				currentMessage = "The cookware must be empty. please remove the current ingredient by pressing 'R'";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			
			//check if tea spoon/spoon is rendered with ingredients on it to put into the cookware.
			if(!teaSpoon.isEmpty() && teaSpoon.isTaken() && ingredientOnTeaSpoon.isRendered() && oilOnSpoon) {
				if(cookware.isEmpty()) //if the gas is not on and the cooking pot is empty - set to false because we added ingredient.
					cookware.setEmpty(false);
				
				if(cookware.getKitchenTool().getNameGameItem().equals("cooking pan")) {
					if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "cooking pan")) 
						SimulationService.addDuplicates(currentIngredientGui, saveAmount, "cooking pan");
					else
						SimulationService.addIngredientsIntoCookingPan(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(cookware.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				}
				else if(cookware.getKitchenTool().getNameGameItem().equals("cooking pot")) {
					if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "cooking pot")) 
						SimulationService.addDuplicates(currentIngredientGui, saveAmount, "cooking pot");
					else
						SimulationService.addIngredientsIntoCookingPot(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(cookware.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				}
					
				currentMessage = currentIngredientGui + " Added in the cookware";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
				ingredientOnTeaSpoon.setRendered(false);
				teaSpoon.setEmpty(true);
				emptyTeaSpoon.render(window);
			}
			
			else if(!spoon.isEmpty() && spoon.isTaken() && ingredientOnSpoon.isRendered() && oilOnSpoon) {
				if(cookware.isEmpty()) //if the gas is not on and the cooking pot is empty - set to false because we added ingredient.
					cookware.setEmpty(false);
				if(cookware.getKitchenTool().getNameGameItem().equals("cooking pan")) {
					if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "cooking pan")) 
						SimulationService.addDuplicates(currentIngredientGui, saveAmount, "cooking pan");
					else
						SimulationService.addIngredientsIntoCookingPan(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(cookware.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				}
				else if(cookware.getKitchenTool().getNameGameItem().equals("cooking pot")) {
					if(SimulationService.checkDuplicates(currentIngredientGui, saveAmount, "cooking pot")) 
						SimulationService.addDuplicates(currentIngredientGui, saveAmount, "cooking pot");
					else
						SimulationService.addIngredientsIntoCookingPot(currentIngredientGui, saveAmount);
					log.addedIngredientsKitchenTools(cookware.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
				}
				
				currentMessage = currentIngredientGui + " Added in the cookware";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
				ingredientOnSpoon.setRendered(false);
				spoon.setEmpty(true);
				emptySpoon.setRendered(true);
			}
			else if((spoon.isEmpty() && spoon.isTaken()) || (teaSpoon.isEmpty() && teaSpoon.isTaken())) {
				currentMessage = "You can't insert into the cookware empty spoon / tea spoon";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			else if((!spoon.isEmpty() && spoon.isTaken() && ingredientOnSpoon.isRendered() && !oilOnSpoon) || (!teaSpoon.isEmpty() && teaSpoon.isTaken() && ingredientOnTeaSpoon.isRendered() && !oilOnSpoon)) {
				currentMessage = "You can't insert into the cookware " + currentIngredientGui;
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}

			//if bowl with the ingredients is rendered, so we can add the ingredients from the bowl to the pan
			if(ingredientBowl.isRendered() && bowl.isCombined() && !bowl.isEmpty() && cookware.isEmpty()) {
				cookware.setIngredientsBowl(true);
				ingredientBowl.setRendered(false);
				emptyBowl.setRendered(true);
				bowl.setEmpty(true);
				bowl.setCombined(false);
				log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
				if(cookware.isEmpty()) //if the gas is not on and the cooking pot is empty - set to false because we added ingredient.
					cookware.setEmpty(false);
				cookware.setTexture("not empty", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to cook cookware.png");
				log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "added ingredient", "/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to cook cookware.png",hudTimer.getMinutes(),hudTimer.getSeconds());
			}
			if(ingredientBowl.isRendered() && !bowl.isCombined() && !bowl.isEmpty()) {
				currentMessage = "You can't insert into the cookware not combined ingredients.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			if(ingredientBowl.isRendered() && bowl.isCombined() && !bowl.isEmpty() && !cookware.isEmpty()) {
				currentMessage = "The cookware must be empty. Please put it back by pressing 'P'.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			if(emptyBowl.isRendered() && bowl.isEmpty()) {
				currentMessage = "You can't insert into the cookware empty bowl.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			
			//if processed ingredients is rendered - the player have to add the processed ingredients into the bowl.
			if(ingredientProcessed.isRendered()) {
				currentMessage = "You have to insert the processed ingredients into the bowl";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			
			if(spatula.isEmpty() && spatula.isTaken() && !cookware.isGasIsOn() && cookware.isTimeChecked()) {
				if(!cookware.isCooked() && !cookware.isBurned()) {
					emptySpatula.setRendered(false);
					spatula.setEmpty(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients to cook spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					cookware.setTexture("empty", "");
					cookware.setEmpty(true);
					cookware.setCheckCookTime(1);
					cookware.setIngredientsBowl(false);
					log.checkCookware(cookware, hudTimer.getMinutes(), hudTimer.getSeconds());
					log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/frypanTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				if(cookware.isCooked()) {
					emptySpatula.setRendered(false);
					spatula.setEmpty(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients cooked spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					cookware.setTexture("empty", "");
					cookware.setEmpty(true);
					cookware.setCheckCookTime(1);
					cookware.setIngredientsBowl(false);
					log.checkCookware(cookware, hudTimer.getMinutes(), hudTimer.getSeconds());
					log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/frypanTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				if(cookware.isBurned()) {
					emptySpatula.setRendered(false);
					spatula.setEmpty(false);
					ingredientOnSpatula.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/combinedingredients burned spatula gui.png");
					ingredientOnSpatula.setRendered(true);
					cookware.setTexture("empty", "");
					cookware.setEmpty(true);
					cookware.setCheckCookTime(1);
					cookware.setIngredientsBowl(false);
					log.checkCookware(cookware, hudTimer.getMinutes(), hudTimer.getSeconds());
					log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/frypanTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
			else if(spatula.isTaken() && !cookware.isGasIsOn() && cookware.isTimeChecked() && !spatula.isEmpty()) {
				currentMessage = "You can't take another ingredient when the spatula is not empty.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			else if(spatula.isTaken() && spatula.isEmpty() && cookware.isGasIsOn() && !cookware.isEmpty()) {
				currentMessage = "You can't take the ingredient when the gas is on. Turn the gas off.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			else if(spatula.isTaken() && spatula.isEmpty() && cookware.isEmpty()) {
				currentMessage = "There is nothing on the cookware.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
			else if(spatula.isTaken() && spatula.isEmpty() && !cookware.isEmpty() && !cookware.isTimeChecked()) {
				currentMessage = "You can't take the ingredient when it's not cooked.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
		
		if(window.isKeyPressed(GLFW_KEY_T) && cookware.getKitchenTool().getNameGameItem().equals("cooking pan") && !cookware.isIngredientsBowl()) {
			if(!isGuiRendered() && !cookware.isGasIsOn() && cookware.isTimeChecked() && !SimulationService.ingredientOnKitchenTools("dish")) {
				if(cookware.isBurned()) {
					cookware.setTexture("empty", "");
					ingredientTaken.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/burger buns burned gui.png");
					ingredientTaken.setRendered(true);
					cookware.setEmpty(true);
					cookware.setCheckCookTime(1);
					log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/frypanTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				}
				else if((cookware.isCooked() || !cookware.isCooked()) && !cookware.isBurned()) {
					cookware.setTexture("empty", "");
					ingredientTaken.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/burger buns gui.png");
					ingredientTaken.setRendered(true);
					cookware.setEmpty(true);
					cookware.setCheckCookTime(1);
					log.changedTextures(cookware.getKitchenTool().getNameGameItem(), "empty", "/resources/textures/frypanTexture.png", hudTimer.getMinutes(), hudTimer.getSeconds());
				}
			}
			if((bowl.isTaken() || teaSpoon.isTaken() || spoon.isTaken() || spatula.isTaken()) && !cookware.isGasIsOn() && cookware.isTimeChecked()) {
				currentMessage = "You can't use this kitchen tool to pick up the current ingredient.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkIngredientsActions(Window window, IngredientData ingredient) throws Exception {
		//if the crouser mouse on the button 'Take' - the player can click on it to take the amount of ingredient.
		hudAmount.setIngredientSelected(ingredient.getIngredient().getNameGameItem());
		if(hudAmount.isHoverTake()) {
			if(MouseInput.isLeftButtonPressed()) {
				saveIngredient = ingredient.getIngredient().getNameGameItem();
				saveAmount = hudAmount.getAmount();
				if(ingredientTaken.isRendered()) {
					currentMessage = "You must put the current ingredient on the kitchen tool.";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
					resetHudAmount(ingredient);
				}
				
				if((!SimulationService.isBurgerBuns(saveIngredient) || SimulationService.isBurgerBuns(saveIngredient)) && spatula.isTaken()) {
					currentMessage = "You can't take the ingredient with current kitchen tool.";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
					resetHudAmount(ingredient);
				}
				
				else if((saveAmount == 0 && isGuiRendered()) || (SimulationService.isBurgerBuns(saveIngredient) && saveAmount == 0)) {
					currentMessage = "the amount must be more than 0";
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
				}
				
				else if(!SimulationService.isBurgerBuns(saveIngredient) && !isGuiRendered()) {
					currentMessage = "You must take kitchen tool to put "+ saveIngredient;
					messageTime = hudTimer.getSeconds();
					hudExceptions.setRendered(true);
					resetHudAmount(ingredient);
				}
				/* check if guis is rendered and render the new gui of the ingredient that the player took */
				else if(SimulationService.isBurgerBuns(saveIngredient) && hudAmount.getAmount()!=0 && !isGuiRendered()) {
					currentIngredientGui = ingredient.getIngredient().getNameGameItem();
					ingredientTaken.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/"+currentIngredientGui+" gui.png");
					ingredientTaken.setRendered(true);
					resetHudAmount(ingredient);
				}
				
				else if(hudAmount.getAmount()!=0 && isGuiRendered()) {
					/*
					 * check if empty bowl / ingredient bowl is rendered is rendered - if rendered the player can add ingredients.
					 */
					if(!SimulationService.isBurgerBuns(saveIngredient) && !SimulationService.isMeasureSpoonOrTeaSpoon(ingredient) && bowl.isTaken() && bowl.isEmpty() && emptyBowl.isRendered() && bowl.isInSimulation()) {
						currentIngredientGui = ingredient.getIngredient().getNameGameItem();;
						emptyBowl.setRendered(false);
						bowl.setEmpty(false);
						if(SimulationService.checkDuplicates(currentIngredientGui,saveAmount,"bowl")) 
							SimulationService.addDuplicates(currentIngredientGui,saveAmount,"bowl");
						else
							SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
						ingredientBowl.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/"+saveIngredient+" gui.png");
						ingredientBowl.setRendered(true);
						log.addedIngredientsKitchenTools(bowl.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
						log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
						log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"not empty","/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png",hudTimer.getMinutes(),hudTimer.getSeconds());
						currentMessage = saveIngredient + " Added into the bowl";
						messageTime = hudTimer.getSeconds();
						hudExceptions.setRendered(true);
						resetHudAmount(ingredient);
					}
					else if(!SimulationService.isBurgerBuns(saveIngredient) && !SimulationService.isMeasureSpoonOrTeaSpoon(ingredient) && ingredientBowl.isRendered() && !bowl.isEmpty() && bowl.isTaken() && bowl.isInSimulation()) {
						currentIngredientGui = ingredient.getIngredient().getNameGameItem();
						if(SimulationService.checkDuplicates(currentIngredientGui,saveAmount,"bowl")) 
							SimulationService.addDuplicates(currentIngredientGui,saveAmount,"bowl");
						else
							SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
						ingredientBowl.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/"+saveIngredient+" gui.png");
						ingredientBowl.setRendered(true);
						log.addedIngredientsKitchenTools(bowl.getKitchenTool().getNameGameItem(), currentIngredientGui, saveAmount,hudTimer.getMinutes(),hudTimer.getSeconds());
						log.checkBowl(bowl,hudTimer.getMinutes(),hudTimer.getSeconds());
						log.changedTextures(bowl.getKitchenTool().getNameGameItem(),"not empty","/resources/recipes/"+recipeChosen.getName()+"/ingredients bowl.png",hudTimer.getMinutes(),hudTimer.getSeconds());
						currentMessage = saveIngredient + " Added into the bowl";
						messageTime = hudTimer.getSeconds();
						hudExceptions.setRendered(true);
						resetHudAmount(ingredient);
					}
					else if(SimulationService.isMeasureSpoonOrTeaSpoon(ingredient) && bowl.isTaken() || SimulationService.isBurgerBuns(saveIngredient) && bowl.isTaken()) {
						currentMessage = "You can't take " + saveIngredient + " into bowl";
						messageTime = hudTimer.getSeconds();
						hudExceptions.setRendered(true);
						resetHudAmount(ingredient);
					}
					/* if empty tea spoon/spoon gui is rendered -> add the ingredients
					 * the player can add only one ingredient on the tea spoon/spoon
					 * */
					if((!SimulationService.isBurgerBuns(saveIngredient) && spoon.isTaken()  && spoon.isInSimulation()) || (teaSpoon.isTaken() && teaSpoon.isInSimulation())) {
						if(SimulationService.isMeasureSoon(ingredient) && spoon.isEmpty() && emptySpoon.isRendered() && hudAmount.getAmount()==1) {
							oilOnSpoon = SimulationService.isOil(ingredient);
							currentIngredientGui = ingredient.getIngredient().getNameGameItem();;
							emptySpoon.setRendered(false);
							spoon.setEmpty(false);
							ingredientOnSpoon.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/"+saveIngredient+" spoon gui.png");
							ingredientOnSpoon.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if(!SimulationService.isMeasureSoon(ingredient) && spoon.isEmpty() && emptySpoon.isRendered() && hudAmount.getAmount()==1) {
							currentMessage = "You have to use tea spoon";
							messageTime = hudTimer.getSeconds();
							hudExceptions.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if(SimulationService.isMeasureTeaSoon(ingredient) && teaSpoon.isEmpty() && emptyTeaSpoon.isRendered() && hudAmount.getAmount()==1) {
							oilOnSpoon = SimulationService.isOil(ingredient);
							currentIngredientGui = ingredient.getIngredient().getNameGameItem();;
							emptyTeaSpoon.setRendered(false);
							teaSpoon.setEmpty(false);
							ingredientOnTeaSpoon.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/"+saveIngredient+" tea spoon gui.png");
							ingredientOnTeaSpoon.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if(!SimulationService.isMeasureTeaSoon(ingredient) && teaSpoon.isEmpty() && emptyTeaSpoon.isRendered() && hudAmount.getAmount()==1) {
							currentMessage = "You have to use spoon";
							messageTime = hudTimer.getSeconds();
							hudExceptions.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if((ingredientOnSpoon.isRendered() && !spoon.isEmpty() && spoon.isInSimulation()) || (ingredientOnTeaSpoon.isRendered() && !teaSpoon.isEmpty() && teaSpoon.isInSimulation())) {
							currentMessage = "The spoon / tea spoon have to be empty.";
							messageTime = hudTimer.getSeconds();
							hudExceptions.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if(!SimulationService.isMeasureSpoonOrTeaSpoon(ingredient) && (spoon.isTaken() || teaSpoon.isTaken())) {
							currentMessage = "You can't take " + saveIngredient + " with tea spoon / spoon";
							messageTime = hudTimer.getSeconds();
							hudExceptions.setRendered(true);
							resetHudAmount(ingredient);
						}
						else if((spoon.isTaken() || teaSpoon.isTaken()) && hudAmount.getAmount()!=1) {
							currentMessage = "The amount must be 1!";
							messageTime = hudTimer.getSeconds();
							hudExceptions.setRendered(true);
							resetHudAmount(ingredient);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void checkFoodProcessorActions(Window window, FoodProcessor foodProcessor) throws Exception {
		//'O' - turn on the food processor to process the food.
		if(window.isKeyPressed(GLFW_KEY_O)) {
			if(!foodProcessor.isEmpty()) {
				foodProcessor.setOn(true);
				startToFoodProcess = hudTimer.getSeconds();
				hudMiniTimer.init(window);
				hudMiniTimer.setSeconds(0);
				hudMiniTimer.setMinutes(0);
				hudMiniTimer.setTime(1);
				hudMiniTimer.setRendered(true);
				hudMiniTimer.setFoodProcessorInUse(true);
			}
			else if(foodProcessor.isEmpty()) {
				currentMessage = "You can't process food without ingredients.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			}
		}
		
		//'T' - check if there is any gui that rendered and take the mixed ingredient after turning off the food processor
		if(window.isKeyPressed(GLFW_KEY_T)) {
			if(!isGuiRendered() && !foodProcessor.isOn() && foodProcessor.isProcessedIngredients()) {
				ingredientProcessed.setFileName("src/main/java/resources/recipes/"+recipeChosen.getName()+"/processedingredients foodProcessor.png");
				ingredientProcessed.setRendered(true);
				foodProcessor.setEmpty(true);
				foodProcessor.setProcessedIngredients(false);
				hudMiniTimer.setRendered(false);
				hudMiniTimer.setFoodProcessorInUse(false);
			}
			 else if(foodProcessor.isOn()) {
				currentMessage = "You can't take while the mixer or blender is on!";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			 }
			 else if(!foodProcessor.isOn() && !foodProcessor.isProcessedIngredients()) {
				currentMessage = "Insert ingredients and press 'O' to process ingredients.";
				messageTime = hudTimer.getSeconds();
				hudExceptions.setRendered(true);
			 }
			log.checkFoodProcessor(foodProcessor,hudTimer.getMinutes(),hudTimer.getSeconds());
		}
		
		//'I' - insert the ingredients into the food processor, only in the mixer the player can add the ingredients when it's on.
		if(window.isKeyPressed(GLFW_KEY_I)) {
			if((foodProcessor.getFoodProcessor().getNameGameItem().equals("blender") && !foodProcessor.isOn()) || (foodProcessor.getFoodProcessor().getNameGameItem().equals("mixer"))) {
				if(foodProcessor.isEmpty()) //check if the food processor is empty.
					foodProcessor.setEmpty(false);
					
				if(ingredientBowl.isRendered() && bowl.isInSimulation() && bowl.isTaken() && !bowl.isCombined()) {
					ingredientBowl.setRendered(false);
					emptyBowl.setRendered(true);
					bowl.setEmpty(true);
				}
				//check if tea spoon/spoon is rendered with ingredients on it to put into the food processor.
				else if(teaSpoon.isInSimulation() && ingredientOnTeaSpoon.isRendered() && teaSpoon.isTaken() && !teaSpoon.isEmpty()) {
					ingredientOnTeaSpoon.setRendered(false);
					teaSpoon.setEmpty(true);
					if(foodProcessor.getFoodProcessor().getNameGameItem().equals("blender"))
						SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
					else if(foodProcessor.getFoodProcessor().getNameGameItem().equals("mixer"))
						SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
					emptyTeaSpoon.setRendered(true);
					}
					else if(spoon.isInSimulation() && ingredientOnSpoon.isRendered() && spoon.isTaken() && !spoon.isEmpty()) {
						ingredientOnSpoon.setRendered(false);
						spoon.setEmpty(true);
						if(foodProcessor.getFoodProcessor().getNameGameItem().equals("blender"))
							SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
						else if(foodProcessor.getFoodProcessor().getNameGameItem().equals("mixer"))
							SimulationService.addIngredientsIntoBowl(currentIngredientGui, saveAmount);
						emptySpoon.setRendered(true);
					}
					if(ingredientTaken.isRendered() && ingredientTaken.getFileName().contains("burger buns")) {
						currentMessage = "You can't process burger buns";
						messageTime = hudTimer.getSeconds();
						hudExceptions.setRendered(true);
					}
			}
			log.checkFoodProcessor(foodProcessor,hudTimer.getMinutes(),hudTimer.getSeconds());
		}
	}
	
	//reset the hud amount after taking the ingredient and the amount of the ingredient.
	private void resetHudAmount(IngredientData ingredient) {
		hudAmount.setAmount(0);
		ingredient.getIngredient().setSelected(false);
		hudAmount.setRendered(false);
	}
	
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.5f, 0.5f, 0.5f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(10);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }
	
    //check if there is any gui that rendered.
	private boolean isGuiRendered() {
		for(HudGui gui : guiList) {
			if(gui.isRendered())
				return true;
		}
		return false;
	}
	
	//search the selected ingredient.
	private IngredientData searchSelectedIngredient() {
		IngredientData selectedIngredient = null;
		for(IngredientData ingredient : ingredients) {
			if(ingredient.getIngredient().isSelected()) {
				selectedIngredient = ingredient;
				ingredientFound = true;
				return selectedIngredient;
			}
		}
		return selectedIngredient;
	}
	
}
