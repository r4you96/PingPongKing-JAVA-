import java.awt.event.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.Timer;

public class PingPongS extends JFrame {

	GamePanel gamePanel; //���� �г�
	HomePanel homePanel; //Ȩ ȭ�� �г�
	JLayeredPane lp = new JLayeredPane();
	JButton startButton; //Ȩ �гο��� ���� �гη� ���� ��ư
	JButton restartButton; //pause��ư ������ ������ �ٽ� �����ϴ� ��ư
	JButton pauseButton; //������ �����ϰ� resume��ư�� restart��ư�� Ȱ��ȭ ��Ŵ
	JButton resumeButton; //������ �̾ �ϴ� ��ư
	
	Point bp; //���� ���� ��ǥ

	Random rand; //���� ������ ����� ����
	
	int count = 0; //�� �������� �ʹ� ���ܼ� �������� �ʵ��� Ȱ���� ����
	int playerX = 50; //�÷��̾� ��ǥ
	int computerX = 280; //��ǻ�� �¿�
	int hitC = 0; //��ǻ�Ͱ� ���� ĥ Ȯ��
	int cHitDirection; //��ǻ�Ͱ� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
	int pHitDirection; //�÷��̾� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
	int pLeftRight; //�÷��̾ ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
	int cLeftRight; //��ǻ�Ͱ� ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
	int turn; //���� �� 0�̸� �÷��̾� 1�̸� ��ǻ��
	int decision; //0�̸�  ����� 1�̸� �¸� 2�� �й�
	
	//���Ž� ���� 
	int playersmash = 0; // 0�̸� ���� ���� 1�̸� ���Ž� ����
	int smashGauge = 0; // 360�̸� ���Ž� ��� ����
	
	//����
	int playerScore; //�÷��̾��� ����
	int computerScore; //��ǻ���� ����
	
	int pImgSequence = 0; // �÷��̾� ���� ����
	int cImgSequence = 0; // ��ǻ�� ���� ����
	
	//����ȭ��
	private final String homePic = "/res/home.gif";
	Image home = new ImageIcon(getClass().getResource(homePic)).getImage();
	
	//�й�, �¸� ��Ʈ
	private final String LOSE_P = "/res/Lpoint.png"; //����Ʈ ����
	Image loseP = new ImageIcon(getClass().getResource(LOSE_P)).getImage();
	private final String LOSE_M = "/res/Lmatch.png"; //��� �й�
	Image loseM = new ImageIcon(getClass().getResource(LOSE_M)).getImage();
	private final String WIN_P = "/res/Wpoint.png"; //����Ʈ ����
	Image winP = new ImageIcon(getClass().getResource(WIN_P)).getImage();
	private final String WIN_M = "/res/Wmatch.png"; //��� �¸�
	Image winM = new ImageIcon(getClass().getResource(WIN_M)).getImage();
	
	//��� ��� �̹���
	private final String TABLE_PIC = "/res/table.png"; //���� ���̺�
	Image table = new ImageIcon(getClass().getResource(TABLE_PIC)).getImage();
	
	//������
	private final String SCORE_0 = "/res/0score.png";
	Image score0 = new ImageIcon(getClass().getResource(SCORE_0)).getImage();
	private final String SCORE_1 = "/res/1score.png";
	Image score1 = new ImageIcon(getClass().getResource(SCORE_1)).getImage();
	private final String SCORE_2 = "/res/2score.png";
	Image score2 = new ImageIcon(getClass().getResource(SCORE_2)).getImage();
	private final String SCORE_3 = "/res/3score.png";
	Image score3 = new ImageIcon(getClass().getResource(SCORE_3)).getImage();
	
	//�÷��̾� ��� �ڼ�
	private final String PLAYER_PIC = "/res/wait.png";
	Image player = new ImageIcon(getClass().getResource(PLAYER_PIC)).getImage();
	
	//���Ž� ����
	private final String FLAME_PIC = "/res/flame.gif";
	Image FLAME = new ImageIcon(getClass().getResource(FLAME_PIC)).getImage();
	private final String GAUGE_PIC = "/res/gauge.png";
	Image GAUGE = new ImageIcon(getClass().getResource(GAUGE_PIC)).getImage();
	
