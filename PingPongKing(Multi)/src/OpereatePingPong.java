import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.Timer;

public class OpereatePingPong extends JPanel {
	int playerType = 2;

	BallPoint bp; // ���� ���� ��ǥ

	Random rand; // ���� ������ ����� ����

	int player1X = 50; // �÷��̾� ��ǥ
	int player2X = 280; // ��ǻ�� �¿�
	int p1HitDirection; // �÷��̾� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
	int p2HitDirection; // ��ǻ�Ͱ� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
	int p1LeftRight; // �÷��̾ ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
	int p2LeftRight; // ��ǻ�Ͱ� ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
	boolean player1Turn; // ���� �� 0�̸� �÷��̾� 1�̸� ��ǻ��
	int decision; // 0�̸� ����� 1�̸� �¸� 2�� �й�
	int bounceX = 160;
	// ����
	int player1Score; // �÷��̾��� ����
	int player2Score; // ��ǻ���� ����

	int p1ImgSequence = 0; // �÷��̾� ���� ����
	int p2ImgSequence = 0; // ��ǻ�� ���� ����
	int bouncingSequence = 6;
	int bouncePoint;
	boolean gameProgress = false;
	JLabel player1Name;
	JLabel player2Name;
	String player1Nickname;
	String player2Nickname;
	// ������
	private final String SCORE[] = { "/res/0score.png", "/res/1score.png", "/res/2score.png", "/res/3score.png" };

	// �÷��̾� �ڼ�
	private final String player1HitMotion[] = { "/res/wait.png", "/res/pHit1.png", "/res/pHit2.png", "/res/pHit3.png",
			"/res/pHit4.png" };

	// ��ǻ�� �ڼ�
	private final String player2HitMotion[] = { "/res/waitC.png", "/res/serve1.png", "/res/serve2.png",
			"/res/serve3.png", "/res/waitC.png", "/res/cHit1.png", "/res/cHit2.png", "/res/cHit3.png",
			"/res/cHit4.png" };
	private final String bounceFiles[] = { "/res/bouncing0.png", "/res/bouncing1.png", "/res/bouncing2.png",
			"/res/bouncing3.png", "/res/bouncing4.png" };

	// �÷��̾�� ��ǻ�� �̹������� �־��� arrayList
	ArrayList<Image> player1Img = new ArrayList<>();
	ArrayList<Image> player2Img = new ArrayList<>();
	ArrayList<Image> bounceArray = new ArrayList<>();

	Timer player1Timer; // ĳ���� Ÿ�̸�
	Timer player2Timer; // ��ǻ�� �ɸ��� Ÿ�̸�
	Timer ballT; // �� Ÿ�̸�
	Connection conn; // DB ���� Connection ��ü��������

	// DB�� �����ϴ� �޼ҵ�
	private void dbConnectionInit() {
		try {
			Class.forName("com.mysql.jdbc.Driver"); // JDBC����̹��� JVM�������� ��������
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB �����ϱ�
		} catch (ClassNotFoundException cnfe) {
			System.out.println("JDBC ����̹� Ŭ������ ã�� �� �����ϴ� : " + cnfe.getMessage());
		} catch (Exception ex) {
			System.out.println("DB ���� ���� : " + ex.getMessage());
		}
	}

	public class BallPoint {
		double x, y;

		public BallPoint(double xp, double yp) {
			x = xp;
			y = yp;
		}
	}

	public OpereatePingPong() {
		setBounds(0, 0, 550, 1000);
		setLayout(null); // ���� ��ǥ�� ���� �迭 �ǵ���

		rand = new Random(); // ���� ���� ����� ���� �Լ� ����

		// ��ǻ��, �÷��̾�, �� Ÿ�̸� ����
		player1Timer = new Timer(60, new CharacterListener());
		player2Timer = new Timer(60, new ComputerListener());
		ballT = new Timer(50, new BallListener());

		Font font = new Font("�ü�", Font.BOLD, 15);
		player1Name = new JLabel("");
		player2Name = new JLabel("");
		player1Name.setBounds(0, 20, 170, 30);
		player2Name.setBounds(0, 60, 170, 30);
		player1Name.setFont(font);
		player2Name.setFont(font);
		add(player1Name);
		add(player2Name);
		// �̹��� ��ü ����
		imageLoad();


		bp = new BallPoint(325, 280);

		setSize(550, 1000); // �г�ũ�� ����

		dbConnectionInit();
	}

