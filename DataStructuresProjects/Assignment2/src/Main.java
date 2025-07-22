public class Main {
    public static void main(String[] args) {
        DataStructure DT = new DataStructure();
        Point p1 = new Point(323, 257, "CR235");
        Point p2 = new Point(339, 222, "EC169");
        Point p3 = new Point(395, 231, "PZ764");
        Point p4 = new Point(471, 232, "TE220");
        Point p5 = new Point(505, 370, "RW435");
        Point p6 = new Point(451, 372, "QW098");
        Point p7 = new Point(254, 305, "VJ983");
        Point p8 = new Point(531, 426, "NP882");
        Point p9 = new Point(247, 255, "XP742");
        Point p10 = new Point(186, 224, "AL108");
        Point p11 = new Point(142, 304, "ZJ164");
        Point p12 = new Point(204, 432, "PV858");
        Point p13 = new Point(275, 521, "NU884");
        Point p14 = new Point(562, 264, "PP061");
        Point p15 = new Point(482, 538, "JK744");
        Point p16 = new Point(330, 352, "FN642");
        Point p17 = new Point(361, 451, "KS084");
        Point p18 = new Point(436, 496, "BB781");
        double try1 = DT.distance(p1,p2);
        double try2 = DT.distance(p7,p9);
        Point[] arr = {p1,p2,p7,p9};
        double minDis = DT.distance(arr[0],arr[1]);
        Point[] minPair ={arr[0],arr[1]};
        for (int i=0; i<arr.length-1;i++){
            for (int j =0;j<arr.length-1 & j!=i;j++){
                double tmpDis = DT.distance(arr[i],arr[j]);
                if (tmpDis<minDis){
                    minDis=tmpDis;
                    minPair[0]=arr[i];
                    minPair[1]=arr[j];
                }
            }
        }
       /* DT.addPoint(p1);
        DT.addPoint(p2);
        DT.addPoint(p3);
        DT.addPoint(p4);
        DT.addPoint(p5);
        DT.addPoint(p6);
        DT.addPoint(p7);
        DT.addPoint(p8);
        DT.addPoint(p9);
        DT.addPoint(p10);
        DT.addPoint(p11);
        DT.addPoint(p12);
        DT.addPoint(p13);*/
        DT.addPoint(p14);
        DT.addPoint(p15);
        DT.addPoint(p16);
        DT.addPoint(p17);
        //DT.addPoint(p18);
       // Container checkMedian = DT.getMedian(true);
        //Point[] try1 = DT.getPointsInRangeOppAxis(100, 600, true);
        //double density = DT.getDensity();
        //System.out.println(density);
        //Boolean axis = DT.getLargestAxis();
        //System.out.println(axis);
        //DT.narrowRange(400, 500, true);
        //Point[] try1 = DT.getPointsInRangeRegAxis(100, 600, true);
        Point[] pairs1 = DT.nearestPair();
    }
}
