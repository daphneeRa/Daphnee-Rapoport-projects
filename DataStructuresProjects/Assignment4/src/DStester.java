import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class DStester {
    private static void good(){
        System.out.println("good");
    }
    public static void main(String[] args){
        System.out.println("this checker was written by the Legendary Tomer Cohen.");
        MyDataStructure ds = new MyDataStructure(20);
        HashingUtils hs = new HashingUtils();
        int[] array = {0,9,3,5,7,10,13,15};
        System.out.println("inserting 0,9,3,5,7,10,13,15 ");
        for(int i:array)
        ds.insert(i);

        System.out.print("checking if 3 contains: ");
        if(!ds.contains(3))
            System.out.println("3 should be in but its not!");
        else
            good();

        System.out.println("deleting 3");
        ds.delete(3);

        System.out.print("checking if 3 contains again: ");
        if(ds.contains(3))
            System.out.println("3 should be deleted but its not!");
        else
            good();


        System.out.print("checking rank of 10: ");
        int r = ds.rank(10);
        if(r!=5)
            System.out.println("rank(10) should be 5 but yours is: "+r);
        else
            good();

        System.out.print("checking rank of 4: ");
        r = ds.rank(4);
        if(r!=1)
            System.out.println("rank(4) should be 1 but yours is: "+r);
        else
            good();

        System.out.print("checking rank of 14: ");
        r = ds.rank(14);

        if(r!=6)
            System.out.println("rank(14) should be 6 but yours is: "+r);
        else
            good();


        System.out.print("checking select of 1: ");
        int s=ds.select(1);
        if(s!=0)
            System.out.println("select(1) should be 0 but yours is: "+s);
        else
            good();

        System.out.print("checking select of 3: ");
        s=ds.select(3);
        if(s!=7)
            System.out.println("select(3) should be 7 but yours is: "+s);
        else
            good();

        System.out.print("checking range of 4 to 16: ");
        List L= ds.range(4,16);
        if(L!=null)
            System.out.println("range(4,16) should be null but yours isnt");
        else
            good();

        System.out.print("checking range of 5 to 16: ");
        L= ds.range(5,16);
        if(!L.toString().equals("[5, 7, 9, 10, 13, 15]"))
            System.out.println("range(5,16) should be [5, 7, 9, 10, 13, 15] but yours is: "+L);
        else
            good();


    }


}
