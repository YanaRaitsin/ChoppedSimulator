package admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import login.BackgroundJPanel;

@SuppressWarnings("serial")
public class IngredientFrame extends JFrame {
	
	private BackgroundJPanel ingredientBackground;
	private IngredientPanel ingredientPanel;
	
	public IngredientFrame() throws Exception {
		super("Chopped Simulator: Admin Panel");
		setSize(1280, 720);
		
		ingredientBackground = new BackgroundJPanel("resLogin/Admin_Screen.png");
		ingredientBackground.setBorder(BorderFactory.createEmptyBorder(280, 280, 280, 280));
		
		ingredientPanel = new IngredientPanel(this);
		ingredientPanel.initInsertNewIngredientPanel();
		ingredientPanel.setPreferredSize(new Dimension(1280/2, 120)); //640x120
		
		ingredientBackground.add(ingredientPanel, BorderLayout.CENTER);
		
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(windowSize.width/2 - getWidth()/2, windowSize.height/2 - getHeight()/2);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/main/choppedlogo.png")));
		
		setContentPane(ingredientBackground);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
