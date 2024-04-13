package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;


public class serverConnections<T> implements Connections<T>  {

    protected ConcurrentHashMap<Integer,BlockingConnectionHandler<T>> activeClientsMap;

    public serverConnections(){
        activeClientsMap = new ConcurrentHashMap<Integer,BlockingConnectionHandler<T>>();
    }

    public boolean connect(int connectionId, BlockingConnectionHandler<T> handler){
        System.out.println(connectionId +" connected");
        return (activeClientsMap.putIfAbsent(connectionId, handler) == null);
    }

    public boolean send(int connectionId, T msg){
        while (activeClientsMap.get(connectionId).send(msg) == false){}
        return true;
    }

    public void disconnect(int connectionId){
        activeClientsMap.remove(connectionId);
    }

    public BlockingConnectionHandler<T> getCH(int connectionId){
        return activeClientsMap.get(connectionId);
    }
}
