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
        }
    }


    public MessegeManager(ClientHandler user, String function, JSONObject data) throws IOException, SQLException {
        this(user,function,data,null);
    }

    public JSONObject getResponse() {
        return response;
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

    private void loadProfile(String uuid) throws SQLException { //Second version to test
        response.put("function" , "loadProfile");
        JSONArray data = new JSONArray();

        ResultSet setUser = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE uuid='"+uuid+"';");
        setUser.next();

        CallableStatement questions = MainServer.getConnection().prepareCall("{call amountQuestion(?,?)}");
        questions.setString("INuuid", uuid);
        questions.registerOutParameter("amountQ", Types.INTEGER);
        questions.execute();

        CallableStatement miasto = MainServer.getConnection().prepareCall("{call cityOfUser(?,?)}");
        miasto.setString("INuuid", uuid);
        miasto.registerOutParameter("amountQ", Types.VARCHAR);
        miasto.execute();

        CallableStatement answersUser = MainServer.getConnection().prepareCall("{call amountAnwser(?,?)}");
        answersUser.setString("INuuid", uuid);
        answersUser.registerOutParameter("amountA", Types.INTEGER);
        answersUser.execute();

        JSONArray graphics = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("nick",setUser.getString("nick"));
        user.put("name",setUser.getString("name"));
        user.put("surname",setUser.getString("surname"));
        user.put("isPremium",setUser.getBoolean("isPremium"));
        user.put("questions",questions.getInt("amountQ"));
        user.put("answers", answersUser.getInt("amountA"));
        user.put("phone",setUser.getString("phonenr"));
        user.put("city",miasto.getString("outCity"));
        Blob blobAvatar = setUser.getBlob("avatar");
        graphics.put(Base64.getEncoder().encodeToString(blobAvatar.getBytes(0, (int) (blobAvatar.length()-1))));
        data.put(graphics);
        data.put(user);
        response.put("data",data);
    }

    private void getHomeQuestions () throws SQLException {
        response.put("function" , "getHomeQuestions");
        JSONArray questions = new JSONArray();
        ResultSet setQ = MainServer.createStatement().executeQuery("SELECT q.id AS `qid`, q.uuid AS `uid`, r.avatar AS `avatar`, q.text AS`qText`, q.isFinished AS `isFinished`, q.views AS `qViews`, q.date AS `qDate` FROM Question q INNER JOIN Category c ON c.id=q.categoryId INNER JOIN RegisteredUser r ON r.uuid=q.uuid ORDER BY q.views DESC LIMIT 0,10");
        while(setQ.next()){
            ResultSet Answers = MainServer.createStatement().executeQuery("SELECT Count(*) AS `count` FROM Answer WHERE questionId = "+setQ.getInt("qid")+';');
            Answers.next();
            JSONObject quest = new JSONObject();
            quest.put("qid",setQ.getInt("qid"));
            quest.put("uuid",setQ.getString("uid"));
            quest.put("date",setQ.getTimestamp("qDate"));
            quest.put("category", setQ.getString("cName"));
            quest.put("text", setQ.getString("qText"));
            quest.put("answersAmount",Answers.getInt("count"));
            quest.put("views",setQ.getInt("qViews"));
            quest.put("isFinished",setQ.getBoolean("isFinished"));
            byte[] blobAsBytes = setQ.getBlob("avatar").getBytes(1L, (int) setQ.getBlob("avatar").length());
            quest.put("DataOuptputStreamL",blobAsBytes.length);
            quest.put("DataOuptputStream",blobAsBytes);
            questions.put(quest);
        }
    }

}
