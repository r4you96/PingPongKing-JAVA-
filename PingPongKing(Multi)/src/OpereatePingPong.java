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

	BallPoint bp; // 공의 현재 좌표

	Random rand; // 여러 변수에 사용할 랜덤

	int player1X = 50; // 플레이어 좌표
	int player2X = 280; // 컴퓨터 좌우
	int p1HitDirection; // 플레이어 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
	int p2HitDirection; // 컴퓨터가 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
	int p1LeftRight; // 플레이어가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
	int p2LeftRight; // 컴퓨터가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
	boolean player1Turn; // 공격 턴 0이면 플레이어 1이면 컴퓨터
	int decision; // 0이면 경기중 1이면 승리 2면 패배
	int bounceX = 160;
	// 점수
	int player1Score; // 플레이어의 점수
	int player2Score; // 컴퓨터의 점수

	int p1ImgSequence = 0; // 플레이어 사진 순서
	int p2ImgSequence = 0; // 컴퓨터 사진 순서
	int bouncingSequence = 6;
	int bouncePoint;
	boolean gameProgress = false;
	JLabel player1Name;
	JLabel player2Name;
	String player1Nickname;
	String player2Nickname;
	// 점수판
	private final String SCORE[] = { "/res/0score.png", "/res/1score.png", "/res/2score.png", "/res/3score.png" };

	// 플레이어 자세
	private final String player1HitMotion[] = { "/res/wait.png", "/res/pHit1.png", "/res/pHit2.png", "/res/pHit3.png",
			"/res/pHit4.png" };

	// 컴퓨터 자세
	private final String player2HitMotion[] = { "/res/waitC.png", "/res/serve1.png", "/res/serve2.png",
			"/res/serve3.png", "/res/waitC.png", "/res/cHit1.png", "/res/cHit2.png", "/res/cHit3.png",
			"/res/cHit4.png" };
	private final String bounceFiles[] = { "/res/bouncing0.png", "/res/bouncing1.png", "/res/bouncing2.png",
			"/res/bouncing3.png", "/res/bouncing4.png" };

	// 플레이어와 컴퓨터 이미지들을 넣어줄 arrayList
	ArrayList<Image> player1Img = new ArrayList<>();
	ArrayList<Image> player2Img = new ArrayList<>();
	ArrayList<Image> bounceArray = new ArrayList<>();

	Timer player1Timer; // 캐릭터 타이머
	Timer player2Timer; // 컴퓨터 케릭터 타이머
	Timer ballT; // 공 타이머
	Connection conn; // DB 연결 Connection 객체참조변수

	// DB를 연결하는 메소드
	private void dbConnectionInit() {
		try {
			Class.forName("com.mysql.jdbc.Driver"); // JDBC드라이버를 JVM영역으로 가져오기
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ccwpingpong", "root", "dmlrhd"); // DB 연결하기
		} catch (ClassNotFoundException cnfe) {
			System.out.println("JDBC 드라이버 클래스를 찾을 수 없습니다 : " + cnfe.getMessage());
		} catch (Exception ex) {
			System.out.println("DB 연결 에러 : " + ex.getMessage());
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
		setLayout(null); // 절대 좌표에 따라 배열 되도록

		rand = new Random(); // 여러 곳에 사용할 랜덤 함수 생성

		// 컴퓨터, 플레이어, 공 타이머 선언
		player1Timer = new Timer(60, new CharacterListener());
		player2Timer = new Timer(60, new ComputerListener());
		ballT = new Timer(50, new BallListener());

		Font font = new Font("궁서", Font.BOLD, 15);
		player1Name = new JLabel("");
		player2Name = new JLabel("");
		player1Name.setBounds(0, 20, 170, 30);
		player2Name.setBounds(0, 60, 170, 30);
		player1Name.setFont(font);
		player2Name.setFont(font);
		add(player1Name);
		add(player2Name);
		// 이미지 객체 선언
		imageLoad();


		bp = new BallPoint(325, 280);

		setSize(550, 1000); // 패널크기 선언

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
		// 플레이어 이미지 배열에 추가
		for (i = 0; i < 5; i++)
			player1Img.add(new ImageIcon(getClass().getResource(player1HitMotion[i])).getImage());
		for (i = 0; i < 9; i++)
			player2Img.add(new ImageIcon(getClass().getResource(player2HitMotion[i])).getImage());
		for (i = 0; i < 5; i++)
			bounceArray.add(new ImageIcon(getClass().getResource(bounceFiles[i])).getImage());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(new Color(255, 203, 57)); // 게임 배경 색깔 칠하기
		g.fillRect(0, 0, 550, 1000);

		g.drawImage(new ImageIcon(getClass().getResource("/res/table.png")).getImage(), 140, 350, this); // 테이블 그려주기

		// 점수판
		g.drawImage(new ImageIcon(getClass().getResource(SCORE[player1Score])).getImage(), 170, 50, this);
		g.drawImage(new ImageIcon(getClass().getResource(SCORE[player2Score])).getImage(), 280, 50, this);

		// 점수판 :
		g.setColor(Color.BLACK);
		g.fillOval(260, 70, 10, 10);
		g.fillOval(260, 100, 10, 10);

		if (decision == 0) { // 경기가 진행중일 시에
			// 케릭터 그려주기
			if (p1ImgSequence < 5 && p1ImgSequence > 0)
				g.drawImage(player1Img.get(p1ImgSequence), player1X, 550, this);
			else if (p1ImgSequence == 5 || p1ImgSequence == 0)
				g.drawImage(player1Img.get(0), player1X, 550, this);
			// 컴퓨터 그려주기
			if (p2ImgSequence < 9 && p2ImgSequence > 0)
				g.drawImage(player2Img.get(p2ImgSequence), player2X, 170, this);
			else if (p2ImgSequence == 9 || p2ImgSequence == 0)
				g.drawImage(player2Img.get(0), player2X, 170, this);
		} else if (decision == 1) { // 승리시 포즈
			// 승리 멘트
			if (player1Score < 3)
				g.drawImage(new ImageIcon(getClass().getResource("/res/p1Point.png")).getImage(), 180, 150, this);
			else
				g.drawImage(new ImageIcon(getClass().getResource("/res/p1Match.png")).getImage(), 180, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/loseC.png")).getImage(), player2X, 150, this);
			g.drawImage(new ImageIcon(getClass().getResource("/res/win.png")).getImage(), player1X, 550, this);
		} else if (decision == 2) { // 패배시 포즈
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

		// 공 그려주긔
		if (decision == 0) {
			g.setColor(Color.BLACK);
			g.fillOval((int) bp.x, (int) bp.y, 10, 10);
		}
		if (!gameProgress) {
			g.setColor(new Color(255, 203, 57)); // 게임 배경 색깔 칠하기
			g.fillRect(0, 0, 550, 1000);
			g.drawImage(new ImageIcon(getClass().getResource("/res/home.gif")).getImage(), 15, 50, this);
			player1Name.setText("");
			player2Name.setText("");
		}
	}

	// 공을 받아 칠 수 있는지 판단해 주는 매소드
	boolean p1HitAvailable() {
		if (bp.y > 550 && bp.y < 705 && player1Turn && p1ImgSequence == 0) { // 공의 y좌표가 500 이상이고, 플레이어가 공을 쳐야하는 턴일때
			if (p2HitDirection == 0 && player1X == 50) { // 컴퓨터가 왼쪽으로 공을 치고, 플레이어가 왼쪽일때
				p1LeftRight = 0; // 플레이어가 왼쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				return true;
			} else if (p2HitDirection == 1 && player1X == 190) { // 컴퓨터가 오른쪽으로 공을 치고, 플레이어가 오른쪽일때
				p1LeftRight = 1; // 플레이어가 오른쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				return true;
			} else // 그 이외의 경우에는 공을 칠수 없음
				return false;
		} else
			return false;
	}

	boolean p2HitAvailable() {
		// 컴퓨터가 공을 치는경우와 못치는 경우
		if (bp.y > 240 && bp.y < 370 && !player1Turn && p2ImgSequence == 4) { // 공의 y좌표가 500 이상이고, 플레이어가 공을 쳐야하는 턴일때
			if (p1HitDirection == 0 && player2X == 140) { // 컴퓨터가 왼쪽으로 공을 치고, 플레이어가 왼쪽일때
				p2LeftRight = 0; // 플레이어가 왼쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				return true;
			} else if (p1HitDirection == 1 && player2X == 280) { // 컴퓨터가 오른쪽으로 공을 치고, 플레이어가 오른쪽일때
				p2LeftRight = 1; // 플레이어가 오른쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				return true;
			} else // 그 이외의 경우에는 공을 칠수 없음
				return false;
		} else
			return false;

	}

	public void enemyHit(int enemy) {
		int enemyX = enemy / 10;
		if (playerType == 1 && decision == 0) {
			if (enemyX <= 225) { // 왼쪽을 클릭한 경우 왼쪽으로 케릭터 이동
				player2X = 140;
			} else { // 오른쪽을 클릭한 경우 오른쪽으로 케릭터 이동
				player2X = 280;
			}
			if (p2HitAvailable()) { // 공을 쳤을때
				if (p2LeftRight == 0) { // 공을 왼쪽에서 쳤을때
					bp.x = 185;
					bp.y = 280;
				} else { // 공을 오른쪽에서 쳤을때
					bp.x = 325;
					bp.y = 280;
				}
				player1Turn = true;
				p2HitDirection = enemy % 10;
			}
			player2Timer.start(); // 케릭터 움직이기
		} else if (playerType == 2 && decision == 0) {
			if (enemyX <= 225) { // 왼쪽을 클릭한 경우 왼쪽으로 케릭터 이동
				player1X = 50;
			} else { // 오른쪽을 클릭한 경우 오른쪽으로 케릭터 이동
				player1X = 190;
			}
			if (p1HitAvailable()) { // 공을 쳤을때
				if (p1LeftRight == 0) { // 공을 왼쪽에서 쳤을때
					bp.x = 195;
					bp.y = 680;
				} else { // 공을 오른쪽에서 쳤을때
					bp.x = 340;
					bp.y = 680;
				}
				player1Turn = false;
				p1HitDirection = enemy % 10;
			}
			player1Timer.start(); // 케릭터 움직이기
		}
		// 승패 결정후 다시 시작
		if (player1Score < 3 && player2Score < 3 && gameProgress) { // 3점이 되서 끝나기 전의 경우
			if (decision == 2 || decision == 1) { // 승,패 결과가 나온 이후의 경우
				// 컴퓨터가 서브
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
			if (player1Turn) { // 컴퓨터가 쳐서 플레이어에게 공이 가는 턴
				if (p2LeftRight == 1) { // 컴퓨터가 오른쪽에서 쳤을때
					if (p2HitDirection == 0) { // 오른쪽에서 왼쪽으로 칠때
						bp.x -= 5;
						bp.y += 15;
						bouncePoint = 2;
					} else { // 오른쪽에서 오른쪽으로 쳤을때
						bp.x += 2.0 / 3.0;
						bp.y += 18;
						bouncePoint = 3;
					}

				} else { // 컴퓨터가 왼쪽에서 쳤을때
					if (p2HitDirection == 0) { // 왼쪽에서 왼쪽으로 칠때
						bp.x += 2.0 / 3.0;
						bp.y += 18;
						bouncePoint = 2;
					} else { // 왼쪽에서 오른쪽으로 칠때
						bp.x += 5;
						bp.y += 15;
						bouncePoint = 3;
					}
				}
				if (bp.y == 505 || bp.y == 514) {
					bouncingSequence = 0;

					bounceX = (int) bp.x;
				}
			} else { // 플레이어가 쳐서 컴퓨터에게 공이 가는 턴
				if (p1HitDirection == 1) { // 플레이어가 오른쪽으로 쳤을때
					if (p1LeftRight == 0) { // 플레이어가 왼쪽에서 친 경우
						bp.x += 5;
						bp.y -= 15;
						bouncePoint = 1;
					} else { // 플레이어가 오른쪽에서 친 경우
						bp.x -= 2.0 / 3.0;
						bp.y -= 18;
						bouncePoint = 1;
					}
				}
				if (p1HitDirection == 0) { // 플레이어 왼쪽으로 쳤을때
					if (p1LeftRight == 0) { // 플레이어가 왼쪽에서 쳤을때
						bp.x -= 2.0 / 3.0;
						bp.y -= 18;
						bouncePoint = 0;
					} else { // 플레이어가 오른쪽으로 쳤을때
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

			// 승리, 패배 경우
			if (bp.y > 750) { // 지는경우
				decision = 2; // 패배 상태로 전환
				ballT.stop(); // 공 멈춤

				System.out.println("졋다");

				if (player2Score < 3) // 컴퓨터 스코어 추가
					player2Score++;
			}
			if (bp.y < 150) { // 이기는 경우
				decision = 1; // 승리 상태로 전환
				ballT.stop(); // 공 멈춤

				if (player1Score < 3) // 플레이어 스코어 추가
					player1Score++;
			}
			repaint();
		}
	}

	// 플레이어 케릭터와 컴퓨터 케릭터의 모션을 그려줄 타이머 리스너
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
			if (p1ImgSequence == 0) { // 게임이 진행중인 경우이고 케릭터가 멈춰진 상태여야만 함
				player1Timer.start(); // 케릭터 움직이기
				if (xPoint <= 225) { // 왼쪽을 클릭한 경우 왼쪽으로 케릭터 이동
					player1X = 50;
				} else { // 오른쪽을 클릭한 경우 오른쪽으로 케릭터 이동
					player1X = 190;
				}
			}

			if (p1HitAvailable()) { // 공을 쳤을때
				if (p1LeftRight == 0) { // 공을 왼쪽에서 쳤을때
					bp.x = 195;
					bp.y = 680;
				} else { // 공을 오른쪽에서 쳤을때
					bp.x = 340;
					bp.y = 680;
				}
				player1Turn = false;
				p1HitDirection = player % 10;
			}
		} else if (playerType == 2 && decision == 0) {
			player2Timer.start(); // 케릭터 움직이기
			if (p2ImgSequence == 4) {
				if (xPoint <= 225) { // 왼쪽을 클릭한 경우 왼쪽으로 케릭터 이동
					player2X = 140;
				} else { // 오른쪽을 클릭한 경우 오른쪽으로 케릭터 이동
					player2X = 280;
				}
			}
			if (p2HitAvailable()) { // 공을 쳤을때
				if (p2LeftRight == 0) { // 공을 왼쪽에서 쳤을때
					bp.x = 185;
					bp.y = 280;
				} else { // 공을 오른쪽에서 쳤을때
					bp.x = 325;
					bp.y = 280;
				}
				player1Turn = true;
				p2HitDirection = player % 10;
			}
		}

		
		// 승패 결정후 다시 시작
		if (player1Score < 3 && player2Score < 3 && gameProgress) { // 3점이 되서 끝나기 전의 경우
			if (decision == 2 || decision == 1) { // 승,패 결과가 나온 이후의 경우
				// 컴퓨터가 서브
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
					Statement stmt = conn.createStatement(); // SQL 문을 작성을 위한 Statement 객체 생성
					// 현재 DB에 있는 내용 추출해서 선수 목록을 names 리스트에 출력하기
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

					stmt.close(); // statement는 사용후 닫는 습관

				} catch (SQLException sqlex) {
					System.out.println("SQL 에러 : " + sqlex.getMessage());
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

		player1X = 50; // 플레이어 좌표
		player2X = 280; // 컴퓨터 좌우
		p2HitDirection = 0; // 컴퓨터가 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
		p1HitDirection = 0; // 플레이어 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
		p1LeftRight = 0; // 플레이어가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
		p2LeftRight = 1; // 컴퓨터가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
		player1Turn = true; // 공격 턴 0이면 플레이어 1이면 컴퓨터
		decision = 0; // 0이면 경기중 1이면 승리 2면 패배

		player1Score = 0; // 플레이어의 점수
		player2Score = 0; // 컴퓨터의 점수

		p1ImgSequence = 0; // 플레이어 사진 순서
		p2ImgSequence = 0; // 컴퓨터 사진 순서

		bp.x = 325;
		bp.y = 280;

		// 게임 시작 타이머 가동
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