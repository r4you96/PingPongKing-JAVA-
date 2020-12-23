import java.awt.event.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.Timer;

public class PingPongS extends JFrame {

	GamePanel gamePanel; //게임 패널
	HomePanel homePanel; //홈 화면 패널
	JLayeredPane lp = new JLayeredPane();
	JButton startButton; //홈 패널에서 게임 패널로 들어가는 버튼
	JButton restartButton; //pause버튼 누르고 게임을 다시 시작하는 버튼
	JButton pauseButton; //게임을 정지하고 resume버튼과 restart버튼을 활성화 시킴
	JButton resumeButton; //게임을 이어서 하는 버튼
	
	Point bp; //공의 현재 좌표

	Random rand; //여러 변수에 사용할 랜덤
	
	int count = 0; //공 움직임이 너무 끊겨서 움직이지 않도록 활용할 변수
	int playerX = 50; //플레이어 좌표
	int computerX = 280; //컴퓨터 좌우
	int hitC = 0; //컴퓨터가 공을 칠 확률
	int cHitDirection; //컴퓨터가 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
	int pHitDirection; //플레이어 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
	int pLeftRight; //플레이어가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
	int cLeftRight; //컴퓨터가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
	int turn; //공격 턴 0이면 플레이어 1이면 컴퓨터
	int decision; //0이면  경기중 1이면 승리 2면 패배
	
	//스매시 관련 
	int playersmash = 0; // 0이면 보통 상태 1이면 스매쉬 상태
	int smashGauge = 0; // 360이면 스매쉬 사용 가능
	
	//점수
	int playerScore; //플레이어의 점수
	int computerScore; //컴퓨터의 점수
	
	int pImgSequence = 0; // 플레이어 사진 순서
	int cImgSequence = 0; // 컴퓨터 사진 순서
	
	//시작화면
	private final String homePic = "/res/home.gif";
	Image home = new ImageIcon(getClass().getResource(homePic)).getImage();
	
	//패배, 승리 멘트
	private final String LOSE_P = "/res/Lpoint.png"; //포인트 실점
	Image loseP = new ImageIcon(getClass().getResource(LOSE_P)).getImage();
	private final String LOSE_M = "/res/Lmatch.png"; //경기 패배
	Image loseM = new ImageIcon(getClass().getResource(LOSE_M)).getImage();
	private final String WIN_P = "/res/Wpoint.png"; //포인트 득점
	Image winP = new ImageIcon(getClass().getResource(WIN_P)).getImage();
	private final String WIN_M = "/res/Wmatch.png"; //경기 승리
	Image winM = new ImageIcon(getClass().getResource(WIN_M)).getImage();
	
	//배경 요소 이미지
	private final String TABLE_PIC = "/res/table.png"; //핑퐁 테이블
	Image table = new ImageIcon(getClass().getResource(TABLE_PIC)).getImage();
	
	//점수판
	private final String SCORE_0 = "/res/0score.png";
	Image score0 = new ImageIcon(getClass().getResource(SCORE_0)).getImage();
	private final String SCORE_1 = "/res/1score.png";
	Image score1 = new ImageIcon(getClass().getResource(SCORE_1)).getImage();
	private final String SCORE_2 = "/res/2score.png";
	Image score2 = new ImageIcon(getClass().getResource(SCORE_2)).getImage();
	private final String SCORE_3 = "/res/3score.png";
	Image score3 = new ImageIcon(getClass().getResource(SCORE_3)).getImage();
	
	//플레이어 대기 자세
	private final String PLAYER_PIC = "/res/wait.png";
	Image player = new ImageIcon(getClass().getResource(PLAYER_PIC)).getImage();
	
	//스매쉬 관련
	private final String FLAME_PIC = "/res/flame.gif";
	Image FLAME = new ImageIcon(getClass().getResource(FLAME_PIC)).getImage();
	private final String GAUGE_PIC = "/res/gauge.png";
	Image GAUGE = new ImageIcon(getClass().getResource(GAUGE_PIC)).getImage();
	