	//�¸�, �й� �ڼ�
	private final String PLAYER_WIN = "/res/win.png"; 
	Image playerWin = new ImageIcon(getClass().getResource(PLAYER_WIN)).getImage();
	private final String PLAYER_LOSE = "/res/lose.png";
	Image playerLose = new ImageIcon(getClass().getResource(PLAYER_LOSE)).getImage();
	
	 //�� ġ�� �ڼ� �̹��� ��ü ����
	private final String pHit_1 = "/res/pHit1.png";
	private final String pHit_2 = "/res/pHit2.png";
	private final String pHit_3 = "/res/pHit3.png";
	private final String pHit_4 = "/res/pHit4.png";
	
	Image pHit1 = new ImageIcon(getClass().getResource(pHit_1)).getImage();
	Image pHit2 = new ImageIcon(getClass().getResource(pHit_2)).getImage();
	Image pHit3 = new ImageIcon(getClass().getResource(pHit_3)).getImage();
	Image pHit4 = new ImageIcon(getClass().getResource(pHit_4)).getImage();
	
	//��ǻ�� ��� �ڼ�, �� ġ�� �ڼ� �̹��� ��ü ����
	//���
	private final String COMPUTER_PIC = "/res/waitC.png";
	Image computer = new ImageIcon(getClass().getResource(COMPUTER_PIC)).getImage();
	
	//�¸�, �й�
	private final String COMPUTER_WIN = "/res/winC.png";
	Image computerWin = new ImageIcon(getClass().getResource(COMPUTER_WIN)).getImage();
	private final String COMPUTER_LOSE = "/res/loseC.png";
	Image computerLose = new ImageIcon(getClass().getResource(COMPUTER_LOSE)).getImage();
	
	//����
	private final String cServe_1 = "/res/serve1.png";
	private final String cServe_2 = "/res/serve2.png";
	private final String cServe_3 = "/res/serve3.png";
	
	Image cServe1 = new ImageIcon(getClass().getResource(cServe_1)).getImage();
	Image cServe2 = new ImageIcon(getClass().getResource(cServe_2)).getImage();
	Image cServe3 = new ImageIcon(getClass().getResource(cServe_3)).getImage();
	
	//Ÿ��
	private final String cHit_1 = "/res/cHit1.png";
	private final String cHit_2 = "/res/cHit2.png";
	private final String cHit_3 = "/res/cHit3.png";
	private final String cHit_4 = "/res/cHit4.png";
	
	Image cHit1 = new ImageIcon(getClass().getResource(cHit_1)).getImage();
	Image cHit2 = new ImageIcon(getClass().getResource(cHit_2)).getImage();
	Image cHit3 = new ImageIcon(getClass().getResource(cHit_3)).getImage();
	Image cHit4 = new ImageIcon(getClass().getResource(cHit_4)).getImage();
	
	//�÷��̾�� ��ǻ�� �̹������� �־��� �迭
	ArrayList<Image> playerImg = new ArrayList<>(); 
	ArrayList<Image> computerImg = new ArrayList<>();
	
	Timer characterT; //ĳ���� Ÿ�̸�
	Timer computerT; //��ǻ�� �ɸ��� Ÿ�̸�
	Timer ballT;	//�� Ÿ�̸�
	
	smashListener smashListener = new smashListener();

	public static void main(String args[]) {
		new PingPongS().go();
	}
	
