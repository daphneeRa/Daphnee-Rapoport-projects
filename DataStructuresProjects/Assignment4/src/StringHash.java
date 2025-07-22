import java.util.Random;

public class StringHash implements HashFactory<String> {

    private HashingUtils HU;
    public StringHash() {
        this.HU = new HashingUtils();
    }

    @Override
    public HashFunctor<String> pickHash(int k) {
        boolean isPrime = false;
        int q = 0;
        while (!isPrime) {
            q = (int) HU.genLong(Integer.MAX_VALUE/2 +1, Integer.MAX_VALUE);
            isPrime = HU.runMillerRabinTest(q, 50);
        }
        int c = (int)(Math.random()*q)+2;
        return new Functor(q,c,k);
    }

    public class Functor implements HashFunctor<String> {
        final private HashFunctor<Integer> carterWegmanHash;
        final private int c;
        final private int q;


        public Functor(int q, int c, int k){
            this.q = q;
            this.c = c;
            this.carterWegmanHash = new ModularHash().pickHash(k);
        }
        @Override
        public int hash(String key) {
            int k = key.length();
            int sum = 0;
            for(int i=0; i<k; i++){
                int firstMod = (int) HU.modPow(c, k-i, q);
                int secondMod = HU.mod(key.charAt(i)*firstMod, q);
                sum = sum + secondMod;
            }
            int res = HU.mod(sum, q);
            return carterWegmanHash.hash(res);
        }

        public int c() {
            return c;
        }

        public int q() {
            return q;
        }

        public HashFunctor carterWegmanHash() {
            return carterWegmanHash;
        }
    }
}
