import java.util.Random;

public class MultiplicativeShiftingHash implements HashFactory<Long> {

    private HashingUtils HU;
    public MultiplicativeShiftingHash() {
        this.HU = new HashingUtils();
    }

    @Override
    public HashFunctor<Long> pickHash(int k) {
        int a = (int)(Math.random()*(Integer.MAX_VALUE-3))+2;
        return new Functor(a, k);
    }

    public class Functor implements HashFunctor<Long> {
        final public static long WORD_SIZE = 64;
        final private long a;
        final private long k;


        public Functor(long a,long k){
            this.a = a;
            this.k = k;
        }
        @Override
        public int hash(Long key) {
            long to_shift = a*key;
            return (int) to_shift >>> (WORD_SIZE-k);
        }

        public long a() {
            return a;
        }

        public long k() {
            return k;
        }
    }
}
