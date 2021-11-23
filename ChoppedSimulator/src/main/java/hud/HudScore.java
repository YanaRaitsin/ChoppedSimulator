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
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import data.Recipe;
import engine.Utils;
import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;
import simulator.SimulationService;

public class HudScore {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture scoreScreen;
    
    private GuiTexture recipeSelected;
    
    private boolean back;
    
    private boolean rendered;
    
    private Recipe loadRecipe;
    
    private int score;
    
    public void init(Window window) throws Exception {
        this.vg = window.getWindowOptions().antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
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

        counter = 0;
        
        loadRecipe = SimulationService.getRecipeChosen();
        
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
        scoreScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/score.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
        recipeSelected = new GuiTexture(loader.loadTexture("src/main/java/resources/recipesMenu/"+loadRecipe.getName()+".png"), new Vector2(-0.4f, 0.3f), new Vector2(0.1f, 0.2f));
        rendered = false;
    }
    
    //init the background of the score screen and the rectangle and than init into the screen the score after serving the dish and the name of the chosen recipe.
    public void render(Window window) {
    	guiRenderer.render(scoreScreen);
    	
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        score = SimulationService.getScore();
        //width - x, height - y
        //init cursor
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        //back button
        int xBack = (int) (window.getWidth() * 0.13f);
        int yBack = (int) (window.getHeight() * 0.88f);
        int radiusBack = 35;
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        back = Math.pow(x - xBack, 2) + Math.pow(y - yBack, 2) < Math.pow(radiusBack, 2);
        
        nvgFontFace(vg, FONT_NAME);
        
        nvgBeginPath(vg);
        nvgRect(vg, window.getWidth() * 0.07f, window.getHeight() * 0.85f , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        
        nvgFontSize(vg, 45.0f);
        if (back) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.08f, window.getHeight() * 0.89f, String.format("Go Back To Menu", counter)); 
       
        //score box
        nvgBeginPath(vg);
        nvgRect(vg, window.getWidth() * 0.25f, window.getHeight() * 0.25f , 700, 300);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        nvgFontSize(vg, 45.0f);
        nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        nvgText(vg, window.getWidth() * 0.36f, window.getHeight() * 0.32f, String.format(loadRecipe.getName(), counter)); 
        nvgText(vg, window.getWidth() * 0.36f, window.getHeight() * 0.415f, String.format("Score: "+score, counter)); 
        
        nvgEndFrame(vg);
        
    	//init the selected recipe picture.
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	nvgBeginPath(vg);
    	
    	guiRenderer.render(recipeSelected);
    	
        nvgEndFrame(vg);

        rendered = true;
        // Restore state
        window.restoreState();
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

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isBack() {
		return back;
	}

}
