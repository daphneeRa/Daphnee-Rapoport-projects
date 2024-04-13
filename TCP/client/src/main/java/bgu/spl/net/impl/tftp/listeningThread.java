package bgu.spl.net.impl.tftp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class listeningThread implements Runnable {

    private BufferedInputStream in;
    private BufferedOutputStream out;
    private TftpEncoderDecoderClient encoderDecoder;
    private TftpProtocolClient protocol;

    listeningThread(BufferedInputStream in_, BufferedOutputStream out_, TftpEncoderDecoderClient encoderDecoder_, TftpProtocolClient protocol_){
        this.in = in_;
        this.out = out_;
        this.encoderDecoder = encoderDecoder_;
        this.protocol = protocol_;
    }

    public void run(){
        try {
            int read;
            while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                byte[] nextMessage = encoderDecoder.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage, out);
                    if(protocol.getStatus() != 1 && protocol.getStatus() != 2){ //dirq?
                        synchronized(protocol.lock){
                            protocol.setStatus(0);
                            protocol.lock.notifyAll();
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

