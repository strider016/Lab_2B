package lab2b.Client.VOIP;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class MediaStream {
    static final int BUFFER_VS_FRAMES_RATIO = 16; //32
    static final boolean DEBUG= false;
    private static final int TIME_OUT = 5000;

    private Receiver receiver = null;
    private Sender sender = null;
    private DatagramSocket receiverSocket,senderSocket;

    public MediaStream(int port) throws IOException{
        receiverSocket = new DatagramSocket(port);
        receiverSocket.setSoTimeout(TIME_OUT);
        senderSocket = new DatagramSocket();

        AudioFormat format = new AudioFormat(22050, 16, 1, true, true);
        receiver = new Receiver(receiverSocket, format);
        sender = new Sender(senderSocket, format);
    }

    public synchronized void connectTo(InetAddress remoteAddress, int remotePort){
        sender.connectTo(remoteAddress,remotePort);
        receiver.connectTo(remoteAddress);
    }

    public synchronized void startStream(){
        //receiver.startConversation();
        //sender.startConversation();
        receiver.start();
        sender.start();
    }

    public synchronized void stopStream(){
        receiver.stopConversation();
        sender.stopConversation();
    }

    public synchronized void close(){
        if (receiverSocket != null)
            receiverSocket.close();
        if (senderSocket != null)
            senderSocket.close();
    }
}
