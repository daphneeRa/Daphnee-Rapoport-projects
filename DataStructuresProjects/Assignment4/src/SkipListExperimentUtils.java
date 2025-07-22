import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SkipListExperimentUtils {
    public static double measureLevels(double p, int x) {
        AbstractSkipList tests = new IndexableSkipList(p);
        double sum = 0;
        for (int i=1; i<=x; i++){
            sum = sum + tests.generateHeight();
        }
        return sum/x + 1;
    }

    /*
     * The experiment should be performed according to these steps:
     * 1. Create the empty Data-Structure.
     * 2. Generate a randomly ordered list (or array) of items to insert.
     *
     * 3. Save the start time of the experiment (notice that you should not
     *    include the previous steps in the time measurement of this experiment).
     * 4. Perform the insertions according to the list/array from item 2.
     * 5. Save the end time of the experiment.
     *
     * 6. Return the DS and the difference between the times from 3 and 5.
     */

    public static List<Integer> createList (int size){
        List<Integer> output = new ArrayList<Integer>();
        for (int i=0; i<=size;i++){
            output.add(i*2);
        }
        return output;
    }

    public static List<Integer> createList2 (int size){
        List<Integer> output = new ArrayList<Integer>();
        for (int i=0; i<=2*size;i++){
            output.add(i);
        }
        return output;
    }
    public static Pair measureInsertions(double p, int size) {
        long time;
        long sumTime = 0; ///***
        AbstractSkipList SL = new IndexableSkipList(p);
        List<Integer> temp = createList(size);
        //long startTime = System.nanoTime();
        for(int i=size; i>=0 ; i--) {
            int index = (int) (Math.random() * i);
            long startTime = System.nanoTime(); ///***
            SL.insert(temp.get(index));
            long endTime = System.nanoTime(); ///***
            temp.remove(index);
            sumTime = sumTime + (endTime - startTime); ///***
        }
        //long endTime = System.nanoTime();
        //time = (endTime - startTime)/ (size + 1);
        time = (sumTime)/ (size + 1); ///***
        return new Pair(SL, time);
    }


    public static double measureSearch(AbstractSkipList skipList, int size) {
        long time;
        long sumTime = 0;
        List<Integer> temp = createList2(size);
        //long startTime = System.nanoTime();
        for(int i=2*size; i>=0 ; i--) {
            int index = (int) (Math.random() * i);
            long startTime = System.nanoTime(); ///***
            skipList.search(temp.get(index));
            long endTime = System.nanoTime(); ///***
            temp.remove(index);
            sumTime = sumTime + (endTime - startTime); ///***
        }
        //long endTime = System.nanoTime();
        //time = (endTime - startTime)/ (size + 1);
        time = (sumTime)/ (size + 1); ///***
        return time;
    }

    public static double measureDeletions(AbstractSkipList skipList, int size) {
        long time;
        long sumTime = 0; ///***
        List<Integer> temp = createList(size);
        //long startTime = System.nanoTime();
        for(int i=size; i>=0 ; i--) {
            int index = (int) (Math.random() * i);
            AbstractSkipList.Node place = skipList.find(temp.get(index));
            long startTime = System.nanoTime(); ///***
            skipList.delete(place);
            long endTime = System.nanoTime(); ///***
            temp.remove(index);
            sumTime = sumTime + (endTime - startTime); ///***
        }
        //long endTime = System.nanoTime();
        //time = (endTime - startTime)/ (size + 1);
        time = (sumTime)/ (size + 1); ///***
        return time;
    }

    private static void second_exp(){
        double[] p = {0.33,0.5,0.75,0.9};
        int[] x = {1000, 2500, 5000, 10000, 15000, 20000, 50000};
        Pair ins;
        double[][] results = new double[8][4];
        results[0][0] = 0;
        results[0][1] = 1;
        results[0][2] = 2;
        results[0][3] = 3;
        double time_int = 0;
        double time_del = 0;
        double time_search = 0;
        for(int i=0;i<4;i++) {
            for (int j=0; j<7; j++) {
                results[j+1][0] = x[j];
                for(int k=1; k<=30; k++) {
                    ins = measureInsertions(p[i], x[j]);
                    time_int = time_int + BigDecimal.valueOf((long)ins.second()).doubleValue();
                    time_search = time_search + measureSearch((AbstractSkipList) ins.first(), x[j]);
                    time_del = time_del + measureDeletions((AbstractSkipList) ins.first(), x[j]);
                }
                results[j+1][1] = time_int/30;
                results[j+1][2] = time_search/30;
                results[j+1][3] = time_del/30;
            }
            //System.out.println(Arrays.deepToString(results));
            for (int l = 0; l < 8; l++) {
                for (int j = 0; j < 4; j++) {
                    System.out.print(results[l][j] + " ");
                }

                System.out.println();
            }
            System.out.println("------------------------------------------------------------------");
            results = new double[8][4];
            results[0][0] = 0;
            results[0][1] = 1;
            results[0][2] = 2;
            results[0][3] = 3;
        }
    }

    public static void main(String[] args) {
        //double avg = measureLevels(0.5, 1);
        //System.out.println(avg);
        /*double[] test = {2.0172,1.9881,2.0117,2.0299,2.0117};
        double sum =0;
        double exp = 2;
        for (int i=0; i<=4; i++){
            sum = sum + Math.abs(test[i]- exp);
        }
        System.out.println(sum*0.2);*/
        second_exp();
    }
}
