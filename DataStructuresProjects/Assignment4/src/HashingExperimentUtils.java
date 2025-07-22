import java.util.Collections; // can be useful
import java.util.List;
import java.util.Random;

public class HashingExperimentUtils {
    final private static int k = 16;
    private static HashingUtils HU = new HashingUtils();
    public static Pair<Double, Double> measureOperationsChained(double maxLoadFactor) {
        ChainedHashTable<Integer,Integer> hashTable = new ChainedHashTable<>(new ModularHash(),k,maxLoadFactor);
        int numItems = (int) (maxLoadFactor*Math.pow(2, k)-1);
        Integer[] elements = HU.genUniqueIntegers((int) (numItems*1.5));
        long startTime = System.nanoTime();
        for(int i=0; i<numItems; i++){
            hashTable.insert(elements[i],elements[i]);
        }
        long endTime = System.nanoTime();
        double ins_time = (double) ((endTime-startTime)/numItems);
        startTime = System.nanoTime();
        Random rand = new Random();
        for(int i=0; i< numItems/2; i++){
            int index = rand.nextInt(numItems);
            hashTable.search(elements[index]);
        }
        for(int i=0; i< numItems/2; i++){
            int randKey = numItems + rand.nextInt(elements.length - numItems);
            hashTable.search(randKey);
        }
        endTime = System.nanoTime();
        double search_time = (double) ((endTime-startTime)/numItems);
        return new Pair<>(ins_time,search_time);
    }

    public static Pair<Double, Double> measureOperationsProbing(double maxLoadFactor) {
        ProbingHashTable<Integer,Integer> hashTable = new ProbingHashTable<>(new ModularHash(),k,maxLoadFactor);
        int numItems = (int) (maxLoadFactor*Math.pow(2, k)-1);
        Integer[] elements = HU.genUniqueIntegers((int) (numItems*1.5));
        long startTime = System.nanoTime();
        for(int i=0; i<numItems; i++){
            hashTable.insert(elements[i],elements[i]);
        }
        long endTime = System.nanoTime();
        double ins_time = (double) ((endTime-startTime)/numItems);
        startTime = System.nanoTime();
        Random rand = new Random();
        for(int i=0; i< numItems/2; i++){
            int index = rand.nextInt(numItems);
            hashTable.search(elements[index]);
        }
        for(int i=0; i< numItems/2; i++){
            int randKey = numItems + rand.nextInt(elements.length - numItems);
            hashTable.search(randKey);
        }
        endTime = System.nanoTime();
        double search_time = (double) ((endTime-startTime)/numItems);
        return new Pair<>(ins_time,search_time);
    }

    public static Pair<Double, Double> measureLongOperations() {
        ChainedHashTable<Long,Long> hashTable = new ChainedHashTable<>(new MultiplicativeShiftingHash(), k,(long) 1);
        int numItems = (int) (Math.pow(2, k)-1);
        Long[] elements = HU.genUniqueLong((int) (numItems*1.5));
        long startTime = System.nanoTime();
        for(int i=0; i<numItems; i++){
            hashTable.insert(elements[i],elements[i]);
        }
        long endTime = System.nanoTime();
        double ins_time = (double) ((endTime-startTime)/numItems);
        startTime = System.nanoTime();
        Random rand = new Random();
        for(int i=0; i< numItems/2; i++){
            int index = rand.nextInt(numItems);
            hashTable.search(elements[index]);
        }
        for(int i=0; i< numItems/2; i++){
            long randKey = numItems + rand.nextInt(elements.length - numItems);
            hashTable.search(randKey);
        }
        endTime = System.nanoTime();
        double search_time = (double) ((endTime-startTime)/numItems);
        return new Pair<>(ins_time,search_time);
    }

    public static Pair<Double, Double> measureStringOperations() {
        ChainedHashTable<String,String> hashTable = new ChainedHashTable<>(new StringHash(),k,1);
        int numItems = (int) (Math.pow(2, k)-1);
        List<String> elements = HU.genUniqueStrings((int) (numItems*1.5), 10, 20);
        long startTime = System.nanoTime();
        for(int i=0; i<numItems; i++){
            hashTable.insert(elements.get(i),elements.get(i));
        }
        long endTime = System.nanoTime();
        double ins_time = (double) ((endTime-startTime)/numItems);
        startTime = System.nanoTime();
        Random rand = new Random();
        for(int i=0; i< numItems/2; i++){
            int index = rand.nextInt(numItems);
            hashTable.search(elements.get(i));
        }
        for(int i=0; i< numItems/2; i++){
            int randKey = numItems + rand.nextInt(elements.size() - numItems);
            hashTable.search(elements.get(randKey));
        }
        endTime = System.nanoTime();
        double search_time = (double) ((endTime-startTime)/numItems);
        return new Pair<>(ins_time,search_time);
    }

    public static void main(String[] args) {
        double[] load_factors = {(1.0 / 2.0), (3.0 / 4.0), (7.0 / 8.0), (15.0 / 16.0)};
        double sum_ins = 0;
        double sum_search = 0;
        for (double fact : load_factors) {
            for (int i = 0; i < 30; i++) {
                Pair<Double, Double> res = measureOperationsProbing(fact);
                sum_ins = sum_ins + res.first();
                sum_search = sum_search + res.second();
            }
            System.out.print(sum_ins / 30 + "   ");
            System.out.println(sum_search / 30);
            sum_search = 0;
            sum_ins = 0;
        }
        System.out.println("----------------------------------------------------------------");
        double[] load_factors2 = {(1.0 / 2.0), (3.0 / 4.0), 1, (3.0 / 2.0), 2};
        sum_ins = 0;
        sum_search = 0;
        for (double fact : load_factors2) {
            for (int i = 0; i < 30; i++) {
                Pair<Double, Double> res = measureOperationsChained(fact);
                sum_ins = sum_ins + res.first();
                sum_search = sum_search + res.second();
            }
            System.out.print(sum_ins / 30 + "   ");
            System.out.println(sum_search / 30);
            sum_search = 0;
            sum_ins = 0;
        }
        System.out.println("----------------------------------------------------------------");
        sum_ins = 0;
        sum_search = 0;
        double sum_ins2 = 0;
        double sum_search2 = 0;
        for (int i = 0; i < 10; i++) {
            Pair<Double, Double> resL = measureLongOperations();
            sum_ins = sum_ins + resL.first();
            sum_search = sum_search + resL.second();
            Pair<Double, Double> resS = measureStringOperations();
            sum_ins2 = sum_ins2 + resS.first();
            sum_search2 = sum_search2 + resS.second();
        }
        System.out.println("Avg long ins " + sum_ins / 10);
        System.out.println("Avg long search " + sum_search / 10);
        System.out.println("Avg string ins " + sum_ins2 / 10);
        System.out.println("Avg string search " + sum_search2 / 10);
    }
}
