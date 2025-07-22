import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        HashFactory<Integer> hashFactory = new ModularHash();
        ProbingHashTable<Integer,AbstractSkipList.Node> hashTable = new ProbingHashTable(hashFactory, 3, 1);
        hashTable.insert(2,new AbstractSkipList.Node(2));
        System.out.println(hashTable.search(2));
    }
}
