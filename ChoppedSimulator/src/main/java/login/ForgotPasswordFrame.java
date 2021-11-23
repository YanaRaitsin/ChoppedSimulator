package login;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ForgotPasswordFrame extends JFrame{
	
	private BackgroundJPanel loginBackground;
	private ForgotPasswordPanel forgotPasswordPanel;
	
	public ForgotPasswordFrame() throws Exception
	{
		super("Chopped Simulator: Forgot Password");
		setSize(1280, 720);
		
		loginBackground = new BackgroundJPanel("resLogin/Login_screen.png");
		loginBackground.setBorder(BorderFactory.createEmptyBorder(250, 250, 250, 250));
		
		forgotPasswordPanel = new ForgotPasswordPanel(this);
		forgotPasswordPanel.setPreferredSize(new Dimension(1280/2, 120)); //640x120
		forgotPasswordPanel.initForgotPassword();
		
		loginBackground.add(forgotPasswordPanel, BorderLayout.CENTER);
		
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(windowSize.width/2 - getWidth()/2, windowSize.height/2 - getHeight()/2);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/main/choppedlogo.png")));
		
		setContentPane(loginBackground);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

}
