import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class SlidingPuzzle extends JFrame{
	private final int SIZE = 3;// Başlangıçta 3 olarak ayarlanmıştı, herhangi bir boyutu denemek için buradan değiştirebilirsiniz
    private JPanel controlPanel = new JPanel();
	private JPanel puzzlePanel = new JPanel(new GridLayout(SIZE,SIZE));
	private JPanel infoPanel = new JPanel();
	private JPanel contentPane;
	private BubbleButton shuffleButton;
	private BubbleButton undoButton;
	private JLabel[][] labels;
	private JButton[][] buttons;
	private JLabel moveLabel;
	private int moveCount = 0;
	private long startTime;
	private JLabel timeLabel;
    ImageIcon[] icon = new ImageIcon[SIZE*SIZE]; // Resim parçalarını icon olarak tutar.
	
	private int emptyRow, emptyCol;
	
	final int cellSize = 500/SIZE; 
	private BufferedImage backgroundImage;

	public class BubbleButton extends JButton{
		public BubbleButton(String buttonName){
        super(buttonName);
		setContentAreaFilled(false);
		}

		protected void paintComponent(Graphics g) {
			if (getModel().isArmed()) {
				g.setColor(getBackground().darker());
			} else {
				g.setColor(getBackground());
			}
			// Daire çizimi
			g.fillOval(0, 0, getSize().width - 1, getSize().height - 1);
			super.paintComponent(g);
		}

		protected void paintBorder(Graphics g) {
			g.setColor(getForeground());
			// Dairenin kenar çizgisi
			g.drawOval(0, 0, getSize().width - 1, getSize().height - 1);
		}
	}

	public SlidingPuzzle() {
		setTitle("Button Puzzle");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		
		controlPanel.setOpaque(false);

        // setContentPane(controlPanel);

		
		 // Create the shuffle button and add an ActionListener
		 shuffleButton = new BubbleButton("Shuffle");
		 shuffleButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Ortalamak için
		 shuffleButton.setPreferredSize(new Dimension(100, 100));
		 shuffleButton.addActionListener(new ActionListener() {
			 @Override
			 public void actionPerformed(ActionEvent e) {
				 shuffleButtons();
				 startTime = System.currentTimeMillis();
			 }
		 });
		 
		undoButton = new BubbleButton("Undo");
		undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		undoButton.setPreferredSize(new Dimension(100, 100)); 
		undoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
			}
		});

		GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0; // X koordinatı
        constraints.gridy = 0; // Y koordinatı
        constraints.insets = new Insets(5, 5, 5, 5); // Bileşenler arası boşluk
		

		controlPanel.add(shuffleButton, constraints);
		constraints.gridy++;
		controlPanel.add(undoButton,constraints);

		
		
		infoPanel.setOpaque(false);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		moveLabel = new JLabel("Moves: " + moveCount);
	    timeLabel = new JLabel("Elapsed Time: 0:00");
        infoPanel.add(moveLabel);
        infoPanel.add(timeLabel); 
		
		startTime = System.currentTimeMillis(); // Oyun başlangıç zamanını kaydet
		startTimer(); // Timer'ı başlat
		buttons = new JButton[SIZE][SIZE];
        labels = new JLabel[SIZE][SIZE];
		createPuzzle();

		
		try {
            backgroundImage = ImageIO.read(new File("mainBackground.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }

		 contentPane = new JPanel() { // JFrame deki ana içerik alanıdır.
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g.setFont(new Font("Arial", Font.BOLD, 50));
                g.setColor(Color.BLUE);
                g.drawString("Puzzle Game", getWidth()/2 - 150, 80);
            }
        };

		contentPane.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		contentPane.add(controlPanel,gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPane.add(puzzlePanel, gbc);

		gbc.gridx = 2;
        gbc.gridy = 0;
        contentPane.add(infoPanel, gbc);
		

        setContentPane(contentPane);
	}
	private void updateElapsedTime() {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Geçen süreyi saniye cinsine çevir

        long minutes = elapsedTime / 60; // Dakikaları hesapla
        long seconds = elapsedTime % 60; // Saniyeleri hesapla

        timeLabel.setText("Elapsed Time: " + minutes + ":" + String.format("%02d", seconds)); // Zaman etiketini güncelle
    }

	private void startTimer() {
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateElapsedTime(); // Her saniye geçen süreyi güncelle
			}
		});
		timer.start(); // Timer'ı başlat
	}

    public void createPuzzle(){

        try{
            BufferedImage image = ImageIO.read(new File("sandy.jpg")); // Resim dosyasını ekler.

            int pieceWidth = image.getWidth() / SIZE; // Parçanın genişliğini ayarlar.
            
            int pieceHeight = image.getHeight() / SIZE; // Parçanın yüksekliğini ayarlar
            
            int count = 0;
            // Resmi parçalara böler ve arrayin içine koyar.
					for(int i =0;i<SIZE;i++){  
						for(int j = 0;j<SIZE;j++){
                            BufferedImage imagePiece;
                          
                            if(i != SIZE - 1 || j != SIZE - 1){
                            // Resmi parçalar.
                            imagePiece = image.getSubimage(j*pieceWidth, i*pieceHeight,pieceWidth,pieceHeight);

                            //Resmi ölçeklendirir.
		                    Image scaledPiece = imagePiece.getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);
                            
                            // Parçaları icona çevirir.
		                    icon[count] = new ImageIcon(scaledPiece);
                            count++;
                            
                            }else{
                                imagePiece = new BufferedImage(pieceWidth, pieceHeight, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D g2d = imagePiece.createGraphics();
                                g2d.setColor(Color.WHITE);
                                g2d.fillRect(0, 0, pieceWidth, pieceHeight);
                                g2d.dispose();
                                icon[count] = new ImageIcon(imagePiece);
                            }
                        }
                    }        
        } catch(IOException exception){
            exception.printStackTrace();
        }

        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new JButton();
                if(!(i == SIZE-1 && j == SIZE-1)){
                    labels[i][j] = new JLabel(String.valueOf(i*SIZE+j+1));
                    labels[i][j].setForeground(new Color(0, 0, 0, 0));
					buttons[i][j].setIcon(icon[count]);
                    buttons[i][j].add(labels[i][j], BorderLayout.CENTER);
                    
                    
                }else{
                    labels[i][j] = new JLabel(" ");
                    buttons[i][j].add(labels[i][j]);
                    buttons[i][j].setIcon(null); // son butonun resmini siler.
                }
                    buttons[i][j].setPreferredSize(new Dimension(cellSize, cellSize));
                    buttons[i][j].addActionListener(new ButtonListener());
                    buttons[i][j].setBorderPainted(true); // Buton kenar çizgisini kaldır
                    puzzlePanel.add(buttons[i][j]);
                    count++;
            }
        }
        
		
		setVisible(true);
		emptyRow = SIZE - 1;
		emptyCol = SIZE - 1;
		buttons[emptyRow][emptyCol].setIcon(null);
		buttons[emptyRow][emptyCol].setBackground(new Color(255,202,213));
               
			shuffleButtons(); // puzzle must initialized as shuffled
			
			
			setVisible(true);
			
			
    }

	
	

    public static void playMusic(String filePath){
        try{
            File song = new File(filePath);
            AudioInputStream audio = AudioSystem.getAudioInputStream(song); // Java'nın ses dosyası biçimini desteklemesi için yapılan bir işlem
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        }catch(Exception exception){
        exception.printStackTrace();
        }
    }

    private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton clickedButton = (JButton) e.getSource();

			// Tıklanan butonun konumunu bul
			int clickedRow = -1;
			int clickedCol = -1;
			
			for (int i = 0; i < SIZE; i++) {
				for (int j = 0; j < SIZE; j++) {
					if (buttons[i][j] == clickedButton) {
						clickedRow = i;
						clickedCol = j;
						break;
					}
				}
			}

			// Tıklanan buton ile boş butonun yerini değiştir
			swapButtons(clickedRow, clickedCol);

			if(isSolved()) {
				//create the congrats frame, then 2 panels Panel A for label, Panel B for restart/ close buttons
				
				JFrame congratsFrame = new JFrame();
				congratsFrame.setSize(300,200);
				congratsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				congratsFrame.setLocationRelativeTo(puzzlePanel);
				
				//The label carrier panel
				JPanel congratsPanel = new JPanel();
				congratsPanel.setLayout(new BorderLayout());
				
				JLabel congratsLabel = new JLabel("Congratulations, you've solved the puzzle. Please choose one option below.");
				congratsLabel.setHorizontalAlignment(SwingConstants.CENTER);
				
				
				//The button carrier panel
				JPanel congratsButtonPanel = new JPanel();
				congratsButtonPanel.setLayout(new FlowLayout());
				
				//restart button
				JButton restartButton = new JButton("Restart");
				restartButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						puzzlePanel.removeAll();
						moveCount = 0;
		moveLabel.setText("Moves: " + moveCount);
						createPuzzle();
						congratsFrame.setVisible(false);
						congratsFrame.dispose();
					}
				}); // butona basınca puzzle'ı yeniliyor
				
				JButton quitButton = new JButton("Quit");
				quitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				}); // butona basınca ekranı kapatıyor


				//add buttons to buttonPanel
				congratsButtonPanel.add(restartButton);
				congratsButtonPanel.add(quitButton);
				
				//add buttonPanel to congratsPanel
				congratsPanel.add(congratsButtonPanel, BorderLayout.SOUTH);
				//add congratsLabel to congratsPanel
				congratsPanel.add(congratsLabel, BorderLayout.CENTER);
				
				//add congratsPanel to congratsFrame
				congratsFrame.add(congratsPanel);
				congratsFrame.setVisible(true);
				
			}
		}
	}

   // Butonların yerini değiştirme metodu
	private void swapButtons(int clickedRow, int clickedCol) {
		// Tıklanan butonun boş butonun yanında olup olmadığını kontrol et
		if (isValidMove(clickedRow, clickedCol)) {
			moveCount++;
			moveLabel.setText("Moves: " + moveCount);
			// Tıklanan buton ile boş butonun yerini değiştir
			JButton temp = buttons[clickedRow][clickedCol];
			
			buttons[clickedRow][clickedCol] = buttons[emptyRow][emptyCol];
			buttons[emptyRow][emptyCol] = temp;
			
			JLabel tempL = labels[clickedRow][clickedCol];
			labels[clickedRow][clickedCol] = labels[emptyRow][emptyCol];
			labels[emptyRow][emptyCol] = tempL;
		

			// Panelin içeriğini güncelle
			puzzlePanel.removeAll();
			
			for (JButton[] row : buttons) {
				for (JButton button : row) {
					puzzlePanel.add(button);
				}
			}

			// Yeniden çiz
			puzzlePanel.revalidate();
			puzzlePanel.repaint();

			// Boş butonun konumunu güncelle
			emptyRow = clickedRow;
			emptyCol = clickedCol;
		}
	}
    
    public boolean isValidMove(int clickedRow, int clickedCol){
		if ((Math.abs(clickedRow - emptyRow) == 1 && clickedCol == emptyCol)
				|| (Math.abs(clickedCol - emptyCol) == 1 && clickedRow == emptyRow)) {
			return true;
			
		} else {
			return false;
		}

	}

    private void shuffleButtons() {
	    Random random = new Random();
	    
	    do {
	    for (int i = 0; i < SIZE * SIZE; i++) {
	        int j = random.nextInt(SIZE * SIZE);

	        // Convert the one-dimensional indices to two-dimensional
	        int rowI = i / SIZE; //0 0 0   1 1 1   2 2 2 
	        int colI = i % SIZE; // 0 1 2	0 1 2	0 1 2  	
	        int rowJ = j / SIZE;
	        int colJ = j % SIZE;

	        // Swap the buttons in the array
	        JButton temp = buttons[rowI][colI];
	        buttons[rowI][colI] = buttons[rowJ][colJ];
	        buttons[rowJ][colJ] = temp;
	        
	    	
			JLabel tempL = labels[rowI][colI];
			labels[rowI][colI] = labels[rowJ][colJ];
			labels[rowJ][colJ] = tempL;
	    moveCount = 0;
		moveLabel.setText("Moves: " + moveCount);
	        
	    }
	    }while(!isSolvable());

	    // Update the emptyRow and emptyCol variables if the empty button is shuffled
	    
        for (int i = 0; i < SIZE; i++) {
	        for (int j = 0; j < SIZE; j++) {
	            if (buttons[i][j].getIcon() == null) {
	                emptyRow = i;
	                emptyCol = j;
	            }
	        }
	    }
		
		

	    // Refresh the panel to reflect the changes
	    puzzlePanel.removeAll();
	    for (int i = 0; i < SIZE; i++) {
	        for (int j = 0; j < SIZE; j++) {
	            puzzlePanel.add(buttons[i][j]);
	        }
	    }
	    puzzlePanel.revalidate();
	    puzzlePanel.repaint();
	}

    public boolean isSolvable() {
		int inversion = 0;
		for (int i = 0; i < SIZE * SIZE - 1; i++) { // 0 <= i <= 7
			int rowI = i / SIZE; // 0 0 0 1 1 1 2 2 2
			int colI = i % SIZE; // 0 1 2 0 1 2 0 1 2
			int buttonIvalue;

			if (labels[rowI][colI].getText().equals(" ")) {
				continue;
			}
			else {
				 buttonIvalue = Integer.parseInt(labels[rowI][colI].getText());
			}
			for (int j = i + 1; j < SIZE * SIZE; j++) { // 1 =< =<8
				int rowJ = j / SIZE; // 0 0 0 1 1 1 2 2 2
				int colJ = j % SIZE; // 0 1 2 0 1 2 0 1 2
				int buttonJvalue;

				if (labels[rowJ][colJ].getText().equals(" ")) {
					continue;
				}
				else {
					 buttonJvalue = Integer.parseInt(labels[rowJ][colJ].getText());
				}

				if (buttonIvalue > buttonJvalue) {
					inversion++;
				}
			}
		}
		return (inversion % 2 == 0);
	}



	

	private boolean isSolved() { // Butonların üstündeki textleri 0'dan 9'a sıralı mı diye kontrol eder.
	String[][] controlArray = new String[SIZE][SIZE];
	int value = 1;
	for (int row = 0; row < SIZE; row++) {
		for (int col = 0; col < SIZE; col++) {
			controlArray[row][col] = String.valueOf(value);
			
			if(row == SIZE-1 && col == SIZE-1) {
				if(!labels[row][col].getText().equals(" ")) {
				return false;}
			}
			else if(!controlArray[row][col].equals(labels[row][col].getText())){
               return false;
			}
			value++; 
		}
	}
	return true;
}

 
    public static void main(String[] args) {
		SwingUtilities.invokeLater(SlidingPuzzle::new);
		//playMusic("Closing-Theme-Song.wav");
	}
}

