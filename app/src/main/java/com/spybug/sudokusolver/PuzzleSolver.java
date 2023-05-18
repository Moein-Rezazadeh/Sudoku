package com.spybug.sudokusolver;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    private void simulateLoadFileFromStorage() {
        String content =
                        "0,0,0,0,7,0,0,0,0\n" +
                        "0,9,0,5,0,6,0,8,0\n" +
                        "0,0,8,4,0,1,2,0,0\n" +
                        "0,5,9,0,0,0,8,4,0\n" +
                        "7,0,0,0,0,0,0,0,6\n" +
                        "0,2,3,0,0,0,5,7,0\n" +
                        "0,0,5,3,0,7,4,0,0\n" +
                        "0,1,0,6,0,8,0,9,0\n" +
                        "0,0,0,0,1,0,0,0,0";
        prepareBoard(content);
    }

    private void loadFileFromStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                readFileFromUri(uri);
            }
        }
    }

    private void readFileFromUri(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            String content = total.toString();
            prepareBoard(content);
            resetTimer();
        } catch (Exception e) {

        }
    }

    private void prepareBoard(String content) {
        clearTable();
        String[] rows = content.trim().split("\n");
        for (int i = 0; i < rows.length; i++) {
            TableRow tempTR = (TableRow) mTableLayout.getChildAt(i);
            String[] columns = rows[i].trim().split(",");
            for (int j = 0; j < columns.length; j++) {
                TableEntryEditText et = (TableEntryEditText) tempTR.getChildAt(j);
                String text = columns[j].trim();
                if (!text.equals("0")) {
                    et.setText(text);
                }
            }
        }
    }

    private void solveFully() {
        if (mBoard.solvePuzzle()) {
            TableEntryEditText tempET;
            int size = mBoard.getSize();
            ignoreNextText = true;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    tempET = findViewById(i * size + j);
                    if (tempET != null) {
                        tempET.setText(String.format("%s", mBoard.getSolvedData(i * size + j)));
                        tempET.disableEditability();
                    }
                }
            }
            ignoreNextText = false;
            mFullySolveButton.setEnabled(false);
        } else {
            Toast.makeText(getApplicationContext(), "Puzzle could not be solved", Toast.LENGTH_SHORT).show();
        }
    }


    private void startTimer() {
        resetTimer();
        if (mBoard.solvePuzzle()) {
            triggerTimer();
        } else {
            Toast.makeText(getApplicationContext(), "Puzzle could not be solved", Toast.LENGTH_SHORT).show();
        }
    }

    private void triggerTimer() {
        int size = mBoard.getSize();
        outer:
        for (int i = 0; i < mTableLayout.getChildCount(); i++) {
            TableRow tempTR = (TableRow) mTableLayout.getChildAt(i);
            for (int j = 0; j < tempTR.getChildCount(); j++) {
                TableEntryEditText et = (TableEntryEditText) tempTR.getChildAt(j);
                if (et.getText().toString().equals("")) {
                    et.setText(mBoard.getSolvedData(i * size + j) + "");
                    startProgress();
                    break outer;
                }
            }
        }
        resetTimer();
    }

    private void startProgress() {
        resetTimer();
        animator = new ValueAnimator();
        animator.setIntValues(0, 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(TIMER_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                triggerTimer();
            }
        });
        animator.addUpdateListener(animation -> {
            progressBar.setProgress((Integer) animation.getAnimatedValue());
        });
        animator.start();
    }

    private void resetTimer() {
        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.removeAllListeners();
            animator.cancel();
        }
        progressBar.setProgress(0);
    }

    private void clearTable() {
        resetTimer();
        ignoreNextText = true;
        for (int i = 0; i < mTableLayout.getChildCount(); i++) { //loops through all rows in tableLayout
            TableRow tempTR = (TableRow) mTableLayout.getChildAt(i); //sets a temp TableRow
            for (int j = 0; j < tempTR.getChildCount(); j++) { //loops through all editTexts in current TableRow
                TableEntryEditText et = (TableEntryEditText) tempTR.getChildAt(j);
                et.enableEditability();
                et.setText(""); //clears the editText
                et.setInvalidEntry(false);
            }
        }
        ignoreNextText = false;
        mBoard.deleteData();
        if (!mFullySolveButton.isEnabled())
            mFullySolveButton.setEnabled(true);
    }

    private void checkEntry(int editTextId) {
        TableEntryEditText tempET;
        int startPos;

        switch (mBoard.checkData(editTextId)) {
            case 1:
                //set col background for elements to red
                for (int i = mBoard.computeX(editTextId); i < mBoard.getSize() * mBoard.getSize(); i += 9) {
                    tempET = (TableEntryEditText) findViewById(i);
                    if (tempET != null)
                        tempET.setInvalidEntry(true);
                    //tempET.setBackgroundResource(R.drawable.invalid_cell);
                }
                break;
            case 2:
                //set row background for elements to red
                startPos = mBoard.computeY(editTextId) * mBoard.getSize();
                for (int i = startPos; i < startPos + 9; i++) {
                    tempET = (TableEntryEditText) findViewById(i);
                    if (tempET != null)
                        tempET.setInvalidEntry(true);
                    //tempET.setBackgroundResource(R.drawable.invalid_cell);
                }
                break;
            case 3:
                //set box background for elements to red
                startPos = (editTextId - (editTextId % 3)) - ((editTextId / 9) % 3 * 9);
                for (int y = startPos; y < 27 + startPos; y += 9) {
                    for (int loc = y; loc < y + 3; loc++) {
                        tempET = (TableEntryEditText) findViewById(loc);
                        if (tempET != null)
                            tempET.setInvalidEntry(true);
                        //tempET.setBackgroundResource(R.drawable.invalid_cell);
                    }
                }

                break;
            case 0:
                //remove element from list and set background to normal
                break;
        }
    }

    private void createTable(final int size) {

        for (int i = 0; i < size; i++) {
            final TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)); //sets width and height
            tableRow.setId(i + 100); //sets id between 100 and 100 + boardSize

            for (int j = 0; j < size; j++) {
                final TableEntryEditText editText = new TableEntryEditText(this);
                editText.setId(i * size + j); //sets id between 0 and 80
                editText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));

                mTableLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        editText.setHeight(mTableLayout.getWidth() / 9);
                    }
                });
                //editText.setHeight(tableRow.getWidth());
                //http://stackoverflow.com/questions/3591784/getwidth-and-getheight-of-view-returns-0

                editText.addTextChangedListener(new TextWatcher() { //add custom textwatcher class that accesses edittext parent and such and changes background id's of all edititexts
                    //will need a way to check if certain set of edittexts background has been set and change it if changes

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (ignoreNextText)
                            return;

                        if (editText.length() > 1) {
                            String newInput = s.toString().substring(s.length() - 1); //grabs the last value entered
                            editText.setText(newInput); //sets the text to the newInput
                            editText.setSelection(editText.length()); //sets cursor to end of editText
                            checkEntry(editText.getId());
                        } else if (editText.length() == 1 && s.length() == 1) {
                            mBoard.setData(editText.getId(), Integer.parseInt(s.toString())); //sets board data when something entered
                            checkEntry(editText.getId());
                        } else if (count == 0 && editText.length() == 0) {
                            mBoard.deleteSingleData(editText.getId());
                            checkEntry(editText.getId());
                            Log.v("onTextChanged", "deleting data at index " + editText.getId());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                tableRow.addView(editText);
            }

            mTableLayout.addView(tableRow); //add the row to the table
        }
    }
}