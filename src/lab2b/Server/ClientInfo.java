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
    private boolean inSession = false;
    private int inSessionWithID;
    private boolean User;

    public ClientInfo(Server server) {
        this.id = 0;
        this.server = server;
        username = "";
        User = false;
    }

    public ClientInfo(Socket socket,int id,Server server) throws IOException {
        this.id = id;
        this.server = server;
        printWriter = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ipAddress = socket.getInetAddress();
        username = "";
        User = true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void run(){
        try {
            while (run) {
                String msg = reader.readLine();
                if (msg != null) {
                    System.out.println(username + ": " + msg);
                    if (msg.startsWith("/") || msg.startsWith("SIP"))
                        handleCommand(msg);
                    else
                        handleMessage(msg);
                }
            }
        }catch (NullPointerException ignored){
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            server.removeClient(id);
            System.out.println(id + " - " + username + ": has disconnected.");
        }
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
                if (!server.UserExist(msg)) {
                    setUsername(msg);
                    send("OK Nickname changed.\n");
                }else
                    send("Nickname in use.\n");
                break;

            case "/who":
                send(server.getAllClientNames(id));
                break;

            case "/error":
                //Get list of error
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
                send("You have been disconnected.\n");
                break;

            default:
                send("Invalid command\n");
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
        else if (msg.toLowerCase().equals("/error"))
            return "/error";
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
        ClientInfo tmp;
        String[] array;
        array = msg.split(" ");
        boolean sendMsg=false;
        switch (command){
            case "INVITE":
                tmp = server.GetUser(array[2]);
                if(tmp.User) {
                    if (!username.equals(tmp.username)) {
                        if (!this.inSession) {
                            if (!tmp.inSession) {
                                this.inSession = true;
                                tmp.inSession = true;

                                this.inSessionWithID = tmp.id;
                                tmp.inSessionWithID = this.id;

                                msg = msg.replace(("#" + tmp.username), tmp.ipAddress.toString());
                                sendMsg = true;
                                break;
                            }
                        }
                        send("SIP CANCEL BUSY");
                    }else
                        send("SIP CANCEL Can't call yourself.");
                }else
                    send("SIP CANCEL User don't exist.");
                break;


            case "TRO":
                tmp = server.GetUser(array[2]);
                if(tmp.User) {
                    msg = msg.replace(" " + array[2], " " + ipAddress.getHostAddress());
                    sendMsg = true;
                }
                break;

            case "ACK":
                tmp = server.GetUser(array[2]);
                if(tmp.User) {
                    msg = msg.replace(" " + array[2], "");
                    sendMsg = true;
                }
                break;

            case "BYE":
                tmp = server.GetUser(array[2]);
                if(tmp.User) {
                    if (this.inSession) {
                        if (tmp.inSession) {
                            if (tmp.id == this.inSessionWithID && tmp.inSessionWithID == this.id) {
                                msg = msg.replace(" " + array[2], "");
                                this.inSession = false;
                                this.inSessionWithID = -1;
                                tmp.inSession = false;
                                tmp.inSessionWithID = -1;
                                sendMsg = true;
                                break;
                            }
                        } else {
                            send("SIP CANCEL RECEIVER NOT IN SESSION");
                            break;
                        }
                    } else {
                        send("SIP CANCEL NOT IN SESSION");
                    }
                }
                break;

            case "200":
                tmp = server.GetUser(array[3]);
                if(tmp.User) {
                    msg = msg.replace(" " + array[3], "");
                    sendMsg = true;
                }
                break;

            case "CANCEL":
                tmp = server.GetUser(array[2]);
                if(tmp.User) {
                    msg = msg.replace(" " + array[2], "");
                    sendMsg = true;
                }
                break;

            case "ABORT":
                tmp = server.GetUser(inSessionWithID);
                if(tmp.User) {
                    if (this.inSession) {
                        if (tmp.inSession) {
                            if (tmp.id == this.inSessionWithID && tmp.inSessionWithID == this.id) {
                                this.inSession = false;
                                this.inSessionWithID = -1;
                                tmp.inSession = false;
                                tmp.inSessionWithID = -1;
                                msg = "\nSIP CANCEL The other party aborted session.";
                                sendMsg = true;
                                break;
                            } else {
                                this.inSession = false;
                                inSessionWithID = -1;
                            }
                        }
                    }
                }
                break;

            default:
                System.out.println(id + " - " + username + ": Handle SIP: invalid sip.");
                send("SIP CANCEL Invalid sip command.");
                tmp=server.GetUser(inSessionWithID);
                msg = "SIP CANCEL Something went wrong.";
                inSession = false;
                inSessionWithID = -1;
                tmp.inSession = false;
                tmp.inSessionWithID = -1;
                sendMsg = true;
                break;
        }

        if (sendMsg)//tmp.User && !username.equals(tmp.username))
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
        }else if (msg.toUpperCase().startsWith("SIP ABORT")){
            return "ABORT";
        }
        return "UNKNOWN";
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
