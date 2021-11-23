package game;


import engine.GameEngine;
import engine.IGameLogic;
import engine.Window;

public class ChoppedSettings {
	
	private static boolean vSync;
	private static IGameLogic gameLogic;
	private static Window.WindowOptions opts;
	private static GameEngine gameEngine;
	
	/*
	 * startMenu() - 
	 * When the user login into the simulation, it will init the first screen - the menu of the simulation.
	 */
	public static void startMenu() {
        try {
            vSync = true;
            gameLogic = new ChoppedMenu();
            opts = new Window.WindowOptions();
            opts.cullFace = true;
            opts.showFps = true;
            opts.compatibleProfile = true;
            gameEngine = new GameEngine("Chopped Simulator",vSync, opts, gameLogic);
            gameEngine.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        } 
	}
	
	/*
	 * startNewWindow(IGameLogic screen) - changes the screen of the simulation.
	 */
	public static void startNewWindow(IGameLogic screen) throws Exception {
        gameEngine.setNewScreen(screen);
	}
	
}
