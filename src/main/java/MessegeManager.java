import com.mysql.cj.jdbc.BlobFromLocator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.*;
import java.util.Base64;
import java.util.UUID;

public class MessegeManager {
    private JSONObject response = new JSONObject();
    String privateKey="";
    public MessegeManager(ClientHandler user,String function, JSONObject data, byte[][] images) throws SQLException {
        switch (function){
            case "addQuestion":{
                JSONObject myObject = new JSONObject();
                response.put("function","addQuestion");
                if(user.getUuid()!=null) {
                    addQuestion(myObject,data.getString("text"),data.getInt("category"),user.getUuid().toString(),images);
                }
                else {
                    myObject.put("success",false);
                    response.put("data",myObject);
                }
                break;
            }
            case "login":{
                response.put("function","login");
                login(user,data.getString("username"),data.getString("password"));
                break;
            }
            case "register":{
                response.put("function","register");
                register(data.getString("username"),data.getString("password"),data.getString("firstName"),data.getString("lastName"),data.getString("email"));
                break;
            }
            case "handshakeNonRegistered":{
                response.put("function","handshakeNonRegistered");
                handshakeNonRegistered(data.getString("androidID"));
                break;
            }
            case "getCategories":{
                response.put("function","getCategories");
                getCategories();
                break;
            }
            case "loadMyProfile":{
                response.put("function","loadMyProfile");
                loadMyProfile(user);
                break;
            }
            case "getHomeQuestions":{
                response.put("function","getHomeQuestions");
                getHomeQuestions();
                break;
            }
            case "editProfile":{
                response.put("function","editProfile");
                editProfile(user,data,images);
            }
            case "logout":{
                response.put("function","logout");
                logout(user,data.getString("androidID"));
            }
        }
    }


    public MessegeManager(ClientHandler user, String function, JSONObject data) throws IOException, SQLException {
        this(user,function,data,null);
    }

    public JSONObject getResponse() {
        return response;
    }

    private void logout(ClientHandler user, String androidID) throws SQLException {
        JSONObject myObject = new JSONObject();
        if(user.getUuid()!=null){
            user.logout();
            handshakeNonRegistered(androidID);
        }else {
            myObject.put("success",false);
            response.put("data",myObject);
        }
    }

    public void getCategories() throws SQLException {
        JSONArray myArray = new JSONArray();
        ResultSet categories = MainServer.createStatement().executeQuery("SELECT * FROM Category");
        while(categories.next()){
            JSONObject object = new JSONObject();
            object.put("id",categories.getInt("id"));
            object.put("name",categories.getString("name"));
            Integer tmpint = (Integer) categories.getObject("maincat");
            if(tmpint != null) object.put("pid",tmpint);
            myArray.put(object);
        }
        JSONObject temp = new JSONObject();
        temp.put("categories",myArray);
        response.put("data",temp);
    }

    private void login(ClientHandler user, String username, String password) throws SQLException {
        JSONObject myObject = new JSONObject();
        if(!(username.contains("'")||username.contains("--")||username.contains(";")||username.contains("*")||username.contains("?")||username.contains("%")||username.contains("\\")||username.contains("/"))){
            ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE nick = '"+username+"' AND password = '"+password+"';");
            if(set.next()){
                myObject.put("success",true);
                user.setUuid(UUID.fromString(set.getString("uuid")));
            }else{
                myObject.put("success",false);
            }
        }else{
            myObject.put("success",false);
        }
        response.put("data",myObject);
    }

    private void register(String username, String password, String name, String surname, String email) throws SQLException {
        JSONObject myObject = new JSONObject();
        UUID uuid = null;
        int result = 3;
        while (result == 3) {
            uuid = UUID.randomUUID();
            CallableStatement cStmt = MainServer.getConnection().prepareCall("{call register(?,?,?,?,?,?,?)}");

            cStmt.setString("INusername", username);
            cStmt.setString("INpassword", password);
            cStmt.setString("INname", name);
            cStmt.setString("INemail", email);
            cStmt.setString("INsurname", surname);
            cStmt.setString("INuuid", uuid.toString());
            cStmt.registerOutParameter("retval", Types.INTEGER);
            cStmt.execute();

            result = cStmt.getInt("retval");
        }
        myObject.put("success",result);
        response.put("data",myObject);
    }

    private void addQuestion(JSONObject myObject, String text, int category_id, String uuid, byte[][] images) throws SQLException {
        CallableStatement cStmt = MainServer.getConnection().prepareCall("{call insertquestion(?,?,?,?)}");
        cStmt.setString("INmessage",text);
        cStmt.setInt("INcategory",category_id);
        cStmt.setString("INuuid",uuid);
        cStmt.registerOutParameter("qid", Types.INTEGER);

        cStmt.execute();

        int qid = cStmt.getInt("qid");
        for(int i=0;i<images.length;i++){
            CallableStatement imStmt = MainServer.getConnection().prepareCall("{call setgraphic(?,?,?)}");
            imStmt.setInt("pid",qid);
            imStmt.setBlob("image",new SerialBlob(images[i]));
            imStmt.setBoolean("isquestion",true);

            imStmt.execute();
        }
        myObject.put("success",true);
        response.put("data",myObject);
    }

    private void handshakeNonRegistered(String androidID) throws SQLException {
        JSONObject myObject = new JSONObject();
        CallableStatement cStmt = MainServer.getConnection().prepareCall("{call addphoneuser(?)}");
        cStmt.setString("androidID",androidID);
        cStmt.execute();
        myObject.put("success",true);
        response.put("data",myObject);
    }

