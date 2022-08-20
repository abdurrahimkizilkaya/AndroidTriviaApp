package com.appstraction.trivia;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.appstraction.trivia.data.Repository;
import com.appstraction.trivia.model.Question;
import com.appstraction.trivia.model.Score;
import com.appstraction.trivia.util.Prefs;
import com.google.android.material.snackbar.Snackbar;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;

    private TextView highestScoreText;
    private TextView scoreText;
    private TextView questionTextview;
    private TextView textViewOutOf;

    private Button buttonNext;
    private Button buttonTrue;
    private Button buttonFalse;

    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        highestScoreText = findViewById(R.id.highest_score_text);
        scoreText = findViewById(R.id.score_text);
        questionTextview = findViewById(R.id.question_textview);
        textViewOutOf = findViewById(R.id.text_view_out_of);

        buttonNext = findViewById(R.id.button_next);
        buttonTrue = findViewById(R.id.button_true);
        buttonFalse = findViewById(R.id.button_false);

        cardView = findViewById(R.id.card_view);

        score = new Score();
        prefs = new Prefs(MainActivity.this);
        Log.d("TAG", "onCreate: " +prefs.getHighestScore());

        // Retrieve the last state
        currentQuestionIndex = prefs.getState();

        highestScoreText.setText(MessageFormat.format("Highest: {0}", String.valueOf(prefs.getHighestScore())));
        scoreText.setText(MessageFormat.format("Current Score: {0}",
                String.valueOf(score.getScore())));

        questionList = new Repository().getQuestions(questionArrayList -> {
                    questionTextview.setText(questionArrayList.get(currentQuestionIndex)
                            .getAnswer());

                    updateCounter(questionArrayList);
                }

        );


        buttonNext.setOnClickListener(view -> {
            getNextQuestion();
        });
        buttonTrue.setOnClickListener(view -> {
            checkAnswer(true);
            updateQuestion();
        });
        buttonFalse.setOnClickListener(view -> {
            checkAnswer(false);
            updateQuestion();
        });
    }

    private void getNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    private void checkAnswer(boolean userChoseCorrect) {
        boolean answer = questionList.get(currentQuestionIndex).isAnswerTrue();
        int snackMessageId = 0;
        if (userChoseCorrect == answer) {
            snackMessageId = R.string.correct_answer;
            fadeAnimation();
            addPoints();
        } else {
            deductPoints();
            snackMessageId = R.string.incorrect;
            shakeAnimation();
        }
        Snackbar.make(cardView, snackMessageId, Snackbar.LENGTH_SHORT)
                .show();

    }

    private void updateCounter(ArrayList<Question> questionArrayList) {
        textViewOutOf.setText(String.format(getString(R.string.text_formatted),
                currentQuestionIndex, questionArrayList.size()));
    }

    private void fadeAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                questionTextview.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextview.setText(question);
        updateCounter((ArrayList<Question>) questionList);
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.shake_animation);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                questionTextview.setTextColor(Color.RED);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    private void deductPoints() {


        if (scoreCounter > 0) {
            scoreCounter -= 100;
            score.setScore(scoreCounter);
            scoreText.setText(MessageFormat.format("Current Score: {0}",
                    String.valueOf(score.getScore())));

        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
        }
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        scoreText.setText(String.valueOf(score.getScore()));
        scoreText.setText(MessageFormat.format("Current Score: {0}",
                String.valueOf(score.getScore())));

    }

    @Override
    protected void onPause() {
        prefs.saveHighestScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        Log.d("State", "onPause: saving state " + prefs.getState() );
        Log.d("Pause", "onPause: saving score " + prefs.getHighestScore() );
        super.onPause();
    }

}