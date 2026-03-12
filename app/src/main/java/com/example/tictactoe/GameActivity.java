package com.example.tictactoe;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    TextView statusText;
    Button resetBtn;
    Button[] buttons = new Button[9];
    MediaPlayer moveSound;

    String[] board = {"", "", "", "", "", "", "", "", ""};
    String currentPlayer = "X";
    boolean gameActive = true;
    String mode = "friend";

    int[][] winPositions = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        statusText = findViewById(R.id.statusText);
        resetBtn = findViewById(R.id.resetBtn);

        buttons[0] = findViewById(R.id.btn0);
        buttons[1] = findViewById(R.id.btn1);
        buttons[2] = findViewById(R.id.btn2);
        buttons[3] = findViewById(R.id.btn3);
        buttons[4] = findViewById(R.id.btn4);
        buttons[5] = findViewById(R.id.btn5);
        buttons[6] = findViewById(R.id.btn6);
        buttons[7] = findViewById(R.id.btn7);
        buttons[8] = findViewById(R.id.btn8);

        moveSound = MediaPlayer.create(this, R.raw.move_sound);

        mode = getIntent().getStringExtra("mode");
        if (mode == null) {
            mode = "friend";
        }

        updateTurnText();

        for (int i = 0; i < buttons.length; i++) {
            int index = i;
            buttons[i].setOnClickListener(v -> handleMove(index));
        }

        resetBtn.setOnClickListener(v -> resetGame());
    }

    private void handleMove(int index) {
        if (!gameActive || !board[index].equals("")) {
            return;
        }

        board[index] = currentPlayer;
        playMoveSound();

        if (currentPlayer.equals("X")) {
            buttons[index].setText("✕");
            buttons[index].setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            buttons[index].setText("◯");
            buttons[index].setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        if (checkWinnerFor(currentPlayer)) {
            gameActive = false;
            showResultDialog(getResultMessage(currentPlayer));
            return;
        }

        if (isDraw()) {
            gameActive = false;
            showResultDialog("Draw!");
            return;
        }

        if (mode.equals("friend")) {
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
            updateTurnText();
        } else if (mode.equals("solo")) {
            if (currentPlayer.equals("X")) {
                currentPlayer = "O";
                updateTurnText();
                computerMove();
            }
        }
    }

    private void computerMove() {
        if (!gameActive) {
            return;
        }

        int bestMove = findBestMove();

        if (bestMove != -1) {
            board[bestMove] = "O";
            playMoveSound();
            buttons[bestMove].setText("◯");
            buttons[bestMove].setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        if (checkWinnerFor("O")) {
            gameActive = false;
            showResultDialog("You Lose!");
            return;
        }

        if (isDraw()) {
            gameActive = false;
            showResultDialog("Draw!");
            return;
        }

        currentPlayer = "X";
        updateTurnText();
    }

    private void playMoveSound() {
        if (moveSound != null) {
            try {
                moveSound.seekTo(0);
                moveSound.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int move = -1;

        for (int i = 0; i < board.length; i++) {
            if (board[i].equals("")) {
                board[i] = "O";
                int score = minimax(board, 0, false);
                board[i] = "";

                if (score > bestScore) {
                    bestScore = score;
                    move = i;
                }
            }
        }

        return move;
    }

    private int minimax(String[] boardState, int depth, boolean isMaximizing) {
        if (checkWinnerForMinimax(boardState, "O")) {
            return 10 - depth;
        }

        if (checkWinnerForMinimax(boardState, "X")) {
            return depth - 10;
        }

        if (isBoardFull(boardState)) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;

            for (int i = 0; i < boardState.length; i++) {
                if (boardState[i].equals("")) {
                    boardState[i] = "O";
                    int score = minimax(boardState, depth + 1, false);
                    boardState[i] = "";
                    bestScore = Math.max(score, bestScore);
                }
            }

            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;

            for (int i = 0; i < boardState.length; i++) {
                if (boardState[i].equals("")) {
                    boardState[i] = "X";
                    int score = minimax(boardState, depth + 1, true);
                    boardState[i] = "";
                    bestScore = Math.min(score, bestScore);
                }
            }

            return bestScore;
        }
    }

    private boolean checkWinnerFor(String player) {
        for (int[] winPosition : winPositions) {
            int a = winPosition[0];
            int b = winPosition[1];
            int c = winPosition[2];

            if (board[a].equals(player) &&
                    board[b].equals(player) &&
                    board[c].equals(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWinnerForMinimax(String[] boardState, String player) {
        for (int[] winPosition : winPositions) {
            int a = winPosition[0];
            int b = winPosition[1];
            int c = winPosition[2];

            if (boardState[a].equals(player) &&
                    boardState[b].equals(player) &&
                    boardState[c].equals(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull(String[] boardState) {
        for (String cell : boardState) {
            if (cell.equals("")) {
                return false;
            }
        }
        return true;
    }

    private boolean isDraw() {
        for (String cell : board) {
            if (cell.equals("")) {
                return false;
            }
        }
        return true;
    }

    private void resetGame() {
        currentPlayer = "X";
        gameActive = true;

        for (int i = 0; i < board.length; i++) {
            board[i] = "";
            buttons[i].setText("");
        }

        updateTurnText();
    }

    private void updateTurnText() {
        if (mode.equals("solo")) {
            if (currentPlayer.equals("X")) {
                statusText.setText("Your Turn");
            } else {
                statusText.setText("Computer Turn");
            }
        } else {
            statusText.setText("Player " + currentPlayer + " Turn");
        }
    }

    private String getResultMessage(String winner) {
        if (mode.equals("solo")) {
            if (winner.equals("X")) {
                return "You Win!";
            } else {
                return "You Lose!";
            }
        } else {
            return "Player " + winner + " Wins!";
        }
    }

    private void showResultDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(message);

        builder.setPositiveButton("Play Again", (dialog, which) -> resetGame());

        builder.setNegativeButton("Menu", (dialog, which) -> {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moveSound != null) {
            moveSound.release();
            moveSound = null;
        }
    }
}