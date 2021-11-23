package hud;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.NumberFormat;

import javax.swing.Timer;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import org.lwjgl.nanovg.NVGColor;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import org.lwjgl.system.MemoryUtil;

import engine.Utils;
import engine.Window;
import simulator.SimulationService;
import utils.Logs;

import static org.lwjgl.system.MemoryUtil.NULL;

public class HudTimer {
	
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
        
        if(SimulationService.isLoadLog()) {
			String logFolder = "SavedSimulationFiles";
			File file = new File(logFolder);
			if(file.exists())
				time = Logs.initHudTimer();
        }
        if(!SimulationService.isLoadLog() && SimulationService.getRecipeChosen().getType().equals("dinner")) //If the type of the recipe is dinner - set the time to 25.
        	time = 25;
        else if(!SimulationService.isLoadLog() && SimulationService.getRecipeChosen().getType().equals("dessert")) //If the type of the recipe is dessert - set the time to 15.
        	time = 15;
        
        timer = new Timer(1000, null);
        initial = System.currentTimeMillis();
        timer.start();
        rendered = false;
    }

    //init the upper bar and the timer of the chosen recipe, if the chosen recipe is dessert - the timer will init to 15 minutes.
    //if the chosen recipe is dinner - it will init the timer to 25 minutes.
    public void render(Window window) {
    	updateTime();
    	nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);

        //Upper bar
        nvgBeginPath(vg);
        nvgRect(vg, 0, window.getHeight() * 0.05f , window.getWidth(), 50);
        nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
        nvgFill(vg);

        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        float xcenter = 50;
        float ycenter = window.getHeight() / 13.5f;
        int radius = 20;

        //Circle
        nvgBeginPath(vg);
        nvgCircle(vg, xcenter, ycenter, radius);
        nvgFillColor(vg, rgba(0xDC, 0xDC, 0xDC, 200, colour));
        nvgFill(vg);

        //Setting the text time
        nvgFontSize(vg, 25.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_TOP);
        nvgText(vg, 50, window.getHeight() / 16f, String.format("%02d", counter));

        // Render timer text
        nvgFontSize(vg, 40.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFillColor(vg, rgba(0xe6, 0xea, 0xed, 255, colour));
        nvgText(vg, window.getWidth() * 0.9f, window.getHeight() * 0.053f, format.format(minutes) + ":"+ format.format(seconds));

        nvgEndFrame(vg);
        // Restore state
        window.restoreState();
        rendered = true;
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

	public void setTime(long time) {
		this.time = time;
	}

}
