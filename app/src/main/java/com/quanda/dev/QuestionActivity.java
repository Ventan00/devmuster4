package com.quanda.dev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {
    public static QuestionActivity questionActivity;
    public static Question currQuestion;
    public List<Anserw> answers;
    public AnswersListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        questionActivity = this;



        ((TextView)findViewById(R.id.userName)).setText(currQuestion.getSenderName());
        ((TextView)findViewById(R.id.hoursAgo)).setText("0 hours ago");
        ((TextView)findViewById(R.id.topic)).setText(currQuestion.getCategory());
        ((TextView)findViewById(R.id.question)).setText(currQuestion.getText());
        ((TextView)findViewById(R.id.answersAmount)).setText(String.valueOf(currQuestion.getAnswersAmount()));
        ((TextView)findViewById(R.id.viewsAmount)).setText(String.valueOf(currQuestion.getViewsAmount()));


        this.answers = new ArrayList<>();
        adapter = new AnswersListAdapter(this, answers);
        ((ListView)findViewById(R.id.answersList)).setAdapter(adapter);
    }

    public void sendAnswer(View view) {

    }

    public void closeAnswer(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
