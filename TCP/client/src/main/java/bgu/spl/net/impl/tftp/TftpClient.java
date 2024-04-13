package bgu.spl.net.impl.tftp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
public class TftpClient {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"localhost", "hello"}; // localhost
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        int port = Integer.parseInt(args[1]);

        try (Socket sock = new Socket(args[0], port);
            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) 
            {
                TftpEncoderDecoderClient encoderDecoder = new TftpEncoderDecoderClient();
                TftpProtocolClient protocol = new TftpProtocolClient();
                Thread keyboardThread = new Thread(new keyboardThread(out,encoderDecoder,protocol));
                Thread listeningThread = new Thread(new listeningThread(in,out,encoderDecoder,protocol));
                keyboardThread.start();
                listeningThread.start();

                keyboardThread.join();
                listeningThread.join();
                System.out.println("Client's threads terminated");
            }
        catch(IOException | InterruptedException e){ 
            System.out.println("Error: "+ e.getMessage());
        }




    }
}