package bgu.spl.net.impl.tftp;
import java.util.concurrent.ConcurrentLinkedQueue;
import bgu.spl.net.api.MessagingProtocol;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TftpProtocolClient implements MessagingProtocol<byte[]> {

    private volatile boolean shouldTerminate = false;
    private volatile int blockNumber=1;
    static Path directory = Paths.get("client"); 
    private String uploadingFilename = "";
    private boolean finishedUpload = false;
    private volatile int status = 0;
    public Object lock = new Object();
    private ByteArrayOutputStream fileNamesDIRQ = new ByteArrayOutputStream();
    private ConcurrentLinkedQueue<byte[]> BcastCollector = new ConcurrentLinkedQueue<byte[]>();
    private FileInputStream inputStream;
    
    public void process(byte[] message, BufferedOutputStream out) {
        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]) & 0x00ff);
        System.out.println("opcode is " + opcode);

        if(opcode == 3){ //DATA
            System.out.println("DATA");
            short size = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
            byte[] cutMessage = new byte[size];
            System.arraycopy(message, 6, cutMessage, 0 , size);
            blockNumber = (short) (((short) message[4]) << 8 | (short) (message[5]) & 0x00ff);
            dataReciver(cutMessage, out);
        }
        else if(opcode == 4){ //ACK 
            byte[] cutMessage = {message[2],message[3]};
            short b_short = (short) (((short) cutMessage[0]) << 8 | (short) (cutMessage[1]) & 0x00ff);
            System.out.println("ACK "+ b_short);
            if(status == 2){
                if(finishedUpload){
                    System.out.println("WRQ " + uploadingFilename + " complete.");
                    status = 0;
                    blockNumber=1;
                    uploadingFilename = "";
                    finishedUpload = false;
                    if(inputStream != null){
                        try {
                            inputStream.close();
                        } catch (IOException e) {}
                    }
                    inputStream = null;
                }
                else{
                    dataSender(out);
                    blockNumber++;
                }
            }
        }
        else if(opcode == 5){ //ERROR
            short errNum = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
            String msg = new String(message, 4, message.length-4, StandardCharsets.UTF_8);
            System.out.println("ERROR "+ errNum + " " + msg);
            if(errNum == 5){ //file alresy exist
                finishedUpload = false;
                uploadingFilename = "";
            }
            else if (errNum == 1){ //file not found
                    String filePathToDelete = "." + File.separator + uploadingFilename;
                    File fileToDelete = new File(filePathToDelete);
                    if(fileToDelete.exists() && status == 1) {
                        fileToDelete.delete();
                    }
                    status = 0;
                    uploadingFilename = "";
            }
            else if (errNum == 6){ //user not logged in
                status = 0;
                uploadingFilename = "";
                finishedUpload = false;
            }

        }
        else if(opcode == 9){
            if(status != 1 && status != 2 && status != 6){
                String msg = new String(message, 3, message.length-3, StandardCharsets.UTF_8);
                System.out.println("BCAST " + message[2] + " " + msg);   
            }
            else{
                BcastCollector.add(message);
            }
        }
    }

    public void dataSender(BufferedOutputStream out){
        try{
            if(blockNumber == 1){
                File filePath = new File(uploadingFilename);
                this.inputStream = new FileInputStream(filePath);
            }
            byte[] dataMsg = new byte[512];
            int index = 0;
            byte[] msg;
            if((index = inputStream.read(dataMsg)) > 0){
                if(index < 512){
                    System.out.println("is less than 512 size");
                    byte [] size = new byte []{(byte)((index) >> 8),(byte)((index) & 0xff)};
                    byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
                    msg = new byte[index + 6];
                    msg[0] = 0;
                    msg[1] = 3;
                    msg[2] = size[0];
                    msg[3] = size[1];
                    msg[4] = byteBlockNumber[0];
                    msg[5] = byteBlockNumber[1];
                    System.arraycopy(dataMsg, 0, msg, 6, index);
                    finishedUpload = true;                    
                }
                else { System.out.println("is more than 512 size");
                    msg = new byte[518];
                    byte[] size = new byte []{(byte)(512 >> 8),(byte)(512 & 0xff)};
                    byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
                    msg[0] = 0;
                    msg[1] = 3;
                    msg[2] = size[0];
                    msg[3] = size[1];
                    msg[4] = byteBlockNumber[0];
                    msg[5] = byteBlockNumber[1];
                    System.arraycopy(dataMsg, 0, msg, 6, 512);
                }
            }
            else if ((index = inputStream.read(dataMsg)) == -1) {
                System.out.println("is 512 size");
                msg = new byte[6];
                byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
                msg[0] = 0;
                msg[1] = 3;
                msg[2] = 0;
                msg[3] = 0;
                msg[4] = byteBlockNumber[0];
                msg[5] = byteBlockNumber[1];
                finishedUpload = true;
                if(inputStream != null){
                    inputStream.close();
                }
                inputStream = null;
            }
            else { msg = new byte[]{};} //WILL NEVER GET HERE
            out.write(msg);
            out.flush();
        } catch(IOException e){}
    }

    public void dataReciver(byte[] dataMsg, BufferedOutputStream out){
        if(status == 6){ //DIRQ
            try {
                fileNamesDIRQ.write(dataMsg);
            } catch (IOException e) {}
            sendAck(blockNumber, out);
            if(dataMsg.length < 512){ // the last data packet to read
                byte[] fileNames = fileNamesDIRQ.toByteArray();
                int index = 0;
                blockNumber = 1;
                for (int i=0; i<= fileNames.length; i++) {
                    if(i == fileNames.length || fileNames[i] == 0){
                        String fileName = new String(fileNames, index, i-index, StandardCharsets.UTF_8);
                        System.out.println(fileName);
                        index = i+1;
                    }
                }
                fileNamesDIRQ.reset();
            }
        }
        else if(status == 1){ //RRQ
            Path filePath = Paths.get(directory.toString(), uploadingFilename);
            try {
                Files.write(filePath, dataMsg);
            } catch (IOException e) {}
            sendAck(blockNumber, out);
            if(dataMsg.length < 512){ // the last data packet to be written
                System.out.println("RRQ " + uploadingFilename + " complete.");
                uploadingFilename = "";
                status = 0;
                blockNumber = 1;
                
            }
        }
        while(!BcastCollector.isEmpty()){
            byte[] msg = BcastCollector.poll();
            String msgS = new String(msg, 3, msg.length-3, StandardCharsets.UTF_8);
            System.out.println("BCAST " + msg[2] + " " + msgS);    
        }
    }

    public void sendAck(int blockNumber, BufferedOutputStream out){
        byte[] byteBlockNumber = new byte []{(byte)(blockNumber >> 8),(byte)(blockNumber & 0xff)};
        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = 4;
        ack[2] = byteBlockNumber[0];
        ack[3] = byteBlockNumber[1];
        try {
            out.write(ack);
            out.flush();
        } catch (IOException e) {}
    }


    public void terminate(){
        this.shouldTerminate = true;
    }

    @Override
    public boolean shouldTerminate(){
        return shouldTerminate;
    } 

    public void setUploadingFilename(String filename){
        this.uploadingFilename = filename;
    }

    public String getUploadingFilename(){
        return this.uploadingFilename;
    }

    public void setStatus(int status_){
        this.status = status_;
    }

    public int getStatus(){
        return this.status;
    }
    
}