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
                }else{
                    System.out.println("Server is unreachable.");
                    run = false;
                    client.endClient();
                }
            }
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    public void setSendUsername(String sendUsername) {
        this.sendUsername = sendUsername;
    }

    private void handleMessage(String msg){
        SIPCommand type = getSIPCommand(msg.toUpperCase());
        String[] array;
        if (Client.StaticGetDebug())
            System.out.println(msg);
        try {
            switch (type) {
                case INVITE:
                    if (sh.InvokeGetState() == lab2b.Client.State.State.IDLE) {
                        array = msg.split(" ");
                        sendUsername = array[3];
                        client.setToUsername(sendUsername);
                        try {
                            client.setRemoteIpAddress(InetAddress.getByName(array[5]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        client.setPort(Integer.parseInt(array[6]));
                        client.setIncomingCall();
                        System.out.println("Incoming call");
                        System.out.print("Do you wanna accept? ");
                    }
                    break;

                case TRO:
                    sh.InvokeCallConfirmation(sendUsername,msg);
                    break;

                case ACK:
                    sh.InvokeCallAccepted(sendUsername);
                    break;

                case BYE:
                    sh.InvokeAbortSession(sendUsername);
                    break;

                case OK:
                    sh.InvokeEndSessionConfirmation();
                    break;

                case CANCEL:
                    sh.InvokeResetState(msg);
                    client.stopStream();
                    break;

                default:
                    sh.InvokeResetState();
            }
        }catch (Exception e){
            e.printStackTrace();
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
