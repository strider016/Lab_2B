package lab2b.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rasmusjansson on 2016-09-26.
 */
public class Server {
    private Map<Integer,ClientInfo> clients;
    private ServerSocket socket;
    public static void main(String[] args){
        Server server = new Server();
        server.Start(args);
    }

    public void Start(String[] args){
        try {
            int port = Integer.parseInt(args[0]);
            socket = new ServerSocket(port);
            clients = new HashMap<>();
            clients.put(0,new ClientInfo(0,this));
            clients.get(0).setUsername("Announcer");
            int seq = 1;
            System.out.println("Waiting for connection...");
            while (true){
                Socket clientSocket = socket.accept();
                clients.put(seq,new ClientInfo(clientSocket,seq,this));
                clients.get(seq).start();
                seq++;
            }
        } catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void removeClient(int id){
        String removedUser = clients.get(id).getUsername();
        clients.remove(id);
        sendToAllClients(0,removedUser + " has disconnected.");
    }

    public synchronized void sendToAllClients(int id, String msg){
        if (clients.containsKey(id)){
            try {
                for (Map.Entry<Integer,ClientInfo> entry : clients.entrySet()){
                    if (!entry.getKey().equals(id) && entry.getKey() != 0){
                        entry.getValue().send(clients.get(id).getUsername() + ": " + msg);
                    }
                }
            }catch (Exception e){
                System.out.println("ERROR: Could not send message to all clients.");
                e.printStackTrace();
            }
        }
    }

    public synchronized String getAllClientNames(int id){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer,ClientInfo> entry : clients.entrySet())
            if (entry.getKey() != id && entry.getKey() != 0)
                sb.append(entry.getValue().getUsername() + '\n');
        return sb.toString();
    }
}
