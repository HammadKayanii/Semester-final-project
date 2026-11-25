# Semester-final-project
Word scramble game for programming fundamentals
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/*
 * WORD SCRAMBLE GAME  (Java Swing)
 * --------------------------------
 * Team split:
 *   PART 1 - Word data and scrambling logic      (Person 1)
 *   PART 2 - GUI design / layout                 (Person 2)
 *   PART 3 - Game logic: guess, hint, skip       (Person 3)
 *   PART 4 - Scoring, rounds, game over, main()  (Person 4)
 */
public class WordScrambleGame extends JFrame implements ActionListener {

    // =====================================================================
    // PART 1 : WORD DATA AND SCRAMBLING LOGIC  (Person 1)
    // =====================================================================

    // Array of words and an array of matching hints (same index = same word)
    static String[] words = {
        "computer", "keyboard", "monitor", "program", "java",
        "variable", "array", "method", "loop", "function",
        "network", "internet", "software", "hardware", "student"
    };
    static String[] hints = {
        "A machine that processes data",
        "You type on it",
        "Screen that shows output",
        "A set of instructions",
        "Our programming language",
        "Stores a value that can change",
        "Stores many values of the same type",
        "A block of code with a name",
        "Repeats a block of code",
        "Another name for a method",
        "Connected computers",
        "Worldwide network",
        "Programs and apps",
        "Physical parts of a computer",
        "Someone who studies"
    };

    boolean[] used = new boolean[words.length];   // remembers which words were already played
    int currentIndex;                             // index of the current word
    String currentWord;                           // the real word
    String scrambledWord;                         // the mixed up word
    Random random = new Random();

    // Picks a random word that has not been used yet
    void pickNewWord() {
        do {
            currentIndex = random.nextInt(words.length);
        } while (used[currentIndex]);

        used[currentIndex] = true;
        currentWord = words[currentIndex];
        scrambledWord = scrambleWord(currentWord);
    }

    // Shuffles the letters of a word (loops + array)
    String scrambleWord(String word) {
        char[] letters = word.toCharArray();
        String result;
        do {
            for (int i = letters.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                char temp = letters[i];
                letters[i] = letters[j];
                letters[j] = temp;
            }
            result = new String(letters);
        } while (result.equals(word));   // make sure it is really scrambled
        return result;
    }

    // =====================================================================
    // PART 2 : GUI DESIGN / LAYOUT  (Person 2)
    // =====================================================================

    JLabel titleLabel, scrambledLabel, hintLabel, messageLabel, scoreLabel, roundLabel, attemptsLabel;
    JTextField guessField;
    JButton submitButton, hintButton, skipButton;

    public WordScrambleGame() {
        setTitle("Word Scramble Game");
        setSize(450, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);          // open in the center of the screen
        setLayout(new GridLayout(9, 1, 5, 5));

        titleLabel = new JLabel("WORD SCRAMBLE", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        scrambledLabel = new JLabel("", JLabel.CENTER);
        scrambledLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scrambledLabel.setForeground(Color.BLUE);

        hintLabel = new JLabel("Hint: (press the Hint button)", JLabel.CENTER);

        guessField = new JTextField();
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setFont(new Font("Arial", Font.PLAIN, 20));

        submitButton = new JButton("Submit Guess");
        hintButton = new JButton("Show Hint (-5 points)");
        skipButton = new JButton("Skip Word");

        messageLabel = new JLabel("Unscramble the word!", JLabel.CENTER);
        scoreLabel = new JLabel("", JLabel.CENTER);
        roundLabel = new JLabel("", JLabel.CENTER);
        attemptsLabel = new JLabel("", JLabel.CENTER);

        // Add everything to the window
        add(titleLabel);
        add(scrambledLabel);
        add(hintLabel);
        add(guessField);
        add(submitButton);
        add(hintButton);
        add(skipButton);
        add(messageLabel);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        bottomPanel.add(roundLabel);
        bottomPanel.add(scoreLabel);
        bottomPanel.add(attemptsLabel);
        add(bottomPanel);

        // Tell the buttons who listens to the clicks
        submitButton.addActionListener(this);
        hintButton.addActionListener(this);
        skipButton.addActionListener(this);
        guessField.addActionListener(this);    // pressing Enter also submits

        startNewGame();
        setVisible(true);
    }

    // =====================================================================
    // PART 3 : GAME LOGIC - GUESS, HINT, SKIP  (Person 3)
    // =====================================================================

    // This method runs whenever a button is clicked
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton || e.getSource() == guessField) {
            checkGuess();
        } else if (e.getSource() == hintButton) {
            showHint();
        } else if (e.getSource() == skipButton) {
            skipWord();
        }
    }

    void checkGuess() {
        String guess = guessField.getText().trim();

        if (guess.equals("")) {                       // empty input check
            messageLabel.setText("Please type a guess first!");
            return;
        }

        if (guess.equalsIgnoreCase(currentWord)) {    // correct answer
            int points = calculatePoints();
            score = score + points;
            messageLabel.setText("Correct! +" + points + " points");
            JOptionPane.showMessageDialog(this, "Correct! You earned " + points + " points.");
            nextRound();
        } else {                                      // wrong answer
            attempts--;
            if (attempts == 0) {
                JOptionPane.showMessageDialog(this, "Out of attempts! The word was: " + currentWord);
                nextRound();
            } else {
                messageLabel.setText("Wrong! Try again.");
                guessField.setText("");
                updateLabels();
            }
        }
    }

    void showHint() {
        hintLabel.setText("Hint: " + hints[currentIndex]);
        hintUsed = true;
        hintButton.setEnabled(false);      // hint can be used only once per word
    }

    void skipWord() {
        JOptionPane.showMessageDialog(this, "Skipped! The word was: " + currentWord);
        nextRound();
    }

    // =====================================================================
    // PART 4 : SCORING, ROUNDS, GAME OVER AND MAIN  (Person 4)
    // =====================================================================

    int score;
    int round;
    int attempts;
    boolean hintUsed;
    final int TOTAL_ROUNDS = 5;
    final int MAX_ATTEMPTS = 3;

    // More attempts left = more points. Using the hint costs 5 points.
    int calculatePoints() {
        int points = 10 + (attempts * 5);
        if (hintUsed) {
            points = points - 5;
        }
        return points;
    }

    void startNewGame() {
        score = 0;
        round = 1;
        for (int i = 0; i < used.length; i++) {   // reset the used words array
            used[i] = false;
        }
        loadWord();
    }

    // Sets up the screen for a new word
    void loadWord() {
        pickNewWord();
        attempts = MAX_ATTEMPTS;
        hintUsed = false;
        scrambledLabel.setText(scrambledWord.toUpperCase());
        hintLabel.setText("Hint: (press the Hint button)");
        messageLabel.setText("Unscramble the word!");
        guessField.setText("");
        hintButton.setEnabled(true);
        updateLabels();
    }

    void nextRound() {
        if (round == TOTAL_ROUNDS) {
            gameOver();
        } else {
            round++;
            loadWord();
        }
    }

    void updateLabels() {
        roundLabel.setText("Round: " + round + "/" + TOTAL_ROUNDS);
        scoreLabel.setText("Score: " + score);
        attemptsLabel.setText("Attempts: " + attempts);
    }

    void gameOver() {
        String rating;
        if (score >= 80) {
            rating = "Excellent!";
        } else if (score >= 50) {
            rating = "Good job!";
        } else {
            rating = "Keep practicing!";
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Game Over!\nFinal Score: " + score + "\n" + rating + "\n\nPlay again?",
                "Game Over", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new WordScrambleGame();
    }
}