	public void setPlayerName(String player1, String player2) {
		player1Name.setText("1P : " + player1);
		player2Name.setText("2P : " + player2);
		player1Nickname = player1;
		player2Nickname = player2;
	}

	void imageLoad() {
		int i;
		// �÷��̾� �̹��� �迭�� �߰�
		for (i = 0; i < 5; i++)
			player1Img.add(new ImageIcon(getClass().getResource(player1HitMotion[i])).getImage());
		for (i = 0; i < 9; i++)
			player2Img.add(new ImageIcon(getClass().getResource(player2HitMotion[i])).getImage());
		for (i = 0; i < 5; i++)
			bounceArray.add(new ImageIcon(getClass().getResource(bounceFiles[i])).getImage());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(new Color(255, 203, 57)); // ���� ��� ���� ĥ�ϱ�
		g.fillRect(0, 0, 550, 1000);

		g.drawImage(new ImageIcon(getClass().getResource("/res/table.png")).getImage(), 140, 350, this); // ���̺� �׷��ֱ�

		// ������
		g.drawImage(new ImageIcon(getClass().getResource(SCORE[player1Score])).getImage(), 170, 50, this);
		g.drawImage(new ImageIcon(getClass().getResource(SCORE[player2Score])).getImage(), 280, 50, this);

		// ������ :
		g.setColor(Color.BLACK);
		g.fillOval(260, 70, 10, 10);
		g.fillOval(260, 100, 10, 10);

		if (decision == 0) { // ��Ⱑ �������� �ÿ�
			// �ɸ��� �׷��ֱ�
			if (p1ImgSequence < 5 && p1ImgSequence > 0)
				g.drawImage(player1Img.get(p1ImgSequence), player1X, 550, this);
			else if (p1ImgSequence == 5 || p1ImgSequence == 0)
				g.drawImage(player1Img.get(0), player1X, 550, this);
			// ��ǻ�� �׷��ֱ�
			if (p2ImgSequence < 9 && p2ImgSequence > 0)
				g.drawImage(player2Img.get(p2ImgSequence), player2X, 170, this);
			else if (p2ImgSequence == 9 || p2ImgSequence == 0)
				g.drawImage(player2Img.get(0), player2X, 170, this);
		} else if (decision == 1) { // �¸��� ����
			// �¸� ��Ʈ
			if (player1Score < 3)
				g.drawImage(new ImageIcon(getClass().getResource("/res/p1Point.png")).getImage(), 180, 150, this);
			else
				g.drawImage(new ImageIcon(getClass().getResource("/res/p1Match.png")).getImage(), 180, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/loseC.png")).getImage(), player2X, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/win.png")).getImage(), player1X, 550, this);
		} else if (decision == 2) { // �й�� ����
			if (player2Score < 3)
				g.drawImage(new ImageIcon(getClass().getResource("/res/p2Point.png")).getImage(), 180, 150, this);
			else
				g.drawImage(new ImageIcon(getClass().getResource("/res/p2Match.png")).getImage(), 180, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/winC.png")).getImage(), player2X, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/lose.png")).getImage(), player1X, 550, this);
		}
		if (bouncingSequence < 5) {
			switch (bouncePoint) {
			case 0:
				g.drawImage(bounceArray.get(bouncingSequence), bounceX - 20, 370, this);
				break;
			case 1:
				g.drawImage(bounceArray.get(bouncingSequence), bounceX - 20, 370, this);
				break;
			case 2:
				g.drawImage(bounceArray.get(bouncingSequence), bounceX - 20, 490, this);
				break;
			case 3:
				g.drawImage(bounceArray.get(bouncingSequence), bounceX - 20, 490, this);
				break;
			}
		}

		// �� �׷��ֱ�
		if (decision == 0) {
			g.setColor(Color.BLACK);
			g.fillOval((int) bp.x, (int) bp.y, 10, 10);
		}
		if (!gameProgress) {
			g.setColor(new Color(255, 203, 57)); // ���� ��� ���� ĥ�ϱ�
			g.fillRect(0, 0, 550, 1000);
			g.drawImage(new ImageIcon(getClass().getResource("/res/home.gif")).getImage(), 15, 50, this);
			player1Name.setText("");
			player2Name.setText("");
		}
	}

	// ���� �޾� ĥ �� �ִ��� �Ǵ��� �ִ� �żҵ�
	boolean p1HitAvailable() {
		if (bp.y > 550 && bp.y < 705 && player1Turn && p1ImgSequence == 0) { // ���� y��ǥ�� 500 �̻��̰�, �÷��̾ ���� �ľ��ϴ� ���϶�
			if (p2HitDirection == 0 && player1X == 50) { // ��ǻ�Ͱ� �������� ���� ġ��, �÷��̾ �����϶�
				p1LeftRight = 0; // �÷��̾ ���ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				return true;
			} else if (p2HitDirection == 1 && player1X == 190) { // ��ǻ�Ͱ� ���������� ���� ġ��, �÷��̾ �������϶�
				p1LeftRight = 1; // �÷��̾ �����ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				return true;
			} else // �� �̿��� ��쿡�� ���� ĥ�� ����
				return false;
		} else
			return false;
	}

	boolean p2HitAvailable() {
		// ��ǻ�Ͱ� ���� ġ�°��� ��ġ�� ���
		if (bp.y > 240 && bp.y < 370 && !player1Turn && p2ImgSequence == 4) { // ���� y��ǥ�� 500 �̻��̰�, �÷��̾ ���� �ľ��ϴ� ���϶�
			if (p1HitDirection == 0 && player2X == 140) { // ��ǻ�Ͱ� �������� ���� ġ��, �÷��̾ �����϶�
				p2LeftRight = 0; // �÷��̾ ���ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				return true;
			} else if (p1HitDirection == 1 && player2X == 280) { // ��ǻ�Ͱ� ���������� ���� ġ��, �÷��̾ �������϶�
				p2LeftRight = 1; // �÷��̾ �����ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				return true;
			} else // �� �̿��� ��쿡�� ���� ĥ�� ����
				return false;
		} else
			return false;

	}

	public void enemyHit(int enemy) {
		int enemyX = enemy / 10;
		if (playerType == 1 && decision == 0) {
			if (enemyX <= 225) { // ������ Ŭ���� ��� �������� �ɸ��� �̵�
				player2X = 140;
			} else { // �������� Ŭ���� ��� ���������� �ɸ��� �̵�
				player2X = 280;
			}
			if (p2HitAvailable()) { // ���� ������
				if (p2LeftRight == 0) { // ���� ���ʿ��� ������
					bp.x = 185;
					bp.y = 280;
				} else { // ���� �����ʿ��� ������
					bp.x = 325;
					bp.y = 280;
				}
				player1Turn = true;
				p2HitDirection = enemy % 10;
			}
			player2Timer.start(); // �ɸ��� �����̱�
		} else if (playerType == 2 && decision == 0) {
			if (enemyX <= 225) { // ������ Ŭ���� ��� �������� �ɸ��� �̵�
				player1X = 50;
			} else { // �������� Ŭ���� ��� ���������� �ɸ��� �̵�
				player1X = 190;
			}
			if (p1HitAvailable()) { // ���� ������
				if (p1LeftRight == 0) { // ���� ���ʿ��� ������
					bp.x = 195;
					bp.y = 680;
				} else { // ���� �����ʿ��� ������
					bp.x = 340;
					bp.y = 680;
				}
				player1Turn = false;
				p1HitDirection = enemy % 10;
			}
			player1Timer.start(); // �ɸ��� �����̱�
		}
		// ���� ������ �ٽ� ����
		if (player1Score < 3 && player2Score < 3 && gameProgress) { // 3���� �Ǽ� ������ ���� ���
			if (decision == 2 || decision == 1) { // ��,�� ����� ���� ������ ���
				// ��ǻ�Ͱ� ����
				p2HitDirection = 0;
				p2LeftRight = 1;
				bp.x = 325;
				bp.y = 280;
				player1Turn = true;

				decision = 0;

				p2ImgSequence = 0;
				player1X = 50;
				player2X = 280;

				player2Timer.start();
				ballT.start();
			}
		}
	}

	public class BallListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (player1Turn) { // ��ǻ�Ͱ� �ļ� �÷��̾�� ���� ���� ��
				if (p2LeftRight == 1) { // ��ǻ�Ͱ� �����ʿ��� ������
					if (p2HitDirection == 0) { // �����ʿ��� �������� ĥ��
						bp.x -= 5;
						bp.y += 15;
						bouncePoint = 2;
					} else { // �����ʿ��� ���������� ������
						bp.x += 2.0 / 3.0;
						bp.y += 18;
						bouncePoint = 3;
					}

				} else { // ��ǻ�Ͱ� ���ʿ��� ������
					if (p2HitDirection == 0) { // ���ʿ��� �������� ĥ��
						bp.x += 2.0 / 3.0;
						bp.y += 18;
						bouncePoint = 2;
					} else { // ���ʿ��� ���������� ĥ��
						bp.x += 5;
						bp.y += 15;
						bouncePoint = 3;
					}
				}
				if (bp.y == 505 || bp.y == 514) {
					bouncingSequence = 0;

					bounceX = (int) bp.x;
				}
			} else { // �÷��̾ �ļ� ��ǻ�Ϳ��� ���� ���� ��
				if (p1HitDirection == 1) { // �÷��̾ ���������� ������
					if (p1LeftRight == 0) { // �÷��̾ ���ʿ��� ģ ���
						bp.x += 5;
						bp.y -= 15;
						bouncePoint = 1;
					} else { // �÷��̾ �����ʿ��� ģ ���
						bp.x -= 2.0 / 3.0;
						bp.y -= 18;
						bouncePoint = 1;
					}
				}
				if (p1HitDirection == 0) { // �÷��̾� �������� ������
					if (p1LeftRight == 0) { // �÷��̾ ���ʿ��� ������
						bp.x -= 2.0 / 3.0;
						bp.y -= 18;
						bouncePoint = 0;
					} else { // �÷��̾ ���������� ������
						bp.x -= 5;
						bp.y -= 15;
						bouncePoint = 0;
					}
				}
				if (bp.y == 374 || bp.y == 380) {
					bouncingSequence = 0;

					bounceX = (int) bp.x;
				}
			}
			if (bouncingSequence < 6)
				bouncingSequence++;

			// �¸�, �й� ���
			if (bp.y > 750) { // ���°��
				decision = 2; // �й� ���·� ��ȯ
				ballT.stop(); // �� ����

				System.out.println("����");

				if (player2Score < 3) // ��ǻ�� ���ھ� �߰�
					player2Score++;
			}
			if (bp.y < 150) { // �̱�� ���
				decision = 1; // �¸� ���·� ��ȯ
				ballT.stop(); // �� ����

				if (player1Score < 3) // �÷��̾� ���ھ� �߰�
					player1Score++;
			}
			repaint();
		}
	}

	// �÷��̾� �ɸ��Ϳ� ��ǻ�� �ɸ����� ����� �׷��� Ÿ�̸� ������
	public class CharacterListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (p1ImgSequence < 5)
				p1ImgSequence++;

			else {
				p1ImgSequence = 0;
				player1Timer.stop();
			}
			repaint();
		}
	}

	public class ComputerListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if (p2ImgSequence < 9)
				p2ImgSequence++;
			else {
				p2ImgSequence = 4;
				player2Timer.stop();
			}
			repaint();
		}
	}

	public void pressed(int player) {
		int xPoint = player / 10;
		if (playerType == 1 && decision == 0) {
			if (p1ImgSequence == 0) { // ������ �������� ����̰� �ɸ��Ͱ� ������ ���¿��߸� ��
				player1Timer.start(); // �ɸ��� �����̱�
				if (xPoint <= 225) { // ������ Ŭ���� ��� �������� �ɸ��� �̵�
					player1X = 50;
				} else { // �������� Ŭ���� ��� ���������� �ɸ��� �̵�
					player1X = 190;
				}
			}

			if (p1HitAvailable()) { // ���� ������
				if (p1LeftRight == 0) { // ���� ���ʿ��� ������
					bp.x = 195;
					bp.y = 680;
				} else { // ���� �����ʿ��� ������
					bp.x = 340;
					bp.y = 680;
				}
				player1Turn = false;
				p1HitDirection = player % 10;
			}
		} else if (playerType == 2 && decision == 0) {
			player2Timer.start(); // �ɸ��� �����̱�
			if (p2ImgSequence == 4) {
				if (xPoint <= 225) { // ������ Ŭ���� ��� �������� �ɸ��� �̵�
					player2X = 140;
				} else { // �������� Ŭ���� ��� ���������� �ɸ��� �̵�
					player2X = 280;
				}
			}
			if (p2HitAvailable()) { // ���� ������
				if (p2LeftRight == 0) { // ���� ���ʿ��� ������
					bp.x = 185;
					bp.y = 280;
				} else { // ���� �����ʿ��� ������
					bp.x = 325;
					bp.y = 280;
				}
				player1Turn = true;
				p2HitDirection = player % 10;
			}
		}

		
		// ���� ������ �ٽ� ����
		if (player1Score < 3 && player2Score < 3 && gameProgress) { // 3���� �Ǽ� ������ ���� ���
			if (decision == 2 || decision == 1) { // ��,�� ����� ���� ������ ���
				// ��ǻ�Ͱ� ����
				p2HitDirection = 0;
				p2LeftRight = 1;
				bp.x = 325;
				bp.y = 280;
				player1Turn = true;

				decision = 0;

				p2ImgSequence = 0;
				player1X = 50;
				player2X = 280;

				player2Timer.start();
				ballT.start();
			}
		}

		if (player1Score == 3 || player2Score == 3) {
			String query1man = null;
			String query2man = null;
			int p1_id = 0, p2_id = 0, winner_id = 0, loser_id = 0, winner_point = 0, loser_point = 0;
			if (playerType == 1) {
				try {
					Statement stmt = conn.createStatement(); // SQL ���� �ۼ��� ���� Statement ��ü ����
					// ���� DB�� �ִ� ���� �����ؼ� ���� ����� names ����Ʈ�� ����ϱ�
					query1man = "SELECT user_id FROM user WHERE nickname = '" + player1Nickname + "'";
					query2man = "SELECT user_id FROM user WHERE nickname = '" + player2Nickname + "'";

					ResultSet rs = stmt.executeQuery(query1man);
					while (rs.next()) {
						p1_id = Integer.parseInt(rs.getString("user_id"));
					}
					rs = stmt.executeQuery(query2man);
					while (rs.next()) {
						p2_id = Integer.parseInt(rs.getString("user_id"));
					}

					if (player1Score == 3) {
						winner_id = p1_id;
						loser_id = p2_id;
						winner_point = 3;
						loser_point = player2Score;
					} else {
						winner_id = p2_id;
						loser_id = p1_id;
						winner_point = 3;
						loser_point = player1Score;
					}

					stmt.executeUpdate("INSERT INTO record (winner_id, loser_id, winner_point, loser_point) VALUES ("
							+ winner_id + "," + loser_id + "," + winner_point + "," + loser_point + ")");

					stmt.close(); // statement�� ����� �ݴ� ����

				} catch (SQLException sqlex) {
					System.out.println("SQL ���� : " + sqlex.getMessage());
					sqlex.printStackTrace();
				}

			}

			player1Score = 0;
			player2Score = 0;
			gameProgress = false;
		}

		repaint();

	}


	

	public void startGame() {
		player1Timer.stop();
		player2Timer.stop();

		player1X = 50; // �÷��̾� ��ǥ
		player2X = 280; // ��ǻ�� �¿�
		p2HitDirection = 0; // ��ǻ�Ͱ� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
		p1HitDirection = 0; // �÷��̾� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
		p1LeftRight = 0; // �÷��̾ ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
		p2LeftRight = 1; // ��ǻ�Ͱ� ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
		player1Turn = true; // ���� �� 0�̸� �÷��̾� 1�̸� ��ǻ��
		decision = 0; // 0�̸� ����� 1�̸� �¸� 2�� �й�

		player1Score = 0; // �÷��̾��� ����
		player2Score = 0; // ��ǻ���� ����

		p1ImgSequence = 0; // �÷��̾� ���� ����
		p2ImgSequence = 0; // ��ǻ�� ���� ����

		bp.x = 325;
		bp.y = 280;

		// ���� ���� Ÿ�̸� ����
		ballT.start();
		player2Timer.start();

		gameProgress = true;
		requestFocus();
		setFocusable(true);
	}

	public void pauseGame() {
		ballT.stop();
		player2Timer.stop();
		player1Timer.stop();

	}

	public void resumeGame() {
		if (decision == 0)
			ballT.start();

		if (p2ImgSequence != 4)
			player2Timer.start();
		if (p1ImgSequence != 0)
			player1Timer.start();
	}
}