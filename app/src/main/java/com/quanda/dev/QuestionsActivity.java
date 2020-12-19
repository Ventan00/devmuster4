package com.quanda.dev;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    private final int CAMERA_REQUEST = 0, GALLERY_REQUEST = 1;

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

        LinearLayout horizontalContainer = findViewById(R.id.horizontal_container);
        for (int i = 0; i < horizontalContainer.getChildCount(); i++) {
            View childView = horizontalContainer.getChildAt(i);
            ImageView imgView = view.findViewById(R.id.included_img);
            Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        }

        // TODO: 12/19/2020
    }

    public void selectImageFromGallery(View view) {
        if (hasGalleryPermissions(this)){
            Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, GALLERY_REQUEST);
        }
    }
    public void selectImageFromCamera(View view) {
        if (hasCameraPermissions(this)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            this.startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                System.out.println("DENIED");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("Failure to grant the permissions will result in disabling certain functions of the application")
                            .show();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("Failure to grant the permissions will result in disabling certain functions of the application")
                            .show();
                } else{
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("You have denied this permission.\nSome application features are disabled." +
                            "\nIf you would like to use this feature you must grant the permission in application options." )
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        class ViewAdder{
            Activity activity;
            ViewAdder (Activity activity) {
                this.activity = activity;
            }

            public void addHorizontalView(Bitmap imageBitmap) {
                LinearLayout horizontalContainer = findViewById(R.id.horizontal_container);

                View view = LayoutInflater.from(activity).inflate(R.layout.horizontal_list_item, null);
                ImageButton removeBtn = view.findViewById(R.id.removeBtn);
                removeBtn.setOnClickListener((v -> {
                    horizontalContainer.removeView(view);
                }));

                ImageView imgView = view.findViewById(R.id.included_img);
                imgView.setImageBitmap(imageBitmap);

                horizontalContainer.addView(view);
            }
        }

        ViewAdder viewAdder = new ViewAdder(this);
        if (requestCode == CAMERA_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                viewAdder.addHorizontalView(imageBitmap);
            }
        } else if (requestCode == GALLERY_REQUEST)
        {
            if (resultCode == RESULT_OK){
                Uri pickedImage = data.getData();

                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 250, true);

                viewAdder.addHorizontalView(resized);
                cursor.close();
            }
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
