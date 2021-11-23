package login;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BackgroundJPanel extends JPanel{
	
	private Image background;
	private File file;

	public BackgroundJPanel(String fileName) throws Exception {
		file = new File(fileName);
		this.background = ImageIO.read(file);
		if(!file.exists()) throw new Exception(".png file not found");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background, 0, 0, this);
	}
	
	
}
