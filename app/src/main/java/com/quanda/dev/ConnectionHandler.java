package com.quanda.dev;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.quanda.dev.Data.Data;
import com.quanda.dev.Data.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionHandler extends Thread{
    private static DataOutputStream dos;
    private DataInputStream dis;
    private Socket socket;

    @Override
    public void run() {
        try {
            createConnection();
            while (true) {
                JSONObject packet = readPacket();
                switch (packet.getString("function"))
                {
                    case "getHomeQuestions":
                    {
                        executeGetHomeQuestions(packet.getJSONObject("data"));
                        break;
                    }

                    case "loadProfile":
                    {
                        executeLoadProfile(packet.getJSONObject("data"));
                        break;
                    }

                    case "register":
                    {
                        executeRegister(packet.getJSONObject("data"));
                        break;
                    }

                    case "login":
                    {
                        executeLogin(packet.getJSONObject("data"));
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //Core communication methods
    private void createConnection() throws IOException, JSONException {
        InetSocketAddress address = new InetSocketAddress("46.41.140.24", 25321);
        socket = new Socket();
        socket.connect(address);
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

        String uuid = Data.isUUIDStored();
        if (uuid == null) {
            handshakeNonRegistered();
        } else {
            Data.uuid = uuid;
        }
        HomeActivity.homeActivity.getHomeQuestions();
    }
    private void handshakeNonRegistered() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("androidID", Data.getAndroidID());
        sendPacket("handshakeNonRegistered", data);
    }
    private JSONObject readPacket() throws IOException, JSONException {
        JSONObject object = new JSONObject(dis.readUTF());
        Log.e("READ: ", object.toString());
        return object;
    }
    public static void sendPacket(String function, JSONObject jsonObject){
        new Thread(()->{
            try {
                JSONObject object = new JSONObject();
                object.put("function", function);
                object.put("data", jsonObject);
                Log.e("WRITE: ", object.toString());
                dos.writeUTF(object.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }


    //Server orders
    private void executeGetHomeQuestions(JSONObject data) throws JSONException {
        JSONArray questions = data.getJSONArray("questions");

        for (int i = 0; i < questions.length(); i++) {
            JSONObject e = questions.getJSONObject(i);
            Gson gson = new Gson();
            Question question = gson.fromJson(e.toString(), Question.class);
            HomeActivity.homeActivity.questions.add(question);
        }

        HomeActivity.homeActivity.adapter.notifyDataSetChanged();
    }
    public void executeLoadProfile(JSONObject data) throws JSONException {
        JSONObject info = data.getJSONObject("info");
        ProfileActivity.updateProfileData(
                info.getString("name"),
                info.getBoolean("isPremium"),
                info.getInt("points"),
                info.getInt("questions"),
                info.getInt("answers")
        );

        JSONArray questions = data.getJSONArray("questions");
        for (int i = 0; i<questions.length(); i++){
            JSONObject e = questions.getJSONObject(i);
            Question question = new Gson().fromJson(e.toString(), Question.class);
            ProfileActivity.profileActivity.questions.add(question);
            ProfileActivity.profileActivity.adapter.notifyDataSetChanged();
        }
    }
    public void executeRegister(JSONObject data) throws JSONException{
        boolean success = data.getBoolean("success");
        ProfileActivity.profileActivity.runOnUiThread(()->{
            if(success){
                ProfileActivity.profileActivity.setContentView(R.layout.login);
            }else{
                ProfileActivity.registerError();
            }
        });
    }
    public void executeLogin(JSONObject data) throws JSONException {
        if (data.getBoolean("success")){
            Data.uuid = data.getString("uuid");
            ProfileActivity.profileActivity.runOnUiThread(()->{
                Intent intent = new Intent(ProfileActivity.profileActivity, ProfileActivity.class);
                ProfileActivity.profileActivity.startActivity(intent);
            });
        }else {
            ProfileActivity.loginError();
        }
    }

    /*
    private void isUserInDB() throws JSONException, IOException {
        SharedPreferences storage = HomeActivity.homeActivity.getSharedPreferences(Data.PREFS_NAME, HomeActivity.homeActivity.MODE_PRIVATE);
        if (storage.contains("username") && storage.contains("password")) {
            String username = storage.getString("username", "");
            String password = storage.getString("password", "");

            //Check credentials
            JSONObject jsonObject = new JSONObject();
            JSONArray array = new JSONArray();
            array.put(username);
            array.put(password);
            jsonObject.put("data", array);
            sendPacket("isUserInDb", jsonObject);

            JSONObject answer = readPacket();
            Data.isUser = answer.getBoolean("isPresent");
        } else {
            Data.isUser = false;
        }
    }

    public void loadHomeQuestions() throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "getFirstTenQuestions");
        JSONArray array = new JSONArray();
        jsonObject.put("data", array);
        sendPacket(jsonObject);
    }
    private void handleHomeQuestions(JSONObject answer) throws JSONException {
        JSONArray jsonArray = answer.getJSONArray("data");

        for (int i = 0; i <jsonArray.length(); i++){
            JSONObject e = (jsonArray.getJSONObject(i));
            Question question = new Question(e.getInt("qid"), e.getString("nick"), e.getString("date"), e.getString("category"), e.getString("text"), e.getInt("answersAmount"), e.getInt("views"), e.getBoolean("isFinished"));
            HomeActivity.homeActivity.addQuestion(question);
        }

        HomeActivity.homeActivity.refresh();
    }

    public void loadProfile(String uuid) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "loadProfil");
        JSONArray array = new JSONArray();
        array.put(uuid);
        jsonObject.put("data", array);
        sendPacket(jsonObject);
    }
    public void handleLoadProfil(JSONObject answer) throws JSONException {
        JSONArray jsonArray = answer.getJSONArray("data");
        JSONObject profil = jsonArray.getJSONObject(0);

        ProfileActivity.profileActivity.updateProfilData(
                profil.getString("name"),
                profil.getBoolean("isPremium"),
                profil.getInt("points"),
                profil.getInt("questions"),
                profil.getInt("answers")
        );

        for (int i = 0; i <jsonArray.getJSONArray(1).length(); i++){
            JSONObject e = (jsonArray.getJSONArray(1).getJSONObject(i));
            Question question = new Question(e.getInt("qid"), e.getString("nick"), e.getString("date"), e.getString("category"), e.getString("text"), e.getInt("answersAmount"), e.getInt("views"), e.getBoolean("isFinished"));
            ProfileActivity.profileActivity.addQuestion(question);
            ProfileActivity.profileActivity.refresh();
        }
    }

    public void sendLogin(String username, String password) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "isUserInDB");
        JSONArray array = new JSONArray();
        array.put(username);
        array.put(password);
        jsonObject.put("data", array);

        sendPacket(jsonObject);
    }
    public void handleLogin(JSONObject answer) throws JSONException {
        if (answer.getBoolean("isPresent")){
            //zalogowany
            Data.isUser = true;
            Data.uuid = answer.getString("uuid");
            ProfileActivity.profileActivity.runOnUiThread(()->{
                Intent intent = new Intent(ProfileActivity.profileActivity, ProfileActivity.class);
                ProfileActivity.profileActivity.startActivity(intent);
            });
        }else {
            ProfileActivity.profileActivity.loginError();
        }
    }

    public void sendRegister(String name, String surname, String username, String email, String password) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "register");
        JSONArray array = new JSONArray();
        array.put(name);
        array.put(surname);
        array.put(username);
        array.put(password);
        array.put(email);
        array.put(Data.uuid);
        jsonObject.put("data", array);

        sendPacket(jsonObject);
    }
    public void handleRegister(JSONObject answer) throws JSONException{
        boolean success = answer.getBoolean("success");
        ProfileActivity.profileActivity.runOnUiThread(()->{
            if(success){
                ProfileActivity.profileActivity.setContentView(R.layout.login);
                ProfileActivity.profileActivity.registerSuccess();
            }else{
                ProfileActivity.profileActivity.registerError();
            }
        });
    }

    public void sendQuestion(String qText, String qCategory) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "questionAdd");
        JSONArray array = new JSONArray();
        array.put(qText);
        array.put(qCategory.replaceAll("#", ""));
        array.put(Data.uuid);
        jsonObject.put("data", array);

        sendPacket(jsonObject);
    }
    public void handleQuestion(JSONObject answer) throws JSONException {
        boolean success = answer.getJSONObject("data").getBoolean("success");
        System.out.println("S:" + success);
        QuestionsActivity.questionsActivity.runOnUiThread(()->{
            if(success){
                QuestionsActivity.questionsActivity.setContentView(R.layout.activity_questions);
                QuestionsActivity.questionsActivity.questionAddSuccess();
            }else{
                QuestionsActivity.questionsActivity.questionAddError();
            }
        });
    }

    public void searchQuestions(String byValue){

    }
    public void handleQuestionsSearch(JSONObject answer){

    }

    public void openQuestion(int qid) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("function", "loadAnswers");
        JSONArray array = new JSONArray();
        array.put(qid);
        array.put(Data.uuid);
        jsonObject.put("data", array);
        sendPacket(jsonObject);
    }
    public void handleOpenQuestion(JSONObject answer) throws JSONException {
        JSONArray data = answer.getJSONArray("data");

        for (int i = 0; i < data.length(); i++){

            JSONObject o = data.getJSONObject(i);
            Anserw anserw = new Anserw(o.getString("nick"), o.getString("text"));
            QuestionActivity.questionActivity.answers.add(anserw);
        }
        QuestionActivity.questionActivity.runOnUiThread(()->{
            QuestionActivity.questionActivity.adapter.notifyDataSetChanged();
        });
    }

 */
}
