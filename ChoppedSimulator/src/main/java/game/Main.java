package game;

import javax.swing.SwingUtilities;

import login.LoginFrame;

public class Main {

    public static void main(String[] args) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
						try {
							new LoginFrame();
						} catch (Exception e) {
							e.printStackTrace();
						}
			}
		});
    }
}
