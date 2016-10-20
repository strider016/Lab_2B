package lab2b.Client.VOIP;

import java.net.*;
import javax.sound.sampled.*;

/**
 * Based of the code provided by Johnny Panrike.
 * Code is based of that one found in AudioStreamUDP.
 */
class Receiver extends Thread{
    private InetAddress remoteHost;
    private final DatagramSocket socket;
    private final AudioFormat format;
    private SourceDataLine line;
    private boolean inConversation = true;

    public Receiver(DatagramSocket socket,AudioFormat format){
        this.format = format;
        this.socket = socket;
    }

    public void connectTo(InetAddress remoteHost){
        this.remoteHost = remoteHost;
    }

    public synchronized void stopConversation(){
        inConversation = false;
    }

    public void run() {
        try {
            initializeLine();

            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / MediaStream.BUFFER_VS_FRAMES_RATIO;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            if (MediaStream.DEBUG) {
                System.out.println("bufferLengthInFrames = " + bufferLengthInFrames);
                System.out.println("bufferLengthInBytes = " + bufferLengthInBytes);
            }
            byte[] data = new byte[bufferLengthInBytes];
            DatagramPacket packet = new DatagramPacket(data, bufferLengthInBytes);
            int numBytesRead ;

            line.start();
            int packets = 0;
            System.out.println("Reciving from: " + remoteHost);
            while (inConversation) {
                socket.receive(packet);
                if (remoteHost.equals(packet.getAddress())) {
                    numBytesRead = packet.getLength();
                    if (MediaStream.DEBUG) {
                        System.out.println("Received bytes = " + numBytesRead + ", packets = " + packets++);
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Receive call timed out.");
        } catch (SocketException e) {
            System.out.println("Receiver socket is closed.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }
    }

    private void initializeLine() throws LineUnavailableException{
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)){
            System.err.println("Line matching " + info + " not supported.");
            return;
        }

        line = getSourceDataLine(format);
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
     * @param format
     * @return a open line
     * @throws IllegalStateException if it can't open a dataline for the
     * audioformat.
     */
    private SourceDataLine getSourceDataLine(AudioFormat format){
        Exception audioException = null;
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,format);

            for (Mixer.Info mi : AudioSystem.getMixerInfo()){
                SourceDataLine dataLine = null;
                try {
                    Mixer mixer = AudioSystem.getMixer(mi);
                    dataLine = (SourceDataLine) mixer.getLine(info);
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
