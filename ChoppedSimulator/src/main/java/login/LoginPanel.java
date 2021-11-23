package login;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.persistence.Query;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import admin.RecipeFrame;
import data.User;
import game.ChoppedSettings;
import simulator.JpaConnection;
import simulator.SimulationService;
import utils.PasswordUtils;

@SuppressWarnings("serial")
public class LoginPanel extends JPanel implements ActionListener{
	
	private LoginFrame loginFrame;
	private JTextField emailTextField;
	private JPasswordField passwordTextField;
	private CustomButton registerButton;
	private CustomButton loginButton;
	private JLabel forgetPasswordLabel; //**need to think how to restore or change the password**
	
	 private static Query query;

	public LoginPanel(LoginFrame loginFrame){
		super();
		this.loginFrame = loginFrame;
		setBackground(Color.WHITE);
		setLayout(new GridLayout(4, 2));
		addComponents();
		registerButton.addActionListener(this);
		loginButton.addActionListener(this);
		forgetPasswordLabel.addMouseListener(new MouseAdapter() {
			 public void mouseClicked(MouseEvent e) {
				 loginFrame.dispose();
				 try {
					@SuppressWarnings("unused")
					ForgotPasswordFrame forgotPasswordFrame = new ForgotPasswordFrame();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			 }
		});
	}
	
	public void addComponents() {
		add(new JLabel("Email:"));
		add(emailTextField = new JTextField(45));
		add(new JLabel("Password:"));
		add(passwordTextField = new JPasswordField(12));
		add(registerButton = new CustomButton("Register"));
		add(loginButton = new CustomButton("Login")); 
		add(forgetPasswordLabel = new JLabel("Forgot Password?"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Login")) {
			try {
				checkValidUser();
				if(!isAdmin()) {
					loginFrame.dispose();
			        SimulationService.checkUserData();
					ChoppedSettings.startMenu();
				}
				else {
					loginFrame.dispose();
					new RecipeFrame();
				}
			} catch (Exception userMessage) {
				JOptionPane.showMessageDialog(null, userMessage.getMessage());
			}
		}
		else if(e.getActionCommand().equals("Register")) {
			loginFrame.dispose();
			try {
				@SuppressWarnings("unused")
				RegisterFrame registerFrame = new RegisterFrame();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/*
	 * isAdmin() - if the user is admin.
	 */
	private boolean isAdmin() {
		String checkEmail = emailTextField.getText().toLowerCase();
		if(checkEmail.contains("choppedsimulator@gmail.com"))
			return true;
		return false;
	}
	
	/*
	 * checkValidUser() - checks the input of the user and checks if the user exists.
	 * If the input is empty it will throw exception and if the simulation didn't found the user - it will throw exception.
	 */
	public void checkValidUser() throws Exception {
		String checkPassword = new String(passwordTextField.getPassword());
		String checkEmail = emailTextField.getText().toLowerCase();
		if((emailTextField.getText().equals("") && passwordTextField.getPassword().length==0) || (emailTextField.getText().equals("") || passwordTextField.getPassword().length==0))
			throw new Exception("Please Insert Email And/Or Password");
		else if(!isUserExists(checkEmail,checkPassword))
			throw new Exception("Incorrect Email, Password Or The User Is Not Exists");
	}
	
	/*
	 * isUserExists(String email, String password) - 
	 * The function check if the user exists in the database by the email and the password that inserted in the login window.
	 */
	@SuppressWarnings("rawtypes")
	private boolean isUserExists(String email, String password) {
		List emailFound = null;
		List saltFound = null;
		List securePasswordFound  = null;
		List findUser = null;
		Object salt = null;
		Object securePassword = null;
		Object id = null;
		boolean emailExists = false;
		
		JpaConnection.initEntityManager();
		query = JpaConnection.getEntityManager().createNativeQuery("SELECT email u FROM Users u WHERE u.email = ?email");
		try {
			JpaConnection.getEntityManager().getTransaction().begin();
			query.setParameter("email", email);
			emailFound = query.getResultList();
			if(!emailFound.isEmpty())
				emailExists = true;
			
			query = JpaConnection.getEntityManager().createNativeQuery("SELECT salt u FROM Users u WHERE u.email = ?email");
			query.setParameter("email", email);
			saltFound = query.getResultList();
			if(!saltFound.isEmpty())
				salt = saltFound.get(0);
		
			query = JpaConnection.getEntityManager().createNativeQuery("SELECT password u FROM Users u WHERE u.email = ?email");
			query.setParameter("email", email);
			securePasswordFound = query.getResultList();
			if(!securePasswordFound.isEmpty())
				securePassword = securePasswordFound.get(0);
		
			boolean passwordMatch = PasswordUtils.verifyUserPassword(password,securePassword.toString(),salt.toString());
			if(passwordMatch && emailExists) {
				query = JpaConnection.getEntityManager().createNativeQuery("SELECT id u FROM Users u WHERE u.email = ?email");
				query.setParameter("email", email);
				findUser = query.getResultList();
				if(!findUser.isEmpty())
					id = findUser.get(0);
				User user = JpaConnection.getEntityManager().find(User.class, id);
				SimulationService.setUser(user);
				JpaConnection.getEntityManager().getTransaction().commit();
				return true;
			}
		}catch(Exception e2) {
			JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
			JpaConnection.closeEntityManager();
		}
		return false;
	}	

}