	void go() {
		//
		gamePanel = new GamePanel();
		gamePanel.setBounds(0,0,550,1000);
		gamePanel.setLayout(null); //���� ��ǥ�� ���� �迭 �ǵ���
		
		
		homePanel = new HomePanel();
		homePanel.setBounds(0,0,550,1000);
		homePanel.setLayout(null); //���� ��ǥ�� ���� �迭 �ǵ���
		
		startButton = new JButton(new ImageIcon("src/res/start.png"));
		StartListener startListen = new StartListener();
		startButton.addActionListener(startListen);
		startButton.setBorderPainted(false); //�׵θ� ���� �ֱ�
		
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
		
		rand = new Random(); //���� ���� ����� ���� �Լ� ����
		
		lp.add(homePanel, new Integer(2));
		lp.add(gamePanel, new Integer(1));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //�����ӿ� x��ư ����
		setLayout(new BorderLayout()); // �����ӿ� ���̾ƿ� �־��ֱ�
		add(lp, BorderLayout.CENTER); //�����ӿ� ���� �г� �߰�
		
		//������ ����
		BallListener ballListen = new BallListener();
		CharacterListener charcaterListen = new CharacterListener();
		ComputerListener computerListen = new ComputerListener();
		
		//��ǻ��, �÷��̾�, �� Ÿ�̸� ����
		characterT = new Timer(60, charcaterListen);
		computerT = new Timer(60, computerListen);
		ballT = new Timer(50, ballListen);
		
		//�̹��� ��ü ����
		imageLoad();
		
		bp = new Point(325,280);
		
		setSize(550,1000); //������ ũ�� ����
		setVisible(true); //������ �����ֱ�
		setResizable(false);
	}
	
