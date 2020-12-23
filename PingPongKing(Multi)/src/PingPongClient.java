
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import javax.swing.*;

import se.datadosen.component.RiverLayout;

import java.awt.*;
import java.awt.event.*;

public class PingPongClient {
	JFrame frame;
	String frameTitle = "ä�� Ŭ���̾�Ʈ";
	JTextArea incoming; // ���ŵ� �޽����� ����ϴ� ��
	JTextArea outgoing; // �۽��� �޽����� �ۼ��ϴ� ��
	JList counterParts; // ���� �α����� ä�� ������� ��Ÿ���� ����Ʈ.
	ObjectInputStream reader; // ���ſ� ��Ʈ��
	ObjectOutputStream writer; // �۽ſ� ��Ʈ��
	Socket sock; // ���� ����� ����
	String user; // �� Ŭ���̾�Ʈ�� �α��� �� ������ �̸�
	String user_idNum;
	JButton logButton; // ����� �Ǵ� �α���/�α׾ƿ� ��ư
	boolean gameState;
	Random rand;
	OpereatePingPong game;
	LoginFrame login;
	JoinFrame joins;
	RightPanel mainPanel;

	public static void main(String[] args) {
		PingPongClient client = new PingPongClient();
		client.go();
	}

	private void go() {
		gameState = false;
		// build GUI
		frame = new JFrame(frameTitle + " : �α����ϼ���");

		// �޽��� ���÷��� â
		incoming = new JTextArea(15, 20);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// ��ȭ ��� ���. �ʱ⿡�� "��ü" - ChatMessage.ALL �� ����
		String[] list = { PingPongMessage.ALL };
		counterParts = new JList(list);
		JScrollPane cScroller = new JScrollPane(counterParts);
		cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		counterParts.setVisibleRowCount(5);
		counterParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		counterParts.setFixedCellWidth(100);
		counterParts.setBackground(new Color(255, 203, 57));
		Font font = new Font("�ü�", Font.BOLD, 20);
		counterParts.setFont(font);

		// �޽��� ������ ���� ��ư
		ImageButton gameButton = new ImageButton("src/res/fight.png", new GameButtonListener(), true);
		ImageButton recordButton = new ImageButton("src/res/record.png", new recordButtonListener(), true);
		ImageButton logoutButton = new ImageButton("src/res/logout.png", new LogoutButtonListener(), true);
		gameButton.setBounds(240, 770, 124, 46);
		recordButton.setBounds(240, 840, 124, 46);
		logoutButton.setBounds(240, 910, 124, 46);

		// GUI ��ġ
		mainPanel = new RightPanel();
		mainPanel.setLayout(null);
		mainPanel.add(cScroller);
		mainPanel.add(recordButton);
		mainPanel.add(logoutButton);
		mainPanel.add(gameButton);
		cScroller.setBounds(30, 770, 190, 180);
		/*
		 * buttonPanel.add(sendButton); // buttonPanel.add(Box.createRigidArea(new
		 * Dimension(0,30))); buttonPanel.add(logButton); buttonPanel.add(gameButton);
		 * buttonPanel.add(pingpongButton);
		 */

		rand = new Random();

		// ��Ʈ��ŷ�� �õ��ϰ�, �������� �޽����� ���� ������ ����
		setUpNetworking();
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		mainPanel.setBounds(550, 0, 400, 1000);
		game = new OpereatePingPong();
		game.setBounds(0, 0, 550, 1000);
		game.addMouseListener(new gameMouse());
		// Ŭ���̾�� ������ â ����
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(game);
		frame.add(mainPanel);
		frame.setSize(950, 1030);
		frame.setResizable(false);

		login = new LoginFrame();
		joins = new JoinFrame();
		login.setVisible(true);

		frame.setVisible(false);

		// �������� ��� �����Ƿ� ���⼭ ������ ������� ��� ���� ��
		// �� ������ �����带 �����ϸ�, �� �����ӿ��� ���� ��������� ���ܸ� �߻��ϰԵǰ�
		// �̸� �̿��� ��� �����带 �����ϰ� ���� ��Ű���� ��
	} // close go

