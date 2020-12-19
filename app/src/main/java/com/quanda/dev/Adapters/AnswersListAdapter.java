package com.quanda.dev.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quanda.dev.Data.Anserw;
import com.quanda.dev.R;

import java.util.List;

public class AnswersListAdapter extends ArrayAdapter<Anserw>{
    private final Activity context;
    private List<Anserw> answers;

    public AnswersListAdapter(@NonNull Activity context, @NonNull List<Anserw> answers) {
        super(context, R.layout.answer_item, answers);

        this.context = context;
        this.answers = answers;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Anserw anserw = answers.get(position);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.answer_item, null, true);

        ((TextView)rowView.findViewById(R.id.answerContent)).setText(anserw.getContent());
        ((TextView)rowView.findViewById(R.id.answerUsername)).setText(anserw.getUsername());
        return rowView;
    }
}

