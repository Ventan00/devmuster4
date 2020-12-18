
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class MainServer {
    static String ip = "46.41.140.24";
    static int port = 25321;
    private static Connection conn;
    private static Statement statement;

    private static void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost/devmuster4","pjatk","hackatondev4");
        statement = conn.createStatement();
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Server...");
        createConnection();
        Runnable handlemysql = () -> {
            try {
                handlemysql();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        };
        Thread handlemysqlthread = new Thread(handlemysql);
        handlemysqlthread.start();

        InetSocketAddress adress = new InetSocketAddress(ip,port);
        ServerSocket server = new ServerSocket();
        server.bind(adress);


        System.out.println("Server is running, awaiting for first connection");
        while(true){
            Socket connection = server.accept();
            System.out.println("User connected! IP: "+connection.getRemoteSocketAddress());
            new Thread(()-> {
                try{
                DataInputStream dis = new DataInputStream(connection.getInputStream());
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    while (true) {
                        String input = dis.readUTF();
                        JSONObject jo = new JSONObject(input);
                        System.out.println("message "+input+"\n\n");
                        JSONObject response = new MessegeManager(jo.getString("function"), jo.getJSONArray("data")).getResponse();
                        System.out.println("send data: "+response+"\n\n");
                        dos.writeUTF(response.toString());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }).start();
        }
    }
    private static void handlemysql() throws SQLException, ClassNotFoundException {
        Timestamp lastRefresh = new Timestamp(System.currentTimeMillis());
        while (true){
            if(lastRefresh.compareTo(new Timestamp(System.currentTimeMillis()))>4800000){
                createConnection();
                lastRefresh=new Timestamp(System.currentTimeMillis());
            }
        }
    }
    public static Statement getStatement(){
        return statement;
    }
    public static Statement createStatement(){
        try {
            statement = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }
}
