package login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class CustomButton extends JButton {
	
	private Color borderColor = Color.ORANGE;
	private Color backgroundColor = Color.WHITE;
	private Color pressedColor = Color.ORANGE;
	
	public CustomButton(String text) {
		super(text);
		
		setBorder(BorderFactory.createLineBorder(borderColor, 3));
		setPreferredSize(new Dimension(150, 30));
		setBackground(backgroundColor);
		setFont(new Font("Tahoma", Font.BOLD, 12));
        setText(text);
        
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                if (getModel().isPressed()) {
                    setBackground(pressedColor);
                }
                else if (getModel().isRollover()) {
                    setBackground(pressedColor);
                } 
                else {
                    setBackground(backgroundColor);
                }
            }
        });
	}

}
