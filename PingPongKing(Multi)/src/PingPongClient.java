
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
	String frameTitle = "채팅 클라이언트";
	JTextArea incoming; // 수신된 메시지를 출력하는 곳
	JTextArea outgoing; // 송신할 메시지를 작성하는 곳
	JList counterParts; // 현재 로그인한 채팅 상대목록을 나타내는 리스트.
	ObjectInputStream reader; // 수신용 스트림
	ObjectOutputStream writer; // 송신용 스트림
	Socket sock; // 서버 연결용 소켓
	String user; // 이 클라이언트로 로그인 한 유저의 이름
	String user_idNum;
	JButton logButton; // 토글이 되는 로그인/로그아웃 버튼
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
		frame = new JFrame(frameTitle + " : 로그인하세요");

		// 메시지 디스플레이 창
		incoming = new JTextArea(15, 20);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// 대화 상대 목록. 초기에는 "전체" - ChatMessage.ALL 만 있음
		String[] list = { PingPongMessage.ALL };
		counterParts = new JList(list);
		JScrollPane cScroller = new JScrollPane(counterParts);
		cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		counterParts.setVisibleRowCount(5);
		counterParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		counterParts.setFixedCellWidth(100);
		counterParts.setBackground(new Color(255, 203, 57));
		Font font = new Font("궁서", Font.BOLD, 20);
		counterParts.setFont(font);

		// 메시지 전송을 위한 버튼
		ImageButton gameButton = new ImageButton("src/res/fight.png", new GameButtonListener(), true);
		ImageButton recordButton = new ImageButton("src/res/record.png", new recordButtonListener(), true);
		ImageButton logoutButton = new ImageButton("src/res/logout.png", new LogoutButtonListener(), true);
		gameButton.setBounds(240, 770, 124, 46);
		recordButton.setBounds(240, 840, 124, 46);
		logoutButton.setBounds(240, 910, 124, 46);

		// GUI 배치
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

		// 네트워킹을 시동하고, 서버에서 메시지를 읽을 스레드 구동
		setUpNetworking();
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		mainPanel.setBounds(550, 0, 400, 1000);
		game = new OpereatePingPong();
		game.setBounds(0, 0, 550, 1000);
		game.addMouseListener(new gameMouse());
		// 클라이언드 프레임 창 조정
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

		// 프레임이 살아 있으므로 여기서 만들은 스레드는 계속 진행 됨
		// 이 프레임 스레드를 종료하면, 이 프레임에서 만든 스레드들은 예외를 발생하게되고
		// 이를 이용해 모든 스레드를 안전하게 종료 시키도록 함
	} // close go

	private void setUpNetworking() {
		try {
			 sock = new Socket("220.69.203.88", 5000); // 최철우
			//sock = new Socket("127.0.0.1", 5000); // 소켓 통신을 위한 포트는 5000번 사용키로 함
			reader = new ObjectInputStream(sock.getInputStream());
			writer = new ObjectOutputStream(sock.getOutputStream());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "서버접속에 실패하였습니다. 접속을 종료합니다.");
			ex.printStackTrace();
			frame.dispose(); // 네트워크가 초기 연결 안되면 클라이언트 강제 종료
		}
	} // close setUpNetworking

	// 로그인과 아웃을 담당하는 버튼의 감청자. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout을 처리
	private class LogoutButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			int choice = JOptionPane.showConfirmDialog(null, "Logout합니다");
			if (choice == JOptionPane.YES_OPTION) {
				try {
					writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.LOGOUT, user, "", ""));
					writer.flush();
					// 연결된 모든 스트림과 소켓을 닫고 프로그램을 종료 함
					writer.close();
					reader.close();
					sock.close();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "로그아웃 중 서버접속에 문제가 발생하였습니다. 강제종료합니다");
					ex.printStackTrace();
				} finally {
					System.exit(100); // 클라이언트 완전 종료
				}
			}
		}
	} // close LoginButtonListener inner class

	public class recordButtonListener implements ActionListener {
		String opponent_id = null;
		String queryMan = null;
		Connection conn; // DB 연결 Connection 객체참조변수
		int count = 0;

		public void actionPerformed(ActionEvent ev) {
			int count = 0;
			mainPanel.clearLabel();
			dbConnectionInit();
			String to = (String) counterParts.getSelectedValue();
			try {
				Statement stmt = conn.createStatement(); // SQL 문을 작성을 위한 Statement 객체 생성

				// 현재 DB에 있는 내용 추출해서 선수 목록을 names 리스트에 출력하기
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
						mainPanel.recordLabel[count].setText("對 " +  rs.getString("nickname") + 
								" 전 (승) " + rs.getString("winner_point") + " : " + rs.getString("loser_point") );	
					else if(rs.getString("loser_id").equals(opponent_id))
						mainPanel.recordLabel[count].setText("對 " +  rs.getString("nickname") + 
								" 전 (패) " + rs.getString("loser_point") + " : " + rs.getString("winner_point") );
					count++;
				}

				stmt.close(); // statement는 사용후 닫는 습관

			} catch (SQLException sqlex) {
				System.out.println("SQL 에러 : " + sqlex.getMessage());
				sqlex.printStackTrace();
			}
		}

		// DB를 연결하는 메소드
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC드라이버를 JVM영역으로 가져오기
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB
																													// 연결하기
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC 드라이버 클래스를 찾을 수 없습니다 : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB 연결 에러 : " + ex.getMessage());
			}
		}
	} // close SendButtonListener inner class

	public class GameButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			String to = (String) counterParts.getSelectedValue();
			if (to == null) {
				JOptionPane.showMessageDialog(null, "송신할 대상을 선택한 후 게임을 시작하세요");
				return;
			}
			try {
				incoming.append(user + "와" + to + "경기 시작\n"); // 나의 메시지 창에 보이기
				writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.GAME_START, user, to, ""));
				writer.flush();

				// outgoing.requestFocus();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "게임 시작중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
		}
	} // close SendButtonListener inner class

	// 서버에서 보내는 메시지를 받는 스레드 작업을 정의하는 클래스
	public class IncomingReader implements Runnable {
		public void run() {
			PingPongMessage message;
			PingPongMessage.MsgType type;
			try {
				while (true) {
					message = (PingPongMessage) reader.readObject(); // 서버로기 부터의 메시지 대기
					type = message.getType();
					if (type == PingPongMessage.MsgType.LOGIN_FAILURE) { // 로그인이 실패한 경우라면
						JOptionPane.showMessageDialog(null, "Login이 실패하였습니다. 다시 로그인하세요");
						frame.setTitle(frameTitle + " : 로그인 하세요");
						logButton.setText("Login");
					} else if (type == PingPongMessage.MsgType.SERVER_MSG) { // 메시지를 받았다면 보여줌
						if (message.getSender().equals(user))
							continue; // 내가 보낸 편지면 보일 필요 없음
						incoming.append(message.getSender() + " : " + message.getContents() + "\n");
					} else if (type == PingPongMessage.MsgType.LOGIN_LIST) {
						// 유저 리스트를 추출 해서 counterParts 리스트에 넣어 줌.
						// 나는 빼고 (""로 만들어 정렬 후 리스트 맨 앞에 오게 함)
						String[] users = message.getContents().split("/");
						for (int i = 0; i < users.length; i++) {
							if (user.equals(users[i]))
								users[i] = "";
						}
						users = sortUsers(users); // 유저 목록을 쉽게 볼 수 있도록 정렬해서 제공
						users[0] = PingPongMessage.ALL; // 리스트 맨 앞에 "전체"가 들어가도록 함
						counterParts.setListData(users);
						frame.repaint();
					} else if (type == PingPongMessage.MsgType.GAME_START) {
						String to = message.getSender();
						if (!gameState) {
							gameState = true;
							incoming.append(message.getSender() + "님 에게 게임을 제안받았습니다.\n");
							incoming.append("게임을 시작합니다.\n");

							game.startGame();
							game.playerType = 1;
							writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.GAME_START_RECIEVE, user, to, ""));
							writer.flush();
							game.setPlayerName(user, message.getSender());
						} else {
							incoming.append(message.getSender() + "님 에게 게임을 제안받았습니다.\n");
							incoming.append("게임 중이므로 게임신청을 거절합니다.\n");
						}
					} else if (type == PingPongMessage.MsgType.GAME_START_RECIEVE) {
						String to = message.getSender();
						if (!gameState) {
							gameState = true;
							incoming.append(message.getSender() + "님 에게 게임을 제안했습니다.\n");
							incoming.append("게임을 시작합니다.\n");

							game.startGame();
							game.playerType = 2;
							game.setPlayerName(to, user);
						}
					} else if (type == PingPongMessage.MsgType.NO_ACT) {
						// 아무 액션이 필요없는 메시지. 그냥 스킵
					} else if (type == PingPongMessage.MsgType.HIT) {
						incoming.append(message.getSender() + "상대가 타격" + message.getContents() + "\n");
						int enemy = Integer.parseInt(message.getContents());
						game.enemyHit(enemy);
					} else {
						// 정체가 확인되지 않는 이상한 메시지
						throw new Exception("서버에서 알 수 없는 메시지 도착했음");
					}
				} // close while
			} catch (Exception ex) {
				System.out.println("클라이언트 스레드 종료"); // 프레임이 종료될 경우 이를 통해 스레드 종료
			}
		} // close run

		// 주어진 String 배열을 정렬한 새로운 배열 리턴
		private String[] sortUsers(String[] users) {
			String[] outList = new String[users.length];
			ArrayList<String> list = new ArrayList<String>();
			for (String s : users) {
				list.add(s);
			}
			Collections.sort(list); // Collections.sort를 사용해 한방에 정렬
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
				incoming.append("바운스 위치 x : " + e.getX() + "/ y : " + e.getY() + "\n");
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
		JPasswordField password; // name 필드 디스플레이를 위한 박스
		Connection conn; // DB 연결 Connection 객체참조변수
		int user_num;

		public LoginFrame() {
			JPanel LoginPanel = new JPanel(new RiverLayout());
			LoginButton = new JButton("로그인");
			LoginButton.addActionListener(new LoginButtonListener());
			JoinButton = new JButton("회원가입");
			JoinButton.addActionListener(new JoinButtonListener());

			user_id = new JTextField(12);
			password = new JPasswordField(12);

			LoginPanel.add("br center", new JLabel("로그인 화면"));
			LoginPanel.add("br", new JLabel("아이디"));
			LoginPanel.add(user_id);
			LoginPanel.add("br", new JLabel("비밀번호"));
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

		// DB를 연결하는 메소드
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC드라이버를 JVM영역으로 가져오기
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB
																													// 연결하기
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC 드라이버 클래스를 찾을 수 없습니다 : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB 연결 에러 : " + ex.getMessage());
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
					Statement stmt = conn.createStatement(); // SQL 문을 작성을 위한 Statement 객체 생성
					field_password = password.getText();

					// 현재 DB에 있는 내용 추출해서 선수 목록을 names 리스트에 출력하기
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
						JOptionPane.showMessageDialog(null, "없는 아이디 입니다.", "제목", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (!user_password.equals(field_password)) {
						JOptionPane.showMessageDialog(null, "비밀번호가 틀렸습니다.", "제목", JOptionPane.WARNING_MESSAGE);
						return;
					}
					stmt.close(); // statement는 사용후 닫는 습관

					user = user_nickname;

					try {
						writer.writeObject(new PingPongMessage(PingPongMessage.MsgType.LOGIN, user, "", ""));
						writer.flush();
						frame.setTitle(frameTitle + " (로그인 : " + user_nickname + ")");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "로그인 중 서버접속에 문제가 발생하였습니다.");
						ex.printStackTrace();
					}

					login.setVisible(false);
					frame.setVisible(true);
				} catch (SQLException sqlex) {
					System.out.println("SQL 에러 : " + sqlex.getMessage());
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
		JPasswordField password; // name 필드 디스플레이를 위한 박스
		Connection conn; // DB 연결 Connection 객체참조변수
		int user_num;

		public JoinFrame() {
			JPanel JoinPanel = new JPanel(new RiverLayout());

			ConfirmButton = new JButton("회원가입 하기");

			user_id = new JTextField(12);
			password = new JPasswordField(12);
			club_name = new JTextField(12);
			birth = new JTextField(12);
			JoinPanel.add("br center", new JLabel("회원가입 화면"));
			JoinPanel.add("br", new JLabel("아이디"));
			JoinPanel.add(user_id);
			JoinPanel.add("br", new JLabel("비밀번호"));
			JoinPanel.add(password);
			JoinPanel.add("br", new JLabel("클럽명"));
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

		// DB를 연결하는 메소드
		private void dbConnectionInit() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // JDBC드라이버를 JVM영역으로 가져오기
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwproj", "root", "dmlrhd"); // DB 연결하기
			} catch (ClassNotFoundException cnfe) {
				System.out.println("JDBC 드라이버 클래스를 찾을 수 없습니다 : " + cnfe.getMessage());
			} catch (Exception ex) {
				System.out.println("DB 연결 에러 : " + ex.getMessage());
			}
		}

		public class ConfirmButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				int count = 0;
				String user_password = null;
				String field_password;
				String user_ids;
				try {
					Statement stmt = conn.createStatement(); // SQL 문을 작성을 위한 Statement 객체 생성
					field_password = password.getText();

					// 현재 DB에 있는 내용 추출해서 선수 목록을 names 리스트에 출력하기
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
						JOptionPane.showMessageDialog(null, "중복된 아이디는 사용할 수 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
						return;
					}
					rs = stmt.executeQuery("SELECT * FROM user WHERE club_name = '" + club_name.getText() + "'");
					while (rs.next()) {
						count++;
					}
					if (count > 0) {
						JOptionPane.showMessageDialog(null, "중복된 클럽명은 사용할 수 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
						return;
					}
					stmt.executeUpdate("INSERT INTO user (id, password, club_name) VALUES ( '" + user_id.getText()
							+ "', " + password.getText() + ", '" + club_name.getText() + "')");

					stmt.close(); // statement는 사용후 닫는 습관
					joins.setVisible(false);

				} catch (SQLException sqlex) {
					System.out.println("SQL 에러 : " + sqlex.getMessage());
					sqlex.printStackTrace();
				}

			}
		}
	}
}
