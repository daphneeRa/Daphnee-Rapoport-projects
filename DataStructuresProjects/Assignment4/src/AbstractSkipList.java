import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;

abstract public class AbstractSkipList {
    final protected Node head;
    final protected Node tail;

    public AbstractSkipList() {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        increaseHeight();
    }

    public void increaseHeight() {
        head.addLevel(tail, null);
        //head.jumps.addLast(0);
        tail.addLevel(null, head);
        //tail.jumps.addLast(0);
    }


    abstract Node find(int key);

    abstract public int generateHeight();

    public Node search(int key) {
        Node curr = find(key);

        return curr.key() == key ? curr : null;
    }

    public Node insert(int key) {
        //int jump = 0;
        int nodeHeight = generateHeight();

        while (nodeHeight > head.height()) {
            increaseHeight();
        }

        Node prevNode = find(key);
        if (prevNode.key() == key) {
            return null;
        }

        Node newNode = new Node(key);

        for (int level = 0; level <= nodeHeight && prevNode != null; ++level) {
            Node nextNode = prevNode.getNext(level);

            // updating the field jumps for the new node in the level
            newNode.addLevel(nextNode, prevNode);

            prevNode.setNext(level, newNode);
            nextNode.setPrev(level, newNode);

            // updating the field jumps for the next node in the level
            //nextNode.setJumps(level, nextNode.getJumps(level) - jump);

            while (prevNode != null && prevNode.height() == level) {
                //jump = jump + prevNode.getJumps(level) + 1; // updating the value of jump
                prevNode = prevNode.getPrev(level);
            }
        }
        // updating the field jumps for the next node that higher than the new node
        /*Node curr = newNode.getNext(nodeHeight);
        for (int level = nodeHeight; level <= tail.height() && curr != null; level = level + 1) {
            while (curr != null && curr.height() == level)
                curr = curr.getNext(level);
            if (curr != null)
                curr.setJumps(level + 1, curr.getJumps(level + 1) + 1);
        }*/
        return newNode;
    }

    public boolean delete(Node node) {
        for (int level = 0; level <= node.height(); ++level) {
            Node prev = node.getPrev(level);
            Node next = node.getNext(level);
            prev.setNext(level, next);
            next.setPrev(level, prev);
            //updating the field jumps of the next node in the range of the deleted node height
            //next.setJumps(level, next.getJumps(level) + node.getJumps(level));
        }
        // updating the field jumps for the next node that higher than the deleted node
        /*Node curr = node.getNext(node.height());
        for (int level = node.height(); level <= tail.height() && curr != null; level = level + 1) {
            while (curr != null && curr.height() == level)
                curr = curr.getNext(level);
            if (curr != null)
                curr.setJumps(level + 1, curr.getJumps(level + 1) - 1);
        }*/

        return true;
    }

    public int predecessor(Node node) {
        return node.getPrev(0).key();
    }

    public int successor(Node node) {
        return node.getNext(0).key();
    }

    public int minimum() {
        if (head.getNext(0) == tail) {
            throw new NoSuchElementException("Empty Linked-List");
        }

        return head.getNext(0).key();
    }

    public int maximum() {
        if (tail.getPrev(0) == head) {
            throw new NoSuchElementException("Empty Linked-List");
        }

        return tail.getPrev(0).key();
    }

    private void levelToString(StringBuilder s, int level) {
        s.append("H    ");
        Node curr = head.getNext(level);

        while (curr != tail) {
            s.append(curr.key);
            s.append("    ");
        }

        s.append("T\n");
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int level = head.height(); level >= 0; --level) {
            levelToString(str, level);
        }

        return str.toString();
    }

    public static class Node {
        final private List<Node> next;
        final private List<Node> prev;
        private int height;
        final private int key;
        private LinkedList<Integer> jumps;

        public Node(int key) {
            next = new ArrayList<>();
            prev = new ArrayList<>();
            this.height = -1;
            this.key = key;
            this.jumps = new LinkedList<Integer>();
        }

        public Node getPrev(int level) {
            if (level > height) {
                throw new IllegalStateException("Fetching height higher than current node height");
            }

            return prev.get(level);
        }

        public Node getNext(int level) {
            if (level > height) {
                throw new IllegalStateException("Fetching height higher than current node height");
            }

            return next.get(level);
        }

        public void setNext(int level, Node next) {
            if (level > height) {
                throw new IllegalStateException("Fetching height higher than current node height");
            }

            this.next.set(level, next);
        }

        public void setPrev(int level, Node prev) {
            if (level > height) {
                throw new IllegalStateException("Fetching height higher than current node height");
            }

            this.prev.set(level, prev);
        }

        public void addLevel(Node next, Node prev) {
            ++height;
            this.next.add(next);
            this.prev.add(prev);
        }

        public int height() {
            return height;
        }

        public int key() {
            return key;
        }
/*
        public void addLevelJumps(Node next, Node prev, int jumps) {
            ++height;
            this.next.add(next);
            this.prev.add(prev);
            this.jumps.addLast(jumps);
        }

        public Integer getJumps(int level) {
            if (level < jumps.size())
                return jumps.get(level);
            else
                throw new IllegalStateException("The level is out of range");
        }

        public void setJumps(int level, int jump) {
            if (level < jumps.size())
                jumps.set(level, jump);
            else
                throw new IllegalStateException("The level is out of range");
        }
*/
    }
}