	//승리, 패배 자세
	private final String PLAYER_WIN = "/res/win.png"; 
	Image playerWin = new ImageIcon(getClass().getResource(PLAYER_WIN)).getImage();
	private final String PLAYER_LOSE = "/res/lose.png";
	Image playerLose = new ImageIcon(getClass().getResource(PLAYER_LOSE)).getImage();
	
	 //공 치는 자세 이미지 객체 생성
	private final String pHit_1 = "/res/pHit1.png";
	private final String pHit_2 = "/res/pHit2.png";
	private final String pHit_3 = "/res/pHit3.png";
	private final String pHit_4 = "/res/pHit4.png";
	
	Image pHit1 = new ImageIcon(getClass().getResource(pHit_1)).getImage();
	Image pHit2 = new ImageIcon(getClass().getResource(pHit_2)).getImage();
	Image pHit3 = new ImageIcon(getClass().getResource(pHit_3)).getImage();
	Image pHit4 = new ImageIcon(getClass().getResource(pHit_4)).getImage();
	
	//컴퓨터 대기 자세, 공 치는 자세 이미지 객체 생성
	//대기
	private final String COMPUTER_PIC = "/res/waitC.png";
	Image computer = new ImageIcon(getClass().getResource(COMPUTER_PIC)).getImage();
	
	//승리, 패배
	private final String COMPUTER_WIN = "/res/winC.png";
	Image computerWin = new ImageIcon(getClass().getResource(COMPUTER_WIN)).getImage();
	private final String COMPUTER_LOSE = "/res/loseC.png";
	Image computerLose = new ImageIcon(getClass().getResource(COMPUTER_LOSE)).getImage();
	
	//서브
	private final String cServe_1 = "/res/serve1.png";
	private final String cServe_2 = "/res/serve2.png";
	private final String cServe_3 = "/res/serve3.png";
	
	Image cServe1 = new ImageIcon(getClass().getResource(cServe_1)).getImage();
	Image cServe2 = new ImageIcon(getClass().getResource(cServe_2)).getImage();
	Image cServe3 = new ImageIcon(getClass().getResource(cServe_3)).getImage();
	
	//타격
	private final String cHit_1 = "/res/cHit1.png";
	private final String cHit_2 = "/res/cHit2.png";
	private final String cHit_3 = "/res/cHit3.png";
	private final String cHit_4 = "/res/cHit4.png";
	
	Image cHit1 = new ImageIcon(getClass().getResource(cHit_1)).getImage();
	Image cHit2 = new ImageIcon(getClass().getResource(cHit_2)).getImage();
	Image cHit3 = new ImageIcon(getClass().getResource(cHit_3)).getImage();
	Image cHit4 = new ImageIcon(getClass().getResource(cHit_4)).getImage();
	
	//플레이어와 컴퓨터 이미지들을 넣어줄 배열
	ArrayList<Image> playerImg = new ArrayList<>(); 
	ArrayList<Image> computerImg = new ArrayList<>();
	
	Timer characterT; //캐릭터 타이머
	Timer computerT; //컴퓨터 케릭터 타이머
	Timer ballT;	//공 타이머
	
	smashListener smashListener = new smashListener();

	public static void main(String args[]) {
		new PingPongS().go();
	}
	
