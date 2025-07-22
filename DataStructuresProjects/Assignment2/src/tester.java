public class tester {
    public static void main(String[] args)
    {
        DataStructure DS = new DataStructure();
        DS.addPoint(new Point(1,1));
        DS.addPoint(new Point(2,2));
        DS.addPoint(new Point(4,5));
        DS.addPoint(new Point(8,8));
        DS.addPoint(new Point(-10,10));
        DS.addPoint(new Point(10,-10));
        DS.addPoint(new Point(500,100));
        DS.addPoint(new Point(5,-80));
        DS.addPoint(new Point(-5,20));
        DS.addPoint(new Point(-4,-200));

        System.out.println("checking the point in range methods, if its the same int then the order might change, dosent matter:");


        Point[] pir = DS.getPointsInRangeRegAxis(2,100,false);
        System.out.println("range of y from 2 to 100 should print 2,2 4,5 8,8 -10,10 -5,20 500,100:");
        printer(pir);

        pir = DS.getPointsInRangeRegAxis(2,70,true);
        System.out.println("range of x from 2 to 70 should print 2,2 4,5 5,-80 8,8 10,-10:");
        printer(pir);


        pir = DS.getPointsInRangeRegAxis(5,500,true);
        System.out.println("range of x from 5 to 500  should print 5,-80 8,8 10,-10 500,100 :");
        printer(pir);

        pir = DS.getPointsInRangeOppAxis(2,100,false);
        System.out.println("range of y from 2 to 100 opp  should print -10,10 -5,20 2,2 4,5 8,8 500,100 :");
        printer(pir);

        pir = DS.getPointsInRangeOppAxis(2,70,true);
        System.out.println("range of x from 2 to 70 opp should print 5,-80 10,-10 2,2 4,5 8,8 :");
        printer(pir);


        pir = DS.getPointsInRangeOppAxis(5,500,true);
        System.out.println("range of x from 5 to 500 opp should print 5,-80 10,-10 8,8 500,100:");
        printer(pir);

        System.out.println("get density +1  should be: 1.0000653594771243 ");
        System.out.println(DS.getDensity()+1);
        System.out.println();

        System.out.println("printing the corrent structure by x's should be :");
        System.out.println("-10,10 -5,20 -4,-200 1,1 2,2 4,5 5,-80 8,8 10,-10 500,100 ");
        pir = DS.getPointsInRangeRegAxis(-500,500,true);
        printer(pir);


        System.out.println("narrowing the range to -4 to 5 in the x's:");
        DS.narrowRange(-4,5,true);
        System.out.println("should print now: -4,-200 1,1 2,2 4,5 5,-80");
        pir = DS.getPointsInRangeRegAxis(-4,5,true);
        printer(pir);

        System.out.println("narrowing the range to -1 to 29 in the y's:");
        DS.narrowRange(-1,30,false);
        System.out.println("should print now: 1,1 2,2 4,5");
        pir = DS.getPointsInRangeRegAxis(-4,5,true);
        printer(pir);


        System.out.println("checking getLargestAxism should be the x's so false:");
        System.out.println(DS.getLargestAxis());
        System.out.println();

        System.out.println("checking getMedian in x's should be 2,2:");
        Container c= DS.getMedian(true);
        System.out.println(c.getData().getX()+ "," + c.getData().getY());
        System.out.println();

        System.out.println("checking nearestPairInStrip with 2,2 and width of 2 on x's, should be 1,1 and 2,2 :");
        printer(DS.nearestPairInStrip(c,2,true));

        System.out.println("checking nearestPairInStrip with 3,1 and width of 0.5 on x's, should be empty :");
        printer(DS.nearestPairInStrip(c,0.5,true));

        DS = new DataStructure();
        DS.addPoint(new Point(1,1));
        DS.addPoint(new Point(2,2));
        DS.addPoint(new Point(4,4));
        DS.addPoint(new Point(8,8));
        DS.addPoint(new Point(10,-10));
        DS.addPoint(new Point(-10,10));

        System.out.println("checking getMedian in x's should be 4,4:");
        c= DS.getMedian(true);
        System.out.println(c.getData().getX()+ "," + c.getData().getY());
        System.out.println();

        System.out.println("checking nearestPair should be 1,1 2,2:");
        printer(DS.nearestPair());

        System.out.println("end!");

    }

    public static void printer(Point[] parr){
        for(Point p:parr)
            System.out.print(p.getX() + "," + p.getY()+ " ");
        System.out.println();
        System.out.println();
    }
}
