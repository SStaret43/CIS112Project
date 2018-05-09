package com.company;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Trainer extends Application {
    Stage mainWindow; //the literal frame that pops up
    Scene mainMenuUI, gameUI, explanationUI; //the different screens that we can get to within our "Stage" or frame
    Button playButton, explanationButton, playAgainButton, gameReturnToMenuButton, explanationReturnToMenuButton; //button that user can interact with
    Label explanation1 = new Label("Essentially, this program teaches the computer to put dots in a grid order without overlapping.");
    Label explanation2 = new Label("It might take a while, but it learns as it goes.");
    Label movesMade = new Label("");
    ArrayUnboundedQueue<Integer> allMoves = new ArrayUnboundedQueue<>(); //holds all previous moves for the game
    SortedABPriQ<Integer> previousSuccesses = new SortedABPriQ<>(); //holds all previous moves that were successful
    List<Integer> legal;
    ArrayList<Boolean> occupied = new ArrayList<>();
    ArrayList<ImageView> dots = new ArrayList<>();
    GridPane leftLayout = new GridPane();
    int spaceToPlaceDot;
    int from = 0;
    int to;
    int count;
    int moveCount = 0;
    final int rows = 3;
    final int columns = 3;
    Node node;

    public static void main(String[] args) {
        launch(args);
    }

    public void start (Stage primaryStage) {
        mainWindow = primaryStage;

        //creating object that holds dimensions of user's screen
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

        //giving title to our frame
        mainWindow.setTitle("Dot Placement Training");

        //initializing buttons, giving them a label and dimensions
        playButton = new Button("Play");
        playButton.setPrefWidth(500);
        playButton.setPrefHeight(100);

        explanationButton = new Button("Explanation of Program");
        explanationButton.setPrefWidth(500);
        explanationButton.setPrefHeight(100);

        playAgainButton = new Button("Play Again");
        playAgainButton.setPrefWidth(150);
        playAgainButton.setPrefHeight(50);

        gameReturnToMenuButton = new Button("Return to Menu");
        gameReturnToMenuButton.setPrefWidth(150);
        gameReturnToMenuButton.setPrefHeight(50);

        explanationReturnToMenuButton = new Button("Return to Menu");
        explanationReturnToMenuButton.setPrefWidth(100);

        //the layouts that will determine the formatting of every individual scene
        HBox mainMenuLayout = new HBox(0);
        mainMenuLayout.getChildren().addAll(playButton, explanationButton);
        mainMenuLayout.setAlignment(Pos.CENTER);

        VBox gameOuterLayout = new VBox(20);
        HBox gameBottomLayout = new HBox(20);
        HBox gameTopLayout = new HBox(20);
        VBox rightLayout = new VBox(20);
        rightLayout.setAlignment(Pos.CENTER);
        rightLayout.getChildren().addAll(playAgainButton, gameReturnToMenuButton);
        leftLayout.setPrefSize(300, 300);
        leftLayout.setGridLinesVisible(true);
        for (int i = 0; i < columns; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / columns);
            leftLayout.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / rows);
            leftLayout.getRowConstraints().add(rowConst);
        }
        gameBottomLayout.getChildren().add(movesMade);
        gameBottomLayout.setAlignment(Pos.CENTER);
        gameTopLayout.getChildren().addAll(leftLayout, rightLayout);
        gameTopLayout.setAlignment(Pos.CENTER);
        gameOuterLayout.getChildren().addAll(gameTopLayout, gameBottomLayout);
        gameOuterLayout.setAlignment(Pos.CENTER);
        gameOuterLayout.setPadding(new Insets(20, 20, 20, 20));

        VBox explanationLayout = new VBox(20);
        explanationLayout.getChildren().addAll(explanation1, explanation2, explanationReturnToMenuButton);
        explanationLayout.setAlignment(Pos.CENTER);
        explanationLayout.setPadding(new Insets(20, 20, 20, 20));

        //the scenes that will display within our window
        mainMenuUI = new Scene(mainMenuLayout);
        gameUI = new Scene(gameOuterLayout);
        explanationUI = new Scene(explanationLayout);

        //gets rid of whitespace and does not allow user to resize window
        mainWindow.sizeToScene();
        mainWindow.setResizable(false);

        //shows the main menu
        mainWindow.setScene(mainMenuUI);
        mainWindow.show();


        //centers window on user's screen
        mainWindow.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        mainWindow.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);

        playButton.setOnAction(e -> {
            mainWindow.setScene(gameUI);
            mainWindow.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
            mainWindow.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
            play();
        });

        playAgainButton.setOnAction(e -> {
            play();
        });

        explanationButton.setOnAction(e -> {
            mainWindow.setScene(explanationUI);
            mainWindow.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
            mainWindow.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
        });

        gameReturnToMenuButton.setOnAction(e -> {
            mainWindow.setScene(mainMenuUI);
            mainWindow.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
            mainWindow.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
        });

        explanationReturnToMenuButton.setOnAction(e -> {
            mainWindow.setScene(mainMenuUI);
            mainWindow.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
            mainWindow.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
        });
    }

    public void play() {
        occupied.clear();
        //setting "occupied" ArrayList to false because board is always blank to start; storing all dots within "dots" ArrayList
        for(int i = 0; i < 9; i++) {
            occupied.add(false);
            dots.add(new ImageView(new Image(getClass().getResourceAsStream("Black_Circle.png"))));
            dots.get(i).setFitHeight(100);
            dots.get(i).setFitWidth(100);
        }
        movesMade.setText("Moves made: ");
        node = leftLayout.getChildren().get(0);
        leftLayout.getChildren().clear();
        leftLayout.getChildren().add(0, node);
        from = 0; //Where initial dot will always be placed
        spaceToPlaceDot = 0;
        to = from;
        while(occupied.contains(false)) {
            count = 0;
            moveCount++;
            legal = previousSuccesses.returnQueue();
            for (int i = 0; i < 9; i++) {
                if(legal.contains(i)) {
                    if(!occupied.get(i)) {
                        setLeftLayout(i);
                        allMoves.enqueue(i);
                        occupied.set(i, true);
                        moveCount++;
                    }
                    count++;
                    if (count==9 && legal.size() > 1) {
                        movesMade.setText("");
                        movesMade.setText("Success! Exit and re-run to try again!");
                        break;
                    }
                }
            }
            if(moveCount != 1) {
                spaceToPlaceDot = (int) (Math.random() * 9);
                to = spaceToPlaceDot;
            }
            if(occupied.get(spaceToPlaceDot)) {
                allMoves.enqueue(spaceToPlaceDot);
                break;
            }
            else if(spaceToPlaceDot == 0 && occupied.get(0) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(spaceToPlaceDot == moveCount - 1 && !previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 1 && occupied.get(1) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 2 && occupied.get(2) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 3 && occupied.get(3) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 4 && occupied.get(4) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 5 && occupied.get(5) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 6 && occupied.get(6) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 7 && occupied.get(7) == false) {
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            else if(spaceToPlaceDot == 8 && occupied.get(8) == false){
                setLeftLayout(spaceToPlaceDot);
                if(!previousSuccesses.toString().contains(Integer.toString(spaceToPlaceDot))) {
                    addToSuccess();
                }
            }
            occupied.set(spaceToPlaceDot, true);
            from = to;
            allMoves.enqueue(spaceToPlaceDot);
        }
        printMoves();
    }

    public void printMoves() {
        for(int i = 0; i < moveCount; i++){
            if(count != 9)
            movesMade.setText(movesMade.getText() + allMoves.dequeue() + ", ");
        }
        moveCount = 0;
        System.out.println(previousSuccesses);
    }

    public void addToSuccess() {
        previousSuccesses.enqueue(spaceToPlaceDot);
    }

    public void setLeftLayout(int n) {
        if(n == 0)
            leftLayout.add(dots.get(0), 0, 0);
        else if(n == 1)
            leftLayout.add(dots.get(1), 0, 1);
        else if(n == 2)
            leftLayout.add(dots.get(2), 0, 2);
        else if(n == 3)
            leftLayout.add(dots.get(3), 1, 0);
        else if(n == 4)
            leftLayout.add(dots.get(4), 1, 1);
        else if(n == 5)
            leftLayout.add(dots.get(5), 1, 2);
        else if(n == 6)
            leftLayout.add(dots.get(6), 2, 0);
        else if(n == 7)
            leftLayout.add(dots.get(7), 2, 1);
        else if(n == 8)
            leftLayout.add(dots.get(8), 2, 2);

    }
}
