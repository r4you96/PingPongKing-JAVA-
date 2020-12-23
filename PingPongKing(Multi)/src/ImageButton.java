import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ImageButton extends JButton{
	public ImageButton(String iconUrl, ActionListener al, boolean visible) {
		setIcon(new ImageIcon(iconUrl));
		addActionListener(al);
		setBorderPainted(false);
		setVisible(visible);
	}
}