	void go() {
		//
		gamePanel = new GamePanel();
		gamePanel.setBounds(0,0,550,1000);
		gamePanel.setLayout(null); //절대 좌표에 따라 배열 되도록
		
		
		homePanel = new HomePanel();
		homePanel.setBounds(0,0,550,1000);
		homePanel.setLayout(null); //절대 좌표에 따라 배열 되도록
		
		startButton = new JButton(new ImageIcon("src/res/start.png"));
		StartListener startListen = new StartListener();
		startButton.addActionListener(startListen);
		startButton.setBorderPainted(false); //테두리 없애 주기
		
		restartButton = new JButton(new ImageIcon("src/res/restart.png"));
		restartButton.setBorderPainted(false);
		restartButton.setVisible(false);
		RestartListener restartListen = new RestartListener();
		restartButton.addActionListener(restartListen);
		
		pauseButton = new JButton();
		pauseButton.setBorderPainted(false);
		pauseButton.setIcon(new ImageIcon("src/res/pause.png"));
		PauseListener pauseListen = new PauseListener();
		pauseButton.addActionListener(pauseListen);
		
		resumeButton = new JButton();
		resumeButton.setBorderPainted(false);
		resumeButton.setVisible(false);
		resumeButton.setIcon(new ImageIcon("src/res/resumee.png"));
		ResumeListener resumeListen = new ResumeListener();
		resumeButton.addActionListener(resumeListen);
		
		homePanel.add(startButton);
		startButton.setBounds(199, 700, 153, 169);
		
		gamePanel.add(pauseButton);
		pauseButton.setBounds(25, 25, 49, 53);
		
		gamePanel.add(restartButton);
		restartButton.setBounds(280, 800, 255, 148);
		gamePanel.add(resumeButton);
		resumeButton.setBounds(5, 800, 255, 148);
		
		rand = new Random(); //여러 곳에 사용할 랜덤 함수 생성
		
		lp.add(homePanel, new Integer(2));
		lp.add(gamePanel, new Integer(1));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //프레임에 x버튼 생성
		setLayout(new BorderLayout()); // 프레임에 레이아웃 넣어주기
		add(lp, BorderLayout.CENTER); //프레임에 게임 패널 추가
		
		//리스너 선언
		BallListener ballListen = new BallListener();
		CharacterListener charcaterListen = new CharacterListener();
		ComputerListener computerListen = new ComputerListener();
		
		//컴퓨터, 플레이어, 공 타이머 선언
		characterT = new Timer(60, charcaterListen);
		computerT = new Timer(60, computerListen);
		ballT = new Timer(50, ballListen);
		
		//이미지 객체 선언
		imageLoad();
		
		bp = new Point(325,280);
		
		setSize(550,1000); //프레임 크기 선언
		setVisible(true); //프레임 보여주기
		setResizable(false);
	}
	
	void imageLoad() {
		//플레이어 이미지 배열에 추가
		playerImg.add(player);
		playerImg.add(pHit1);
		playerImg.add(pHit2);
		playerImg.add(pHit3);
		playerImg.add(pHit4);
		
		computerImg.add(computer);
		computerImg.add(cServe1);
		computerImg.add(cServe2);
		computerImg.add(cServe3);
		computerImg.add(computer);
		computerImg.add(cHit1);
		computerImg.add(cHit2);
		computerImg.add(cHit3);
		computerImg.add(cHit4);
	}
	
	
	//공을 받아 칠 수 있는지 판단해 주는 매소드
	boolean hitAvailable() {
		
		if(bp.y>550 &&bp.y<705 && turn == 0 && pImgSequence == 0) { //공의 y좌표가 500 이상이고, 플레이어가 공을 쳐야하는 턴일때
			if(cHitDirection == 0 && playerX == 50) { //컴퓨터가 왼쪽으로 공을 치고, 플레이어가 왼쪽일때
				ballT.setDelay(rand.nextInt(30)+30);
				
				hitC = rand.nextInt(10); //컴퓨터가 공을 칠 확률을 넣어줌
				cHitDirection = rand.nextInt(2); //컴퓨터가 공을 받고 어느 방향으로 칠지 정해줌
				pHitDirection = rand.nextInt(2); //플레이어가 쳤을때 어느 방향으로 갈지 정해줌
				pLeftRight = 0; //플레이어가 왼쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				
				if(playersmash == 0) {
					if(smashGauge <360)
						smashGauge += 90;
					
					}
				else {
					hitC = 10;
					ballT.setDelay(15);
				}
				
				return true; 
			}
			else if(cHitDirection == 1 && playerX == 190) { //컴퓨터가 오른쪽으로 공을 치고, 플레이어가 오른쪽일때
				ballT.setDelay(rand.nextInt(30)+21);
				
				hitC = rand.nextInt(10); 
				cHitDirection = rand.nextInt(2);
				pHitDirection = rand.nextInt(2);
				pLeftRight = 1; //플레이어가 오른쪽에서 쳤기때문에 왼쪽에서 친 상태임을 정의해줌
				
				if(playersmash == 0) {
					if(smashGauge <360)
						smashGauge += 90;
					
					}
				else {
					hitC = 10;
					ballT.setDelay(15);
				}
				
				return true;
			}
			else //그 이외의 경우에는 공을 칠수 없음
				return false;
		}
		else
			return false;	
	}
	

