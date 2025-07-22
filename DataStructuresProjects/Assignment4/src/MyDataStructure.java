import java.util.ArrayList;
import java.util.List;

public class MyDataStructure {
    /*
     * You may add any members that you wish to add.
     * Remember that all the data-structures you use must be YOUR implementations,
     * except for the List and its implementation for the operation Range(low, high).
     */

    private final IndexableSkipList skipList;
    private final ProbingHashTable<Integer,AbstractSkipList.Node> hashTable;
    private final int capacity;

    /***
     * This function is the Init function described in Part 4.
     *
     * @param N The maximal number of items expected in the DS.
     */
    public MyDataStructure(int N) {
        this.capacity = N;
        this.skipList = new IndexableSkipList(0.5);
        int logCalc = (int)(Math.log(N) / Math.log(2));
        HashFactory<Integer> hashFactory = new ModularHash();
        this.hashTable = new ProbingHashTable(hashFactory, logCalc, 1);
    }

    /*
     * In the following functions,
     * you should REMOVE the place-holder return statements.
     */
    public boolean insert(int value) {
        AbstractSkipList.Node toAdd = skipList.find(value);
        if (toAdd.key()==value)
            return false;
        AbstractSkipList.Node pointer = skipList.insert(value);
        hashTable.insert(value,pointer);
        return true;
    }

    public boolean delete(int value) {
        AbstractSkipList.Node toDelete = hashTable.search(value);
        if (toDelete.key()!=value)
            return false;
        else {
            skipList.delete(toDelete);
            hashTable.delete(value);
            return true;
        }
    }

    public boolean contains(int value){
        if (hashTable.search(value)==null)
            return false;
        else
            return true;
    }

    public int rank(int value) {
        return skipList.rank(value);
    }

    public int select(int index) {
        return skipList.select(index);
    }

    public List<Integer> range(int low, int high) {
        if(!this.contains(low))
            return null;
        List<Integer> L = new ArrayList<>(capacity);
        AbstractSkipList.Node curr = hashTable.search(low);
        while (curr.getNext(0)!=null && curr.key()<=high){
            L.add(curr.key());
            curr=curr.getNext(0);
        }
        return L;
    }
}
