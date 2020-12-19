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
                        byte[] imgBitMap = new byte[3];
                        dos.write(imgBitMap);
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
}