    private void loadMyProfile(ClientHandler user) throws SQLException { //Second version to test
        JSONObject myObject =  new JSONObject();
        if(user.getUuid()==null){
            myObject.put("success",false);
            response.put("data",myObject);
        }else {
            String uuid = user.getUuid().toString();
            ResultSet setUser = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE uuid='" + uuid + "';");
            setUser.next();

            CallableStatement questions = MainServer.getConnection().prepareCall("{call amountQuestions(?,?)}");
            questions.setString("INuuid", uuid);
            questions.registerOutParameter("amountQ", Types.INTEGER);
            questions.execute();

            CallableStatement miasto = MainServer.getConnection().prepareCall("{call cityOfUser(?,?)}");
            miasto.setString("INuuid", uuid);
            miasto.registerOutParameter("outCity", Types.VARCHAR);
            miasto.execute();

            CallableStatement answersUser = MainServer.getConnection().prepareCall("{call amountAnwser(?,?)}");
            answersUser.setString("INuuid", uuid);
            answersUser.registerOutParameter("amountA", Types.INTEGER);
            answersUser.execute();

            JSONArray graphics = new JSONArray();
            JSONObject profile = new JSONObject();
            profile.put("nick", setUser.getString("nick"));
            profile.put("name", setUser.getString("name"));
            profile.put("surname", setUser.getString("surname"));
            profile.put("isPremium", setUser.getBoolean("isPremium"));
            profile.put("email", setUser.getString("email"));
            profile.put("bio", setUser.getString("bio"));
            profile.put("points", setUser.getInt("points"));
            profile.put("questions", questions.getInt("amountQ"));
            profile.put("answers", answersUser.getInt("amountA"));
            profile.put("phone", setUser.getString("phonenr"));
            profile.put("city", miasto.getString("outCity"));
            Blob blobAvatar = setUser.getBlob("avatar");
            if(blobAvatar!=null) {
                graphics.put(Base64.getEncoder().encodeToString(blobAvatar.getBytes(1, (int) (blobAvatar.length() - 1))));
            }
            profile.put("img",graphics);
            response.put("data", profile);
        }
    }

    private void getHomeQuestions () throws SQLException {
        JSONArray questions = new JSONArray();
        JSONArray graphics = new JSONArray();

        ResultSet setQ = MainServer.createStatement().executeQuery("SELECT q.id AS `qid`, q.uuid AS `uid`, r.avatar AS `avatar`, q.text AS`qText`, q.isFinished AS `isFinished`, q.views AS `qViews`, q.date AS `qDate`, c.name AS `cName` FROM Question q INNER JOIN Category c ON c.id=q.categoryId INNER JOIN RegisteredUser r ON r.uuid=q.uuid ORDER BY q.views DESC LIMIT 0,10");
        while(setQ.next()){
            CallableStatement answers = MainServer.getConnection().prepareCall("{call amountAnwser(?,?)}");
            answers.setString("INuuid", setQ.getString("qid"));
            answers.registerOutParameter("amountA", Types.INTEGER);
            answers.execute();

            JSONObject quest = new JSONObject();
            quest.put("qid",setQ.getInt("qid"));
            quest.put("uuid",setQ.getString("uid"));
            quest.put("date",setQ.getTimestamp("qDate"));
            quest.put("category", setQ.getString("cName"));
            quest.put("text", setQ.getString("qText"));
            quest.put("answersAmount",answers.getInt("amountA"));
            quest.put("views",setQ.getInt("qViews"));
            quest.put("isFinished",setQ.getBoolean("isFinished"));
            Blob blobAvatar = setQ.getBlob("avatar");
            graphics.put(Base64.getEncoder().encodeToString(blobAvatar.getBytes(1, (int) (blobAvatar.length()-1))));
            quest.put("avatar", graphics);
            questions.put(quest);
        }
        response.put("data",questions);
    }

    private void editProfile(ClientHandler user, JSONObject data, byte[][] images) throws SQLException {
        JSONObject myObject = new JSONObject();
        if(user.getUuid()==null){
            myObject.put("success",false);
        }else{
            String uuid = user.getUuid().toString();
            String name = (data.has("name")? data.getString("name"):null);
            String surname = (data.has("surname")? data.getString("surname"):null);
            String email = (data.has("email")? data.getString("email"):null);
            String phone = (data.has("phone")? data.getString("phone"):null);
            String bio = (data.has("bio")? data.getString("bio"):null);
            SerialBlob avatar = null;
            if(images.length>0){
                avatar=new SerialBlob(images[0]);
            }
            CallableStatement cStmt = MainServer.getConnection().prepareCall("{call editProfile(?,?,?,?,?,?,?,?)}");
            cStmt.setString("INuuid",uuid);
            cStmt.setString("INname",name);
            cStmt.setString("INsurname",surname);
            cStmt.setString("INemail",email);
            cStmt.setString("INphone",phone);
            cStmt.setString("INbio",bio);
            cStmt.setBlob("INavatar",avatar);
            cStmt.registerOutParameter("outval", Types.INTEGER);
            cStmt.execute();

            int a = cStmt.getInt("outval");
            if(a==0)
                myObject.put("success",true);
            else
                myObject.put("success",false);
        }
        response.put("data",myObject);
    }


}
