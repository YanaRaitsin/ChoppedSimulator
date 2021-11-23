package login;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import data.User;
import simulator.JpaConnection;
import utils.MailService;
import utils.PasswordUtils;

@SuppressWarnings("serial")
public class ForgotPasswordPanel extends JPanel implements ActionListener{
	
	private ForgotPasswordFrame forgotPasswordFrame;
	private JPanel enterEmail;
	private JTextField emailField;
	private CustomButton sendButton;
	private CustomButton backButton;
	
	private JPanel verifyCodePanel;
	private JTextField codeField;
	private CustomButton verifyCodeButton;
	private CustomButton backToForgotPasswordButton;
	
	private JPanel newPasswordPanel;
	private JPasswordField newPasswordField;
	private CustomButton resetPasswordButton;
	private MailService mailService;
	
	private int saveUserId;
	
	public ForgotPasswordPanel(ForgotPasswordFrame forgotPasswordFrame){
		super();
		this.forgotPasswordFrame = forgotPasswordFrame;
		setBackground(Color.WHITE);
		mailService = new MailService();
	}
	
	//init each panel for the reset password (enter email screen, enter code screen and enter new password screen).
	public void initForgotPassword() {
		setLayout(new GridLayout(1, 2));
		enterEmail = new JPanel();
		enterEmail.setBackground(Color.WHITE);
		enterEmail.setLayout(new GridLayout(4, 2));
		enterEmail.add(new JLabel("Enter Your Email:"));
		enterEmail.add(emailField = new JTextField(45));
		enterEmail.add(sendButton = new CustomButton("Reset Password"));
		enterEmail.add(backButton = new CustomButton("Go Back"));
		add(enterEmail);
		backButton.addActionListener(this);
		sendButton.addActionListener(this);
	}
	
	public void initEnterCodePanel() {
		verifyCodePanel = new JPanel();
		verifyCodePanel.setBackground(Color.WHITE);
		verifyCodePanel.setLayout(new GridLayout(4,2));
		verifyCodePanel.add(new JLabel("Please Enter Your Code That You Recived In Your Mail:"));
		verifyCodePanel.add(codeField = new JTextField(45));
		verifyCodePanel.add(verifyCodeButton = new CustomButton("Verify Code"));
		verifyCodePanel.add(backToForgotPasswordButton = new CustomButton("Back To Forgot Password"));
		add(verifyCodePanel);
		backToForgotPasswordButton.addActionListener(this);
		verifyCodeButton.addActionListener(this);
	}
	
	private void initNewPasswordPanel() {
		newPasswordPanel = new JPanel();
		newPasswordPanel.setBackground(Color.WHITE);
		newPasswordPanel.setLayout(new GridLayout(3,2));
		newPasswordPanel.add(new JLabel("Enter New Password:"));
		newPasswordPanel.add(newPasswordField = new JPasswordField(45));
		newPasswordPanel.add(resetPasswordButton = new CustomButton("Change Password"));
		add(newPasswordPanel);
		resetPasswordButton.addActionListener(this);
	}
	
	//checkEamil() - if the email that inserted by the user is exists in the database.
	public void checkEamil() throws Exception {
		String checkEmail = emailField.getText().toLowerCase();
		if(emailField.getText().equals(""))
			throw new Exception("Please Enter An Email");
		else if(!isExists(checkEmail))
			throw new Exception("Email Is Not Exists In The Simulation, Please Try Again.");
	}
	
	//checkCode() - if the code that been send to the user is the same code that the user inserted.
	public void checkCode() throws Exception {
		if(codeField.getText().equals(""))
			throw new Exception("Please Enter A Code");
		else if(!mailService.verifyCode(codeField.getText()))
			throw new Exception("Code Inccorrent, Please Try Again.");
	}
	
	/*
	 * isExists(String email) - the function check if the email exists, if exists it save the user id (to set the new password).
	 */
	@SuppressWarnings("unchecked")
	private boolean isExists(String email) {
		List<User> users = new Vector<User>();
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
			try {
				users = JpaConnection.getEntityManager().createQuery("SELECT u FROM User u").getResultList();
				JpaConnection.getEntityManager().getTransaction().commit();
			} catch(Exception e2) {
					JpaConnection.getEntityManager().getTransaction().rollback();
			} finally {
					JpaConnection.closeEntityManager();
			}
		for(User findUser : users) {
			if(findUser.getEmail().equals(email)) {
				saveUserId = findUser.getId();
				return true;
			}
		}
		return false;
	}
	
	/*
	 * changePassword(int saveUserId, String password) -
	 * If the user inserted the correct resetting code that been send in the email of the user, the user can reset the password.
	 * The function saved the id of the user if the simulation found the user by his email.
	 * The function sets the new password to the database.
	 */
	private void changePassword(int saveUserId, String password) {
		JpaConnection.initEntityManager();
		JpaConnection.getEntityManager().getTransaction().begin();
		try {
			User user = JpaConnection.getEntityManager().find(User.class, saveUserId);
			String salt = PasswordUtils.getSalt(30);
			String securePassword = PasswordUtils.generateSecurePassword(password, salt);
			user.setPassword(securePassword);
			user.setSalt(salt);
			JpaConnection.getEntityManager().getTransaction().commit();
			JOptionPane.showMessageDialog(null, "Password Changed Successfully");
		} catch(Exception e2) {
				JpaConnection.getEntityManager().getTransaction().rollback();
		} finally {
				JpaConnection.closeEntityManager();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Reset Password")) {
			try {
				checkEamil();
				mailService.sendRestCode(emailField.getText());
				remove(enterEmail);
				forgotPasswordFrame.getContentPane().remove(this);
				initEnterCodePanel();
				forgotPasswordFrame.getContentPane().add(this);
				forgotPasswordFrame.getContentPane().validate();
			} catch (Exception emailMessage) {
				JOptionPane.showMessageDialog(null, emailMessage.getMessage());
			}
		}
		
		else if(e.getActionCommand().equals("Go Back")) {
			forgotPasswordFrame.dispose();
			try {
				@SuppressWarnings("unused")
				LoginFrame loginFrame = new LoginFrame();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		else if(e.getActionCommand().equals("Back To Forgot Password")) {
			remove(verifyCodePanel);
			forgotPasswordFrame.getContentPane().remove(this);
			initForgotPassword();
			forgotPasswordFrame.getContentPane().add(this);
			forgotPasswordFrame.getContentPane().validate();
		}
		
		else if(e.getActionCommand().equals("Verify Code")) {
			try {
				checkCode();
				remove(verifyCodePanel);
				forgotPasswordFrame.getContentPane().remove(this);
				initNewPasswordPanel();
				forgotPasswordFrame.getContentPane().add(this);
				forgotPasswordFrame.getContentPane().validate();
			} catch (Exception verifyMessage) {
				JOptionPane.showMessageDialog(null, verifyMessage.getMessage());
			}
		}
		
		else if(e.getActionCommand().equals("Change Password")) {
			String changePassword = new String(newPasswordField.getPassword());
			changePassword(saveUserId,changePassword);
			forgotPasswordFrame.dispose();
			try {
				@SuppressWarnings("unused")
				LoginFrame loginFrame = new LoginFrame();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

}