	void imageLoad() {
		//�÷��̾� �̹��� �迭�� �߰�
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
	
	
	//���� �޾� ĥ �� �ִ��� �Ǵ��� �ִ� �żҵ�
	boolean hitAvailable() {
		
		if(bp.y>550 &&bp.y<705 && turn == 0 && pImgSequence == 0) { //���� y��ǥ�� 500 �̻��̰�, �÷��̾ ���� �ľ��ϴ� ���϶�
			if(cHitDirection == 0 && playerX == 50) { //��ǻ�Ͱ� �������� ���� ġ��, �÷��̾ �����϶�
				ballT.setDelay(rand.nextInt(30)+30);
				
				hitC = rand.nextInt(10); //��ǻ�Ͱ� ���� ĥ Ȯ���� �־���
				cHitDirection = rand.nextInt(2); //��ǻ�Ͱ� ���� �ް� ��� �������� ĥ�� ������
				pHitDirection = rand.nextInt(2); //�÷��̾ ������ ��� �������� ���� ������
				pLeftRight = 0; //�÷��̾ ���ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				
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
			else if(cHitDirection == 1 && playerX == 190) { //��ǻ�Ͱ� ���������� ���� ġ��, �÷��̾ �������϶�
				ballT.setDelay(rand.nextInt(30)+21);
				
				hitC = rand.nextInt(10); 
				cHitDirection = rand.nextInt(2);
				pHitDirection = rand.nextInt(2);
				pLeftRight = 1; //�÷��̾ �����ʿ��� �Ʊ⶧���� ���ʿ��� ģ �������� ��������
				
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
			else //�� �̿��� ��쿡�� ���� ĥ�� ����
				return false;
		}
		else
			return false;	
	}
	

	public class BallListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			
			if(turn == 0) { //��ǻ�Ͱ� �ļ� �÷��̾�� ���� ���� ��
				if(cLeftRight == 1) { //��ǻ�Ͱ� �����ʿ��� ������
					if(cHitDirection == 0) { //�����ʿ��� �������� ĥ��
						bp.x -= 5;
						bp.y += 15;
					}
					else { //�����ʿ��� ���������� ������
						count++;
						if(count%3 == 0)
							bp.x += 2;
						bp.y += 18;
					}
				}
				else { //��ǻ�Ͱ� ���ʿ��� ������
					if(cHitDirection == 0) { //���ʿ��� �������� ĥ��
						count++;
						if(count%3 == 0)
							bp.x += 2;
						bp.y += 18;
					}
					else { //�����ʿ��� �������� ĥ��
						bp.x += 5;
						bp.y += 15;
					}
					
				}
			}
			else { //�÷��̾ �ļ� ��ǻ�Ϳ��� ���� ���� ��
				if(pHitDirection == 1) { //�÷��̾ ���������� ������
					if(pLeftRight == 0) { //�÷��̾ ���ʿ��� ģ ���
						bp.x += 5;
						bp.y -= 15;
					}
					else { //�÷��̾ �����ʿ��� ģ ���
						count++;
						if(count%3 == 0)
							bp.x -= 2;
						bp.y -= 18;
					}			
				}
				if(pHitDirection == 0) { //�÷��̾� �������� ������
					if(pLeftRight == 0) { //�÷��̾ ���ʿ��� ������
						count++;
						if(count%3 == 0)
							bp.x -= 2;
						bp.y -= 18;
					}
					else { //�÷��̾ ���������� ������
						bp.x -= 5;
						bp.y -= 15;
					}
				}
			}
				
			//��ǻ�Ͱ� ���� ġ�°��� ��ġ�� ���
			if(turn == 1) {
				if(hitC<7) { //��ǻ�Ͱ� ���� ġ�� ���
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
				else { //��ǻ�Ͱ� ���� ��ġ�� ���
					if(bp.y < 370 && bp.y > 350) {
						if(pHitDirection == 0) //�÷��̾ ���� �������� ������ ��ǻ�� �ɸ��͸� �������� �Ű���
							computerX = 140;
						else //�÷��̾ ���� ���������� ������ ��ǻ�� �ɸ��͸� ���������� �Ű���
							computerX = 280;
						computerT.start();
						}
					}
			}
			
			//�¸�, �й� ���
			if(bp.y>750){ //���°��
				playersmash = 0;
				decision = 2; // �й� ���·� ��ȯ
				ballT.stop(); // �� ����
				
				if(computerScore<3) //��ǻ�� ���ھ� �߰�
					computerScore++;
			}
			if(bp.y<150) { //�̱�� ���
				decision = 1; //�¸� ���·� ��ȯ
				ballT.stop(); //�� ����
				playersmash = 0;
				
				if(playerScore<3) //�÷��̾� ���ھ� �߰�
					playerScore++;
			}
			
			gamePanel.repaint();
		}
	}
	
	//�÷��̾� �ɸ��Ϳ� ��ǻ�� �ɸ����� ����� �׷��� Ÿ�̸� ������
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
			this.addMouseListener(this);			// ���콺 Ŭ�� �ڵ鷯 ���
			this.addKeyListener(smashListener);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(new Color(255,203,57)); // ���� ��� ���� ĥ�ϱ�
			g.fillRect(0, 0, 550, 1000);
			
			g.drawImage(table, 140, 350, this); // ���̺� �׷��ֱ�
			
			//������
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
			//������ :
			g.setColor(Color.BLACK);
			g.fillOval(260,70,10,10);
			g.fillOval(260,100,10,10);
			
			//���Ž� ������
			g.setColor(Color.RED);
			g.fillRect(480,694 - smashGauge,29,smashGauge);
			
			g.drawImage(GAUGE,450,300,90,400,this);
			
			if(playersmash == 1) //�÷��̾ ���Ž��� ����ϸ� ��Ž
				g.drawImage(FLAME,playerX+50,600,100,125,this);
			
			if(decision==0) { //��Ⱑ �������� �ÿ�
			//�ɸ��� �׷��ֱ�
				if(pImgSequence<5 && pImgSequence>0)
					g.drawImage(playerImg.get(pImgSequence),playerX,550,this);
				else if(pImgSequence == 5 || pImgSequence == 0)
					g.drawImage(playerImg.get(0),playerX,550,this);
			
			//��ǻ�� �׷��ֱ�			
				if(cImgSequence<9 && cImgSequence>0)
					g.drawImage(computerImg.get(cImgSequence),computerX,170,this);
				else if(cImgSequence == 9 || cImgSequence == 0)
					g.drawImage(computerImg.get(0),computerX,170,this);
			}
			else if(decision==1) { //�¸��� ����
				ballT.setDelay(50);
				//�¸� ��Ʈ
				if(playerScore<3)
					g.drawImage(winP,170,150,this);
				else
					g.drawImage(winM,165,150,this);
				g.drawImage(computerLose,computerX,150,this);
				g.drawImage(playerWin,playerX,550,this);
			}
			else if(decision==2) { //�й�� ����
				ballT.setDelay(50);
				if(computerScore<3)
					g.drawImage(loseP,165,150,this);
				else
					g.drawImage(loseM,160,150,this);
				g.drawImage(computerWin,computerX,150,this);
				g.drawImage(playerLose,playerX,550,this);
			}
			
			//�� �׷��ֱ�
			if(decision == 0) {
				g.setColor(Color.BLACK);
				g.fillOval(bp.x,bp.y,10,10);	
			}
		}
		
		public void mousePressed(MouseEvent arg0) {
			if(decision== 0 && pImgSequence==0) { //������ �������� ����̰� �ɸ��Ͱ� ������ ���¿��߸� ��
				characterT.start(); //�ɸ��� �����̱�
				if(arg0.getX()<=225) { //������ Ŭ���� ��� �������� �ɸ��� �̵�
					playerX = 50;
				} 
				else { //�������� Ŭ���� ��� ���������� �ɸ��� �̵�
					playerX = 190;		
				}
			}
			
			if(hitAvailable()) { //���� ������
				if(pLeftRight == 0) { //���� ���ʿ��� ������ 
					bp.x = 195;
					bp.y = 680;
				}
				else { //���� �����ʿ��� ������
					bp.x = 340;
					bp.y = 680;
				}
				turn = 1;
			}
			
			//���� ������ �ٽ� ����
			if(playerScore<3 && computerScore<3) { //3���� �Ǽ� ������ ���� ���
				if(decision ==2 || decision == 1) { //��,�� ����� ���� ������ ���
					//��ǻ�Ͱ� ����
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
			
			g.setColor(new Color(255,203,57)); // ���� ��� ���� ĥ�ϱ�
			g.fillRect(0, 0, 550, 1000);
			
			g.drawImage(home, 15, 50, this);
		} 
	}
	
	public class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			characterT.stop();
			computerT.stop();
			
			playerX = 50; //�÷��̾� ��ǥ
			computerX = 280; //��ǻ�� �¿�
			cHitDirection = 0; //��ǻ�Ͱ� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
			pHitDirection = 0; //�÷��̾� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
			pLeftRight = 0; //�÷��̾ ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
			cLeftRight = 1; //��ǻ�Ͱ� ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
			turn = 0; //���� �� 0�̸� �÷��̾� 1�̸� ��ǻ��
			decision = 0; //0�̸�  ����� 1�̸� �¸� 2�� �й�
			
			playerScore = 0; //�÷��̾��� ����
			computerScore = 0; //��ǻ���� ����
			
			pImgSequence = 0; // �÷��̾� ���� ����
			cImgSequence = 0; // ��ǻ�� ���� ����
			
			bp.x = 325;
			bp.y = 280;
			
			//���� ���� Ÿ�̸� ����
			ballT.start();
			computerT.start();	
			
			lp.setLayer(gamePanel, 1);
			lp.setLayer(homePanel, 0);
			
			gamePanel.requestFocus();
			gamePanel.setFocusable(true);
		}
	}
	
