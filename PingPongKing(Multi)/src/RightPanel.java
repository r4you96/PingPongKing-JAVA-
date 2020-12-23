import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RightPanel extends JPanel {
	RecordLabel idLabel;
	RecordLabel nicknameLabel;
	RecordLabel totalWinLabel;
	RecordLabel totalLoseLabel;
	RecordLabel opponentWinLabel;
	RecordLabel opponentLoseLabel;
	RecordLabel[] recordLabel;


	public RightPanel() {
		int i;
		// TODO Auto-generated constructor stub
		recordLabel = new RecordLabel[5];
		String texting = "";
		Font font = new Font("±Ã¼­", Font.BOLD, 20);
		idLabel = new RecordLabel(texting, font);
		idLabel.setBounds(110, 88, 300, 50);
		nicknameLabel = new RecordLabel(texting, font);
		nicknameLabel.setBounds(200, 123, 300, 50);
		totalWinLabel = new RecordLabel(texting, font);
		totalWinLabel.setBounds(100, 218, 300, 50);
		totalLoseLabel = new RecordLabel(texting, font);
		totalLoseLabel.setBounds(190, 218, 300, 50);
		opponentWinLabel = new RecordLabel(texting, font);
		opponentWinLabel.setBounds(100, 313, 300, 50);
		opponentLoseLabel = new RecordLabel(texting, font);
		opponentLoseLabel.setBounds(190, 313, 300, 50);
		
		font = new Font("±Ã¼­", Font.BOLD, 15);
		for(i = 0; i<5; i++) {
			recordLabel[i] = new RecordLabel(texting, font);
			recordLabel[i].setBounds(100, 453+i*53, 400, 48);
			add(recordLabel[i]);
		}
		
		setLayout(null);
		add(idLabel);
		add(nicknameLabel);
		add(totalWinLabel);
		add(totalLoseLabel);
		add(opponentWinLabel);
		add(opponentLoseLabel);
		
	}
	public void clearLabel() {
		idLabel.setText("");
		nicknameLabel.setText("");
		totalWinLabel.setText("");
		totalLoseLabel.setText("");
		opponentWinLabel.setText("");
		opponentLoseLabel.setText("");
		for(int i=0; i<5; i++)
			recordLabel[i].setText("");
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(new ImageIcon(getClass().getResource("/res/rigthPanel.png")).getImage(), 0, 0, this);
	}

	public class RecordLabel extends JLabel {
		public RecordLabel(String text, Font font) {
			super(text);
			setFont(font);
		}
	}

}