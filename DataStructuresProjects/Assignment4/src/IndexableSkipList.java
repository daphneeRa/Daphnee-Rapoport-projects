public class IndexableSkipList extends AbstractSkipList {
    final protected double probability;
    public IndexableSkipList(double probability) {
        super();
        this.probability = probability;
    }

    @Override
    public Node find(int val) {
        Node p= head;
        for (int i= p.height(); i>=0 ;i--) {
            while (p.getNext(i)!=null && p.getNext(i).key()<=val)
                p = p.getNext(i);
        }
        return p;
    }

    @Override
    public int generateHeight() {
        double rand = Math.random();
        int height = 0;
        while (rand >= probability) {
            height++;
            rand = Math.random();
        }
        return height;
    }

    public int rank(int val) {
        /*Node p = head;
        int rank = 0;
        for (int i = head.height(); i >= 0; i--) {
            while (p.getNext(i) != null && p.getNext(i).key() <= val) {
                p = p.getNext(i);
                rank = rank + p.getJumps(i) + 1;

            }
        }
        return rank;*/
        return 1;
    }

    public int select(int index) {
        /*Node p = head;
        for (int i = head.height(); i >= 0 && index>=0; i--) {
            while (p.getNext(i) != null && p.getNext(i).getJumps(i) < index) {
                p = p.getNext(i);
                index = index - p.getJumps(i) - 1;
            }
        }
        return p.key();*/
        return 1;
    }
}
