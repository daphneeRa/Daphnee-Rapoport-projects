package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class keyboardThread implements Runnable {

    private BufferedOutputStream out;
    private TftpEncoderDecoderClient encoderDecoder;
    private TftpProtocolClient protocol;

    keyboardThread(BufferedOutputStream out_, TftpEncoderDecoderClient encoderDecoder_, TftpProtocolClient protocol_){
        this.out = out_;
        this.encoderDecoder = encoderDecoder_;
        this.protocol = protocol_;
    }

    public void run(){
        try (Scanner scanner = new Scanner(System.in)){
            String input = scanner.nextLine();
            while (!protocol.shouldTerminate() && input != null) {
                synchronized( protocol.lock){
                    try {
                        byte[] msg;
                        String[] words = input.trim().split("\\s+",2);
                        String command = words[0];
                        if(words.length >1){
                            msg = createMsg(command, words[1]);
                        }
                        else {
                            msg = createMsg(command, "");
                        }
                        if(msg != null){
                            out.write(encoderDecoder.encode(msg));
                            out.flush();
                            protocol.lock.wait();
                        }
                    } catch (IOException e) {}
                    catch (InterruptedException interrupt){
                        Thread.currentThread().interrupt(); // ???
                    }
                }
                if(protocol.shouldTerminate()){
                    break;
                }
                input = scanner.nextLine();
            }
        }
    }

    
    public byte[] createMsg (String command, String content){
        byte[] msg;
        if(command.equals("LOGRQ")){
            if(content==""){
                System.out.println("Not defined command");
                return null;
            }
            msg = new byte[content.length()+2];
            msg[0] = 0;
            msg[1] = 7;
            System.out.println("content "+ content);
            System.arraycopy(content.getBytes(), 0, msg, 2, content.getBytes().length);
            return msg;
        }
        else if(command.equals("DELRQ")){
            if(content==""){
                System.out.println("Not defined command");
                return null;
            }
            msg = new byte[content.length()+2];
            msg[0] = 0;
            msg[1] = 8;
            System.out.println("content "+ content);
            System.arraycopy(content.getBytes(), 0, msg, 2, content.getBytes().length);
            return msg;
        }
        else if(command.equals("RRQ")){
            if(content==""){
                System.out.println("Not defined command");
                return null;
            }
            msg = new byte[content.length()+2];
            msg[0] = 0;
            msg[1] = 1;
            Path filePath = Paths.get(protocol.directory.toString(), content);
            if(!Files.exists(filePath)){
                try {
                    Files.createFile(filePath);
                    
                } catch (IOException e) { e.printStackTrace();}
            }
            else{
                System.out.println("File already exist");
                return null;
            }
            System.arraycopy(content.getBytes(), 0, msg, 2, content.getBytes().length);
            protocol.setUploadingFilename(content);
            protocol.setStatus(1);
            return msg;
        }
        else if(command.equals("WRQ")){
            if(content==""){
                System.out.println("Not defined command");
                return null;
            }
            msg = new byte[content.length()+2];
            msg[0] = 0;
            msg[1] = 2;
            String relativePathToFile = "."+ File.separator+ content;
            File file = new File(relativePathToFile);
            if(!file.exists()){
                System.out.println("file does not exist");
                return null;
            }
            System.arraycopy(content.getBytes(), 0, msg, 2, content.getBytes().length);
            protocol.setUploadingFilename(content);
            protocol.setStatus(2);
            return msg;
        }
        else if(command.equals("DIRQ")){
            msg = new byte[2];
            msg[0] = 0;
            msg[1] = 6;
            protocol.setStatus(6);
            return msg;
        }
        else if(command.equals("DISC")){
            msg = new byte[2];
            msg[0] = 0;
            msg[1] = 10;
            protocol.terminate();
            return msg;
        }
        else{
            System.out.println("Not defined command");
            return null;
        }
    }
}