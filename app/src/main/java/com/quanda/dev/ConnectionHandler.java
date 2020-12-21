package com.quanda.dev;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.quanda.dev.Data.Alert;
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
import java.util.ArrayList;
import java.util.List;

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

                    case "addQuestion":
                    {
                        executeAddQuestion(packet.getJSONObject("data"));
                        break;
                    }

                    case "getCategories":
                    {
                        executeGetCategories(packet.getJSONObject("data"));
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

        JSONObject savedCredentials = Data.getSavedCredentials(HomeActivity.homeActivity);
        if (savedCredentials == null) {
            //handshakeNonRegistered();
        } else {
            //ConnectionHandler.sendPacket("login", savedCredentials);
        }

        //HomeActivity.homeActivity.getHomeQuestions();
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
    public static void sendPacketWithBytesArrays(String function, JSONObject data, List<byte[]> bytes) {
        new Thread(()->{
            try {
                JSONObject object = new JSONObject();
                object.put("function", function);
                object.put("data", data);
                dos.writeUTF(object.toString());
                Log.e("SEND: ", object.toString());

                for (byte[] array : bytes) {
                    dos.writeInt(array.length);
                    System.out.println(array.length);
                    dos.write(array);
                }
                Log.e("SEND: ", "bytes (" + bytes.size()  + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Server orders
    private void executeGetHomeQuestions(JSONObject data) throws JSONException, IOException {
        JSONArray questions = data.getJSONArray("questions");

        for (int i = 0; i < questions.length(); i++) {
            JSONObject e = questions.getJSONObject(i);
            Gson gson = new Gson();
            Question question = gson.fromJson(e.toString(), Question.class);
            HomeActivity.homeActivity.questions.add(question);
        }

        HomeActivity.homeActivity.adapter.notifyDataSetChanged();

        for (int i = 0; i < questions.length(); i++) {
            int imgLen = dis.readInt();
            byte[] img = new byte[imgLen];
            dis.readFully(img, 0 , imgLen);
            Bitmap imgBitMap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Question question = HomeActivity.homeActivity.questions.get(i);
            question.setImgBitMap(imgBitMap);

            HomeActivity.homeActivity.adapter.notifyDataSetChanged();
        }


    }
    public void executeLoadProfile(JSONObject data) throws JSONException, IOException {
        JSONObject info = data.getJSONObject("info");
        ProfileActivity.profileActivity.profileInfo = info;
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
        }
        ProfileActivity.profileActivity.adapter.notifyDataSetChanged();

        int imgLen = dis.readInt();
        if (imgLen != 0) {
            byte[] img = new byte[imgLen];
            dis.readFully(img, 0, imgLen);
            ProfileActivity.profileActivity.setProfileAvatar(img);
        }
    }
    public void executeRegister(JSONObject data) throws JSONException{
        int success = data.getInt("success");
        ProfileActivity.profileActivity.runOnUiThread(()->{
            switch (success) {
                case 0:
                    ProfileActivity.profileActivity.setContentView(R.layout.login);
                    Alert.show(ProfileActivity.profileActivity.findViewById(R.id.bottom_nav), "Register complete! You can sign in");
                    break;
                case 1:
                    Alert.show(ProfileActivity.profileActivity.findViewById(R.id.bottom_nav), "ERROR! Username is taken");
                    break;
                case 2:
                    Alert.show(ProfileActivity.profileActivity.findViewById(R.id.bottom_nav), "ERROR! Email is taken");
                    break;
            }
        });
    }
    public void executeLogin(JSONObject data) throws JSONException {
        boolean success = data.getBoolean("success");
        ProfileActivity.profileActivity.runOnUiThread(()->{
            if (success){
                Data.isLogged = true;
                Intent intent = new Intent(ProfileActivity.profileActivity, ProfileActivity.class);
                ProfileActivity.profileActivity.startActivity(intent);
            }else {
                Alert.show(ProfileActivity.profileActivity.findViewById(R.id.bottom_nav), "Username or password is incorrect");
            }
        });
    }
    public void executeAddQuestion(JSONObject data) throws JSONException {
        boolean success = data.getBoolean("success");
        if (success) {
            QuestionsActivity.questionsActivity.runOnUiThread(()->{
                QuestionsActivity.questionsActivity.setContentView(R.layout.activity_questions);
                View view = QuestionsActivity.questionsActivity.findViewById(R.id.bottom_nav);
                Alert.show(view, "SUCCESS! Question added");
            });
        } else {
            QuestionsActivity.questionsActivity.runOnUiThread(()->{
                View view = OpenedQuestionActivity.openedQuestionActivity.findViewById(R.id.questionAddContainer);
                Alert.show(view, "ERROR");
            });
        }
    }
    public void executeGetCategories(JSONObject data) throws JSONException {
        JSONArray categoriesArray = data.getJSONArray("categories");

        List<JSONObject> parentsList = new ArrayList<>();
        for (int i = 0; i<categoriesArray.length(); i++) {
            JSONObject c = categoriesArray.getJSONObject(i);
            if (!c.has("pid")) {
                JSONObject parentCategory = new JSONObject();
                parentCategory.put("id", c.getInt("id"));
                parentCategory.put("name", c.getString("name"));
                parentCategory.put("isParent", true);
                parentsList.add(parentCategory);
            }
        }

        List<JSONObject> rsList = new ArrayList<>();
        for (JSONObject parentCategory : parentsList) {
            rsList.add(parentCategory);
            for (int i = 0; i<categoriesArray.length(); i++) {
                JSONObject c = categoriesArray.getJSONObject(i);

                if (c.has("pid")){
                    if (parentCategory.getInt("id") == c.getInt("pid")) {
                        JSONObject childCategory = new JSONObject();
                        childCategory.put("id", c.getInt("id"));
                        childCategory.put("name", c.getString("name"));
                        childCategory.put("isParent", false);
                        rsList.add(childCategory);
                    }
                }
            }
        }

        QuestionsActivity.questionsActivity.categories = rsList;
        QuestionsActivity.questionsActivity.spinnerAdapter.notifyDataSetChanged();
    }
}
