package lab2b.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientInfo extends Thread{
    private Socket socket;
    private String username;
    private int id;
    private PrintWriter printWriter;
    private BufferedReader reader;
    private boolean run = true;
    private Server server;
    private InetAddress ipAddress;

    public ClientInfo(int id,Server server) throws IOException {
        this.id = id;
        this.server = server;
    }

    public ClientInfo(Socket socket,int id,Server server) throws IOException {
        this.socket = socket;
        this.id = id;
        this.server = server;
        printWriter = new PrintWriter(this.socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        ipAddress = socket.getInetAddress();
    }

    public String getUsername() {
        return username;
    }
    
    public int getClientId() {
        return id;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void run(){
        try {
            while (run){
                String msg = reader.readLine();
                if(msg.startsWith("/"))
                    handleCommand(msg);
                else
                    handleMessage(msg);
            }
        }catch(NullPointerException e){

        }catch (Exception e){
            e.printStackTrace();
        }
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
                setUsername(msg);
                break;

            case "/who":
                send(server.getAllClientNames(id));
                break;

            case "/whoami":
                send(username);
                break;

            case "/call":
                send("Not implemented.");
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
        else if (msg.toLowerCase().startsWith("/call"))
            return "/call";
        else if (msg.toLowerCase().equals("/quit"))
            return "/quit";
        else
            return "Invalid command";
    }

    private String extractMessage(String cmd,String msg){
        if (cmd.equals(msg.toLowerCase()))
            return msg;
        String[] tmp = msg.split(cmd+" ");
        if (tmp.length>1)
            return tmp[1];
        return "invalid command";
    }

    private String getCommandList(){
        StringBuilder sb = new StringBuilder();
        sb.append("/help   \tGet list of all available commands.\n");
        sb.append("/nick   \tChange nickname. /nick <nickname>\n");
        sb.append("/who    \tSee connected users.\n");
        sb.append("/whoami \tWhat's my nickname.\n");
        sb.append("/call   \tCall another user. /call <username>\n");
        sb.append("/quit   \tDisconnect from server.\n");
        return sb.toString();
    }
}
