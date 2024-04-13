package bgu.spl.net.impl.tftp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoderClient implements MessageEncoderDecoder<byte[]> {
    
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private List<Byte> opcode = new LinkedList<Byte>();
    private List<Byte> dataLen = new LinkedList<Byte>();

    @Override
    public byte[] decodeNextByte(byte nextByte) { //cant recive error
        if(opcode.size()<2){
            opcode.add(nextByte);
            pushByte(nextByte);
            if(opcode.size() == 2){
                byte[] a = {opcode.get(0), opcode.get(1)};
                short opcodeS = (short) (((short) a[0]) << 8 | (short) (a[1]) & 0x00ff);
                if((opcodeS == 6 | opcodeS == 10) && len == 2){
                    opcode = new LinkedList<Byte>();;
                    return popMsg();
                }
            }
            return null;
        }
        else{
            byte[] a = {opcode.get(0), opcode.get(1)};
            short opcodeS = (short) (((short) a[0]) << 8 | (short) (a[1]) & 0x00ff);
            if(opcodeS == 3 | opcodeS == 4){
                if(opcodeS == 4 && len == 3){
                    pushByte(nextByte);
                    opcode = new LinkedList<Byte>();;
                    return popMsg();
                }
                else if(opcodeS == 3){
                    if(dataLen.size()<2){
                        dataLen.add(nextByte);
                        pushByte(nextByte);
                        return null;
                    }
                    else{
                        byte[] b = {dataLen.get(0), dataLen.get(1)};
                        short dataLenS = (short) (((short) b[0]) << 8 | (short) (b[1]) & 0x00ff);
                        if(len+1 == dataLenS+6){
                            pushByte(nextByte);
                            opcode = new LinkedList<Byte>();;
                            dataLen = new LinkedList<Byte>();;
                            return popMsg();
                        }
                    }
                }
            } 
            else if(opcodeS==5){
                if(len>4 && nextByte == 0){
                    pushByte(nextByte);
                    opcode = new LinkedList<Byte>();
                    return popMsg();
                }
            }
            else if(opcodeS==9){
                if(len>3 && nextByte==0){
                    pushByte(nextByte);
                    opcode = new LinkedList<Byte>();
                    return popMsg();
                }
            }
            else if(nextByte == 0){ //other opcodes ending
                opcode = new LinkedList<Byte>();
                return popMsg();
            }
        }
        pushByte(nextByte);
        return null;   
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private byte[] popMsg(){
        byte[] newMsg = new byte[len];
        System.arraycopy(bytes, 0, newMsg, 0, len);
        len = 0;
        return newMsg;
    }

    @Override
    public byte[] encode(byte[] message) {
        short opcodeS = (short) (((short) message[0]) << 8 | (short) (message[1]) & 0x00ff);
        System.out.println(opcodeS);
        if(opcodeS == 3 | opcodeS == 4 | opcodeS == 6 | opcodeS == 10){
            return message;
        }
        else{
            byte[] returnMsg = new byte[message.length+1];
            System.arraycopy(message, 0, returnMsg, 0, message.length);
            returnMsg[returnMsg.length-1] = 0;
            return returnMsg;
        }
    }
}