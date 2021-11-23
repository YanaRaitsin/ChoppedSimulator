package game;

import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import hud.HudLoading;
import hud.HudMain;
import hud.HudProfile;
import hud.HudRecipe;
import hud.HudScore;
import hud.HudSettings;
import hud.RecipeLoader;
import simulator.SimulationService;

public class ChoppedMenu implements IGameLogic {
    
    private HudMain hudMain;
    
    private HudLoading hudLoading;
    
    private HudScore hudScore;
    
    private HudProfile hudProfile;
    
    private HudSettings hudSettings;
    
    private boolean startSelected;
    
    private HudRecipe hudRecipe;
    
    private Vector<RecipeLoader>[] recipesLoader;
    
    private IGameLogic simulation;
    
    //because the lwjgl working of while loop, the values are not inc or dec only once. The simulation will add boolean variables so the value will only inc and dec once.
    private boolean mouseButtonClicked = false;

    public ChoppedMenu() {
    	SimulationService.setStartSimlation(false);
    	startSelected = false;
    	hudMain = new HudMain();
    	hudLoading = new HudLoading();
    	hudRecipe = new HudRecipe();
		simulation = new ChoppedSimulation();
		hudScore = new HudScore();
		hudProfile = new HudProfile();
		hudSettings = new HudSettings();
    }

    @Override
    public void init(Window window) throws Exception {
    	if(!SimulationService.isShowHudScore()) {
    		hudMain.init(window);
    		hudMain.setRendered(true);
    	}
    	if(SimulationService.isShowHudScore()) {
			hudScore.init(window);
			hudMain.setRendered(false);
			hudScore.setRendered(true);
			SimulationService.calculateScore();
			SimulationService.addDish();
			SimulationService.setShowHudScore(false);
		}
    }
    
    public void initLoadingScreen(Window window) throws Exception {
		hudLoading.init(window);
    }