	public class BallListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			
			if(turn == 0) { //컴퓨터가 쳐서 플레이어에게 공이 가는 턴
				if(cLeftRight == 1) { //컴퓨터가 오른쪽에서 쳤을때
					if(cHitDirection == 0) { //오른쪽에서 왼쪽으로 칠때
						bp.x -= 5;
						bp.y += 15;
					}
					else { //오른쪽에서 오른쪽으로 쳤을때
						count++;
						if(count%3 == 0)
							bp.x += 2;
						bp.y += 18;
					}
				}
				else { //컴퓨터가 왼쪽에서 쳤을때
					if(cHitDirection == 0) { //왼쪽에서 왼쪽으로 칠때
						count++;
						if(count%3 == 0)
							bp.x += 2;
						bp.y += 18;
					}
					else { //오른쪽에서 왼쪽으로 칠때
						bp.x += 5;
						bp.y += 15;
					}
					
				}
			}
			else { //플레이어가 쳐서 컴퓨터에게 공이 가는 턴
				if(pHitDirection == 1) { //플레이어가 오른쪽으로 쳤을때
					if(pLeftRight == 0) { //플레이어가 왼쪽에서 친 경우
						bp.x += 5;
						bp.y -= 15;
					}
					else { //플레이어가 오른쪽에서 친 경우
						count++;
						if(count%3 == 0)
							bp.x -= 2;
						bp.y -= 18;
					}			
				}
				if(pHitDirection == 0) { //플레이어 왼쪽으로 쳤을때
					if(pLeftRight == 0) { //플레이어가 왼쪽에서 쳤을때
						count++;
						if(count%3 == 0)
							bp.x -= 2;
						bp.y -= 18;
					}
					else { //플레이어가 오른쪽으로 쳤을때
						bp.x -= 5;
						bp.y -= 15;
					}
				}
			}
				
			//컴퓨터가 공을 치는경우와 못치는 경우
			if(turn == 1) {
				if(hitC<7) { //컴퓨터가 공을 치는 경우
					if(bp.y<280) { 
						if(pHitDirection == 0) {
							computerX = 140;
							cLeftRight = 0;
							bp.x = 185;
							bp.y = 280;
						}
						else {
							computerX = 280;
							cLeftRight = 1;
							bp.x = 325;		
							bp.y = 280;
						}	
						computerT.start();
						turn = 0;
						pHitDirection = rand.nextInt(2);
						}
					}
				else { //컴퓨터가 공을 못치는 경우
					if(bp.y < 370 && bp.y > 350) {
						if(pHitDirection == 0) //플레이어가 공을 왼쪽으로 쳤으면 컴퓨터 케릭터를 왼쪽으로 옮겨줌
							computerX = 140;
						else //플레이어가 공을 오른쪽으로 쳤으면 컴퓨터 케릭터를 오른쪽으로 옮겨줌
							computerX = 280;
						computerT.start();
						}
					}
			}
			
			//승리, 패배 경우
			if(bp.y>750){ //지는경우
				playersmash = 0;
				decision = 2; // 패배 상태로 전환
				ballT.stop(); // 공 멈춤
				
				if(computerScore<3) //컴퓨터 스코어 추가
					computerScore++;
			}
			if(bp.y<150) { //이기는 경우
				decision = 1; //승리 상태로 전환
				ballT.stop(); //공 멈춤
				playersmash = 0;
				
				if(playerScore<3) //플레이어 스코어 추가
					playerScore++;
			}
			
