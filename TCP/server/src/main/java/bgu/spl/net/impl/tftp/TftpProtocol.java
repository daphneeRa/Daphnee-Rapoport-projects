package bgu.spl.net.impl.tftp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

class clients {
    static ConcurrentHashMap<Integer, Integer> clientsIds = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> names = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Boolean> files = new ConcurrentHashMap<>();
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<byte[]> connections;
    private String username;
    private volatile int blockNumber=1;
    static Path directory = Paths.get("Flies"); 
    private byte[] dataMsg; 
    private String uploadingFilename;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        List<Path> files;
        System.out.println(connectionId +" start");
        try {
            files = Files.walk(directory).filter(Files::isRegularFile).collect(Collectors.toList());
            for(Path file: files){
                clients.files.put(file.getFileName().toString(), true);
            }
        } catch (IOException e) {System.out.println(e.getMessage());}
    }

    @Override
    public void process(byte[] message) {
        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]) & 0x00ff);
        if(opcode == 1){ //RRQ
            System.out.println("RRQ");
            byte[] cutMessage = new byte[message.length-2];
            System.arraycopy(message, 2, cutMessage, 0 , message.length-2);
            tftpProcessing(cutMessage, 1);
        }
        else if(opcode == 2){ //WRQ
            System.out.println("WRQ");
            byte[] cutMessage = new byte[message.length-2];
            System.arraycopy(message, 2, cutMessage, 0 , message.length-2);
            tftpProcessing(cutMessage, 2);
        }
        else if(opcode == 3){ //DATA
            System.out.println("DATA");
            short b_short = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
            byte[] cutMessage = new byte[b_short];
            System.arraycopy(message, 6, cutMessage, 0 , b_short);
            blockNumber = (short) (((short) message[4]) << 8 | (short) (message[5]) & 0x00ff);
            dataMsg = cutMessage;
            dataReciver();
        }
        else if(opcode == 4){ //ACK 
            System.out.println("ACK");
            byte[] cutMessage = {message[2],message[3]};
            short b_short = (short) (((short) cutMessage[0]) << 8 | (short) (cutMessage[1]) & 0x00ff);
            if(dataMsg.length == 0){
                return;
            }
            if(b_short == blockNumber){
                blockNumber++;
                dataSender();
            }
            else {
                errorSender(0, "block number isn't compatible");
            }
        }
        else if(opcode == 6){ //DIRQ
            System.out.println("DIRQ");
            tftpProcessing(message, 6);
        }
        else if(opcode == 7){ //LOGRQ
            byte[] cutMessage = new byte[message.length-2];
            System.arraycopy(message, 2 , cutMessage, 0 , message.length-2);
            tftpProcessing(cutMessage, 7);
        }
        else if(opcode == 8){ //DELRQ
            System.out.println("DELRQ");
            byte[] cutMessage = new byte[message.length-2];
            System.arraycopy(message, 2 , cutMessage, 0 , message.length-2);
            tftpProcessing(cutMessage, 8);
        }
        else if(opcode == 10){ //DISC 
            System.out.println("DISC");
            tftpProcessing(message, 10);
        }
        else {
            errorSender(4, "Illegal TFTP operation");
        }
    }

    public void tftpProcessing(byte[] message, int type){
        if(type == 1){ //RRQ
            //handeling errors
            if(!clients.clientsIds.containsKey(connectionId)){
                String msg = " User not logged in";
                errorSender(6, msg);
                return;
            }
            String filename = new String(message, StandardCharsets.UTF_8);
            if(!clients.files.containsKey(filename)){
                errorSender(1, " File not found");
                return;
            }
            if(!clients.files.get(filename)){
                errorSender(2, "Access violation - File cannot be read");
                return;
            }
            Path filePath;
            try {
                filePath = Files.walk(directory).filter(path -> path.getFileName().toString().equals(filename)).findFirst().orElse(null);
                byte[] fileContent = Files.readAllBytes(filePath);
                dataMsg = fileContent;
            } catch (IOException e) {}
            dataSender();            
        }
        if(type == 2){ // WRQ
            //error handeling
            if(!clients.clientsIds.containsKey(connectionId)){
                String msg = " User not logged in";
                errorSender(6, msg);
                return;
            }
            String filename = new String(message, StandardCharsets.UTF_8);
            FileStore fileStore;
            try {
                fileStore = Files.getFileStore(directory);
                long availableSpace = fileStore.getUsableSpace();
                if(availableSpace == 0){
                    errorSender(3, "Disk full");
                    return;
                }
            } catch (IOException e) {}
            if(clients.files.containsKey(filename)){
                errorSender(5, "File already exist");
                return;
            }
            if(clients.files.containsKey(filename) && !clients.files.get(filename)){
                errorSender(2, "Access violation - File cannot be read");
                return;
            }
            try {
                Path filePath = directory.resolve(filename);
                Files.createFile(filePath);
                clients.files.put(filename, false);
                byte[] ack = {0,4,0,0};
                connections.send(connectionId, ack);
            } catch (IOException e) {}
            uploadingFilename = filename;
        }
        else if(type == 6){ //DIRQ
            if(!clients.clientsIds.containsKey(connectionId)){
                String msg = " User not logged in";
                errorSender(6, msg);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            clients.files.forEach((file, status) -> {
                if(status){
                    stringBuilder.append(file).append((char)0);
                }
            });
            byte[] fileNamesBytes = stringBuilder.toString().getBytes();
            byte[] msg = new byte[fileNamesBytes.length-1];
            System.arraycopy(fileNamesBytes, 0, msg, 0, fileNamesBytes.length-1);
            dataMsg = msg;
            blockNumber =1;
            dataSender();
        }
        else if(type == 7) { //LOGRQ
            String userName = new String(message, StandardCharsets.UTF_8); 
            if(clients.names.putIfAbsent(userName, 0) == null){ //logging in the client
                clients.clientsIds.put(connectionId, 0);
                System.out.println(userName.toString()  + " logged in");
                this.username = userName;
                byte[] ack = {0,4,0,0};
                connections.send(connectionId, ack);
            }
            else{ //name already taken
                String msg = "User already logged in";
                errorSender(7, msg);
                return;
            }
        }
        else if(type == 8) { //DELRQ
            if(!clients.clientsIds.containsKey(connectionId)){
                String msg = " User not logged in";
                errorSender(6, msg);
                return;
            }
            String filename = new String(message, StandardCharsets.UTF_8);
            if(!clients.files.containsKey(filename)){
                errorSender(1, " File not found");
                return;
            }
            if(!clients.files.get(filename)){
                errorSender(2, "Access violation - File cannot be deleted");
                return;
            }
            else{
                Path filePath;
                try {
                    filePath = Files.walk(directory).filter(path -> path.getFileName().toString().equals(filename)).findFirst().orElse(null);
                    Files.delete(filePath);
                } catch (IOException e) {}
                clients.files.remove(filename);
                byte[] ack = {0,4,0,0};
                connections.send(connectionId, ack);
                bcastSender(0, filename);
            }
        }
        else if(type == 10){ //DISC
            if(!clients.clientsIds.containsKey(connectionId)){
                String msg = " User not logged in";
                errorSender(6, msg);
                return;
            }
            System.out.println(username + " disconnect");
            byte[] ack = {0,4,0,0};
            connections.send(connectionId, ack);
            connections.disconnect(connectionId);
            clients.names.remove(username);
            clients.clientsIds.remove(connectionId);
            shouldTerminate = true;
        }
    }

    public void errorSender(int errorType, String msg){
        byte[] msgByte = msg.getBytes(StandardCharsets.UTF_8);
        byte[] errorMsg = new byte[msgByte.length + 4];
        if(errorType == 0){
            byte[] errorTypeMsg = {0,5,0,0};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 1){
            byte[] errorTypeMsg = {0,5,0,1};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 2){
            byte[] errorTypeMsg = {0,5,0,2};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 3){
            byte[] errorTypeMsg = {0,5,0,3};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 4){
            byte[] errorTypeMsg = {0,5,0,4};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 5){
            byte[] errorTypeMsg = {0,5,0,5};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        else if (errorType == 6){
            byte[] errorTypeMsg = {0,5,0,6};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length); 
        }
        else if (errorType == 7){ 
            byte[] errorTypeMsg = {0,5,0,7};
            System.arraycopy(errorTypeMsg, 0, errorMsg, 0, 4);                
            System.arraycopy(msgByte, 0, errorMsg, 4, msgByte.length);
        }
        connections.send(connectionId, errorMsg);
    }

    public void dataSender(){
        int index = 0;
        if(index <= dataMsg.length){
            if(dataMsg.length-index < 512){
                System.out.println(blockNumber);
                byte [] a_bytes = new byte []{(byte)((dataMsg.length-index) >> 8),(byte)((dataMsg.length-index) & 0xff)};
                byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
                byte[] msg = new byte[dataMsg.length-index + 6];
                msg[0] = 0;
                msg[1] = 3;
                msg[2] = a_bytes[0];
                msg[3] = a_bytes[1];
                msg[4] = byteBlockNumber[0];
                msg[5] = byteBlockNumber[1];
                if(dataMsg.length != 0){
                    System.arraycopy(dataMsg, 0, msg, 6, dataMsg.length-index);
                }
                connections.send(connectionId, msg);
                dataMsg = new byte[]{} ;
            }
            else {
                byte[] msg = new byte[518];
                byte[] a_bytes = new byte []{(byte)(512 >> 8),(byte)(512 & 0xff)};
                byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
                msg[0] = 0;
                msg[1] = 3;
                msg[2] = a_bytes[0];
                msg[3] = a_bytes[1];
                msg[4] = byteBlockNumber[0];
                msg[5] = byteBlockNumber[1];
                System.arraycopy(dataMsg, 0, msg, 6, 512);
                //update dataMsg field
                byte[] newDataMsg = new byte[dataMsg.length-512];
                if(dataMsg.length == 512){
                    dataMsg = new byte[]{};
                }
                else{
                    System.arraycopy(dataMsg, 512, newDataMsg, 0, newDataMsg.length);
                    dataMsg = newDataMsg;
                }
                index+= 512;
                connections.send(connectionId, msg);
            }

        }
    }

    public void bcastSender(int type, String filename){
        byte[] msgStart = {0, 9, (byte)type}; //type is 0 or 1
        byte[] msgData = filename.getBytes(StandardCharsets.UTF_8);
        byte[] msg = new byte[3+msgData.length];
        System.arraycopy(msgStart, 0, msg, 0, 3);
        System.arraycopy(msgData, 0, msg, 3,  msgData.length);
        clients.clientsIds.forEach((clientId, value) -> {
            connections.send(clientId, msg);
        }); 
    }

    public void dataReciver(){
       
        Path filePath = directory.resolve(uploadingFilename);
        try { 
            Files.write(filePath, dataMsg);
        } catch (IOException e) {}
        byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = 4;
        ack[2] = byteBlockNumber[0];
        ack[3] = byteBlockNumber[1];
        connections.send(connectionId, ack); 
        if(dataMsg.length < 512){ // the last data packet to be written
            blockNumber = 1;
            clients.files.put(uploadingFilename, true);
            bcastSender(1, uploadingFilename);
            uploadingFilename = null;
        }
        dataMsg = new byte[]{};
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    } 


    
}