    @SuppressWarnings("static-access")
	@Override
    public void input(Window window, MouseInput mouseInput) throws Exception {
        if(mouseInput.isLeftButtonPressed() && !SimulationService.isStartSimlation()) {
        	if(hudMain.isHoverExit()) {
        		GLFW.glfwSetWindowShouldClose(window.getWindowHandle(), true);
        	}
        	
        	//If continue button appered - the simulation crashed. the player can continue the last session if he will click on the button.
        	else if(hudMain.isHoverContinue() && hudMain.isRendered()) {
        		SimulationService.setStartSimlation(true);
        		SimulationService.setLoadLog(true);
        		//initLoadingScreen(window);
        		ChoppedSettings.startNewWindow(simulation);
        		simulation.init(window);
        	}
        	
        	//Start simulation - will show the recipes selection.
        	else if(hudMain.isHoverStart() && hudMain.isRendered()) {
        		hudMain.setRendered(false);
        		hudRecipe.init(window);
        		recipesLoader = hudRecipe.getRecipesLoader();
        		startSelected = true;
        	}
        	
        	//Profile Button - will show the 5 dishes that the player tried in the simulation.
        	else if(hudMain.isHoverProfile() && hudMain.isRendered()) {
        		hudMain.setRendered(false);
        		hudProfile.init(window);
        		hudProfile.setRendered(true);
        	}
        	
        	//Settings screen - control the vsync (turn it on or off) and control guide of the simulation.
        	else if(hudMain.isHoverSettings() && hudMain.isRendered()) {
        		hudMain.setRendered(false);
        		hudSettings.init(window);
        		hudSettings.setRendered(true);
        	}
        	
        	//Settings screen - if the player click on show control guide.
        	else if(hudSettings.isRendered() && hudSettings.isControlGuide()) {
        		hudSettings.setShowControlGuide(true);
        	}
        	
        	//Settings screen - if the player click on go back in controls guide (it will return back into menu).
        	else if(hudSettings.isRendered() && hudSettings.isShowControlGuide() && hudSettings.isBack()) {
        		hudSettings.setShowControlGuide(false);
        		hudSettings.setRendered(true);
        	}
        	
        	//Settings screen - Control of the vSync, if right arrow clicked, the vSync will be true.
        	else if(hudSettings.isRendered() && !hudSettings.isShowControlGuide() && hudSettings.isRightArrow()) {
        		if(!window.isvSync()) {
        			window.setvSync(true);
        			hudSettings.setvSync(true);
        		}
        	}
        	
        	//Settings screen - Control of the vSync, if left arrow clicked, the vSync will be false.
        	else if(hudSettings.isRendered() && !hudSettings.isShowControlGuide() && hudSettings.isLeftArrow()) {
        		if(window.isvSync()) {
        			window.setvSync(false);
        			hudSettings.setvSync(false);
        		}
        	}
        	
        	//Settings screen - if the player clicked on "Go Back" it will return back to the menu.
        	else if(hudSettings.isRendered() && hudSettings.isBack()) {
        		hudSettings.setRendered(false);
        		hudMain.setRendered(true);
        	}
        	
        	//If the player clicked on go back - it will return back to the menu.
        	else if(hudProfile.isRendered() && hudProfile.isBack()) {
        		hudProfile.setRendered(false);
        		hudMain.setRendered(true);
        	}
        	
        	//The score screen - if it's renedered and the player clicked on "Go Back", it will return back to the menu.
        	else if(hudScore.isRendered() && hudScore.isBack()) {
        		hudMain.init(window);
        		hudScore.setRendered(false);
        		hudMain.setRendered(true);
        	}
        	
        	//If the player clicked on "Go back" button - it will return to the menu.
        	else if(hudRecipe.isRendered() && hudRecipe.isBack()) {
        		startSelected = false;
        		hudRecipe.setRendered(false);
        		hudMain.setRendered(true);
        	}
        	
        	//if there is more than 5 recipes, the recipe screen will init the pages to show the user all the recipes. the user can move to the next page by perssing with the mouse to the right arrow.
        	else if(hudRecipe.isRendered() && hudRecipe.isHoverNext() && hudRecipe.isNextRendered() && !mouseButtonClicked) 
        		hudRecipe.moveToNextPage();
        	
        	//the user can move to the previous page by perssing with the mouse to the left arrow.
        	else if(hudRecipe.isRendered() && hudRecipe.isHoverPrevious() && hudRecipe.isPreviousRendered() && !mouseButtonClicked)
        		hudRecipe.moveToPreviousPage();
        	
        	//If the player in the recipes selection, the simulation will find which recipe choosed the init the recipe choosed in the simulation service.
        	if(hudRecipe.isRendered()) {
        		for(int i=0; i<recipesLoader.length; i++) {
        			for(RecipeLoader load : recipesLoader[i]) {
        				if(hudRecipe.isRendered() && load.isHover()) {
        	        		startSelected = false;
        	        		SimulationService.setStartSimlation(true);
        	        		//initLoadingScreen(window);
        					SimulationService.setRecipeChosen(load.getRecipe());
        	        		hudRecipe.setRendered(false);
        	        		ChoppedSettings.startNewWindow(simulation);
        	        		simulation.init(window);
        				}
        			}
        		}
        	}
        }
       
    	mouseButtonClicked = mouseInput.isLeftButtonPressed();
    }

    @Override
    public void update(float interval, MouseInput mouseInput, Window window) throws Exception {
    }
    
    @Override
    public void render(Window window) throws Exception {
    	if(hudMain.isRendered() && !startSelected && !SimulationService.isStartSimlation() && !hudScore.isRendered())
    		hudMain.render(window);

    	//else if(!hudRecipe.isRendered() && SimulationService.isStartSimlation())
    		//hudLoading.render(window);
    	
    	else if(startSelected && !hudMain.isRendered() && !SimulationService.isStartSimlation())
    		hudRecipe.render(window);
    	
    	else if(hudScore.isRendered() && !hudMain.isRendered())
    		hudScore.render(window);
    	
    	else if(hudProfile.isRendered() && !hudMain.isRendered())
    		hudProfile.render(window);
    	
    	else if(hudSettings.isRendered() && !hudMain.isRendered())
    		hudSettings.render(window);
    }
    
    @Override
    public void cleanup() {
       if(hudMain!=null)
        	hudMain.cleanup();
        else if(hudLoading!=null)
        	hudLoading.cleanup();
        else if(hudRecipe!=null)
        	hudRecipe.cleanup();
        else if(hudSettings!=null)
        	hudSettings.cleanup();
    }

}
