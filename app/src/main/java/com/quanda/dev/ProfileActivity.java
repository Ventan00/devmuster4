package com.quanda.dev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements BottomNavManager{
    public static ProfileActivity profileActivity;
    public List<Question> questions = new ArrayList<>();
    public QuestionListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileActivity = this;

        if(Data.uuid != null) {
            try {
                displayProfile();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            displayLoginAndRegister();
        }

        LinearLayout profileContainer = findViewById(R.id.profileContainer);
        profileContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.item_selected));
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
    private void displayProfile() throws JSONException {
        setContentView(R.layout.activity_profile);
        setUpList();
        loadProfile(Data.uuid);
    }
    private void displayLoginAndRegister(){
        setContentView(R.layout.login);
    }
    private void setUpList() {
        adapter = new QuestionListAdapter(this, questions);
        ListView profileList = findViewById(R.id.myQuestions);
        profileList.setAdapter(adapter);
    }
    public void loadProfile(String uuid) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("uuid", uuid);

        ConnectionHandler.sendPacket("loadProfile", data);
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

        if (eName.contains(" ")){
            Alert.show(findViewById(R.id.eContainer), "NAME can't contains special chars");
            return;
        }

        if (eSurname.contains(" ")){
            Alert.show(findViewById(R.id.eContainer), "SURNAME can't contains special chars");
            return;
        }

        if (eUsername.contains(" ")){
            Alert.show(findViewById(R.id.eContainer), "USERNAME can't contains special chars");
            return;
        }

        if (eName.length() <= 3 || eName.length() >= 20) {
            Alert.show(findViewById(R.id.eContainer), "NAME length must be between 3-20");
            return;
        }

        if (eSurname.length() <= 3 || eSurname.length() >= 20) {
            Alert.show(findViewById(R.id.eContainer), "Surname length must be between 3-20");
            return;
        }

        if (eUsername.length() <= 3 || eUsername.length() >= 20) {
            Alert.show(findViewById(R.id.eContainer), "Username length must be between 3-20");
            return;
        }

        if (eContact.length() >= 30) {
            Alert.show(findViewById(R.id.eContainer), "Contact length is more than 30");
            return;
        }

        if (eBio.length() >= 250) {
            Alert.show(findViewById(R.id.eContainer), "Bio length is more than 250");
            return;
        }


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
    public static void registerError() {
        // TODO: 12/17/2020
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
    public static void loginError() {
        // TODO: 12/17/2020
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