	//��ư �����ʵ� 
	public class PauseListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			ballT.stop();
			computerT.stop();
			characterT.stop();
			
			gamePanel.removeMouseListener(gamePanel); //���콺 ������ ����
			
			resumeButton.setVisible(true);
			restartButton.setVisible(true);
		}
	}
	public class RestartListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			playerX = 50; //�÷��̾� ��ǥ
			computerX = 280; //��ǻ�� �¿�
			cHitDirection = 0; //��ǻ�Ͱ� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
			pHitDirection = 0; //�÷��̾� ���� ��� �������� ġ����(0�̸� ���� 1�̸� ������)
			pLeftRight = 0; //�÷��̾ ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
			cLeftRight = 1; //��ǻ�Ͱ� ���� ������ ��� �ƴ���(0�̸� ���� 1�̸� ������)
			turn = 0; //���� �� 0�̸� �÷��̾� 1�̸� ��ǻ��
			decision = 0; //0�̸�  ����� 1�̸� �¸� 2�� �й�
			
			smashGauge = 0; //������ 0���� �ʱ�ȭ
			
			playerScore = 0; //�÷��̾��� ����
			computerScore = 0; //��ǻ���� ����
			
			pImgSequence = 0; // �÷��̾� ���� ����
			cImgSequence = 0; // ��ǻ�� ���� ����
			
			bp.x = 325;
			bp.y = 280;
			
			gamePanel.addMouseListener(gamePanel); //���콺 ������ ����
			
			//���� ���� Ÿ�̸� ����
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

			gamePanel.addMouseListener(gamePanel); //���콺 ������ �߰�
			
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