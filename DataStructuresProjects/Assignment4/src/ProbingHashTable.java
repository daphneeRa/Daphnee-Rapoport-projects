import javax.swing.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class ProbingHashTable<K, V> implements HashTable<K, V> {
    final static int DEFAULT_INIT_CAPACITY = 4;
    final static double DEFAULT_MAX_LOAD_FACTOR = 0.75;
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    private int capacity;
    private HashFunctor<K> hashFunc;
    private Pair<K,V>[] hashTable;
    private int amount;
    private Pair<K,V> DELETED;


    /*
     * You should add additional private members as needed.
     */

    public ProbingHashTable(HashFactory<K> hashFactory) {
        this(hashFactory, DEFAULT_INIT_CAPACITY, DEFAULT_MAX_LOAD_FACTOR);
    }

    public ProbingHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        if (maxLoadFactor<1 && maxLoadFactor>0)
            this.maxLoadFactor = maxLoadFactor;
        else
            this.maxLoadFactor = DEFAULT_MAX_LOAD_FACTOR;
        this.capacity = 1 << k;
        this.hashFunc = hashFactory.pickHash(k);
        this.hashTable= new Pair[capacity];
        for (int i = 0; i<capacity;i++ )
            hashTable[i]=null;
        this.amount=0;
        this.DELETED = new Pair<>(null,null);
    }

    public V search(K key) {
        int j = hashFunc.hash(key);
        for (int i = 0; i<capacity ;i++) {
            if (hashTable[j] == null)
                return null;
            if (hashTable[j].first().equals(key))
                return hashTable[j].second();

            j = HashingUtils.mod(j + 1, capacity);
        }
        return null;
    }

    public void insert(K key, V value) {
        Pair<K,V> newEl= new Pair<>(key,value);
        if ((double)((amount+1)/capacity) >= maxLoadFactor)
            rehashing();
        int place = hashFunc.hash(key);
        boolean isPlaced=false;
        while (!isPlaced){
            if (hashTable[place] == null){
                hashTable[place] = newEl;
                amount=amount+1;
                isPlaced = true;
            }
            else if (hashTable[place].equals(DELETED)) {
                hashTable[place] = newEl;
                isPlaced = true;
            }
            else
                place=HashingUtils.mod(place+1,capacity);
        }
    }

    public boolean delete(K key) {
        boolean isDeleted= false;
        int j = hashFunc.hash(key);
        int i = 0;
        while (!isDeleted && i<capacity) {
            if (hashTable[j].first().equals(key)){
                hashTable[j]=DELETED;
                isDeleted =true;
            }
            else {
                j = HashingUtils.mod(j + 1, capacity);
                i=i+1;
            }
        }
        return isDeleted;
    }

    public HashFunctor<K> getHashFunc() {
        return hashFunc;
    }

    public int capacity() { return capacity; }
    public void rehashing(){
        Pair<K,V>[] copy = this.hashTable;
        //increase the fields
        this.capacity= capacity*2;
        this.amount= 0;
        this.hashFunc = hashFactory.pickHash((int)(Math.log(capacity) / Math.log(2)));
        this.hashTable= new Pair[capacity];
        for(int i =0 ; i<capacity;i++)
            this.hashTable[i]=null;
        //entering the elements to the new table
        for(int i = 0; i<copy.length;i++){
            if (copy[i]!=null)
                this.insert(copy[i].first(),copy[i].second());
        }
    }
}