			gamePanel.repaint();
		}
	}
	
	//플레이어 케릭터와 컴퓨터 케릭터의 모션을 그려줄 타이머 리스너
	public class CharacterListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			if(pImgSequence<5)
				pImgSequence++;
			
			else {
				pImgSequence = 0;
				characterT.stop();
			}
			gamePanel.repaint();
		}
	}
	
	public class ComputerListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			if(cImgSequence<9)
				cImgSequence++;
			else {
				cImgSequence = 4;
				computerT.stop();
			}
			gamePanel.repaint();
		}
	}
		
	public class GamePanel extends JPanel implements MouseListener{
		
		public GamePanel() {
			this.addMouseListener(this);			// 마우스 클릭 핸들러 등록
			this.addKeyListener(smashListener);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(new Color(255,203,57)); // 게임 배경 색깔 칠하기
			g.fillRect(0, 0, 550, 1000);
			
			g.drawImage(table, 140, 350, this); // 테이블 그려주기
			
			//점수판
			switch(playerScore){
			case 0 :
				g.drawImage(score0, 170, 50, this);
				break;
			case 1 :
				g.drawImage(score1, 170, 50, this);
				break;
			case 2 :
				g.drawImage(score2, 170, 50, this);
				break;
			case 3 :
				g.drawImage(score3, 170, 50, 80, 80, this);
				break;
			}
			switch(computerScore){
			case 0 :
				g.drawImage(score0, 280, 50, this);
				break;
			case 1 :
				g.drawImage(score1, 280, 50, this);
				break;
			case 2 :
				g.drawImage(score2, 280, 50, this);
				break;
			case 3 :
				g.drawImage(score3, 280, 50, 80, 80, this);
				break;
			}
			//점수판 :
			g.setColor(Color.BLACK);
			g.fillOval(260,70,10,10);
			g.fillOval(260,100,10,10);
			
			//스매시 게이지
			g.setColor(Color.RED);
			g.fillRect(480,694 - smashGauge,29,smashGauge);
			
			g.drawImage(GAUGE,450,300,90,400,this);
			
			if(playersmash == 1) //플레이어가 스매쉬를 사용하면 불탐
				g.drawImage(FLAME,playerX+50,600,100,125,this);
			
			if(decision==0) { //경기가 진행중일 시에
			//케릭터 그려주기
				if(pImgSequence<5 && pImgSequence>0)
					g.drawImage(playerImg.get(pImgSequence),playerX,550,this);
				else if(pImgSequence == 5 || pImgSequence == 0)
					g.drawImage(playerImg.get(0),playerX,550,this);
			
			//컴퓨터 그려주기			
				if(cImgSequence<9 && cImgSequence>0)
					g.drawImage(computerImg.get(cImgSequence),computerX,170,this);
				else if(cImgSequence == 9 || cImgSequence == 0)
					g.drawImage(computerImg.get(0),computerX,170,this);
			}
			else if(decision==1) { //승리시 포즈
				ballT.setDelay(50);
				//승리 멘트
				if(playerScore<3)
					g.drawImage(winP,170,150,this);
				else
					g.drawImage(winM,165,150,this);
				g.drawImage(computerLose,computerX,150,this);
				g.drawImage(playerWin,playerX,550,this);
			}
			else if(decision==2) { //패배시 포즈
				ballT.setDelay(50);
				if(computerScore<3)
					g.drawImage(loseP,165,150,this);
				else
					g.drawImage(loseM,160,150,this);
				g.drawImage(computerWin,computerX,150,this);
				g.drawImage(playerLose,playerX,550,this);
			}
			
			//공 그려주긔
			if(decision == 0) {
				g.setColor(Color.BLACK);
				g.fillOval(bp.x,bp.y,10,10);	
			}
		}
		
		public void mousePressed(MouseEvent arg0) {
			if(decision== 0 && pImgSequence==0) { //게임이 진행중인 경우이고 케릭터가 멈춰진 상태여야만 함
				characterT.start(); //케릭터 움직이기
				if(arg0.getX()<=225) { //왼쪽을 클릭한 경우 왼쪽으로 케릭터 이동
					playerX = 50;
				} 
				else { //오른쪽을 클릭한 경우 오른쪽으로 케릭터 이동
					playerX = 190;		
				}
			}
			
			if(hitAvailable()) { //공을 쳤을때
				if(pLeftRight == 0) { //공을 왼쪽에서 쳤을때 
					bp.x = 195;
					bp.y = 680;
				}
				else { //공을 오른쪽에서 쳤을때
					bp.x = 340;
					bp.y = 680;
				}
				turn = 1;
			}
			
			//승패 결정후 다시 시작
			if(playerScore<3 && computerScore<3) { //3점이 되서 끝나기 전의 경우
				if(decision ==2 || decision == 1) { //승,패 결과가 나온 이후의 경우
					//컴퓨터가 서브
					cHitDirection = 0; 
					cLeftRight = 1;
					bp.x = 325;
					bp.y = 280;
					turn = 0;
			
					decision = 0;
				
					cImgSequence = 0;
					playerX = 50;
					computerX = 280;
					
					computerT.start();
					ballT.start();
				}	
			}
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
		@Override
		public void mouseClicked(MouseEvent arg0) {}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}

	
	}
	public class smashListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			int keyEvent = arg0.getKeyCode();
			
			switch(keyEvent) {
			case KeyEvent.VK_S : 
				if(smashGauge > 350 && decision == 0) {
					playersmash = 1;
					smashGauge = 0;
				}
			break;
			}
		}
		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
	
	public class HomePanel extends JPanel{
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(new Color(255,203,57)); // 게임 배경 색깔 칠하기
			g.fillRect(0, 0, 550, 1000);
			
			g.drawImage(home, 15, 50, this);
		} 
	}
	
	public class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			characterT.stop();
			computerT.stop();
			
			playerX = 50; //플레이어 좌표
			computerX = 280; //컴퓨터 좌우
			cHitDirection = 0; //컴퓨터가 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
			pHitDirection = 0; //플레이어 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
			pLeftRight = 0; //플레이어가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
			cLeftRight = 1; //컴퓨터가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
			turn = 0; //공격 턴 0이면 플레이어 1이면 컴퓨터
			decision = 0; //0이면  경기중 1이면 승리 2면 패배
			
			playerScore = 0; //플레이어의 점수
			computerScore = 0; //컴퓨터의 점수
			
			pImgSequence = 0; // 플레이어 사진 순서
			cImgSequence = 0; // 컴퓨터 사진 순서
			
			bp.x = 325;
			bp.y = 280;
			
			//게임 시작 타이머 가동
			ballT.start();
			computerT.start();	
			
			lp.setLayer(gamePanel, 1);
			lp.setLayer(homePanel, 0);
			
			gamePanel.requestFocus();
			gamePanel.setFocusable(true);
		}
	}
	
	//버튼 리스너들 
	public class PauseListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			ballT.stop();
			computerT.stop();
			characterT.stop();
			
			gamePanel.removeMouseListener(gamePanel); //마우스 리스너 제거
			
			resumeButton.setVisible(true);
			restartButton.setVisible(true);
		}
	}
	public class RestartListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			playerX = 50; //플레이어 좌표
			computerX = 280; //컴퓨터 좌우
			cHitDirection = 0; //컴퓨터가 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
			pHitDirection = 0; //플레이어 공을 어느 방향으로 치는지(0이면 왼쪽 1이면 오른쪽)
			pLeftRight = 0; //플레이어가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
			cLeftRight = 1; //컴퓨터가 공을 쳤을때 어디서 쳤는지(0이면 왼쪽 1이면 오른쪽)
			turn = 0; //공격 턴 0이면 플레이어 1이면 컴퓨터
			decision = 0; //0이면  경기중 1이면 승리 2면 패배
			
			smashGauge = 0; //게이지 0으로 초기화
			
			playerScore = 0; //플레이어의 점수
			computerScore = 0; //컴퓨터의 점수
			
			pImgSequence = 0; // 플레이어 사진 순서
			cImgSequence = 0; // 컴퓨터 사진 순서
			
			bp.x = 325;
			bp.y = 280;
			
			gamePanel.addMouseListener(gamePanel); //마우스 리스너 제거
			
			//게임 시작 타이머 가동
			ballT.start();
			computerT.start();	
			
			restartButton.setVisible(false);
			resumeButton.setVisible(false);
		}
	}
	public class ResumeListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			if(decision == 0)
				ballT.start();
		
			if(cImgSequence != 4)
				computerT.start();
			if(pImgSequence != 0)
				characterT.start();

			gamePanel.addMouseListener(gamePanel); //마우스 리스너 추가
			
			resumeButton.setVisible(false);
			restartButton.setVisible(false);
		}
	}
	public class HomeListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			
			lp.setLayer(gamePanel, 0);
			lp.setLayer(homePanel, 1);
		}
	}
}