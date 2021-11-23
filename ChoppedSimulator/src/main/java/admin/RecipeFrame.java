package admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import login.BackgroundJPanel;

@SuppressWarnings("serial")
public class RecipeFrame extends JFrame{
	
	private BackgroundJPanel loginBackground;
	private RecipePanel recipePanel;

	public RecipeFrame() throws Exception {
		super("Chopped Simulator: Admin Panel");
		setSize(1280, 720);
		
		loginBackground = new BackgroundJPanel("resLogin/Login_Screen.png");
		loginBackground.setBorder(BorderFactory.createEmptyBorder(280, 280, 280, 280));
		
		recipePanel = new RecipePanel(this);
		recipePanel.initOptionsPanel();
		recipePanel.setPreferredSize(new Dimension(1280/2, 120)); //640x120
		
		loginBackground.add(recipePanel, BorderLayout.CENTER);
		
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(windowSize.width/2 - getWidth()/2, windowSize.height/2 - getHeight()/2);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/main/choppedlogo.png")));
		
		setContentPane(loginBackground);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
