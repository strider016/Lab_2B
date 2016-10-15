package lab2b.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientInfo extends Thread{
    private String username;
    private final int id;
    private PrintWriter printWriter;
    private BufferedReader reader;
    private boolean run = true;
    private final Server server;
    private InetAddress ipAddress;

    public ClientInfo(Server server) {
        this.id = 0;
        this.server = server;
        username = "";
    }

    public ClientInfo(Socket socket,int id,Server server) throws IOException {
        this.id = id;
        this.server = server;
        printWriter = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ipAddress = socket.getInetAddress();
        username = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void run(){
        try {
            while (run){
                String msg = reader.readLine();
                if(msg.startsWith("/") || msg.startsWith("SIP"))
                    handleCommand(msg);
                else
                    handleMessage(msg);
            }
        }catch(NullPointerException ignored){

        }catch (Exception e){
            e.printStackTrace();
        }
        server.removeClient(id);
        System.out.println(id + " - " + username + ": has disconnected.");
    }

    public void send(String msg){
        try {
            printWriter.println(msg);
            printWriter.flush();
        }catch (NullPointerException e){
            run = false;
        }
    }

    private void handleCommand(String msg){
        String command = extractCommand(msg);
        msg = extractMessage(command,msg);
        switch (command){
            case "/help":
                send(getCommandList());
                break;

            case "/nick":
                if (server.GetUser(msg) == null) {
                    setUsername(msg);
                    send("OK Nickname changed");
                }else
                    send("Nickname in use");
                break;

            case "/who":
                send(server.getAllClientNames(id));
                break;

            case "/whoami":
                send(username);
                break;

            case "sip":
                ClientInfo tmp = null;
                System.out.println(msg);
                if (msg.startsWith("SIP INVITE")) {
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[2]);
                    msg = msg.replace(("#" + tmp.username), tmp.ipAddress.toString());
                    System.out.println(msg);
                }else if (msg.startsWith("SIP TRO ")){
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[2]);
                    msg = msg.replace(" "+array[2],"");
                    System.out.println(msg);
                }else if (msg.startsWith("SIP ACK ")){
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[2]);
                    msg = msg.replace(" "+array[2],"");
                    System.out.println(msg);
                }else if (msg.startsWith("SIP BYE ")){
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[2]);
                    msg = msg.replace(" "+array[2],"");
                    System.out.println(msg);
                }else if (msg.startsWith("SIP 200")){
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[3]);
                    msg = msg.replace(" "+array[3],"");
                    System.out.println(msg);
                }else if (msg.startsWith("SIP CANCEL")){
                    String[] array = msg.split(" ");
                    tmp = server.GetUser(array[2]);
                    msg = msg.replace(" "+array[2],"");
                    System.out.println(msg);
                }
                assert tmp != null;
                tmp.send(msg);
                break;

            case "/quit":
                run = false;
                send("You have been disconnected.");
                server.removeClient(id);
                break;

            default:
                send("Invalid command");
        }
    }

    private void handleMessage(String msg){
        server.sendToAllClients(id,msg);
    }

    private String extractCommand(String msg){
        if (msg.toLowerCase().startsWith("/help"))
            return "/help";
        else if (msg.toLowerCase().startsWith("/nick"))
            return "/nick";
        else if (msg.toLowerCase().equals("/who"))
            return "/who";
        else if (msg.toLowerCase().equals("/whoami"))
            return "/whoami";
        else if (msg.toLowerCase().startsWith("sip"))
            return "sip";
        else if (msg.toLowerCase().equals("/quit"))
            return "/quit";
        else
            return "Invalid command";
    }

    private String extractMessage(String cmd,String msg){
        if (cmd.equals("sip"))
            return msg;
        if (cmd.equals(msg.toLowerCase()))
            return msg;
        String[] tmp = msg.split(cmd+" ");
        if (tmp.length>1)
            return tmp[1];
        return "invalid command";
    }

    private String getCommandList(){
        return "/help   \tGet list of all available commands.\n" +
                "/nick   \tChange nickname. /nick <nickname>\n" +
                "/who    \tSee connected users.\n" +
                "/whoami \tWhat's my nickname.\n" +
                "/call   \tCall another user. /call <username>\n" +
                "/quit   \tDisconnect from server.\n";
    }
}
