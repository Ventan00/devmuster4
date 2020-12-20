import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

public class ClientHandler extends Thread {
    private UUID uuid;
    private Socket connection;
    public ClientHandler(Socket connection){
       this.connection=connection;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run(){
       try{
           DataInputStream dis = new DataInputStream(connection.getInputStream());
           DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
           while (true) {
               String input = dis.readUTF();
               JSONObject jo = new JSONObject(input);
               System.out.println("\u001B[33m"+"<--"+"\u001B[0m"+connection.getRemoteSocketAddress()+" called: "+jo.getString("function")+"\n");
               JSONObject response;
               if(jo.getJSONObject("data").has("img")){
                   int amount = jo.getJSONObject("data").getInt("img");
                   byte[][] images = new byte[amount][];
                   for(int i=0;i<amount;i++){
                       int lenght = dis.readInt();
                       byte[] temp = new byte[lenght];
                       dis.read(temp);
                       images[i]=temp;
                   }
                   response = new MessegeManager(this,jo.getString("function"), jo.getJSONObject("data"),images).getResponse();
               }else{
                   response = new MessegeManager(this,jo.getString("function"), jo.getJSONObject("data")).getResponse();
               }

               if(response.getJSONObject("data").has("img")){
                   JSONArray array = response.getJSONObject("data").getJSONArray("img");
                   response.getJSONObject("data").put("img",array.length());
                   byte[][] images = new byte[array.length()][];
                   for(int i=0;i<array.length();i++){
                       images[i] = Base64.getDecoder().decode(array.getString(i));
                   }
                   System.out.println("\u001B[33m"+"-->"+"\u001B[0m"+" Sending data for user: "+connection.getRemoteSocketAddress()+"\n");
                   dos.writeUTF(response.toString());
                   System.out.println("\u001B[33m"+"-->"+"\u001B[0m"+" Sending images for user: "+connection.getRemoteSocketAddress()+"\n");
                   for(int i=0;i<array.length();i++){
                       dos.write(images[i]);
                   }
               }else{
                   System.out.println("\u001B[33m"+"-->"+"\u001B[0m"+" Sending data for user: "+connection.getRemoteSocketAddress()+"\n");
                   dos.writeUTF(response.toString());
               }
           }
       }catch(Exception e){
           System.out.println("\u001B[31m"+" User "+connection.getRemoteSocketAddress()+" disconnected!"+"\u001B[0m");
           MainServer.disconnect(this);
       }
   }
}
