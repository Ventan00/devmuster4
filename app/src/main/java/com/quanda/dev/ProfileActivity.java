package com.quanda.dev;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.quanda.dev.Adapters.QuestionListAdapter;
import com.quanda.dev.Data.Alert;
import com.quanda.dev.Data.Data;
import com.quanda.dev.Data.Question;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements BottomNavManager{
    public static ProfileActivity profileActivity;
    public List<Question> questions = new ArrayList<>();
    public QuestionListAdapter adapter;
    public JSONObject profileInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileActivity = this;

        if(Data.isLogged) {
            displayProfile();
            profileInfo = new JSONObject();
            //ConnectionHandler.sendPacket("loadProfile", new JSONObject());
        }else {
            displayLoginAndRegister();
        }

        LinearLayout profileContainer = findViewById(R.id.profileContainer);
        profileContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_nav_item_selected));
    }

    public void setProfileAvatar(byte[] img) {
        ImageView profileAvatar = findViewById(R.id.profileAvatar);
        Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
        profileAvatar.setImageBitmap(bitmap);
    }
    public static void updateProfileData(String name,  boolean isPremium, int points, int questions, int answers) {
        profileActivity.runOnUiThread(()->{
            ((TextView)profileActivity.findViewById(R.id.profilName)).setText(name);
            ((TextView)profileActivity.findViewById(R.id.profilPoints)).setText(points + " points");
            ((TextView)profileActivity.findViewById(R.id.profilQuestions)).setText(String.valueOf(questions));
            ((TextView)profileActivity.findViewById(R.id.profilAnswers)).setText(String.valueOf(answers));
            ((TextView)profileActivity.findViewById(R.id.profilPremium)).setText( isPremium ? "Premium Member" : ((TextView)profileActivity.findViewById(R.id.profilPremium)).getText().toString());
        });
    }
    private void displayProfile() {
        setContentView(R.layout.activity_profile);
        setUpList();
    }
    private void displayLoginAndRegister(){
        setContentView(R.layout.login);
    }
    private void setUpList() {
        adapter = new QuestionListAdapter(this, questions);
        ListView profileList = findViewById(R.id.myQuestions);
        profileList.setAdapter(adapter);
    }


    //Edit profile
    public void edit(View view) {
        setContentView(R.layout.edit_profile);
    }
    public void done(View view) {
        String eName = ((EditText)findViewById(R.id.eName)).getText().toString();
        String eSurname = ((EditText)findViewById(R.id.eSurname)).getText().toString();
        String eUsername = ((EditText)findViewById(R.id.eUsername)).getText().toString();
        String eContact = ((EditText)findViewById(R.id.eContact)).getText().toString();
        String eBio = ((EditText)findViewById(R.id.eBio)).getText().toString();

        Intent intent = new Intent(this, ProfileActivity.class);
        overridePendingTransition(0, 0);
        startActivity(intent);

        Alert.show(findViewById(R.id.bottom_nav), "You edited your profile");
    }
    public void cancel(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    //Register
    public void switchToRegister(View view){
        setContentView(R.layout.register);
    }
    public void signUp(View view) throws JSONException {
        String rFirstName = ((EditText)findViewById(R.id.rName)).getText().toString();
        String rLastName = ((EditText)findViewById(R.id.rSurname)).getText().toString();
        String rUsername = ((EditText)findViewById(R.id.rUsername)).getText().toString();
        String rPassword = ((EditText)findViewById(R.id.rPassword)).getText().toString();
        String rEmail = ((EditText)findViewById(R.id.rEmail)).getText().toString();

        JSONObject data = new JSONObject();
        data.put("firstName", rFirstName);
        data.put("lastName", rLastName);
        data.put("username", rUsername);
        data.put("password", rPassword);
        data.put("email", rEmail);

        ConnectionHandler.sendPacket("register", data);
    }


    //Login
    public void switchToLogIn(View view) {
        setContentView(R.layout.login);
    }
    public void logIn(View view) throws JSONException {
        String lUsername = ((EditText)findViewById(R.id.lUsername)).getText().toString();
        String lPassword = ((EditText)findViewById(R.id.lPassword)).getText().toString();

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        JSONObject data = new JSONObject();
        data.put("username", lUsername);
        data.put("password", lPassword);

        ConnectionHandler.sendPacket("login", data);
    }


    //Navigation
    @Override
    public void home(View view) { openActivity(this, HomeActivity.class); }

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
