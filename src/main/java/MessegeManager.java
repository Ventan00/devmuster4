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
    public MessegeManager(ClientHandler user,String function, JSONObject data, byte[][] images) throws SQLException, IOException {
        switch (function){
            case "addQuestion":{
                JSONObject myObject = new JSONObject();
                response.put("function","addQuestion");
                if(user.getUuid()!=null)
                    addQuestion(myObject,data.getString("text"),data.getInt("category"),user.getUuid().toString(),images);
                else {
                    myObject.put("success",false);
                    response.put("data",myObject);
                }
                break;
            }
            case "login":{
                JSONObject myObject = new JSONObject();
                response.put("function","login");
                login(user,myObject,data.getString("username"),data.getString("password"));
                break;
            }
            /*case "isUserInDB":{
                response.put("function","isUserInDB");
                isUserInDB(data.getString(0), data.getString(1));
                break;
            }
            case "getFirstTenQuestions":{
                response.put("function","getFirstTenQuestions");
                getFirstTenQuestions();
                break;
            }
            case "loadAnswers":{
                response.put("function","loadAnswers");
                loadAnswers(data.getInt(0),data.getString(1));
                break;
            }
            case "loadProfil":{
                response.put("function","loadProfil");
                loadProfil(data.getString(0));
                break;
            }
            case "handshake":{
                response.put("function","handshake");
                handshake(data.getString(0));
                break;
            }
            case "register":{
                response.put("function","register");
                register(data.getString(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5));
                break;
            }
            case "questionAdd":{
                response.put("function","questionAdd");
                questionAdd(data.getString(0),data.getString(1),data.getString(2));
                break;
            }
            case "getRankingInCity":{
                response.put("function","getRankingInCity");
                getRankingInCity(data.getString(0),data.getInt(1),data.getInt(2));
                break;
            }
            case "logout":{
                response.put("function","logout");
                logout(data.getString(0));
                break;
            }
            case "verifyTel":{
                response.put("function","verifyTel");
                verifyTel(data.getString(0),data.getString(1));
                break;
            }
            case "keywords":{
                response.put("function","keywords");
                keywords(data.getJSONArray(0));
                break;
            }
            case "putAnswer":{
                response.put("function","putAnswer");
                putAnswer(data.getInt(0),data.getString(1),data.getInt(2),data.getInt(3),data.getString(4));
                break;
            }
            */
        }
    }



    public MessegeManager(ClientHandler user, String function, JSONObject data) throws IOException, SQLException {
        this(user,function,data,null);
    }

    public JSONObject getResponse() {
        return response;
    }
    private void login(ClientHandler user, JSONObject myObject, String username, String password) throws SQLException {
        if(!(username.contains("'")||username.contains("--")||username.contains(";")||username.contains("*")||username.contains("?")||username.contains("%")||username.contains("\\")||username.contains("/"))){
            ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE nick = '"+username+"' AND password = '"+password+"';");
            System.out.println(username+" "+password);
            set.next();
            if(set.getFetchSize()==1){
                myObject.put("success",true);
                user.setUuid(UUID.fromString(set.getString("uuid")));
            }else{
                System.out.println(1);
                myObject.put("success",false);
            }
        }else{
            System.out.println(2);
            myObject.put("success",false);
        }
        response.put("data",myObject);
    }

    private void register(ClientHandler user, JSONObject myObject, String username, String password){

    }

    //new fucntion
    private void addQuestion(JSONObject myObject, String text, int category_id, String uuid, byte[][] images) throws SQLException {
        CallableStatement cStmt = MainServer.getConnection().prepareCall("{call insertquestion(?,?,?)}");
        cStmt.setString("message",text);
        cStmt.setInt("category",category_id);
        cStmt.setString("uuid",uuid);
        cStmt.registerOutParameter("qid", Types.INTEGER);

        cStmt.execute();

        int qid = cStmt.getInt("qid");
        for(int i=0;i<images.length;i++){
            CallableStatement imStmt = MainServer.getConnection().prepareCall("{call setgraphic(?,?,?)}");
            imStmt.setInt("pid",qid);
            imStmt.setBlob("image",new SerialBlob(images[i]));
            imStmt.setBoolean("isquestion",true);

            cStmt.execute();
        }
        myObject.put("success",true);
        response.put("data",myObject);
    }

    /*//działa (dodać avatar)
    private void loadProfil(String uuid) throws SQLException {
        ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE uuid='"+uuid+"';");
        set.next();
        ResultSet questions = MainServer.createStatement().executeQuery("SELECT COUNT(*) AS `abc` FROM Question WHERE uuid='"+uuid+"';");
        questions.next();
        ResultSet Miasto = MainServer.createStatement().executeQuery("SELECT city FROM Localisation WHERE id="+set.getInt(8));
        Miasto.next();
        ResultSet answersUser = MainServer.createStatement().executeQuery("SELECT COUNT(*) AS `blah` FROM Answer WHERE uuid = '"+uuid+"';");
        answersUser.next();

        JSONArray array = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("nick",set.getString("nick"));
        user.put("name",set.getString("name"));
        user.put("surname",set.getString("surname"));
        user.put("points",set.getInt("points"));
        user.put("isPremium",set.getBoolean("isPremium"));
        user.put("avatar",Base64.getEncoder().encodeToString(set.getBlob("avatar").getBytes(1l,(int) set.getBlob("avatar").length())));
        user.put("questions",questions.getInt("abc"));
        user.put("answers",answersUser.getInt("blah"));
        user.put("city",Miasto.getString("city"));
        array.put(user);

        JSONArray questions2 = new JSONArray();
        ResultSet set2 = MainServer.createStatement().executeQuery("SELECT q.id AS `a`, q.uuid AS `b`, r.nick AS `c`, r.avatar AS `d`, c.name AS `e`, q.text AS`f`, q.isFinished AS `g`, q.views AS `h`, q.date AS `i` FROM Question q INNER JOIN Category c ON c.id=q.categoryId INNER JOIN RegisteredUser r ON r.uuid=q.uuid WHERE r.uuid = '"+uuid+"' ORDER BY q.date DESC LIMIT 0,10");
        while(set2.next()){
            ResultSet Answers = MainServer.createStatement().executeQuery("SELECT Count(*) AS `j` FROM Answer WHERE questionId = "+set2.getInt("a")+';');
            Answers.next();
            JSONObject user2 = new JSONObject();
            user2.put("qid",set2.getInt("a"));
            user2.put("uid",set2.getString("b"));
            user2.put("nick",set2.getString("c"));
            user2.put("category", set2.getString("e"));
            user2.put("text", set2.getString("f"));
            user2.put("isFinished",set2.getBoolean("g"));
            user2.put("views",set2.getInt("h"));
            user2.put("date",set2.getTimestamp("i"));
            user2.put("answersAmount",Answers.getInt("j"));
            //user.put("avatar",User.getBlob("d")==null? "null": new SerialBlob(User.getBlob("avatar").getBytes(1l,(int) User.getBlob("avatar").length())));
            questions2.put(user2);
        }
        array.put(questions2);

        response.put("data",array);
    }

    //działa (dodać avatar)
    private void getFirstTenQuestions() throws SQLException, IOException {
        ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM Question ORDER BY date DESC LIMIT 0, 10");

        JSONArray questions = new JSONArray();
        while(set.next()){
            ResultSet User = MainServer.createStatement().executeQuery("SELECT nick, avatar FROM RegisteredUser WHERE uuid = '"+set.getString(7)+"';");
            User.next();
            ResultSet Category = MainServer.createStatement().executeQuery("SELECT `name` FROM Category WHERE id = "+set.getInt(5)+';');
            Category.next();
            ResultSet Answers = MainServer.createStatement().executeQuery("SELECT Count(*) AS `abc` FROM Answer WHERE questionId = "+set.getInt(5)+';');
            Answers.next();
            JSONObject user = new JSONObject();
            user.put("qid",set.getInt("id"));
            user.put("uid",set.getString("uuid"));
            user.put("nick",User.getString("nick"));
            user.put("category", Category.getString("name"));
            user.put("text", set.getString("text"));
            user.put("isFinished",set.getBoolean("isFinished"));
            user.put("views",set.getInt("views"));
            user.put("date",set.getTimestamp("date"));
            user.put("answersAmount",Answers.getInt("abc"));
            //user.put("avatar",User.getBlob("avatar")==null? "null": new SerialBlob(User.getBlob("avatar").getBytes(1l,(int) User.getBlob("avatar").length())));
            questions.put(user);
        }
        response.put("data",questions);
    }

    //działa
    private void isUserInDB(String user, String password) throws SQLException {
        user=user.toLowerCase();
        if(!user.contains("--") && !password.contains("--") && !user.contains(";") && !password.contains(";")) {
            ResultSet set = MainServer.createStatement().executeQuery("SELECT COUNT(*) AS `a`, uuid `b` FROM RegisteredUser WHERE nick = '" + user + "' AND password = '" + password+"' GROUP BY uuid;");
            if (set.next()) {
                response.put("isPresent", true);
                response.put("uuid",set.getString("b"));
            }
            else{
                response.put("isPresent", false);
                response.put("uuid", "null");
            }
        }else{
            response.put("isPresent", false);
            response.put("uuid", "null1");
        }


    }

    //dodać avatar, zrobić test
    private void loadAnswers(int qid, String uuid) throws SQLException {
        JSONObject object =  new JSONObject();
        ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM User WHERE uuid='"+uuid+"';");
        ResultSet set2 = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE uuid='"+uuid+"';");
        set.next();
        if(set2.next()){
            int searches = set.getInt("searches");
            boolean premium = set2.getBoolean("isPremium");
            if(searches<=7 || premium){
                //możesz zdobyć te pytania
                ResultSet answers = MainServer.createStatement().executeQuery("SELECT * FROM Answer a INNER JOIN RegisteredUser r ON r.uuid = a.uuid WHERE questionId="+qid+";");
                JSONArray answer =  new JSONArray();
                while (answers.next()){
                    JSONObject temp = new JSONObject();
                    int aid = answers.getInt("id");
                    String text = answers.getString("text");
                    String nick = answers.getString("nick");
                    int likes = answers.getInt("like");
                    int pid = answers.getInt("parentId");
                    temp.put("aid",aid);
                    temp.put("text",text);
                    temp.put("nick",nick);
                    temp.put("likes",likes);
                    temp.put("pid",pid);
                    answer.put(temp);
                }
                response.put("data",answer);
                MainServer.createStatement().executeUpdate("UPDATE User SET searches="+(++searches)+" WHERE uuid = '"+uuid+"';");
            }else{
                response.put("data",new JSONArray().put(false));
            }
        }else{
            int searches = set.getInt("searches");
            if(searches<=3){
                //możesz zdobyć te pytania
                ResultSet answers = MainServer.createStatement().executeQuery("SELECT * FROM Answer a INNER JOIN RegisteredUser r ON r.uuid = a.uuid WHERE questionId="+qid+";");
                JSONArray answer =  new JSONArray();
                while (answers.next()){
                    JSONObject temp = new JSONObject();
                    int aid = answers.getInt("id");
                    String text = answers.getString("text");
                    String nick = answers.getString("nick");
                    int likes = answers.getInt("like");
                    int pid = answers.getInt("parentId");
                    temp.put("aid",aid);
                    temp.put("text",text);
                    temp.put("nick",nick);
                    temp.put("likes",likes);
                    temp.put("pid",pid);
                    answer.put(temp);
                }
                response.put("data",answer);
                MainServer.createStatement().executeUpdate("UPDATE User SET searches="+(++searches)+" WHERE uuid = '"+uuid+"';");
            }else{
                response.put("data",new JSONArray().put(false));
            }
        }

    }

    //to test
    private void handshake(String telId) throws SQLException {
        ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM User WHERE telId = '"+telId+"';");
        UUID uuid;
        if(set.getFetchSize()==0){
            uuid = UUID.randomUUID();
            int results=1;
            while(results!=0){
                set = MainServer.createStatement().executeQuery("SELECT * FROM User WHERE uuid = '"+uuid.toString()+"';");
                results=set.getFetchSize();
            }
           MainServer.createStatement().executeUpdate("INSERT INTO User VALUES('"+uuid+"',"+0+",+'"+telId+"');");
        }else{
            uuid=UUID.fromString(set.getString(0));
            MainServer.createStatement().executeUpdate("UPDATE User SET telId='"+telId+"' WHERE uuid = '"+uuid+"';");
        }
        response.put("uuid",uuid.toString());
    }

    //to test
    private void getRankingInCity(String Nazwa, int DownLimit, int UpLimit) throws SQLException, JSONException {
        ResultSet set = MainServer.createStatement().executeQuery("SELECT ru.Nick `a`, ru.points `b`, l.city `c` FROM RegisteredUser ru INNER JOIN Localisation l on l.id = ru.id WHERE l.city ='" + Nazwa + "' ORDER BY ru.points DESC;");
        int i = 0;
        set.absolute(DownLimit);
        JSONObject jsonObject = new JSONObject();
        while( set.next() || i == UpLimit){
            JSONArray arr = new JSONArray();
            arr.put(set.getString("a"));
            arr.put(set.getString("b"));
            arr.put(set.getString("c"));
            i++;
        }
        response.put("data",jsonObject);
    }

    //działa
    private void register(String name, String surname, String nick, String password, String email, String uuid) throws SQLException{
        ResultSet sett = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE nick='"+nick+"'");
        if(sett.next())
        {
            response.put("success",false);
            return;
        }
        int set = MainServer.createStatement().executeUpdate("INSERT INTO RegisteredUser VALUES('"+uuid+"','"+nick+"','"+name+"','"+surname+"','"+email+"','"+password+"',0,0,0,NULL)");
        response.put("success", set != 0);
    }

    //działa
    private void questionAdd(String text, String category, String uuid) throws SQLException {
        JSONObject temp = new JSONObject();
        ResultSet MySet = MainServer.createStatement().executeQuery("SELECT * FROM User WHERE uuid = '"+uuid+"';");
        MySet.next();
        ResultSet MySet2 = MainServer.createStatement().executeQuery("SELECT * FROM RegisteredUser WHERE uuid = '"+uuid+"';");
        if(MySet2.next()){
            if(MySet2.getBoolean("isPremium")||MySet.getInt("searches")<=7){
                ResultSet valu = MainServer.createStatement().executeQuery("SELECT MAX(id) AS `a` FROM Question");
                valu.next();
                ResultSet valu2 = MainServer.createStatement().executeQuery("SELECT id `a` FROM Category WHERE name = '"+category+"'");
                valu2.next();
                int set = MainServer.createStatement().executeUpdate("INSERT INTO Question (`id`,`text`,`isFinished`,`categoryId`,`views`,`uuid`) VALUES ("+(valu.getInt("a")+1)+",'"+text+"',0,'"+valu2.getInt("a")+"',0,'"+uuid+"');");
                temp.put("success",set != 0);
                if(set != 0){
                    ResultSet val = MainServer.createStatement().executeQuery("SELECT searches FROM User WHERE uuid = '"+uuid+"'");
                    val.next();
                    MainServer.createStatement().executeUpdate("UPDATE User SET searches = "+val.getInt("searches")+" WHERE uuid= '"+uuid+"';");
                }
            }else{
                temp.put("success",false);
            }
        }else{
            if(MySet.getInt("searches")<=3) {
                ResultSet valu = MainServer.createStatement().executeQuery("SELECT MAX(id) AS `a` FROM Question");
                valu.next();
                ResultSet valu2 = MainServer.createStatement().executeQuery("SELECT id `a` FROM Category WHERE name = '"+category+"'");
                valu2.next();
                int set =  MainServer.createStatement().executeUpdate("INSERT INTO Question (`id`,`text`,`isFinished`,`categoryId`,`views`,`uuid`) VALUES ("+(valu.getInt("a")+1)+",'"+text+"',0,'"+valu2.getInt("a")+"',0,'"+uuid+"');");
                temp.put("success", set != 0);
                if(set != 0){
                    ResultSet val = MainServer.createStatement().executeQuery("SELECT searches FROM User WHERE uuid = '"+uuid+"';");
                    val.next();
                    MainServer.createStatement().executeUpdate("UPDATE User SET searches = "+val.getInt("searches")+" WHERE uuid= '"+uuid+"';");
                }
            }else{
                temp.put("success",false);
            }
        }
        response.put("data",temp);

    }

    //to test
    private void logout(String telId) throws SQLException{
        int set = MainServer.createStatement().executeUpdate("UPDATE User SET telId = NULL WHERE telId = '"+telId+"';");
        if(set!=0)
            handshake(telId);

    }

    //to test
    private void verifyTel(String uuid, String tel) throws SQLException {
        MainServer.createStatement().executeUpdate("UPDATE User SET telId = '"+tel+"' WHERE uuid = '"+uuid+"';");
    }

    //to test
    private void keywords(JSONArray array) throws SQLException {
        List<Object> list = array.toList();
        StringBuilder queryString =  new StringBuilder();
        for(Object o: list){
            queryString.append("text like '%"+((String)o)+"%' OR ");
        }
        queryString.delete(queryString.length()-4,queryString.length());
        JSONArray questions = new JSONArray();
        ResultSet set2 = MainServer.createStatement().executeQuery("SELECT q.id AS `a`, q.uuid AS `b`, r.nick AS `c`, r.avatar AS `d`, c.name AS `e`, q.text AS`f`, q.isFinished AS `g`, q.views AS `h`, q.date AS `i` FROM Question q INNER JOIN Category c ON c.id=q.categoryId INNER JOIN RegisteredUser r ON r.uuid=q.uuid WHERE "+queryString.toString()+";" );

        while(set2.next()){
            ResultSet Answers = MainServer.createStatement().executeQuery("SELECT Count(*) AS `j` FROM Answer WHERE questionId = "+set2.getInt("a")+';');
            Answers.next();
            JSONObject user2 = new JSONObject();
            user2.put("qid",set2.getInt("a"));
            user2.put("uid",set2.getString("b"));
            user2.put("nick",set2.getString("c"));
            user2.put("category", set2.getString("e"));
            user2.put("text", set2.getString("f"));
            user2.put("isFinished",set2.getBoolean("g"));
            user2.put("views",set2.getInt("h"));
            user2.put("date",set2.getTimestamp("i"));
            user2.put("answersAmount",Answers.getInt("j"));
            //user.put("avatar",User.getBlob("d")==null? "null": new SerialBlob(User.getBlob("avatar").getBytes(1l,(int) User.getBlob("avatar").length())));
            questions.put(user2);
        }
        response.put("data",questions);


    }

    //to test
    private void putAnswer(int id, String text, int questionId, int like, String uuid) throws SQLException {
        int set = MainServer.createStatement().executeUpdate("INSERT INTO Answer VALUES(" + id +","+  text +","+ questionId +","+ like +","+ uuid+");");
        int points = 0;
        ResultSet setN = MainServer.createStatement().executeQuery("SELECT uuid 'uuid', like 'like' FROM Answer WHERE uuid =" + uuid + ";");
        if(setN.next()){
            ResultSet setP = MainServer.createStatement().executeQuery("SELECT points 'points' FROM RegisteredUser WHERE uuid =" + uuid + ";");
            points += setP.getInt("points") ;
            if(setN.next()){
                points += 1 +  setN.getInt("like")  * 2;
                int setU = MainServer.createStatement().executeUpdate("UPDATE RegisteredUser SET(" + points + ") WHERE uuid=" + uuid + ";");
            }
            response.put("success", set !=0); // ???
        }
    }
    */
}
