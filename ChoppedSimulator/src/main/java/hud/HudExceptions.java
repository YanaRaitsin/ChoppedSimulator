package hud;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
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

public class HudExceptions {
	
	private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private DoubleBuffer posx;

    private DoubleBuffer posy;

    private int counter;
    
    private int fade;
    
    private boolean rendered;
    
    private int secondsLeft;

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
        secondsLeft = 0;
        rendered = false;
    }

    /*
     * render the message of the actions of the user, the message is rendered for 3 seconds.
     */
    public void renderMessage(Window window, String message, int startTime) {
    	int showMessage = startTime - 3;
		int checkNegtiveTime;
    	if(showMessage > 0) { //positive number
			if(showMessage != HudTimer.getSeconds() && rendered)
				messageSimulation(window, message);
			else
				rendered = false;
    	}
		checkNegtiveTime = -showMessage;
		if(showMessage < 0 && secondsLeft == 0) { //negative number 
			if(HudTimer.getSeconds()==0)
				secondsLeft = 59 - (--checkNegtiveTime);
		}
		if(showMessage < 0) {
			if(HudTimer.getSeconds() != secondsLeft && rendered)
				messageSimulation(window, message);
			else
				rendered = false;
		}
    }
    
    public void messageSimulation(Window window, String message) {
    	glViewport(0,0,window.getWidth(),window.getHeight());
		nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);
    	//width - x, height - y
    	nvgBeginPath(vg);
    	nvgRect(vg, 0, window.getHeight() * 0.9f , window.getWidth(), 50);
    	nvgFillColor(vg, rgba(0x01, 0x01, 0x01, 200, colour));
    	nvgFill(vg);
    	nvgFontSize(vg, 45.0f);
    	nvgFillColor(vg, rgba(0xFF, 0xFF, 0xFF, 200, colour));
    	nvgText(vg, window.getWidth() * 0.08f , window.getHeight() * 0.935f, String.format(message, counter)); 
    	nvgEndFrame(vg);
    
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
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }

	public int getFade() {
		return fade;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public void setFade(int fade) {
		this.fade = fade;
	}

}
