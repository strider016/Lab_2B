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
            int seq = 1;
            System.out.println("Waiting for connection...");
            while (true){
                Socket clientSocket = socket.accept();
                clients.put(seq,new ClientInfo(clientSocket,seq));
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
        clients.remove(id);
    }
}
