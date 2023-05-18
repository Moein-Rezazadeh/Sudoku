# Overview
Sudoku Solver is an app for Android built in Android Studio. This is a personal project made by myself to solve any given sudoku puzzle. 
It uses a brute force algorithm to test values in each column and continually go down row by row until the puzzle is filled with correct values.
To solve sudoku, I used a timer that solves a house of sudoku after each completion and continues until all the houses are filled.

## Screenshots

Empty grid  |  Entering values
:-------------------------:|:-------------------------:
![Empty grid](https://iili.io/HgqkG3v.jpg)  |  ![Entering values](https://iili.io/HgqPquf.jpg)

Solved puzzle |  Errors when entering duplicates
:-------------------------:|:-------------------------:
![Solved puzzle](https://iili.io/HgqU8W7.jpg)  |  ![Errors when entering duplicates](https://iili.io/HgqP7uj.jpg)


# Code

```java
public class PuzzleSolver extends AppCompatActivity {

    private static final int FILE_REQUEST_CODE = 3;
    private TableLayout mTableLayout;
    private Board mBoard;

    private Button mFullySolveButton;
    private boolean ignoreNextText; //decides whether to skip textWatcher

    final static int DEFAULT_BOARD_SIZE = 9;
    private ProgressBar progressBar;
    private int TIMER_DURATION = 5000;
    private ValueAnimator animator;
```
Puzzle Solver java file, the changes of this project are mostly done in this file.

This file is responsible for converting and displaying the main page of the application. Several fields have been created in this file
The fields created by the manufacturer have nothing to do with the fields added for this project:

#### FILE_ REQUEST_ CODE

In Android, whenever we need to read a file from the main memory in the application, we need to
Request Android. This request is sent along with a code that Android does not respond to
The request returns the request code so that the code can find out which request this answer was.
Here, code 3 is specified

#### Progress Bar


While building the main page of the app, I used a `ProgressBar` to display the timer.
In this file, we get it from the page and place it in the ProgressBar field so that it can be used throughout the code.

#### TIMER_ DURATION

Determines the duration of the timer to display the next answer in milliseconds.
Here, the number 5000 milliseconds is recorded, which represents 5 seconds. So, after every five seconds, the empty box fill with right answer.

#### Animator

In Android, a ValueAnimator should be used to create a timer and momentarily use the current value.



```java
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoard = new Board(DEFAULT_BOARD_SIZE);

        setContentView(R.layout.activity_puzzle_solver);
        mTableLayout = (TableLayout) findViewById(R.id.board_table);
        progressBar = findViewById(R.id.timer);
        createTable(DEFAULT_BOARD_SIZE);

        ignoreNextText = false;

        findViewById(R.id.load_table).setOnClickListener(v -> {
            loadFileFromStorage();
        });

        findViewById(R.id.single_solve_button).setOnClickListener(v -> {
            startTimer();
        });

        findViewById(R.id.clear_puzzle).setOnClickListener(v -> {
            clearTable();
        });

        findViewById(R.id.fully_solve_button).setOnClickListener(v -> {
            solveFully();
        });
        simulateLoadFileFromStorage();
    }
```
#### onCreate method
This is the main method of the program and whenever the application and the main page are created, this method is called.

For the Load File button, we created an OnClickListener that calls the LoadFileFromStorage method whenever it is clicked and reads the sudoku file from the main memory.

For the Step Step button, we created an OnClickListener that, by clicking on it, the StartTimer method is called, which starts the timer and displays the answer individually.

For the Clear Table button, we also created an OnClickListener that, when clicked, will call the ClearTable method to reset the table.

For the Solve Fully button, we also created an OnClickListener that, when clicked, Solve the whole table at once without time.


# Features
* Solves a puzzle relatively quickly given valid numbers
  * Only allows the user to enter in numbers, so there is no way to accidentally enter in a wrong character 
* If invalid numbers are entered into the puzzle, it will notify you right away with a red background
* Can clear the board to reset what is there
* The board can be saved and reloaded from a database

## Planned updates
* Ability to solve the puzzle one number at a time, so as to not spoil the solution
* Be able to customize the theming and colors for the app to the user's liking
* Read in a puzzle from the camera by taking a picture and parsing the board
* Support board sizes other then just 9x9 boards
* Add a hints page to help users learn better strategies for solving puzzles
* Add a contact form in the app to submit any feature requests or bugs
