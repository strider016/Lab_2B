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

    public String getHostAddress() { return ipAddress.getHostAddress(); }

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

                //handles the SIP task e.g. invite, ack, tro
                handleSIP(msg);
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

    private void handleSIP(String msg){

        String command = extractSIP(msg);
        ClientInfo tmp = null;

        String[] array;
        array = msg.split(" ");

        switch (command){
            case "INVITE":
                tmp = server.GetUser(array[2]);
                msg = msg.replace(("#" + tmp.username), tmp.ipAddress.toString());
                break;
            //testasdasdfasdaasdasd

            case "TRO":
                tmp = server.GetUser(array[2]);
                msg = msg.replace(" "+array[2],"");
                break;

            case "ACK":
                tmp = server.GetUser(array[2]);
                msg = msg.replace(" "+array[2],"");
                break;

            case "BYE":
                tmp = server.GetUser(array[2]);
                msg = msg.replace(" "+array[2],"");
                break;

            case "200":
                tmp = server.GetUser(array[3]);
                msg = msg.replace(" "+array[3],"");
                break;

            case "CANCEL":
                tmp = server.GetUser(array[2]);
                msg = msg.replace(" "+array[2],"");
                break;

            default:
                System.out.println("handle sip: invalid sip ");
                break;
        }

        assert tmp != null;
        tmp.send(msg);
    }

    private String extractSIP(String msg){
        if (msg.toUpperCase().startsWith("SIP INVITE")){
            return "INVITE";
        }else if (msg.toUpperCase().startsWith("SIP TRO ")){
            return "TRO";
        }else if (msg.toUpperCase().startsWith("SIP ACK ")){
            return "ACK";
        }else if (msg.toUpperCase().startsWith("SIP BYE ")){
            return "BYE";
        }else if (msg.toUpperCase().startsWith("SIP 200")){
            return "200";
        }else if (msg.toUpperCase().startsWith("SIP CANCEL")){
            return "CANCEL";
        }
        return null;
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
