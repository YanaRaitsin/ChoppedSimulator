package hud;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;
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
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.NumberFormat;

import javax.swing.Timer;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import engine.Utils;
import engine.Window;
public class HudMiniTimer {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private long time;
    
    private Timer timer;
    
    private long remaining;
    
    private NumberFormat format;
    
    private long initial;
    
    private static int minutes;
    
    private static int seconds;
    
    //if the user using the food processor, it will init the mini timer box in a different point in the screen.
    private boolean foodProcessorInUse;
    
    //if the user using the cookware, it will init the mini timer box in a different point in the screen.
    private boolean cookwareInUse;
    
    //if the user using the oven, it will init the mini timer box in a different point in the screen.
    private boolean ovenInUse;
    
    private boolean rendered = false;

    public void init(Window window) throws Exception {
        this.vg = window.getOptions().antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
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
        timer = new Timer(1000, null);
        initial = System.currentTimeMillis();
        timer.start();
        rendered = false;
    }

    /*
     * init the mini timer for blender/baking/cooking (for the user).
     */
    public void render(Window window) {
    	updateTime();

    	glViewport(0,0,window.getWidth(),window.getHeight());
    	nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	int yMiniTimerText = 2;
    	float yMiniTimerBox = 2;
    	
    	if(foodProcessorInUse)
    		initMiniTimer(window,yMiniTimerText,yMiniTimerBox);
    	else if(cookwareInUse) {
    		yMiniTimerText = 3;
    		yMiniTimerBox = 3;
    		initMiniTimer(window,yMiniTimerText,yMiniTimerBox);
    	}
    	else if(ovenInUse) {
    		yMiniTimerText = 4;
    		yMiniTimerBox = 4;
    		initMiniTimer(window,yMiniTimerText,yMiniTimerBox);
    	}
        	// Restore state
        	window.restoreState();
        	rendered = true;
    }
    
    private void initMiniTimer(Window window, int yText, float yBox) {
		//Upper bar
		nvgBeginPath(vg);
    	nvgRect(vg, 0, window.getHeight() / yBox , window.getWidth() / 10f, 50);
    	nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
    	nvgFill(vg);

    	// Render timer text
    	nvgFontSize(vg, 40.0f);
    	nvgFontFace(vg, FONT_NAME);
    	nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
    	nvgFillColor(vg, rgba(0xe6, 0xea, 0xed, 255, colour));
    	nvgText(vg, window.getWidth() /30f, window.getHeight() /yText, format.format(minutes) + ":"+ format.format(seconds));

    	nvgEndFrame(vg);
    }

    public void incCounter() {
        counter++;
        if (counter > 99) {
            counter = 0;
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
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }
    
    public long convertTime() {
        long converted = (time * 60000) + 1000;
        return converted;
    }

    @SuppressWarnings("static-access")
	public void updateTime() {
         remaining = convertTime();
         long current = System.currentTimeMillis();
         long elapsed = current - initial;
         remaining -= elapsed;
         format = NumberFormat.getNumberInstance();
         format.setMinimumIntegerDigits(2);

         if (remaining < 0)
              remaining = (long) 0;
         this.minutes = (int) (remaining / 60000);
         this.seconds = (int) ((remaining % 60000) / 1000);
         if (remaining == 0) {
              timer.stop();
           }
        }

	public static int getSeconds() {
		return seconds;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public static int getMinutes() {
		return minutes;
	}

	public static void setMinutes(int minutes) {
		HudMiniTimer.minutes = minutes;
	}

	public static void setSeconds(int seconds) {
		HudMiniTimer.seconds = seconds;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setFoodProcessorInUse(boolean foodProcessorInUse) {
		this.foodProcessorInUse = foodProcessorInUse;
	}

	public void setCookwareInUse(boolean cookwareInUse) {
		this.cookwareInUse = cookwareInUse;
	}

	public void setOvenInUse(boolean ovenInUse) {
		this.ovenInUse = ovenInUse;
	}

}
