package com.quanda.dev;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.quanda.dev.Adapters.QuestionListAdapter;
import com.quanda.dev.Data.Question;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BottomNavManager{
    public static boolean firstStart = true;
    public static HomeActivity homeActivity;
    public List<Question> questions;
    public QuestionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        homeActivity = this;

        setupList();

        if (firstStart) {
            firstStart = false;
            new ConnectionHandler().start();
        } else {
            getHomeQuestions();
        }

        ImageButton home = findViewById(R.id.home);
        home.setBackground(getDrawable(R.drawable.bg_nav_item_selected));
    }

    public void getHomeQuestions() {
        JSONObject data = new JSONObject();
        ConnectionHandler.sendPacket("getHomeQuestions", data);
    }

    private void setupList(){
        questions = new ArrayList<>();
        adapter = new QuestionListAdapter(this, questions);
        ListView homeList = findViewById(R.id.homeList);
        homeList.setAdapter(adapter);
    }


    //Navigation
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