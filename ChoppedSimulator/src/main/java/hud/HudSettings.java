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

import engine.Utils;
import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;

public class HudSettings {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture settingsScreen;
    
    private GuiTexture controlsScreen;
    
    private boolean back;
    
    private boolean rendered;
    
    private boolean rightArrow;
    
    private boolean leftArrow;
    
    private boolean controlGuide;
    
    private boolean showControlGuide;
    
    private boolean vSync;
    
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
        
        vSync = window.isvSync();
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
        settingsScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/settings.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
        controlsScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/simulationControls.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
        rendered = false;
    }
    
    //init the settings screen, the user can control the vsync setting by using the arrows and the user can enter into the control guide to see how to play in the simulation.
    public void render(Window window) {
    	guiRenderer.render(settingsScreen);
    	
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        //width - x, height - y
        //init cursor
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        //back button
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        
        nvgFontFace(vg, FONT_NAME);
        
        nvgBeginPath(vg);
        nvgRect(vg, window.getWidth() * 0.09f, window.getHeight() * 0.08f , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        
        nvgFontSize(vg, 45.0f);
        //init the right arrow to control the vsync
        int xRightArrow = (int) (window.getWidth() * 0.56f);
        int yRightArrow = (int) (window.getHeight() * 0.25f);
        int radiusRightArrow = 35;
        rightArrow = Math.pow(x - xRightArrow, 2) + Math.pow(y - yRightArrow, 2) < Math.pow(radiusRightArrow, 2);
        
        nvgRect(vg, xRightArrow, yRightArrow , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        if (rightArrow) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.55f, window.getHeight() * 0.25f, String.format(">>", counter));
        
        //init the left arrow to control the vsync
        int xLeftArrow = (int) (window.getWidth() * 0.45f);
        int yLeftArrow = (int) (window.getHeight() * 0.25f);
        int radiusLeftArrow = 35;
        leftArrow = Math.pow(x - xLeftArrow, 2) + Math.pow(y - yLeftArrow, 2) < Math.pow(radiusLeftArrow, 2);
        
        nvgRect(vg, xRightArrow, yRightArrow , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        //init the right arrow to control the vsync
        if (leftArrow) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.43f, window.getHeight() * 0.25f, String.format("<<", counter));
        
        nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        nvgText(vg, window.getWidth() * 0.48f, window.getHeight() * 0.25f, String.format(""+vSync, counter));
        nvgText(vg, window.getWidth() * 0.31f, window.getHeight() * 0.25f, String.format("Vsync: ", counter));
        
        int xBack = (int) (window.getWidth() * 0.16f);
        int yBack = (int) (window.getHeight() * 0.12f);
        int radiusBack = 35;
        back = Math.pow(x - xBack, 2) + Math.pow(y - yBack, 2) < Math.pow(radiusBack, 2);
        if (back) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.095f, window.getHeight() * 0.12f, String.format("Go Back To Menu", counter));
        
        //init the gui of the control guide.
        int xGuide = (int) (window.getWidth() * 0.76f);
        int yGuide = (int) (window.getHeight() * 0.23f);
        int radiusGide = 35;
        controlGuide = Math.pow(x - xGuide, 2) + Math.pow(y - yGuide, 2) < Math.pow(radiusGide, 2);
        if (controlGuide) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.68f, window.getHeight() * 0.25f, String.format("Controls Guide", counter));
        
        nvgEndFrame(vg);
        
    	//init the selected recipe picture.
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	nvgBeginPath(vg);
    	
        nvgEndFrame(vg);
        
    	//init the the gui of the controls guide
        if(showControlGuide) {
        	nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        	nvgBeginPath(vg);
            int xBackGuide = (int) (window.getWidth() * 0.86f);
            int yBackGuide = (int) (window.getHeight() * 0.08f);
            int radiusBackGuide = 35;
            back = Math.pow(x - xBackGuide, 2) + Math.pow(y - yBackGuide, 2) < Math.pow(radiusBackGuide, 2);
            nvgFontSize(vg, 45.0f);
            if (back) {
                nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
            } else {
                nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
            } 
            nvgText(vg, window.getWidth() * 0.8f, window.getHeight() * 0.08f, String.format("Go Back To Menu", counter));
        	
        	guiRenderer.render(controlsScreen);
        	nvgEndFrame(vg);
        }

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

	public boolean isShowControlGuide() {
		return showControlGuide;
	}

	public void setShowControlGuide(boolean showControlGuide) {
		this.showControlGuide = showControlGuide;
	}

	public boolean isRightArrow() {
		return rightArrow;
	}

	public boolean isLeftArrow() {
		return leftArrow;
	}

	public boolean isControlGuide() {
		return controlGuide;
	}

	public void setvSync(boolean vSync) {
		this.vSync = vSync;
	}

}
