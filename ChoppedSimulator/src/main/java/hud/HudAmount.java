package hud;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import org.lwjgl.nanovg.NVGColor;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.system.MemoryUtil;

import engine.Utils;
import engine.Window;

import static org.lwjgl.system.MemoryUtil.NULL;

public class HudAmount {

    private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private int amount;
    
    private boolean hoverTake;
    
    private boolean rendered;
    
    private boolean hoverMinus;
    
    private boolean hoverPlus;
    
    private String ingredientSelected;

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
        amount = 0;
        rendered = false;
    }

    /*
     * init the amount box of the ingredient, so the user can take the amount for the recipe that he need for the ingredient.
     */
    public void render(Window window) {
    	glViewport(0,0,window.getWidth(),window.getHeight());
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
        //width - x, height - y
        //Amount Box
        nvgBeginPath(vg);
        nvgRect(vg, window.getWidth() - window.getWidth(), 250 , window.getWidth() * 0.28f, window.getHeight() * 0.3f);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);
        nvgFontSize(vg, 35.0f);
        nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 200, colour));
        nvgText(vg, window.getWidth() - window.getWidth() + 45, 300, String.format("Select Amount of "+ingredientSelected, counter));  //x - 75
        nvgEndFrame(vg);
        
        //init the x-y the circle of taking the ingredient
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        float xCircleTake = (window.getWidth() * 0.13f);
        float yCircleTake = (window.getHeight() * 0.45f);
        int radiusTake = 30;
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        hoverTake = Math.pow(x - xCircleTake, 2) + Math.pow(y - yCircleTake, 2) < Math.pow(radiusTake, 2);
        
        // Circle of taking the ml/grams of the ingredient - take button
        nvgBeginPath(vg);
        nvgCircle(vg, xCircleTake, yCircleTake, radiusTake);
        nvgFillColor(vg, rgba(0xDC, 0xDC, 0xDC, 200, colour));
        nvgFill(vg);

        // Clicks Text of taking the amount of the ingredient
        nvgFontSize(vg, 25.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_CENTER );
        if (hoverTake) {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));

        }
        int yTextCenter = (int) (window.getHeight() - window.getHeight() * 0.545f);
        nvgText(vg, xCircleTake, yTextCenter, String.format("Take", counter));
        nvgEndFrame(vg);
        
        //init the plus button for inc the ingredient amount - inc button
        int xCirclePlus = (int) (window.getWidth() - window.getWidth() / 1.03f);
        int yCirclePlus = (int) (window.getHeight() - window.getHeight() * 0.62f);
        int radiusPlus = 20;
        hoverPlus = Math.pow(x - xCirclePlus, 2) + Math.pow(y - yCirclePlus, 2) < Math.pow(radiusPlus, 2);
        
        //init the circle for adding the amount of the ingredient
        nvgBeginPath(vg);
        nvgCircle(vg, xCirclePlus, yCirclePlus, radiusPlus);
        nvgFillColor(vg, rgba(0xDC, 0xDC, 0xDC, 200, colour));
        nvgFill(vg);
        
        // Clicks Text of adding the amount of the ingredient
        if (hoverPlus) {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));

        }
        //init plus text on the circle button
        nvgFontSize(vg, 40.0f);
        int yTextPlus = (int) (window.getHeight() - window.getHeight() * 0.61f);
        nvgText(vg, xCirclePlus, yTextPlus, String.format("+", counter)); 
        nvgEndFrame(vg);
        
        //init the minus button for the amount of the ingredient
        int xCircleMinus = (int) (window.getWidth() - window.getWidth() / 1.28f);
        int yCircleMinus = (int) (window.getHeight() - window.getHeight() * 0.62f);
        int radiusMinus = 20;
        hoverMinus = Math.pow(x - xCircleMinus, 2) + Math.pow(y - yCircleMinus, 2) < Math.pow(radiusMinus, 2);
        
        //init the circle to sub the amount of the ingredient
        nvgBeginPath(vg);
        nvgCircle(vg, xCircleMinus, yCircleMinus, radiusMinus);
        nvgFillColor(vg, rgba(0xDC, 0xDC, 0xDC, 200, colour));
        nvgFill(vg);
        
        //click check text of the sub of the amount
        if (hoverMinus) {
            nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));
        }
        
        //init minus text on the button
        nvgFontSize(vg, 50.0f);
        int yTextMinus = (int) (window.getHeight() - window.getHeight() * 0.61f);
        nvgText(vg, xCircleMinus, yTextMinus, String.format("-", counter)); 
        nvgEndFrame(vg);
        
        //init amount text
        nvgBeginPath(vg);
        int textAmountX = (int) (window.getWidth() - window.getWidth() / 1.15f);
        int textAmountY = (int) (window.getHeight() - window.getHeight() * 0.6f);
        nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 255, colour));
        nvgText(vg, textAmountX, textAmountY, String.format(""+(amount), counter));
        nvgEndFrame(vg);
        
        rendered = true;
        // Restore state
        window.restoreState();
        
    }
    
    public int getAmount() {
    	return amount;
    }
    
    public void incAmount() {
    	amount++;
    }
    
    public void decAmount() {
    	amount--;
    	if(amount == -1)
    		amount=0;
    }
    
    public boolean checkAmount() {
    	return amount >= 0 && amount < 700;
    }
    
    public boolean checkMaxAmount() {
    	return amount==700;
    }

    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public void cleanup() {
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }

	public boolean isHoverTake() {
		return hoverTake;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isHoverMinus() {
		return hoverMinus;
	}

	public boolean isHoverPlus() {
		return hoverPlus;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setIngredientSelected(String ingredientSelected) {
		this.ingredientSelected = ingredientSelected;
	}

}