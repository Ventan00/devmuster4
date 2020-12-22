import com.mysql.cj.jdbc.Blob;
import com.mysql.cj.jdbc.BlobFromLocator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
            object.put("pid",categories.getInt("maincat"));
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
        ResultSet questions = MainServer.createStatement().executeQuery("SELECT COUNT(*) AS `amountQ` FROM Question WHERE uuid='"+uuid+"';");
        questions.next();
        ResultSet Miasto = MainServer.createStatement().executeQuery("SELECT city FROM Localisation WHERE id="+setUser.getInt("localisationId"));
        Miasto.next();
        ResultSet answersUser = MainServer.createStatement().executeQuery("SELECT COUNT(*) AS `amountA` FROM Answer WHERE uuid = '"+uuid+"';");
        answersUser.next();

        JSONObject user = new JSONObject();
        user.put("nick",setUser.getString("nick"));
        user.put("name",setUser.getString("name"));
        user.put("surname",setUser.getString("surname"));
        user.put("isPremium",setUser.getBoolean("isPremium"));
        user.put("questions",questions.getInt("amountQ"));
        user.put("answers",answersUser.getInt("amountA"));
        user.put("phone",setUser.getString("phonenr"));
        user.put("city",Miasto.getString("city"));
        data.put(user);

        JSONArray questions2 = new JSONArray();
        ResultSet set2 = MainServer.createStatement().executeQuery("SELECT q.id AS `qid`, q.uuid AS `uid`, r.nick AS `nick`, r.avatar AS `avatar`, c.name AS `cName`, q.text AS`qText`, q.isFinished AS `isFinished`, q.views AS `qViews`, q.date AS `qDate` FROM Question q INNER JOIN Category c ON c.id=q.categoryId INNER JOIN RegisteredUser r ON r.uuid=q.uuid WHERE r.uuid = '"+uuid+"' ORDER BY q.date DESC LIMIT 0,10");
        while(set2.next()){
            ResultSet Answers = MainServer.createStatement().executeQuery("SELECT Count(*) AS `count` FROM Answer WHERE questionId = "+set2.getInt("qid")+';');
            Answers.next();
            JSONObject user2 = new JSONObject();
            user2.put("qid",set2.getInt("qid"));
            user2.put("nick",set2.getString("nick"));
            user2.put("date",set2.getTimestamp("qDate"));
            user2.put("category", set2.getString("cName")); // ?? w protokole napisane jako INT - trzeba zrobic ewentualnie tablice definicji
            user2.put("text", set2.getString("qText"));
            user2.put("answersAmount",Answers.getInt("count"));
            user2.put("views",set2.getInt("qViews"));
            user2.put("isFinished",set2.getBoolean("isFinished"));
            //user.put("avatar",User.getBlob("d")==null? "null": new SerialBlob(User.getBlob("avatar").getBytes(1l,(int) User.getBlob("avatar").length())));
            questions2.put(user2);
        }
        data.put(questions2);

        response.put("data",data);
        byte[] blobAsBytes = setUser.getBlob("avatar").getBytes(1L, (int) setUser.getBlob("avatar").length());
        response.put("DataOuptputStreamL",blobAsBytes.length);
        response.put("DataOuptputStream",blobAsBytes);
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
