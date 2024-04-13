package bgu.spl.net.srv;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;


public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Integer connectionId;
    private serverConnections<T> connections;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, Integer connectionId, serverConnections<T> connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void run() {

        try (Socket sock = this.sock) { //just for automatic closing
            protocol.start(connectionId, connections);
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }

    @Override
    public boolean send(T msg) {
        synchronized(this){
                if(out == null){
                    try {
                        out = new BufferedOutputStream(sock.getOutputStream());
                    } catch (IOException e) {}
                }
                try {
                    System.out.println(connections.activeClientsMap.containsKey(connectionId));
                    if(msg != null && connections.activeClientsMap.containsKey(connectionId)){
                        System.out.println("try to response");
                        out.write(encdec.encode(msg));
                            out.flush();
                    }   
                } catch (IOException e) {
                    return false;}
                return true;               
        }  
    }
}
