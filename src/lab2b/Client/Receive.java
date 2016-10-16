package lab2b.Client;

import java.io.BufferedReader;
import java.net.InetAddress;

class Receive extends Thread{
    private String sendUsername;
    private final BufferedReader sin;
    private final Client client;
    private final StateHandler sh;

    public Receive(BufferedReader sin,StateHandler sh,Client client){
        this.sin = sin;
        this.sh = sh;
        this.client = client;
    }
    public void run(){
        try {
            String responseMsg;
            boolean run = true;
            while (run){
                responseMsg = sin.readLine();
                if (responseMsg != null) {
                    if (responseMsg.equals("You have been disconnected.")) {
                        run = false;
                        client.endClient();
                    }
                    else if (responseMsg.startsWith("SIP"))
                        handleMessage(responseMsg);
                    else
                        System.out.println(responseMsg);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSendUsername(String sendUsername) {
        this.sendUsername = sendUsername;
    }

    private void handleMessage(String msg){
        SIPCommand type = getSIPCommand(msg.toUpperCase());
        String[] array;
        switch (type){
            case INVITE:
                array = msg.split(" ");
                sendUsername = array[3];
                client.setToUsername(sendUsername);
                try{
                    client.setRemoteIpAddress(InetAddress.getByName(array[5]));
                }catch (Exception e){
                    e.printStackTrace();
                }
                client.setPort(Integer.parseInt(array[6]));
                client.setIncomingCall();
                System.out.println("Incoming call");
                System.out.print("Do you wanna accept? ");
                break;

            case TRO:
                array = msg.split(" ");
                try {
                    client.setRemoteIpAddress(InetAddress.getByName(array[2]));
                }catch (Exception e){
                    e.printStackTrace();
                }
                sh.InvokeCallConfirmation(sendUsername);
                client.setStream();
                client.connectTo();
                client.startStream();
                System.out.println("The conversation has begun with " + sendUsername);
                break;

            case ACK:
                sh.InvokeCallAccepted();
                client.setStream();
                client.connectTo();
                client.startStream();
                System.out.println("The conversation has begun with " + sendUsername);
                break;

            case BYE:
                sh.InvokeAbortSession(sendUsername);
                client.stopStream();
                break;

            case OK:
                sh.InvokeEndSessionConfirmation();
                client.stopStream();
                break;

            case CANCEL:
                sh.InvokeResetState();
                break;

            default:
        }
    }

    private SIPCommand getSIPCommand(String msg){
        if(msg.startsWith("SIP INVITE"))
            return SIPCommand.INVITE;
        else if (msg.startsWith("SIP TRO"))
            return SIPCommand.TRO;
        else if (msg.startsWith("SIP 200 OK"))
            return SIPCommand.OK;
        else if (msg.startsWith("SIP ACK"))
            return SIPCommand.ACK;
        else if (msg.startsWith("SIP BYE"))
            return SIPCommand.BYE;
        else if (msg.startsWith("SIP CANCEL"))
            return SIPCommand.CANCEL;
        return SIPCommand.UNKNOWN;
    }
}
