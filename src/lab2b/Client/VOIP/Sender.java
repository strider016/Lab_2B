package lab2b.Client.VOIP;

import java.net.*;
import javax.sound.sampled.*;

/**
 * Based of the code provided by Johnny Panrike.
 * Code is based of that one found in AudioStreamUDP.
 */
class Sender extends Thread{
    private final DatagramSocket socket;
    private final AudioFormat format;
    private InetAddress remoteAddress;
    private int remotePort;
    private TargetDataLine line;
    private boolean inConversation = true;

    public Sender(DatagramSocket socket,AudioFormat format){
        this.format = format;
        this.socket = socket;
    }

    public void connectTo(InetAddress remoteAddress, int remotePort){
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    public synchronized void stopConversation(){
        inConversation = false;
    }

    public void run(){
        try {
            initializeLine();

            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / MediaStream.BUFFER_VS_FRAMES_RATIO;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            DatagramPacket packet;
            int numBytesRead;

            line.start();
            //noinspection UnusedAssignment
            int packets = 0;
            if (MediaStream.RUNNING_DEBUG)
                System.out.println("Sending to: " + remoteAddress + " " +remotePort);
            while (inConversation) {
                if ((numBytesRead = line.read(data,0,bufferLengthInBytes)) == -1){
                    break;
                }
                packet = new DatagramPacket(data,numBytesRead,remoteAddress,remotePort);
                socket.send(packet);
                if (MediaStream.DEBUG){
                    System.out.println("Bytes sent = " + numBytesRead + ", packets = " + packets++);
                }
            }
        }catch (SocketException e){
            System.out.println("Sender socket is closed.");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.cleanUp();
        }
    }

    private void initializeLine() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)){
            System.err.println("Line matching " + info + " not supported.");
            return;
        }

        line = getTargetDataLine(format);
        if (!line.isOpen())
            line.open(format,line.getBufferSize());
    }

    private void cleanUp(){
        try {
            if (line != null){
                line.stop();
                line.close();
            }
        }catch (Exception ignored){}
    }

    protected void finalize(){
        try {
            this.cleanUp();
            super.finalize();
        }catch (Throwable ignored){}
    }

    /**
     * Thanks to: Paulo Levi.
     * Lines can fail to open because they are already in use.
     * Java sound uses OSS and some linuxes are using pulseaudio.
     * OSS needs exclusive access to the line, and pulse audio
     * highjacks it. Try to open another line.
     * @param format of audio.
     * @return a open line
     * @throws IllegalStateException if it can't open a dataline for the
     * audioformat.
     */
    private TargetDataLine getTargetDataLine(AudioFormat format){
        Exception audioException = null;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,format);

            for (Mixer.Info mi : AudioSystem.getMixerInfo()){
                TargetDataLine dataLine = null;
                try {
                    Mixer mixer = AudioSystem.getMixer(mi);
                    dataLine = (TargetDataLine) mixer.getLine(info);
                    dataLine.open(format);
                    dataLine.start();
                    return dataLine;
                }catch (Exception e){
                    audioException = e;
                }
                if (dataLine != null){
                    try {
                        dataLine.close();
                    }catch (Exception ignored){}
                }
            }
        }catch (Exception e){
            throw new IllegalStateException("Error trying to acquire dataline.",e);
        }
        if (audioException == null){
            throw new IllegalStateException("Couldn't acquire a dataline,"+
                    " this computer dosen't seem to have audio output?");
        }else {
            throw new IllegalStateException("Couldn't acquire a dataline,"+
                    " probably because all are in use. Last exception:",audioException);
        }
    }
}
