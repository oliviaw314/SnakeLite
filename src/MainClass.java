

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.Timer;

import acm.graphics.GLabel;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;

public class MainClass extends GraphicsProgram implements ActionListener
{

    public GOval food;

    private ArrayList<GRect> snakeBody;
    private SnakePart head;

    private int snakeX, snakeY, snakeWidth, snakeHeight;

    public Timer timer = new Timer(200, this);

    private boolean isPlaying, isGameOver;
    private int score, previousScore;
    private GLabel scoreLabel;
    private GLabel instructions;

    GLabel gameTitle = new GLabel("Snake Lite");


    public void run()
    {
        gameTitle.setFont("Courier-15");
        gameTitle.setColor(Color.YELLOW);
        add(gameTitle,getCanvasWidth()/2-gameTitle.getWidth()/2,30);
        System.out.println(getCanvasWidth());

        addKeyListeners();

        setUpInfo();

        food = new Ball(50,50,11,11);
        food.setFillColor(Color.YELLOW);
        food.setFilled(true);
        randomFood();
        snakeBody = new ArrayList<>();

        drawSnake();

        setBackground(Color.BLACK);

        score = 0;


    }

    public void randomFood() {
        // make it so the ball isnt alr touching the snake or the title
       int randX = (int) (Math.random()*(getGCanvas().getWidth() - food.getWidth()));
       int randY = (int) (Math.random()*(getGCanvas().getHeight() - food.getHeight()));
       food.setLocation(randX, randY);
       add(food);
    }

    public void setUpInfo() {
        //change scoreLabel to top left corner of canvas
        scoreLabel = new Scoreboard("Your score is: " + score, 100, 215);
        scoreLabel.setColor(Color.WHITE);
        instructions = new GLabel("Welcome to SnakeLite, where you have to chase the ball! Click anywhere on the screen to start", 100, 200);
        instructions.setColor(Color.WHITE);
        add(scoreLabel);
        add(instructions);
    }
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        removeInstruction();
        gameTitle.setColor(Color.BLUE);
        timer.start();
    }

    public Boolean intersectsFood() {
        return head.intersects(food);
    }

    public Boolean intersectsSnake() {
        for (int i=1; i<snakeBody.size(); i++) {
            if (head.intersects(snakeBody.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void removeInstruction() {
        scoreLabel.setVisible(false);
        instructions.setVisible(false);
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
        if (blockKey) { // unblock key only when key is released
              blockKey=false;
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    goingDown=false;
                    goingLeft=false;
                    goingUp=true;
                    goingRight=false;
                    System.out.println("clicked up");
                    break;

                case KeyEvent.VK_DOWN:
                    goingDown=true;
                    goingLeft=false;
                    goingUp=false;
                    goingRight=false;
                    System.out.println("clicked down");
                    break;

                case KeyEvent.VK_LEFT:
                    goingDown=false;
                    goingLeft=true;
                    goingUp=false;
                    goingRight=false;
                    System.out.println("clicked left");
                    break;

                case KeyEvent.VK_RIGHT:
                    goingDown=false;
                    goingLeft=false;
                    goingUp=false;
                    goingRight=true;
                    System.out.println("clicked right");
                    break;

            }
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
        }
        if (intersectsSnake()) {
            //game over
            GLabel end = new GLabel("GAME OVER", getCanvasWidth()/2-gameTitle.getWidth()/2, getCanvasHeight()/2-gameTitle.getHeight()/2);
            end.setColor(Color.WHITE);
            end.setFont("Courier-50");
            timer.stop();
            add(end);
        }
        if (goingUp) {
            moveUp();
            System.out.println("moving up");
        }
        else if (goingLeft) {
            moveLeft();
            System.out.println("moving left");
        }
        else if (goingRight) {
            moveRight();
            System.out.println("moving right");
        }
        else if (goingDown) {
            moveDown();
            System.out.println("moving down");
        }
        redrawSnake();
    }

    public static void main(String[] args)
    {
        new MainClass().start();
    }
}