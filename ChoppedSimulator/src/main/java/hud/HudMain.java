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

import java.io.File;
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

public class HudMain {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private boolean hoverStart;
    
    private boolean hoverProfile;
    
    private boolean hoverSettings;
    
    private boolean hoverExit;
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture mainScreen;
    
    private boolean rendered;
    
    private boolean hoverContinue;

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
        
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
    	mainScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/mainGame.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
    	rendered = false;
    }

    /*
     * The render and the init of the menu screen, it creates all the buttons and the texts of the button in the menu.
     */
    public void render(Window window) {
    	guiRenderer.render(mainScreen);
    	
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        //width - x, height - y
        //init cursor
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);

        //Start button
        int xStart = (int) (window.getWidth() * 0.17f);
        int yStrart = (int) (window.getHeight() * 0.325f);
        int radiusStart = 35;
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        hoverStart = Math.pow(x - xStart, 2) + Math.pow(y - yStrart, 2) < Math.pow(radiusStart, 2);
        
        nvgFontFace(vg, FONT_NAME);
        float yMenu = 0.3f;
        for(int i=0; i<4; i++) {
            nvgBeginPath(vg);
            nvgRect(vg, window.getWidth() * 0.12f, window.getHeight() * yMenu , window.getWidth() * 0.12f, window.getHeight() * 0.06f);
            nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
            nvgFill(vg);
            yMenu+=0.08f;
        }
        
        nvgFontSize(vg, 45.0f);
        if (hoverStart) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.152f, window.getHeight() * 0.335f, String.format("Start", counter)); 
        
        //Profile button
        int xProfile = (int) (window.getWidth() * 0.17f);
        int yProfile = (int) (window.getHeight() * 0.405f);
        int radiusProfile = 35;
        hoverProfile = Math.pow(x - xProfile, 2) + Math.pow(y - yProfile, 2) < Math.pow(radiusProfile, 2);
        
        nvgFontSize(vg, 45.0f);
        if (hoverProfile) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.148f, window.getHeight() * 0.415f, String.format("Profile", counter)); 
        
       //Settings button
        int xSettings = (int) (window.getWidth() * 0.17f);
        int ySettings = (int) (window.getHeight() * 0.485f);
        int radiusSettings = 35;
        hoverSettings = Math.pow(x - xSettings, 2) + Math.pow(y - ySettings, 2) < Math.pow(radiusSettings, 2);
        
        nvgFontSize(vg, 45.0f);
        if (hoverSettings) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.143f, window.getHeight() * 0.495f, String.format("Settings", counter)); 
        
        //Exit button
        int xExit = (int) (window.getWidth() * 0.168);
        int yExit = (int) (window.getHeight() * 0.57f);
        int radiusExit = 35;
        hoverExit = Math.pow(x - xExit, 2) + Math.pow(y - yExit, 2) < Math.pow(radiusExit, 2);
        
        nvgFontSize(vg, 45.0f);
        if (hoverExit) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.155f, window.getHeight() * 0.575f, String.format("Exit", counter)); 
        
		String logFolder = "SavedSimulationFiles";
		File file = new File(logFolder);
		if(file.exists()) {
			int xContinue = (int) (window.getWidth() * 0.08f);
			int yContinue = (int) (window.getHeight() * 0.92f);
			int radiusContinue = 40;
			hoverContinue = Math.pow(x - xContinue, 2) + Math.pow(y - yContinue, 2) < Math.pow(radiusContinue, 2);
			
	        nvgBeginPath(vg);
			nvgRect(vg, window.getWidth() * 0.03f, window.getHeight() * 0.9f , 260, 55);
			nvgFillColor(vg, rgba(0x5A, 0xC1, 0x8E, 200, colour));
			nvgFill(vg);
			
	        nvgFontSize(vg, 45.0f);
	        if(hoverContinue) { 
	            nvgFillColor(vg, rgba(0x13, 0x75, 0x32, 255, colour));
	        } else {
	            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
	        } 
	        nvgText(vg, window.getWidth() * 0.05f, window.getHeight() * 0.935f, String.format("Continue", counter));  
		}
		
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

	public boolean isHoverStart() {
		return hoverStart;
	}

	public boolean isHoverProfile() {
		return hoverProfile;
	}

	public boolean isHoverSettings() {
		return hoverSettings;
	}

	public boolean isHoverExit() {
		return hoverExit;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isHoverContinue() {
		return hoverContinue;
	}

}
