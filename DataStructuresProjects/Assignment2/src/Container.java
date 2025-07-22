
//Don't change the class name
public class Container {
	private Point data;
	private Container nextX;
	private Container nextY;
	private Container prevX;
	private Container prevY;

	// Constructors
	public Container(Point p, Container nextX, Container nextY, Container prevX, Container prevY) {
		data = p;
		this.nextX = nextX;
		this.nextY = nextY;
		this.prevX = prevX;
		this.prevY = prevY;
	}

	// Don't delete or change this function
	public Point getData() {
		return data;
	}

	public int getData(Boolean axis) {
		if(axis)
			return data.getX();
		else
			return data.getY();
	}

	public Container getNext(Boolean axis) {
		if(axis)
			return nextX;
		else
			return nextY;
	}

	public Container getPrev(Boolean axis) {
		if(axis)
			return prevX;
		else
			return prevY;
	}

	public void setNext(Container c, Boolean axis) {
		if(axis)
			this.nextX = c;
		else
			this.nextY = c;
	}
	public void setPrev(Container c, Boolean axis) {
		if(axis)
			this.prevX = c;
		else
			this.prevY = c;
	}
}