	private void setUpNetworking() {
		try {
			 sock = new Socket("220.69.203.88", 5000); // ��ö��
			//sock = new Socket("127.0.0.1", 5000); // ���� ����� ���� ��Ʈ�� 5000�� ���Ű�� ��
			reader = new ObjectInputStream(sock.getInputStream());
			writer = new ObjectOutputStream(sock.getOutputStream());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "�������ӿ� �����Ͽ����ϴ�. ������ �����մϴ�.");
			ex.printStackTrace();
			frame.dispose(); // ��Ʈ��ũ�� �ʱ� ���� �ȵǸ� Ŭ���̾�Ʈ ���� ����
		}
	} // close setUpNetworking

	// �α��ΰ� �ƿ��� ����ϴ� ��ư�� ��û��. ó������ Login �̾��ٰ� �ϴ� �α��� �ǰ��� Logout�� ó��
	private class LogoutButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			int choice = JOptionPane.showConfirmDialog(null, "Logout�մϴ�");
			if (choice == JOptionPane.YES_OPTION) {
				try {
					writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.LOGOUT, user, "", ""));
					writer.flush();
					// ����� ��� ��Ʈ���� ������ �ݰ� ���α׷��� ���� ��
					writer.close();
					reader.close();
					sock.close();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "�α׾ƿ� �� �������ӿ� ������ �߻��Ͽ����ϴ�. ���������մϴ�");
					ex.printStackTrace();
				} finally {
					System.exit(100); // Ŭ���̾�Ʈ ���� ����
				}
			}
		}
	} // close LoginButtonListener inner class

	public class recordButtonListener implements ActionListener {
		String opponent_id = null;
		String queryMan = null;
		Connection conn; // DB ���� Connection ��ü��������
		int count = 0;

		public void actionPerformed(ActionEvent ev) {
			int count = 0;
			mainPanel.clearLabel();
			dbConnectionInit();
			String to = (String) counterParts.getSelectedValue();
			try {
				Statement stmt = conn.createStatement(); // SQL ���� �ۼ��� ���� Statement ��ü ����

				// ���� DB�� �ִ� ���� �����ؼ� ���� ����� names ����Ʈ�� ����ϱ�
				queryMan = "SELECT * FROM user WHERE nickname = '" + to + "'";
				ResultSet rs = stmt.executeQuery(queryMan);
				rs.next();
				opponent_id = rs.getString("user_id");
				mainPanel.idLabel.setText(rs.getString("id"));
				mainPanel.nicknameLabel.setText(rs.getString("nickname"));

				queryMan = "SELECT count(winner_id) FROM record WHERE winner_id = " + opponent_id;
				rs = stmt.executeQuery(queryMan);
				rs.next();
				mainPanel.totalWinLabel.setText(rs.getString("count(winner_id)"));

				queryMan = "SELECT count(loser_id) FROM record WHERE loser_id = " + opponent_id;
				rs = stmt.executeQuery(queryMan);
				rs.next();
				mainPanel.totalLoseLabel.setText(rs.getString("count(loser_id)"));

				queryMan = "SELECT count(loser_id) FROM record WHERE winner_id = " + user_idNum + " AND loser_id ="
						+ opponent_id;
				rs = stmt.executeQuery(queryMan);
				rs.next();
				mainPanel.opponentLoseLabel.setText(rs.getString("count(loser_id)"));
				
				queryMan = "SELECT count(loser_id) FROM record WHERE winner_id = " + opponent_id + " AND loser_id = "
						+ user_idNum;
				rs = stmt.executeQuery(queryMan);
				rs.next();
				mainPanel.opponentWinLabel.setText(rs.getString("count(loser_id)"));
				
				queryMan = "SELECT  * FROM record INNER JOIN user WHERE user_id = " + user_idNum  
						+ " AND (winner_id =" + opponent_id  +" OR loser_id = "+ opponent_id + ") ORDER BY record_id desc;";
				rs = stmt.executeQuery(queryMan);
				while(rs.next() && count<5) {
					if(rs.getString("winner_id").equals(opponent_id))
						mainPanel.recordLabel[count].setText("�� " +  rs.getString("nickname") + 
								" �� (��) " + rs.getString("winner_point") + " : " + rs.getString("loser_point") );	
					else if(rs.getString("loser_id").equals(opponent_id))
						mainPanel.recordLabel[count].setText("�� " +  rs.getString("nickname") + 
								" �� (��) " + rs.getString("loser_point") + " : " + rs.getString("winner_point") );
					count++;
				}

				stmt.close(); // statement�� ����� �ݴ� ����

			} catch (SQLException sqlex) {
				System.out.println("SQL ���� : " + sqlex.getMessage());
				sqlex.printStackTrace();
			}
		}

		// DB�� �����ϴ� �޼ҵ�
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC����̹��� JVM�������� ��������
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB
																													// �����ϱ�
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC ����̹� Ŭ������ ã�� �� �����ϴ� : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB ���� ���� : " + ex.getMessage());
			}
		}
	} // close SendButtonListener inner class

	public class GameButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			String to = (String) counterParts.getSelectedValue();
			if (to == null) {
				JOptionPane.showMessageDialog(null, "�۽��� ����� ������ �� ������ �����ϼ���");
				return;
			}
			try {
				incoming.append(user + "��" + to + "��� ����\n"); // ���� �޽��� â�� ���̱�
				writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.GAME_START, user, to, ""));
				writer.flush();

				// outgoing.requestFocus();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "���� ������ ������ �߻��Ͽ����ϴ�.");
				ex.printStackTrace();
			}
		}
	} // close SendButtonListener inner class

	// �������� ������ �޽����� �޴� ������ �۾��� �����ϴ� Ŭ����
	public class IncomingReader implements Runnable {
		public void run() {
			PingPongMessage message;
			PingPongMessage.MsgType type;
			try {
				while (true) {
					message = (PingPongMessage) reader.readObject(); // �����α� ������ �޽��� ���
					type = message.getType();
					if (type == PingPongMessage.MsgType.LOGIN_FAILURE) { // �α����� ������ �����
						JOptionPane.showMessageDialog(null, "Login�� �����Ͽ����ϴ�. �ٽ� �α����ϼ���");
						frame.setTitle(frameTitle + " : �α��� �ϼ���");
						logButton.setText("Login");
					} else if (type == PingPongMessage.MsgType.SERVER_MSG) { // �޽����� �޾Ҵٸ� ������
						if (message.getSender().equals(user))
							continue; // ���� ���� ������ ���� �ʿ� ����
						incoming.append(message.getSender() + " : " + message.getContents() + "\n");
					} else if (type == PingPongMessage.MsgType.LOGIN_LIST) {
						// ���� ����Ʈ�� ���� �ؼ� counterParts ����Ʈ�� �־� ��.
						// ���� ���� (""�� ����� ���� �� ����Ʈ �� �տ� ���� ��)
						String[] users = message.getContents().split("/");
						for (int i = 0; i < users.length; i++) {
							if (user.equals(users[i]))
								users[i] = "";
						}
						users = sortUsers(users); // ���� ����� ���� �� �� �ֵ��� �����ؼ� ����
						users[0] = PingPongMessage.ALL; // ����Ʈ �� �տ� "��ü"�� ������ ��
						counterParts.setListData(users);
						frame.repaint();
					} else if (type == PingPongMessage.MsgType.GAME_START) {
						String to = message.getSender();
						if (!gameState) {
							gameState = true;
							incoming.append(message.getSender() + "�� ���� ������ ���ȹ޾ҽ��ϴ�.\n");
							incoming.append("������ �����մϴ�.\n");

							game.startGame();
							game.playerType = 1;
							writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.GAME_START_RECIEVE, user, to, ""));
							writer.flush();
							game.setPlayerName(user, message.getSender());
						} else {
							incoming.append(message.getSender() + "�� ���� ������ ���ȹ޾ҽ��ϴ�.\n");
							incoming.append("���� ���̹Ƿ� ���ӽ�û�� �����մϴ�.\n");
						}
					} else if (type == PingPongMessage.MsgType.GAME_START_RECIEVE) {
						String to = message.getSender();
						if (!gameState) {
							gameState = true;
							incoming.append(message.getSender() + "�� ���� ������ �����߽��ϴ�.\n");
							incoming.append("������ �����մϴ�.\n");

							game.startGame();
							game.playerType = 2;
							game.setPlayerName(to, user);
						}
					} else if (type == PingPongMessage.MsgType.NO_ACT) {
						// �ƹ� �׼��� �ʿ���� �޽���. �׳� ��ŵ
					} else if (type == PingPongMessage.MsgType.HIT) {
						incoming.append(message.getSender() + "��밡 Ÿ��" + message.getContents() + "\n");
						int enemy = Integer.parseInt(message.getContents());
						game.enemyHit(enemy);
					} else {
						// ��ü�� Ȯ�ε��� �ʴ� �̻��� �޽���
						throw new Exception("�������� �� �� ���� �޽��� ��������");
					}
				} // close while
			} catch (Exception ex) {
				System.out.println("Ŭ���̾�Ʈ ������ ����"); // �������� ����� ��� �̸� ���� ������ ����
			}
		} // close run

		// �־��� String �迭�� ������ ���ο� �迭 ����
		private String[] sortUsers(String[] users) {
			String[] outList = new String[users.length];
			ArrayList<String> list = new ArrayList<String>();
			for (String s : users) {
				list.add(s);
			}
			Collections.sort(list); // Collections.sort�� ����� �ѹ濡 ����
			for (int i = 0; i < users.length; i++) {
				outList[i] = list.get(i);
			}
			return outList;
		}
	} // close inner class

	public class gameMouse implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e) {
			String to = (String) counterParts.getSelectedValue();
			if (to != null) {
				int direction = rand.nextInt(2);
				// TODO Auto-generated method stub
				incoming.append("�ٿ ��ġ x : " + e.getX() + "/ y : " + e.getY() + "\n");
				game.pressed(e.getX() * 10 + direction);
				try {
					writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.HIT, user, to,
							Integer.toString(e.getX() * 10 + direction)));
					writer.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (game.gameProgress == false)
					gameState = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	}

	public class LoginFrame extends JFrame {
		JButton LoginButton;
		JButton JoinButton;
		JButton FindButton;
		JTextField user_id;
		JPasswordField password; // name �ʵ� ���÷��̸� ���� �ڽ�
		Connection conn; // DB ���� Connection ��ü��������
		int user_num;

		public LoginFrame() {
			JPanel LoginPanel = new JPanel(new RiverLayout());
			LoginButton = new JButton("�α���");
			LoginButton.addActionListener(new LoginButtonListener());
			JoinButton = new JButton("ȸ������");
			JoinButton.addActionListener(new JoinButtonListener());

			user_id = new JTextField(12);
			password = new JPasswordField(12);

			LoginPanel.add("br center", new JLabel("�α��� ȭ��"));
			LoginPanel.add("br", new JLabel("���̵�"));
			LoginPanel.add(user_id);
			LoginPanel.add("br", new JLabel("��й�ȣ"));
			LoginPanel.add(password);
			LoginPanel.add("br", LoginButton);
			LoginPanel.add("br", JoinButton);

			dbConnectionInit();
			add(LoginPanel);
			setSize(300, 400);
			this.setBounds(700, 300, 300, 400);
			setResizable(false);
			setVisible(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		// DB�� �����ϴ� �޼ҵ�
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC����̹��� JVM�������� ��������
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB
																													// �����ϱ�
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC ����̹� Ŭ������ ã�� �� �����ϴ� : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB ���� ���� : " + ex.getMessage());
			}
		}

		public class LoginButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				int count = 0;
				String user_password = null;
				String field_password;
				String user_ids = null;
				String user_nickname = null;
				try {
					Statement stmt = conn.createStatement(); // SQL ���� �ۼ��� ���� Statement ��ü ����
					field_password = password.getText();

					// ���� DB�� �ִ� ���� �����ؼ� ���� ����� names ����Ʈ�� ����ϱ�
					ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE id = '" + user_id.getText() + "'");
					while (rs.next()) {
						user_idNum = rs.getString("user_id");
						user_ids = rs.getString("id");
						user_password = rs.getString("password");
						user_nickname = rs.getString("nickname");
						count++;
					}
					user_id.setText("");
					password.setText("");
					if (count == 0) {
						JOptionPane.showMessageDialog(null, "���� ���̵� �Դϴ�.", "����", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (!user_password.equals(field_password)) {
						JOptionPane.showMessageDialog(null, "��й�ȣ�� Ʋ�Ƚ��ϴ�.", "����", JOptionPane.WARNING_MESSAGE);
						return;
					}
					stmt.close(); // statement�� ����� �ݴ� ����

					user = user_nickname;

					try {
						writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.LOGIN, user, "", ""));
						writer.flush();
						frame.setTitle(frameTitle + " (�α��� : " + user_nickname + ")");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "�α��� �� �������ӿ� ������ �߻��Ͽ����ϴ�.");
						ex.printStackTrace();
					}

					login.setVisible(false);
					frame.setVisible(true);
				} catch (SQLException sqlex) {
					System.out.println("SQL ���� : " + sqlex.getMessage());
					sqlex.printStackTrace();
				}

			}
		}

		public class JoinButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				joins.setVisible(true);
			}
		}
	}

	public class JoinFrame extends JFrame {
		JButton ConfirmButton;

		JTextField user_id;
		JTextField club_name;
		JTextField birth;
		JPasswordField password; // name �ʵ� ���÷��̸� ���� �ڽ�
		Connection conn; // DB ���� Connection ��ü��������
		int user_num;

		public JoinFrame() {
			JPanel JoinPanel = new JPanel(new RiverLayout());

			ConfirmButton = new JButton("ȸ������ �ϱ�");

			user_id = new JTextField(12);
			password = new JPasswordField(12);
			club_name = new JTextField(12);
			birth = new JTextField(12);
			JoinPanel.add("br center", new JLabel("ȸ������ ȭ��"));
			JoinPanel.add("br", new JLabel("���̵�"));
			JoinPanel.add(user_id);
			JoinPanel.add("br", new JLabel("��й�ȣ"));
			JoinPanel.add(password);
			JoinPanel.add("br", new JLabel("Ŭ����"));
			JoinPanel.add(club_name);

			JoinPanel.add("br", ConfirmButton);
			ConfirmButton.addActionListener(new ConfirmButtonListener());

			dbConnectionInit();
			add(JoinPanel);
			setSize(300, 400);
			this.setBounds(700, 300, 300, 400);
			setResizable(false);
			setVisible(false);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		// DB�� �����ϴ� �޼ҵ�
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC����̹��� JVM�������� ��������
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwproj", "root", "dmlrhd"); // DB �����ϱ�
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC ����̹� Ŭ������ ã�� �� �����ϴ� : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB ���� ���� : " + ex.getMessage());
			}
		}

		public class ConfirmButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				int count = 0;
				String user_password = null;
				String field_password;
				String user_ids;
				try {
					Statement stmt = conn.createStatement(); // SQL ���� �ۼ��� ���� Statement ��ü ����
					field_password = password.getText();

					// ���� DB�� �ִ� ���� �����ؼ� ���� ����� names ����Ʈ�� ����ϱ�
					ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE id = '" + user_id.getText() + "'");
					while (rs.next()) {
						user_num = Integer.parseInt(rs.getString("user_id"));
						user_ids = rs.getString("id");
						user_password = rs.getString("password");
						count++;

					}

					rs = stmt.executeQuery("SELECT * FROM user WHERE id = '" + user_id.getText() + "'");
					while (rs.next()) {
						count++;
					}

					if (count > 0) {
						JOptionPane.showMessageDialog(null, "�ߺ��� ���̵�� ����� �� �����ϴ�.", "���", JOptionPane.WARNING_MESSAGE);
						return;
					}
					rs = stmt.executeQuery("SELECT * FROM user WHERE club_name = '" + club_name.getText() + "'");
					while (rs.next()) {
						count++;
					}
					if (count > 0) {
						JOptionPane.showMessageDialog(null, "�ߺ��� Ŭ������ ����� �� �����ϴ�.", "���", JOptionPane.WARNING_MESSAGE);
						return;
					}
					stmt.executeUpdate("INSERT INTO user (id, password, club_name) VALUES ( '" + user_id.getText()
							+ "', " + password.getText() + ", '" + club_name.getText() + "')");

					stmt.close(); // statement�� ����� �ݴ� ����
					joins.setVisible(false);

				} catch (SQLException sqlex) {
					System.out.println("SQL ���� : " + sqlex.getMessage());
					sqlex.printStackTrace();
				}

			}
		}
	}
}
