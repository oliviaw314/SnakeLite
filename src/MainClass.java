

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.Timer;

import acm.graphics.GLabel;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;
import javax.sound.sampled.*;
import stanford.spl.GBufferedImage_updateAllPixels;

public class MainClass extends GraphicsProgram implements ActionListener
{

    private Clip musicClip;
    private Clip dieSFX;
    private Clip eatingSFX;
    private Clip highScoreSFX;
    public GOval food;

    private ArrayList<GRect> snakeBody;
    private SnakePart head;

    public Timer timer = new Timer(200, this);

    private boolean isPlaying, isGameOver;
    private int score, highestScore;
    private ArrayList<Integer> allScores = new ArrayList<>();
    private GLabel scoreLabel, highScoreLabel, gameTitle;
    private GLabel instructions, howToMove, warning;
    private int previousDirection = KeyEvent.VK_LEFT;
    private GRect rectBarrier;


    public void run()
    {
        removeAll();
        try {
            music();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }

        addKeyListeners();
        setUpInfo();

        food = new Ball(50,50,11,11);
        food.setFillColor(Color.YELLOW);
        food.setFilled(true);
        snakeBody = new ArrayList<>();

        drawSnake();
        setBackground(Color.BLACK);

    }

    public void music() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        URL resource = getClass().getClassLoader().getResource("music.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource);
       musicClip = AudioSystem.getClip();
        musicClip.open(audioStream);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY); //keep looping even after audio is done
    }

    public void setDieSFX() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        URL resource = getClass().getClassLoader().getResource("die.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource);
        dieSFX = AudioSystem.getClip();
        dieSFX.open(audioStream);
        dieSFX.start();
    }

    public void setEatSFX() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        URL resource = getClass().getClassLoader().getResource("eating sound.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource);
        eatingSFX = AudioSystem.getClip();
        eatingSFX.open(audioStream);
        //credit to chatgpt
        FloatControl volumeControl = (FloatControl) eatingSFX.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue(6.0f); // increase volume by 6 decibels
        eatingSFX.start();
    }

    public void setHighScoreSFX() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        URL resource = getClass().getClassLoader().getResource("highScore.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource);
        highScoreSFX = AudioSystem.getClip();
        highScoreSFX.open(audioStream);
        highScoreSFX.start();
    }

    public void randomFood() {
       int randX = (int) (Math.random()*(getGCanvas().getWidth() - food.getWidth()));
       int randY = (int) (Math.random()*(getGCanvas().getHeight() - food.getHeight()));
       food.setLocation(randX, randY);
       add(food);
    }

    public void setUpInfo() {
        instructions = new GLabel("Welcome to SnakeLite, where you have to chase the ball to grow! Click anywhere on the screen to start", 100, 200);
        instructions.setColor(Color.WHITE);
        add(instructions);

        highScoreLabel = new GLabel("HIGH SCORE: " + highestScore, 615, 30);
        highScoreLabel.setColor(Color.WHITE);
        add(highScoreLabel);
        score = 0;
        scoreLabel = new Scoreboard("SCORE: " + score, 30, 30);
        scoreLabel.setColor(Color.WHITE);
        add(scoreLabel);

        gameTitle = new GLabel("Snake Lite");
        gameTitle.setFont("Courier-15");
        gameTitle.setColor(Color.YELLOW);
        add(gameTitle,getCanvasWidth()/2-gameTitle.getWidth()/2,30);

        howToMove = new GLabel("Use the up-left-down-right keys to move the snake",215,220);
        howToMove.setColor(Color.WHITE);
        add(howToMove);

        warning = new GLabel("*Don't touch the red barriers! They're poisonous!!*",215,240);
        warning.setColor(Color.RED);
        add(warning);

    }
    public void mouseClicked(MouseEvent e) {
        if (isGameOver) {
            isGameOver=false;
            run();
        }
        else if (!isPlaying) {
        randomFood();
        super.mouseClicked(e);

        instructions.setVisible(false);
        warning.setVisible(false);
        howToMove.setVisible(false);

        gameTitle.setColor(Color.BLUE);
        timer.start();
        isPlaying=true;
        }
    }

    public boolean intersectsFood() {
        return head.intersects(food);
    }

    public boolean intersectsSnake() {
        for (int i=1; i<snakeBody.size(); i++) {
            if (head.getBounds().intersects(snakeBody.get(i).getBounds())) {
                return true;
            }
        }
        return false;
    }

    public void drawSnake()
    {
        head = new SnakePart(250,260,12,12);
        head.setFillColor(Color.GREEN);
        head.setFilled(true);
        add(head);
        snakeBody.add(head);
        for (int i=1; i<5; i++) {
            SnakePart part = new SnakePart(250 + 14*i,260,12,12);
            part.setFillColor(Color.GREEN);
            part.setFilled(true);
            add(part);
            snakeBody.add(part);
        }
    }

    boolean blockKey = false;

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && previousDirection == KeyEvent.VK_RIGHT ||
                key == KeyEvent.VK_RIGHT && previousDirection == KeyEvent.VK_LEFT ||
                key == KeyEvent.VK_UP && previousDirection == KeyEvent.VK_DOWN ||
                key == KeyEvent.VK_DOWN && previousDirection == KeyEvent.VK_UP) {
            return; // Ignore the key press if it's a 180-degree turn
        }

        if (blockKey) { // unblock key only when key is released
            blockKey = false;
        }
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    goingDown=false;
                    goingLeft=false;
                    goingUp=true;
                    goingRight=false;
                    previousDirection = KeyEvent.VK_UP;
                    break;

                case KeyEvent.VK_DOWN:
                    goingDown=true;
                    goingLeft=false;
                    goingUp=false;
                    goingRight=false;
                    previousDirection = KeyEvent.VK_DOWN;
                    break;

                case KeyEvent.VK_LEFT:
                    goingDown=false;
                    goingLeft=true;
                    goingUp=false;
                    goingRight=false;
                    previousDirection = KeyEvent.VK_LEFT;
                    break;

                case KeyEvent.VK_RIGHT:
                    goingDown=false;
                    goingLeft=false;
                    goingUp=false;
                    goingRight=true;
                    previousDirection = KeyEvent.VK_RIGHT;
                    break;

            }
    }

    boolean goingUp = false;
    boolean goingDown = false;
    boolean goingLeft = true;
    boolean goingRight = false;

    public void keyPressed(KeyEvent keyPressed)
    {
        blockKey=true; //block key if it's alr presesd
    }


    private void redrawSnake()
    {
        for (int i=snakeBody.size()-1; i>0; i--) {
          snakeBody.get(i).setX(snakeBody.get(i-1).getX());
            snakeBody.get(i).setY(snakeBody.get(i-1).getY());
        }
    }

    private void growSnake()
    {
        SnakePart part = new SnakePart(250 + 14*snakeBody.size(),260,12,12);
        part.setFillColor(Color.GREEN);
        part.setFilled(true);
        add(part);
        snakeBody.add(part);
    }

    private void moveUp()
    {
        snakeBody.get(0).setY(head.getY()-14);
    }

    private void moveDown()
    {
        snakeBody.get(0).setY(head.getY()+14);
    }

    private void moveLeft()
    {
        snakeBody.get(0).setX(head.getX()-14);
    }

    private void moveRight()
    {
        snakeBody.get(0).setX(head.getX()+14);
    }


    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if (intersectsFood()) {
            growSnake();
            randomFood();

            score+=20;
            scoreLabel.setText("SCORE: " + score);
            highestScore = Math.max(highestScore, score);
            highScoreLabel.setText("HIGH SCORE: "+highestScore);

            if (score>=60) {
                createGRect();
            }

            try {
                setEatSFX();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        //game ends
        if (intersectsSnake() || head.getY()>getCanvasHeight() || head.getY()<0 || head.getX()>getCanvasWidth() || head.getX()<0
        || score>=60 && rectBarrier.getBounds().intersects(head.getBounds())) {

            timer.stop();

            GLabel end = new GLabel("GAME OVER", 225, getCanvasHeight()/2-gameTitle.getHeight()/2);
            end.setColor(Color.WHITE);
            end.setFont("Courier-50");

            allScores.add(score);

            if (isHighestScore()) {
                end.setText("NEW HIGH SCORE!");
                end.setX(160);
                try {
                    setHighScoreSFX();
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
            }

            GLabel playAgain = new GLabel("Click anywhere to play again", 300, 275);
            playAgain.setColor(Color.WHITE);
            playAgain.setFont("Courier-10");

            add(end);
            add(playAgain);

            isGameOver = true;
            isPlaying=false;

            highestScore = Math.max(highestScore, score);
            highScoreLabel.setText("HIGH SCORE: "+highestScore);

            musicClip.stop();
            musicClip.setFramePosition(0);

            if (!isHighestScore()) {
                try {
                    setDieSFX();
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
            }

            return;
        }
        redrawSnake();

        if (goingUp) {
            moveUp();
        }
        else if (goingLeft) {
            moveLeft();
        }
        else if (goingRight) {
            moveRight();
        }
        else if (goingDown) {
            moveDown();
        }
    }

    public void createGRect() {
        if (rectBarrier != null) {
            remove(rectBarrier);
        }

       rectBarrier  = new GRect(50,50,100,150);
        int randX=0;
        int randY=0;
        while (overlapsWithSnake() || rectBarrier.getBounds().intersects(food.getBounds()) ||
                (rectBarrier.getX()==50 && rectBarrier.getY()==50)) {
            randX = (int) (Math.random()*(getGCanvas().getWidth() - rectBarrier.getWidth()));
            randY = (int) (Math.random()*(getGCanvas().getHeight() - rectBarrier.getHeight()));
            rectBarrier.setLocation(randX, randY);
        }

        rectBarrier.setFillColor(Color.RED);
        rectBarrier.setFilled(true);
        add(rectBarrier);
    }

    public boolean overlapsWithSnake() {
        for (GRect part : snakeBody) {
            if (rectBarrier.getBounds().intersects(part.getBounds())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHighestScore() {
        if (score==0 && allScores.size()==1) {
            return false;
        }
        if (score!=0 && allScores.size()==1) {
            return true;
        }
        for (int i=0; i<allScores.size()-1; i++) {
            if (score <= allScores.get(i)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args)
    {
        new MainClass().start();
    }
}