package lab2b.Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class Server {
    private Map<Integer,ClientInfo> clients;
    public static void main(String[] args){
        Server server = new Server();
        server.Start(args);
    }

    private void Start(String[] args){
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocket socket = new ServerSocket(port);
            clients = new HashMap<>();
            clients.put(0,new ClientInfo(this));
            clients.get(0).setUsername("Announcer");
            int seq = 1;
            System.out.println("Waiting for connection...");
            //noinspection InfiniteLoopStatement
            while (true){
                Socket clientSocket = socket.accept();
                clients.put(seq,new ClientInfo(clientSocket,seq,this));
                clients.get(seq).start();
                seq++;
            }
        } catch (Exception e){
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
                clients.entrySet().stream().filter(entry -> !entry.getKey().equals(id) && entry.getKey() != 0).forEach(entry -> entry.getValue().send(clients.get(id).getUsername() + ": " + msg));
            }catch (Exception e){
                System.out.println("ERROR: Could not send message to all clients.");
                e.printStackTrace();
            }
        }
    }

    public synchronized String getAllClientNames(int id){
        StringBuilder sb = new StringBuilder();
        clients.entrySet().stream().filter(entry -> entry.getKey() != id && entry.getKey() != 0).forEach(entry -> sb.append(entry.getValue().getUsername()).append('\n'));
        return sb.toString();
    }

    public synchronized ClientInfo GetUser(String username){
        for (Map.Entry<Integer,ClientInfo> entry: clients.entrySet()) {
            if (entry.getValue().getUsername().equals(username))
                return entry.getValue();
        }
        return null;
    }

    public synchronized ClientInfo GetUser(int id){
        return clients.get(id);
    }
}
