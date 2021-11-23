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
import java.util.List;
import java.util.Vector;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import data.Dish;
import engine.Utils;
import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;
import simulator.JpaConnection;
import simulator.SimulationService;

public class HudProfile {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture profileScreen;
    
    private boolean back;
    
    private boolean rendered;
    
    private List<Dish> userDishes;
    
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
        
        initUserHistoryDishes();
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
        profileScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/profile.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
        rendered = false;
    }
    
    //init and render the profile screen (the rectangles and than the user history data).
    public void render(Window window) {
    	guiRenderer.render(profileScreen);
    	float yBox = 0.25f;
    	
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        //width - x, height - y
        //init cursor
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        //back button
        int xBack = (int) (window.getWidth() * 0.16f);
        int yBack = (int) (window.getHeight() * 0.12f);
        int radiusBack = 35;
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        back = Math.pow(x - xBack, 2) + Math.pow(y - yBack, 2) < Math.pow(radiusBack, 2);
        
        nvgFontFace(vg, FONT_NAME);
        
        nvgBeginPath(vg);
        nvgRect(vg, window.getWidth() * 0.09f, window.getHeight() * 0.08f , 320, 55);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        
        nvgFontSize(vg, 45.0f);
        if (back) {
            nvgFillColor(vg, rgba(0xFF, 0x80, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } 
        nvgText(vg, window.getWidth() * 0.095f, window.getHeight() * 0.12f, String.format("Go Back To Menu", counter)); 
       
        //profile box data
        for(int i=0; i<5 ;i++) {
        	nvgBeginPath(vg);
        	nvgRect(vg, window.getWidth() * 0.15f, window.getHeight() * yBox , 1400, 100);
        	nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        	nvgFill(vg);
        	yBox+=0.13f;
        }
        nvgFontSize(vg, 45.0f);
        nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        
        initLastDishes(window);
        
        nvgEndFrame(vg);
        
    	//init the selected recipe picture.
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	nvgBeginPath(vg);
    	
        nvgEndFrame(vg);

        rendered = true;
        // Restore state
        window.restoreState();
    }
    
    /*
     * initUserHistoryDishes() - The profile screen will first init all the dishes that the user simulate.
     */
    @SuppressWarnings("unchecked")
	private void initUserHistoryDishes() {
    	userDishes = new Vector<Dish>();
    	List<Dish> totalDishes = new Vector<Dish>();
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			totalDishes = JpaConnection.getEntityManager().createQuery("Select d from Dish d").getResultList();
			JpaConnection.getEntityManager().getTransaction().commit();
		} catch(Exception e) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		
		//init the dishes of the current user in the simulation
		for(Dish currentUserDish : totalDishes) {
			if(SimulationService.getUser().getId() == currentUserDish.getUser().getId())
				userDishes.add(currentUserDish);
		}
    }
    
    /*
     * initLastDishes() - if the list is smaller than 5 - it will init the 5 first simulation history of the user.
     * if the list is bigger than 5 or 10 - it will init the 5 last simulation history of the user.
     */
    private void initLastDishes(Window window) {
    	float yProfileData = 0.31f;
    	if((userDishes.size() <= 5 && userDishes.size()!=0)) {
    		for(int i=0; i<userDishes.size(); i++) {
    	        nvgFontSize(vg, 45.0f);
    	        nvgText(vg, window.getWidth() * 0.18f, window.getHeight() * yProfileData, String.format(userDishes.get(i).getRecipe().getName() + " Date: " + userDishes.get(i).getDate() + " Score: " + userDishes.get(i).getScore(), counter)); 
    	        yProfileData+=0.13;
    		}
    	}
    	else if((userDishes.size() >= 10 && userDishes.size()!=0) || (userDishes.size() <= 9 && userDishes.size()!=0)) {
    		int lastFive = userDishes.size() - 5;
    		for(int i=0; i<lastFive; i++) {
    	        nvgFontSize(vg, 45.0f);
    	        nvgText(vg, window.getWidth() * 0.18f, window.getHeight() * yProfileData, String.format(userDishes.get(i).getRecipe().getName() + " Date: " + userDishes.get(i).getDate() + " Score: " + userDishes.get(i).getScore(), counter)); 
    	        yProfileData+=0.13;
    		}
    	}
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
