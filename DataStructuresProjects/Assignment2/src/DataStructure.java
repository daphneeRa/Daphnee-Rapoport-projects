public class DataStructure implements DT {
	private Container headX;
	private Container tailX;
	private Container headY;
	private Container tailY;
	private int planes;

	//////////////// DON'T DELETE THIS CONSTRUCTOR ////////////////
	public DataStructure()
	{
		headX = null;
		tailX = null;
		headY = null;
		tailY = null;
		planes = 0;
	}

	@Override
	public void addPoint(Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Illegal Point Inserted");
		}
		if (isEmpty()) {
// Updating the fields
			headX = new Container(point, null, null, null, null);
			tailX = headX;
			headY = headX;
			tailY = headX;
			planes = 1;

		} else { // searching the correct place for the new plan is the system
			Container correctPlaceX = findPlace(point, headX, tailX, true);
			Container correctPlaceY = findPlace(point, headY, tailY, false);

			// Placing the new plan according to his X,Y values
			Container newPlane;
			if (correctPlaceX.getData(true) > point.getX()) {
				if (correctPlaceY.getData(false) > point.getY()) {
					newPlane = new Container(point, correctPlaceX, correctPlaceY, correctPlaceX.getPrev(true),
							correctPlaceY.getPrev(false));
					if (correctPlaceX.getPrev(true) != null)// ==null in case the newPlane is placed first
						correctPlaceX.getPrev(true).setNext(newPlane, true);
					correctPlaceX.setPrev(newPlane, true);
					if (correctPlaceY.getPrev(false) != null)// ==null in case the newPlane is placed first
						correctPlaceY.getPrev(false).setNext(newPlane, false);
					correctPlaceY.setPrev(newPlane, false);
				} else {
					newPlane = new Container(point, correctPlaceX, correctPlaceY.getNext(false),
							correctPlaceX.getPrev(true), correctPlaceY);
					if (correctPlaceX.getPrev(true) != null)// ==null in case the newPlane is placed first
						correctPlaceX.getPrev(true).setNext(newPlane, true);
					correctPlaceX.setPrev(newPlane, true);
					if (correctPlaceY.getNext(false) != null) // ==null in case the newPlane is placed last
						correctPlaceY.getNext(false).setPrev(newPlane, false);
					correctPlaceY.setNext(newPlane, false);
				}
			} else {
				if (correctPlaceY.getData(false) > point.getY()) {
					newPlane = new Container(point, correctPlaceX.getNext(true), correctPlaceY, correctPlaceX,
							correctPlaceY.getPrev(false));
					if (correctPlaceX.getNext(true) != null) // ==null in case the newPlane is placed last
						correctPlaceX.getNext(true).setPrev(newPlane, true);
					correctPlaceX.setNext(newPlane, true);
					if (correctPlaceY.getPrev(false) != null)// ==null in case the newPlane is placed first
						correctPlaceY.getPrev(false).setNext(newPlane, false);
					correctPlaceY.setPrev(newPlane, false);
				} else {
					newPlane = new Container(point, correctPlaceX.getNext(true), correctPlaceY.getNext(false),
							correctPlaceX, correctPlaceY);
					if (correctPlaceX.getNext(true) != null) // ==null in case the newPlane is placed last
						correctPlaceX.getNext(true).setPrev(newPlane, true);
					correctPlaceX.setNext(newPlane, true);
					if (correctPlaceY.getNext(false) != null) // ==null in case the newPlane is placed last
						correctPlaceY.getNext(false).setPrev(newPlane,false);
					correctPlaceY.setNext(newPlane, false);
				}
			}
			// Updating the fields
			if (newPlane.getData(true) < headX.getData(true))
				headX = newPlane;
			if (newPlane.getData(true) > tailX.getData(true))
				tailX = newPlane;
			if (newPlane.getData(false) < headY.getData(false))
				headY = newPlane;
			if (newPlane.getData(false) > tailY.getData(false))
				tailY = newPlane;
			planes = planes + 1;
		}

	}

	private Container findPlace(Point point,Container first, Container last, boolean axis) {
		int pointData;
		if(axis) {
			pointData=point.getX();}
		else {
			pointData=point.getY();}
		while (pointData > first.getData(axis) && pointData < last.getData(axis)) {
			first = first.getNext(axis);
			last = last.getPrev(axis);
		}
		if (pointData < first.getData(axis))
			return first;
		else
			return last;
	}

	@Override
	public Point[] getPointsInRangeRegAxis(int min, int max, Boolean axis) {
		if(min > max)
			throw new IllegalArgumentException("The range given is illegal");
		//checks whether there are no planes in the range given, and if so - returns null
		if((axis & (max < headX.getData(true) | min > tailX.getData(true))) ||
				(!axis & (max < headY.getData(false) | min > tailY.getData(false))))
			return null;
		int counter = 0;
		Point[] output = new Point[planes]; //creates the output array with the max size of to total planes in the system
		Container curr;
		int index = 0; //index of the output array
		if (axis)
			curr = headX;
		else
			curr = headY;

		while (curr.getData(axis) < min) {
			curr = curr.getNext(axis);
		}
		while (curr != null && curr.getData(axis) >= min && curr.getData(axis) <= max) {
			output[index] = curr.getData();
			index++;
			counter ++;
			curr = curr.getNext(axis);
		}
		output = returnSmallerArray(counter, output);
		return output;
	}

	@Override
	public Point[] getPointsInRangeOppAxis(int min, int max, Boolean axis) {
		if (min > max)
			throw new IllegalArgumentException("The range given is illegal");
		// checks whether there are no planes in the range given, and if so - returns null
		if ((axis & (max < headX.getData(true) | min > tailX.getData(true)))
				|| (!axis & (max < headY.getData(false) | min > tailY.getData(false))))
			return null;
		Point[] output = new Point[planes]; // creates the output array with the max size of to total planes in the system
		Container curr;
		int counter = 0;
		int index = 0; // index of the output array
		// creates an X axis array
		if (axis) {
			curr = headY;
		} else {
			curr = headX;
		}

		while (curr != null) {
			if (curr.getData(axis) >= min && curr.getData(axis) <= max) {
				output[index] = curr.getData();
				index ++;
				counter ++;
			}
			curr = curr.getNext(!axis);
		}
		output = returnSmallerArray(counter, output);
		return output;
	}

	@Override
	public double getDensity() { // Calculating the density by the given formula
		if (headX.getData().equals(tailX.getData()))
			throw new IllegalArgumentException("Density is undefined");
		double planes = this.planes;
		return planes / ((tailX.getData(true) - headX.getData(true)) * (tailY.getData(false) - headY.getData(false)));
	}

	@Override
	public void narrowRange(int min, int max, Boolean axis) {
		int planesToDelete = 0;
		// deals with the case when all the points in the system should be deleted
		if (max-min < 0) {
			headX = null;
			headY = null;
			tailX = null;
			tailY = null;
			planes = 0;
		}
		else {
			Container currMin;
			Container currMax;
			if (axis) {
				currMin = headX;
				currMax = tailX;
			} else {
				currMin = headY;
				currMax = tailY;
			}
			// goes throw the range between minus infinity and min
			while (currMin.getData(axis) < min) {
				planesToDelete++;
				pointersChange(currMin, !axis);
				currMin = currMin.getNext(axis);
			}
			currMin.setPrev(null, axis); // deletes all points outside the range
			// goes throw the range from max and infinity
			while (currMax.getData(axis) > max) {
				planesToDelete++;
				pointersChange(currMax, !axis);
				currMax = currMax.getPrev(axis);
			}
			currMax.setNext(null, axis); // deletes all points outside the range
			if (axis) {
				this.headX = currMin;
				this.tailX = currMax;
			} else {
				this.headY = currMin;
				this.tailY = currMax;
			}
			this.planes = planes - planesToDelete;
		}
	}

	@Override
	public Boolean getLargestAxis() {
		//TODO - base case where the list is empty
		return ((tailX.getData(true) - headX.getData(true)) > (tailY.getData(false) - headY.getData(false)));
	}

	@Override
	public Container getMedian(Boolean axis) {
		int counter = planes / 2;
		Container curr;
		if (axis) {
			curr = headX;
		} else {
			curr = headY;
		}
		while (counter != 0) {
			curr = curr.getNext(axis);
			counter = counter - 1;
		}
		return curr;
	}


	@Override
	public Point[] nearestPairInStrip(Container container, double width, Boolean axis) {
		double minDist =Integer.MAX_VALUE;
		int length = 1;
		Point[] output = new Point[2];
		Container currRight= container;
		Container currLeft = container;
		int mid = container.getData(axis);
		while (currRight.getNext(axis)!=null && Math.abs(currRight.getNext(axis).getData(axis)-mid)<=(width/2)){
			length= length+1;
			double tmpDist = distance(currRight.getData(),currRight.getNext(axis).getData());
			if (tmpDist<minDist){
				minDist=tmpDist;
				output[0]=currRight.getData();
				output[1]=currRight.getNext(axis).getData();
			}
			currRight= currRight.getNext(axis);
		}
		while (currLeft.getPrev(axis)!=null && Math.abs(currLeft.getPrev(axis).getData(axis)-mid)<=(width/2)){
			length= length+1;
			double tmpDist = distance(currLeft.getData(),currLeft.getPrev(axis).getData());
			if (tmpDist<minDist){
				minDist=tmpDist;
				output[0]=currLeft.getData();
				output[1]=currLeft.getPrev(axis).getData();
			}
			currLeft= currLeft.getPrev(axis);
		}
		if(length == 2)
			return output;
		Point[] opp= new Point[length];
		for (int i = 0; i<length;i++){
			opp[i]= currLeft.getData();
			currLeft= currLeft.getNext(axis);
		}
		mergeSort(opp, 0, length-1,!axis);
		double distLeft;
		double distRight;
		int i = length / 2, j = length / 2;
		while (j < length - 1) {
			distLeft = distance(opp[i], opp[i - 1]);
			distRight = distance(opp[j], opp[j + 1]);
			double tmpDist = Math.min(distLeft,distRight);
			if (tmpDist<minDist)
				minDist=tmpDist;
			if (minDist == distLeft) {
				output[0] = opp[i];
				output[1] = opp[i - 1];
			} else if (minDist == distRight) {
				output[0] = opp[j];
				output[1] = opp[j + 1];
			}
			i = i - 1;
			j = j + 1;
			if (i == 1 && j == length - 1) { //In case array length%2==0
				j = j - 1;
				distLeft = distance(opp[i], opp[i - 1]);
				tmpDist = Math.min(distLeft,distRight);
				if (tmpDist<minDist)
					minDist=tmpDist;
				if (minDist == distLeft) {
					output[0] = opp[i];
					output[1] = opp[i - 1];
				}
				j = j + 1;
			}
		}
	return output;
}


	@Override
		public Point[] nearestPair() {
		Point[] output = new Point[2];
		if (planes < 2)
			return output;
		if (planes == 2) {
			output[0] = headX.getData();
			output[1] = tailX.getData();
			return output;
		}
		if(planes == 3 | planes == 4) {
			Point[] points = getPointsInRangeRegAxis(headX.getData(true), headY.getData(true), true);
			output = nearestPairSpec(points);
			return output;
		}
		boolean axis = getLargestAxis();
		Container median = getMedian(axis);
		Point[] smallCouple;
		Point[] largeCouple;
		// finds the nearest points in each side of the axis, using a recursive function
		if(axis) {
			smallCouple = recNearestPair(headX, median.getPrev(true), axis);
			largeCouple = recNearestPair(median.getNext(true), tailX, axis);
		}
		else {
			smallCouple = recNearestPair(headY, median.getPrev(false), axis);
			largeCouple = recNearestPair(median.getNext(false), tailY, axis);
		}
		double minDist;
		Point[] nearest;
		// finds the minimum distance between both couples of points found
		if (distance(smallCouple[0],smallCouple[1]) < distance(largeCouple[0], largeCouple[1])) {
			minDist = distance(smallCouple[0],smallCouple[1]);
			nearest = smallCouple;
		} else {
			minDist = distance(largeCouple[0],largeCouple[1]);
			nearest = largeCouple;
		}
		Point[] nearestStrip = nearestPairInStrip(median, 2*minDist, axis);
		if(nearestStrip[0] == null) {
			return nearest;
		}
		else if ((axis && (Math.abs(nearestStrip[0].getX() - nearestStrip[1].getX()) < minDist)) ||
				(!axis && (Math.abs(nearestStrip[0].getY() - nearestStrip[1].getY()) < minDist))) {
			return nearestStrip;
		}
		else
			return nearest;
	}

	private void mergeSort(Point[] arr, int left, int right,boolean axis) {
		if (left < right) {
			int mid = (left + right) / 2;
			mergeSort(arr, left, mid,axis);
			mergeSort(arr, mid + 1, right,axis);
			merge(arr, left, mid, right,axis);

		}
	}

	private void merge(Point[] arr, int left, int mid, int right,boolean axis) {
		int n1 = mid - left + 1;
		int n2 = right - mid;
		Point[] L = new Point[n1];
		Point[] R = new Point[n2];
		for (int i = 0; i < n1 ; i=i+1)
			L[i] = arr[left + i];
		for (int j = 0; j < n2 ; j=j+1)
			R[j] = arr[mid +j + 1];
		int i = 0;
		int j = 0;
		int k = left;
		while (i <n1 && j<n2) {
			if(!axis){
			if (L[i].getY() <= R[j].getY()) {
				arr[k] = L[i];
				i = i + 1;
			} else {
				arr[k] = R[j];
				j = j + 1;
			}}
			else{
				if (L[i].getX() <= R[j].getX()) {
					arr[k] = L[i];
					i = i + 1;
				} else {
					arr[k] = R[j];
					j = j + 1;
				}
			}
			k++;
		}
		while (i < n1) {// Copy remaining elements of L[] if any
			arr[k] = L[i];
			i=i+1;
			k=k+1;
		}
		while (j < n2) { // Copy remaining elements of R[] if any
			arr[k] = R[j];
			j=j+1;
			k=k+1;
		}
	}

	// the function is a rec function that find the 2 nearest points in a range given
	private Point[] recNearestPair(Container min, Container max, boolean axis) {
		if (min.getNext(axis).getData(axis) == max.getData(axis)) {
			return new Point[]{ min.getData(), max.getData() };
		} else if (min.getNext(axis).getNext(axis).getData(axis) == max.getData(axis)) {
			Point[] p1 = { min.getData(), min.getNext(axis).getData() };
			Point[] p2 = { min.getData(), max.getData() };
			Point[] p3 = { min.getNext(axis).getData(), max.getData() };
			return hasSmallerDist(p1, hasSmallerDist(p2,p3));
		} else {
			Container mid = getMedianSR(axis, min, max);
			Point[] minDisRec = hasSmallerDist(recNearestPair(min, mid.getPrev(axis),axis), recNearestPair(mid, max,axis));
			double dis = distance(minDisRec[0], minDisRec[1]);
			Point[] nearestPairInStrip = nearestPairInStrip(mid, dis, axis);
			return hasSmallerDist(nearestPairInStrip, minDisRec);

		}
	}


	private Point[] hasSmallerDist(Point[] p1, Point[] p2) {
		double dist1 = distance(p1[0],p1[1]);
		double dist2 = distance(p2[0],p2[1]);
		if (dist1 < dist2)
			return p1;
		else
			return p2;
	}

	private Container getMedianSR(Boolean axis, Container min, Container max) {
		Container first = min;
		Container last = max;
		while (first.getData(axis) <= last.getData(axis)) {
			first = first.getNext(axis);
			last = last.getPrev(axis);
		}
		return first;
	}

	private boolean isEmpty(){
		return (headX == null);
	}

	public static double distance(Point a, Point b) {
		return Math.sqrt(Math.pow(b.getY() - a.getY(), 2) + Math.pow(b.getX() - a.getX(), 2));

	}

	// the two functions changes the X/Y pointers accordingly, to prevent a demolition of the pointers

	private void pointersChange(Container curr, Boolean axis) {
		if(axis) {
			if (curr.getPrev(axis) == null) {
				curr.getNext(axis).setPrev(null, true);
				headX = curr.getNext(axis);
			} else
				curr.getPrev(axis).setNext(curr.getNext(axis), true);
			if (curr.getNext(axis) == null) {
				curr.getPrev(axis).setNext(null,true);
				tailX = curr.getPrev(axis);
			} else
				curr.getNext(axis).setPrev(curr.getPrev(axis), true);
		}
		else {
			if (curr.getPrev(axis) == null) {
				curr.getNext(axis).setPrev(null,false);
				headY = curr.getNext(axis);
			}
			else
				curr.getPrev(axis).setNext(curr.getNext(axis), false);
			if (curr.getNext(axis) == null) {
				curr.getPrev(axis).setNext(null, false);
				tailY = curr.getPrev(axis);
			}
			else
				curr.getNext(axis).setPrev(curr.getPrev(axis), false);
		}

	}

	private Point[] returnSmallerArray(int length, Point[] array) {
		Point[] output = new Point[length];
		for (int i=0; i<length; i++) {
			output[i] = array[i];
		}
		return output;
	}

	private Point[] nearestPairSpec(Point[] arr){
		double minDis =distance(arr[0],arr[1]);
		Point[] minPair ={arr[0],arr[1]};
		for (int i =0; i<arr.length-1;i++){
			for (int j =i+1;j<=arr.length-1;j++){
				double tmpDis = distance(arr[i],arr[j]);
				if (tmpDis<minDis){
					minDis=tmpDis;
					minPair[0]=arr[i];
					minPair[1]=arr[j];
				}
			}
		}
		return minPair;
	}
}

