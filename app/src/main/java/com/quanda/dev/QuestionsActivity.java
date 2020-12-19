package com.quanda.dev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quanda.dev.Adapters.CustomArrayAdapter;
import com.quanda.dev.Adapters.QuestionListAdapter;
import com.quanda.dev.Data.Alert;
import com.quanda.dev.Data.Data;
import com.quanda.dev.Data.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity implements BottomNavManager{
    public static QuestionsActivity questionsActivity;
    public List<Question> questions = new ArrayList<>();
    public QuestionListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        questionsActivity = this;

        ImageButton questions = findViewById(R.id.questions);
        questions.setBackground(getDrawable(R.drawable.bg_nav_item_selected));

        loadList();
    }

    private void loadList() {
        adapter = new QuestionListAdapter(this, questions);
        ListView homeList = findViewById(R.id.questionsList);
        homeList.setAdapter(adapter);
    }



    //Add Question
    public void addQuestion(View view) {
        if (Data.uuid != null) {
            setContentView(R.layout.add_question);
            Spinner staticSpinner = (Spinner) findViewById(R.id.static_spinner);
            List<String> strings = new ArrayList<>();
            final String[] values = getResources().getStringArray(R.array.categories_arrays);
            strings.addAll(Arrays.asList(values));
            ArrayAdapter<String> myAdapter = new CustomArrayAdapter(this, R.layout.simple_spinner_dropdown_item, strings);
            staticSpinner.setAdapter(myAdapter);
        }else{
            Alert.show(findViewById(R.id.bottom_nav), "Only logged useres can add questions");
            return;
        }
    }
    public void cancel(View view) {
        Intent intent = new Intent(this, QuestionsActivity.class);
        startActivity(intent);
    }
    public void done(View view) {
        String qText = ((TextView)findViewById(R.id.questionText)).getText().toString();

        if(qText.length() <= 9){
            Alert.show(findViewById(R.id.questionAddContainer), "Question length must be greater than 10");
            return;
        }

        String qCategory = ((TextView)((Spinner)findViewById(R.id.static_spinner)).getSelectedView()).getText().toString();
        // TODO: 12/12/2020
        //ConnectionHandler.handler.sendQuestion(qText, qCategory);
    }
    public void selectImageFromGallery(View view) {
        // TODO: 12/18/2020
    }
    public void selectImageFromCamera(View view) {
        // TODO: 12/18/2020
    }



    //Bottom nav
    @Override
    public void home(View view) {
        openActivity(this, HomeActivity.class);
    }

    @Override
    public void questions(View view) {
        openActivity(this, QuestionsActivity.class);
    }

    @Override
    public void ranking(View view) {
        openActivity(this, RankingActivity.class);
    }

    @Override
    public void profile(View view) {
        openActivity(this, ProfileActivity.class);
    }
}
