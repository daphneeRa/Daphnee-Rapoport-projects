import java.util.Random;

public class ModularHash implements HashFactory<Integer> {
    private HashingUtils HU;

    public ModularHash() {
        this.HU = new HashingUtils();
    }

    @Override
    public HashFunctor<Integer> pickHash(int k) {
        int a = (int)(Math.random()*(Integer.MAX_VALUE-2))+1;
        int b = (int)(Math.random()*(Integer.MAX_VALUE-1));
        Long[] longVal = HU.genUniqueLong(1);
        Long p = longVal[0];
        int m = (int) Math.pow (2, k);
        HashFunctor<Integer> hashFun = new Functor(a, b, p, m);
        return hashFun;
    }

    public class Functor implements HashFunctor<Integer> {
        final private int a;
        final private int b;
        final private long p;
        final private int m;

        public Functor(int a, int b, long p, int m) {
            this.a = a;
            this.b = b;
            this.p = p;
            this.m = m;
        }

        @Override
        public int hash(Integer key) {
            long firstCal = HashingUtils.mod((a * key) + b, p);
            int hashedValue = (int) HashingUtils.mod(firstCal, m);
            return hashedValue;
        }

        public int a() {
            return a;
        }

        public int b() {
            return b;
        }

        public long p() {
            return p;
        }

        public int m() {
            return m;
        }
    }
}
