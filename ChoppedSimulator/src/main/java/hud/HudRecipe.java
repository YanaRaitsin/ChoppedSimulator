package hud;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjgl.nanovg.NanoVGGL3;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Vector;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import data.Ingredient;
import data.Recipe;
import data.Recipeandingredient;
import engine.Utils;
import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;
import simulator.SimulationService;

public class HudRecipe {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private Vector<RecipeLoader>[] recipesLoader;
    
    private List<Hover> hovers;
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture recipeScreen;
    
    private List<GuiTexture> recipesPictrues;
    
    private List<Recipe> recipes;
    
    private boolean rendered;
    
    private boolean back;
    
    private double pages;
    
    private int extraPage;
    
    private int currentRecipeIndex;
    
    private boolean hoverNext;
    
    private boolean hoverPrevious;
    
    private int currentPage;
    
    private Recipe selectedRecipe;
    
    private boolean nextRendered;
    
    private boolean previousRendered;
    
    public void init(Window window) throws Exception {
        this.vg = window.getWindowOptions().antialiasing ? nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_DEBUG) : nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new Exception("Could not init nanovg");
        }

        fontBuffer = Utils.ioResourceToByteBuffer("/resources/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, 0);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();

        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);

        currentPage = 1;
        
        counter = 0;
        
        currentRecipeIndex = 0;
        
        initPagesInMenu();
        
        hovers = new Vector<Hover>();
        
        selectedRecipe = new Recipe();
        
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
        recipeScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/selectrecipe.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
    	rendered = false;
    }

    public void render(Window window) {
    	guiRenderer.render(recipeScreen);
    	initHovers(window);
    	
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        
        //width - x, height - y
        //init cursor
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        nvgFontFace(vg, FONT_NAME);
        
    	float xRecipe = 45;
        
        for(int i=0; i<5 ; i++) {
        	nvgBeginPath(vg);
        	nvgRect(vg, xRecipe, window.getHeight() * 0.15f , window.getWidth() * 0.18f, 750); //nvgRect(vg, xRecipe, window.getHeight() * 0.15f , window.getWidth() * 0.18f, 620)
        	nvgFillColor(vg, rgba(0x40, 0x40, 0x40, 200, colour));
        	nvgFill(vg);
            xRecipe= xRecipe + (window.getWidth() * 0.19f); //290
        }
        
        //init the next page and previous of the menu if there is more than 5 recipes
        if(recipes.size() > 5) {
        	if(currentPage!=recipesLoader.length) {
        		int xNext = (int) (window.getWidth() * 0.962f);
        		int yNext = (int) (window.getHeight() * 0.95f);
        		int nextRadius = 25;
        		hoverNext = Math.pow(x - xNext, 2) + Math.pow(y - yNext, 2) < Math.pow(nextRadius, 2);
        
        		nvgFontSize(vg, 40.0f);
        		if (currentPage <= recipesLoader.length && hoverNext) {
        			nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        		} else { 
        			nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        		} 
        		nvgText(vg, window.getWidth() * 0.96f, window.getHeight() * 0.95f, String.format(">>", counter));
        		nextRendered = true;
        	}
        	
        	if(currentPage==recipesLoader.length)
        		nextRendered = false;
        
        	//init the previous page of the menu if the user move to the next page.
        	if(currentPage > 1) {
        		int xPrevious = (int) (window.getWidth() * 0.922f);
        		int yPrevious = (int) (window.getHeight() * 0.95f);
        		int previousRadius = 25;
        		hoverPrevious = Math.pow(x - xPrevious, 2) + Math.pow(y - yPrevious, 2) < Math.pow(previousRadius, 2);
        
        		nvgFontSize(vg, 40.0f);
        		if (currentPage > 1 && hoverPrevious) {
        			nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        		} else { 
        			nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        		} 
        		nvgText(vg, window.getWidth() * 0.92f, window.getHeight() * 0.95f, String.format("<<", counter));
        		previousRendered = true;
        	}
        	
        	if(currentPage == 1)
        		previousRendered = false;
        }
        
        //Go Back Button
        nvgBeginPath(vg);
        int xBack = (int) (window.getWidth() * 0.16f);
        int yBack = (int) (window.getHeight() * 0.07f);
        int radiusBack = 35;
        back = Math.pow(x - xBack, 2) + Math.pow(y - yBack, 2) < Math.pow(radiusBack, 2);
        
        nvgRect(vg, window.getWidth() * 0.09f, window.getHeight() * 0.04f , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        
        nvgFontSize(vg, 45.0f);
        if (back) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.095f, window.getHeight() * 0.08f, String.format("Go Back To Menu", counter));
        
    	nvgEndFrame(vg);
   
    	//init the recipes from the array of vector
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	nvgBeginPath(vg);
        loadRecipes(window, recipesLoader[currentPage-1]);
        
        float chooseBox = 90;
        
        for(int i=0; i<5 ; i++) {
        	nvgBeginPath(vg);
        	nvgRect(vg, chooseBox, window.getHeight() * 0.15f , window.getWidth() * 0.135f, 50);
        	nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 200, colour));
        	nvgFill(vg);
        	chooseBox= chooseBox + (window.getWidth() * 0.19f); //290
        }
        
        /*init the number of the recipes on each pages (the selection recipes).
         * if there is one recipe - it will only init one.
         * if there is two - it will do the same like the one recipe.
        */
        float chooseX = 0.065f;
        switch(recipesLoader[currentPage-1].size()) {
        case 1:
        	nvgBeginPath(vg);
        	recipesLoader[currentPage-1].get(0).setHover(Math.pow(x - hovers.get(0).getX(), 2) + Math.pow(y - hovers.get(0).getY(), 2) < Math.pow(hovers.get(0).getRadius(), 2));
        	nvgFontSize(vg, 35.0f);
        	if(recipesLoader[currentPage-1].get(0).isHover()) {
        		selectedRecipe = recipesLoader[currentPage-1].get(0).getRecipe();
        		nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        	} else { 
        		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        	} 
	        nvgText(vg, window.getWidth() * chooseX, window.getHeight() * 0.18f, String.format(recipesLoader[currentPage-1].get(0).getChooseRecipe(), counter));
	    	nvgEndFrame(vg);
	        break;
        case 2:
        	changePagePictrues();
        	nvgBeginPath(vg);
        	for(int i=0; i<2; i++) {
        		recipesLoader[currentPage-1].get(i).setHover(Math.pow(x - hovers.get(i).getX(), 2) + Math.pow(y - hovers.get(i).getY(), 2) < Math.pow(hovers.get(i).getRadius(), 2));
            	nvgFontSize(vg, 35.0f);
            	if(recipesLoader[currentPage-1].get(i).isHover()) {
            		selectedRecipe = recipesLoader[currentPage-1].get(i).getRecipe();
            		nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
            	} else { 
            		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
            	} 
    	        nvgText(vg, window.getWidth() * chooseX, window.getHeight() * 0.18f, String.format(recipesLoader[currentPage-1].get(i).getChooseRecipe(), counter));
        		chooseX+=0.192f;
        	}
        	nvgEndFrame(vg);
            	break;
        case 3:
        	changePagePictrues();
        	nvgBeginPath(vg);
        	for(int i=0; i<3; i++) {
        		recipesLoader[currentPage-1].get(i).setHover(Math.pow(x - hovers.get(i).getX(), 2) + Math.pow(y - hovers.get(i).getY(), 2) < Math.pow(hovers.get(i).getRadius(), 2));
            	nvgFontSize(vg, 35.0f);
            	if(recipesLoader[currentPage-1].get(i).isHover()) {
            		selectedRecipe = recipesLoader[currentPage-1].get(i).getRecipe();
            		nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
            	} else { 
            		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
            	} 
    	        nvgText(vg, window.getWidth() * chooseX, window.getHeight() * 0.18f, String.format(recipesLoader[currentPage-1].get(i).getChooseRecipe(), counter));
        		chooseX+=0.192f;
        	}
        	nvgEndFrame(vg);
        	break;
        case 4:
        	changePagePictrues();
        	nvgBeginPath(vg);
        	for(int i=0; i<4; i++) {
        		recipesLoader[currentPage-1].get(i).setHover(Math.pow(x - hovers.get(i).getX(), 2) + Math.pow(y - hovers.get(i).getY(), 2) < Math.pow(hovers.get(i).getRadius(), 2));
            	nvgFontSize(vg, 35.0f);
            	if(recipesLoader[currentPage-1].get(i).isHover()) {
            		selectedRecipe = recipesLoader[currentPage-1].get(i).getRecipe();
            		nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
            	} else { 
            		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
            	} 
    	        nvgText(vg, window.getWidth() * chooseX, window.getHeight() * 0.18f, String.format(recipesLoader[currentPage-1].get(i).getChooseRecipe(), counter));
        		chooseX+=0.192f;
        	}
        	nvgEndFrame(vg);
        	break;
        case 5:
        	changePagePictrues();
        	nvgBeginPath(vg);
        	for(int i=0; i<5; i++) {
        		recipesLoader[currentPage-1].get(i).setHover(Math.pow(x - hovers.get(i).getX(), 2) + Math.pow(y - hovers.get(i).getY(), 2) < Math.pow(hovers.get(i).getRadius(), 2));
            	nvgFontSize(vg, 35.0f);
            	if(recipesLoader[currentPage-1].get(i).isHover()) {
            		selectedRecipe = recipesLoader[currentPage-1].get(i).getRecipe();
            		nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
            	} else { 
            		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
            	} 
    	        nvgText(vg, window.getWidth() * chooseX, window.getHeight() * 0.18f, String.format(recipesLoader[currentPage-1].get(i).getChooseRecipe(), counter));
        		chooseX+=0.192f;
        	}
        	nvgEndFrame(vg);
        	break;
        }
    	nvgEndFrame(vg);
        
        rendered = true;
        // Restore state
        //window.restoreState();
    }
    
    public void initHovers(Window window) {
    	int x1 = (int) (window.getWidth() * 0.132f);
    	int y1 = (int) (window.getHeight() * 0.18f);
    	int r1 = 30;
    	hovers.add(new Hover(x1,y1,r1));
        
    	int x2 = (int) (window.getWidth() * 0.326);
    	int y2 = (int) (window.getHeight() * 0.18f);
    	int r2 = 30;
    	hovers.add(new Hover(x2,y2,r2));
    	
    	int x3 = (int) (window.getWidth() * 0.52);
    	int y3 = (int) (window.getHeight() * 0.18f);
    	int r3 = 30;
    	hovers.add(new Hover(x3,y3,r3));
    	
    	int x4 = (int) (window.getWidth() * 0.682);
    	int y4 = (int) (window.getHeight() * 0.18f);
    	int r4 = 30;
    	hovers.add(new Hover(x4,y4,r4));
    	
    	int x5 = (int) (window.getWidth() * 0.852);
    	int y5 = (int) (window.getHeight() * 0.18f);
    	int r5 = 30;
    	hovers.add(new Hover(x5,y5,r5));
    }
    
    public void initPagesInMenu() {
        recipes = SimulationService.getSaveRecipes();
        //if there is more than one page of recipes
        if(recipes.size() > 5) {
        	pages = (double) recipes.size() / 5;
        	extraPage=0;
        	if(pages == (int) pages)
        		initRecipeLoader();
        	else {
        		pages = (Math.floor(pages)) + 1;
        		extraPage = recipes.size() % 5;
        		initRecipeLoader();
        	}
        }
        //else only one page
        else {
        	pages = 1;
        	initRecipeLoader();
        }
    }
    
    
    //init per page the recipes
    @SuppressWarnings("unchecked")
	private void initRecipeLoader() {
    	recipesLoader = (Vector<RecipeLoader>[])new Vector[(int) pages];
    	//if there is less than 5 recipes.
    	if(extraPage == 0 && recipes.size() < 5) {
    		for(int i=0; i<recipesLoader.length; i++) {
    			recipesLoader[i] = new Vector<RecipeLoader>();
    			recipesLoader[i] = initListRecipeLoader(i,recipes.size());
    		}
    	}
    	//if there is more than 5 recipes and it's a whole number and not double
    	else if(extraPage == 0 && recipes.size() > 5) {
    		for(int i=0; i<recipesLoader.length; i++) {
    			recipesLoader[i] = new Vector<RecipeLoader>();
    			recipesLoader[i] = initListRecipeLoader(i,5);
    		}
    	}
    	//if there is more than 5 recipes and it's double number
    	else if(extraPage != 0) {
    		for(int i=0; i<recipesLoader.length-1; i++) {
    			recipesLoader[i] = new Vector<RecipeLoader>();
    			recipesLoader[i] = initListRecipeLoader(i,5);
    		}
    		recipesLoader[recipesLoader.length-1] = new Vector<RecipeLoader>();
			recipesLoader[recipesLoader.length-1] = initListRecipeLoader(recipesLoader.length-1,extraPage);
    	}
    }
    
    
    //init for each page the recipe and the ingredients
    private Vector<RecipeLoader> initListRecipeLoader(int currentPage, int numberOfRecipes) {
		Vector<Ingredient> ingredientsRecipe = null;
		if(numberOfRecipes==5) {
			for(int i = 0; i < 5; i++,currentRecipeIndex++) {
    			List<Recipeandingredient> ingredients = recipes.get(currentRecipeIndex).getRecipeandingredients();
				ingredientsRecipe = new Vector<Ingredient>();
    			for(Recipeandingredient ingredient : ingredients)
    				ingredientsRecipe.add(ingredient.getIngredient());
    			recipesLoader[currentPage].add(new RecipeLoader(false,recipes.get(currentRecipeIndex),ingredientsRecipe));
    		}
		}
    	
    	if(numberOfRecipes < 5) {
    		for(int i=0; i < numberOfRecipes; i++) {
        		List<Recipeandingredient> ingredients = recipes.get(currentRecipeIndex).getRecipeandingredients();
    			ingredientsRecipe = new Vector<Ingredient>();
        		for(Recipeandingredient ingredient : ingredients)
        			ingredientsRecipe.add(ingredient.getIngredient());
        		recipesLoader[currentPage].add(new RecipeLoader(false,recipes.get(currentRecipeIndex),ingredientsRecipe));
        		currentRecipeIndex++;
    		}
    	}
    	currentRecipeIndex--;
    	return recipesLoader[currentPage];
    }
    
    /*
     * loadRecipes(Window window, List<RecipeLoader> currentRecipePage) - 
     * The function load of each page in the simulation the pictures of the recipes and the ingredients for each recipe.
     */
    public void loadRecipes(Window window, List<RecipeLoader> currentRecipePage) {
        recipesPictrues = new Vector<GuiTexture>();
    	float moveX = 0;
    	float textX = 0.03f;
    	float textIngredientY = 0.55f;
    	nvgFontSize(vg, 35.0f);
    	for(RecipeLoader loadRecipe : currentRecipePage) {
    		nvgText(vg, window.getWidth() * textX, window.getHeight() * 0.48f, String.format(loadRecipe.getRecipe().getName(), counter));
    		recipesPictrues.add(new GuiTexture(loader.loadTexture("src/main/java/resources/recipesMenu/"+loadRecipe.getRecipe().getName()+".png"), new Vector2(-0.76f+moveX, 0.38f), new Vector2(0.16f, 0.28f)));
    		List<Ingredient> ingredients = loadRecipe.getRecipeData();
    		nvgText(vg, window.getWidth() * textX, window.getHeight() * 0.515f, String.format("Ingredients:", counter));
    		for(Ingredient ingredient : ingredients) {
        		nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
    			nvgText(vg, window.getWidth() * textX, window.getHeight() * textIngredientY, String.format(ingredient.getName(), counter));
    			textIngredientY+=0.027f;
    		}
    		moveX += 0.37f;
        	textX += 0.19f;
        	textIngredientY = 0.55f;
    	}
    	guiRenderer.renderList(recipesPictrues);
    }
    
    private void changePagePictrues() {
    	for(int i=0; i<recipesPictrues.size(); i++)
    		recipesPictrues.remove(i);
    }

    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public void cleanup() {
    	guiRenderer.cleanUp();
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }
    
    public void moveToNextPage() {
		currentPage++;
    }
    
    public void moveToPreviousPage() {
		currentPage--;
    }

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isHoverNext() {
		return hoverNext;
	}

	public boolean isHoverPrevious() {
		return hoverPrevious;
	}

	public Vector<RecipeLoader>[] getRecipesLoader() {
		return recipesLoader;
	}

	public Recipe getSelectedRecipe() {
		return selectedRecipe;
	}

	public boolean isBack() {
		return back;
	}

	public boolean isNextRendered() {
		return nextRendered;
	}

	public boolean isPreviousRendered() {
		return previousRendered;
	}

}
