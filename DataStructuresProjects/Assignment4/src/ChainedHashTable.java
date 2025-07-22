import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class ChainedHashTable<K, V> implements HashTable<K, V> {
    final static int DEFAULT_INIT_CAPACITY = 4;
    final static double DEFAULT_MAX_LOAD_FACTOR = 2;
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    //private int k;
    private int capacity;
    private HashFunctor<K> hashFunc;
    private LinkedList<Pair<K,V>>[] table;
    private int amount;

    /*
     * You should add additional private members as needed.
     */

    public ChainedHashTable(HashFactory<K> hashFactory) {
        this(hashFactory, DEFAULT_INIT_CAPACITY, DEFAULT_MAX_LOAD_FACTOR);
    }

    public ChainedHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        if(maxLoadFactor > 0)
            this.maxLoadFactor = maxLoadFactor;
        else
            this.maxLoadFactor = DEFAULT_MAX_LOAD_FACTOR;
        this.capacity = 1 << k;
        this.hashFunc = hashFactory.pickHash(k);
        this.table = new LinkedList[capacity];
        for(int i=0;i<capacity; i++){
            table[i] = new LinkedList<Pair<K,V>>();
        }
        this.amount = 0;
        //this.k = k;
    }

    public V search(K key) {
        int hashedValue = this.hashFunc.hash(key);
        for(int i=0; i<table[hashedValue].size(); i++){
            Pair<K,V> item = table[hashedValue].get(i);
            if(item.first().equals(key))
                return item.second();
        }
        return null;
    }

    public void insert(K key, V value) {
        if((double)(amount+1)/capacity >= maxLoadFactor)
            rehashing();
        int hashedValue = this.hashFunc.hash(key);
        Pair<K,V> toInsert = new Pair<K,V>(key, value);
        table[hashedValue].addFirst(toInsert);
        amount++;
    }

    public boolean delete(K key) {
        amount--;
        int hashedValue = this.hashFunc.hash(key);
        for(int i=0; i<table[hashedValue].size(); i++) {
            if (table[hashedValue].get(i).first().equals(key)) {
                table[hashedValue].remove(i);
                return true;
            }
        }
        return false;
    }

    public HashFunctor<K> getHashFunc() {
        return hashFunc;
    }

    public int capacity() {
        return capacity;
    }

    public void rehashing(){
        this.capacity = capacity *2;
        this.hashFunc = hashFactory.pickHash((int)(Math.log(capacity) / Math.log(2)));
        LinkedList<Pair<K,V>>[] formerTable = this.table;
        this.table = new LinkedList[capacity];
        for(int i=0; i<capacity ;i++){
            table[i] = new LinkedList<Pair<K,V>>();
        }
        this.amount = 0;
        for(int i=0; i<formerTable.length;i++){
            for(int j=0; j<formerTable[i].size(); j++){
                this.insert(formerTable[i].get(j).first(), formerTable[i].get(j).second());
            }
        }
    }
}
