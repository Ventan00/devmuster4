package com.quanda.dev;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class QuestionsActivity extends AppCompatActivity implements BottomNavManager, PermissionsManager{
    public static QuestionsActivity questionsActivity;
    public List<Question> questions = new ArrayList<>();
    public QuestionListAdapter adapter;
    private final int CAMERA_REQUEST = 0, GALERY_REQUEST = 1;

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
        if (Data.uuid == null) {
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
        String qCategory = ((TextView)((Spinner)findViewById(R.id.static_spinner)).getSelectedView()).getText().toString();
    }
    public void selectImageFromGallery(View view) {

        // TODO: 12/18/2020
    }
    public void selectImageFromCamera(View view) {
        // TODO: 12/18/2020
        if (hasCameraPermissions(this)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            this.startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST)
        {
            if (resultCode == -1)
            {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                LinearLayout horizontalContainer = findViewById(R.id.horizontal_container);

                View view = LayoutInflater.from(this).inflate(R.layout.horizontal_list_item, null);
                ImageView imgView = view.findViewById(R.id.included_img);
                imgView.setImageBitmap(imageBitmap);

                horizontalContainer.addView(view);
            }
        } else if (requestCode == GALERY_REQUEST)
        {

        }
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
