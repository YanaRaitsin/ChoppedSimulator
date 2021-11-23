package login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RegisterFrame extends JFrame implements ActionListener{
	
	private RegisterPanel registerPanelMainPage;
	private RegisterPanel registerPanelSecondPage;
	private BackgroundJPanel registerBackground;
	private CustomButton nextButton;
	private CustomButton registerButton;
	private CustomButton backButton;
	private CustomButton backToLoginButton;
	private JPanel buttonMainPanel;
	private JPanel buttonSecondPanel;
	private JPanel basicInfoPanel;
	private JPanel allergiesPanel;
	private JPanel typesPanel;
	private JPanel preferencePanel;

	public RegisterFrame() throws Exception {
		super("Chopped Simulator: Register");
		setSize(1280,720);
		
		registerBackground = new BackgroundJPanel("resLogin/Register_screen.png");
		registerBackground.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 850));
		registerPanelMainPage = new RegisterPanel(this);
		registerPanelMainPage.setBackground(Color.WHITE);
		registerPanelSecondPage = new RegisterPanel(this);
		
		buttonMainPanel = new JPanel();
		backToLoginButton = new CustomButton("Go To Login");
		nextButton = new CustomButton("Next");
		buttonMainPanel.setBackground(Color.WHITE);
		buttonMainPanel.add(backToLoginButton);
		buttonMainPanel.add(nextButton);
		
		registerBackground.add(registerPanelMainPage);
		
		basicInfoPanel = new JPanel();
		basicInfoPanel.setLayout(new GridLayout(8,2));
		registerPanelMainPage.initBasicInfo(basicInfoPanel);
		
		allergiesPanel = new JPanel();
		allergiesPanel.setLayout(new GridLayout(10,2));
		registerPanelMainPage.initAllergiesCheckBox(allergiesPanel);
		
		typesPanel = new JPanel();
		typesPanel.setLayout(new GridLayout(8,2));
		registerPanelMainPage.initTypes(typesPanel);
		
		preferencePanel = new JPanel();
		preferencePanel.setLayout(new GridLayout(15,2));
		
		buttonSecondPanel = new JPanel();
		registerButton = new CustomButton("Register");
		backButton = new CustomButton("Go Back");
		buttonSecondPanel.setBackground(Color.WHITE);
		buttonSecondPanel.add(registerButton);
		buttonSecondPanel.add(backButton);
		registerPanelSecondPage.initPreference(preferencePanel);
		
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(windowSize.width/2 - getWidth()/2, windowSize.height/2 - getHeight()/2);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/main/choppedlogo.png")));
		
		setContentPane(registerBackground);
		registerPanelMainPage.add(basicInfoPanel);
		registerPanelMainPage.add(typesPanel);
		registerPanelMainPage.add(allergiesPanel);
		registerPanelMainPage.add(buttonMainPanel);
		
		nextButton.addActionListener(this);
		backButton.addActionListener(this);
		registerButton.addActionListener(this);
		backToLoginButton.addActionListener(this);
		
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Next")) {
			try {
				registerPanelMainPage.checkRegistration();
				getContentPane().remove(registerPanelMainPage);
				getContentPane().add(registerPanelSecondPage);
				registerPanelSecondPage.add(preferencePanel);
				registerPanelSecondPage.add(buttonSecondPanel);
				registerPanelSecondPage.setBackground(Color.WHITE);
				registerPanelSecondPage.setSizePanel(1200, 1000);
				registerBackground.setBorder(BorderFactory.createEmptyBorder(50, 150, 0, 0));
				validate();
			} catch (Exception meesage) {
				JOptionPane.showMessageDialog(null, meesage.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Go Back")) {
			getContentPane().remove(registerPanelSecondPage);
			getContentPane().add(registerPanelMainPage);
			registerBackground.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 850));
			validate();
		}
		else if(e.getActionCommand().equals("Register")) {
			registerPanelSecondPage.insertNewUser();
			dispose();
			try {
				@SuppressWarnings("unused")
				LoginFrame loginFrame = new LoginFrame();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getActionCommand().equals("Go To Login")) {
			dispose();
			try {
				@SuppressWarnings("unused")
				LoginFrame loginFrame = new LoginFrame();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

}
