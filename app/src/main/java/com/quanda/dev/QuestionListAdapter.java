package com.quanda.dev;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class QuestionListAdapter extends ArrayAdapter<Question> {
    private final Activity context;
    private List<Question> questions;

    public QuestionListAdapter(@NonNull Activity context, @NonNull List<Question> questions) {
        super(context, R.layout.questions_list, questions);

        this.context = context;
        this.questions = questions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Question question = questions.get(position);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.questions_list, null, true);
        rowView.setTag(question);

        ImageButton userAvatar = rowView.findViewById(R.id.userAvatar);
        TextView userName = rowView.findViewById(R.id.userName);
        TextView hoursAgo = rowView.findViewById(R.id.hoursAgo);
        TextView topic = rowView.findViewById(R.id.topic);
        TextView questionText = rowView.findViewById(R.id.question);
        TextView answersAmount = rowView.findViewById(R.id.answersAmount);
        TextView viewsAmount = rowView.findViewById(R.id.viewsAmount);

        topic.setText("#" + question.getCategory());
        questionText.setText(question.getText());
        answersAmount.setText(String.valueOf(question.getAnswersAmount()));
        viewsAmount.setText(String.valueOf(question.getViewsAmount()));

        int qHAgo = question.hoursAgo();
        int qMAgo = question.minutesAgo();

        if (qHAgo <= 0) {
            hoursAgo.setText(qMAgo  + " minutes ago");
        } else {
            hoursAgo.setText(qHAgo  + " hours ago");
        }

        userName.setText(question.getSenderName());

        if(context.getClass().equals(ProfileActivity.class)){
            userAvatar.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
        }


        /*
        rowView.setOnClickListener((view)->{
            if (Data.uuid != null) {
                try {
                    Question q = (Question) view.getTag();
                    ConnectionHandler.handler.openQuestion(q.getqId());
                    this.context.runOnUiThread(()->{
                        Intent intent = new Intent(this.context, QuestionActivity.class);
                        QuestionActivity.currQuestion = q;

                        this.context.overridePendingTransition(0, 0);
                        this.context.startActivity(intent);
                    });
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                View bNav = context.findViewById(R.id.bottom_nav);
                if(bNav == null)
                    return;

                Alert.show(bNav, "Create an account to check answers");
            }
        });
         */
        return rowView;
    }
}
