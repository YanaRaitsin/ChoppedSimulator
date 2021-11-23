package hud;

import static org.lwjgl.opengl.GL11.glViewport;
import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;

public class HudGui {
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture simulationGui;
    
    private boolean rendered;
    
    private String fileName;

    public void init(Window window) throws Exception {
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
    	rendered = false;
    }

    /*
     * the gui of the actions of the user when he using kitchen tools/taking ingredients/baking/cooking and more...
     */
    public void render(Window window) {
    	glViewport(0,0,window.getWidth(),window.getHeight());
    	simulationGui = new GuiTexture(loader.loadTexture(fileName), new Vector2(0.7f, -0.5f), new Vector2(0.3f, 0.5f));
    	guiRenderer.render(simulationGui);
        rendered = true;
        // Restore state
        window.restoreState();
    }

    public void cleanup() {
    	guiRenderer.cleanUp();
    }

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}


}
