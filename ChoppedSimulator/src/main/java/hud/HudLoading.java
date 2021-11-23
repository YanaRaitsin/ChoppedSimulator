package hud;

import static org.lwjgl.opengl.GL11.glViewport;

import engine.Window;
import guis.GuiLoader;
import guis.GuiRenderer;
import guis.GuiTexture;
import math.Vector2;

public class HudLoading {
    
    private GuiLoader loader;
    
    private GuiRenderer guiRenderer;
    
    private GuiTexture loadingScreen;
    
    private boolean rendered;
    
    /*
     * The loading screen when the user chosen the recipe.
     */
    public void init(Window window) throws Exception {
        loader = new GuiLoader();
        guiRenderer = new GuiRenderer(loader);
        loadingScreen = new GuiTexture(loader.loadTexture("src/main/java/resources/main/loading.png"), new Vector2(0.01f, -0.01f), new Vector2(1.01f, 1.01f));
    	rendered = false;
    }
    
    public void render(Window window) {
    	glViewport(0,0,window.getWidth(),window.getHeight());
    	guiRenderer.render(loadingScreen);
        rendered = true;
    }
    
	public boolean isRendered() {
		return rendered;
	}
	
    public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public void cleanup() {
    	guiRenderer.cleanUp();
    }
}
