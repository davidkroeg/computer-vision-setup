package assignment04;

import java.awt.geom.Point2D;

public class AlgebraicLine {
	
	private double a;
	private double b;
	private double c;
	
	
	//TODO: normalize Line during initialization
	public AlgebraicLine(double a, double b, double c) {
		//TODO: check that a and b are not 0 at the same time
		//TODO: incorporate test to see if parameters are admissible
	}
	
	public AlgebraicLine(Point2D p1, Point2D p2) {
		//TODO: implement
	}
	
	/**
	 * normalize the line
	 */
	void normalize() {
		//TODO: implement
	}
	
	/**
	 * Returns the minimum (perpendicular) distance between this line and the point p;
	 * @param p a 2D point.
	 * @return distance between this line and p as double
	 */
	double distance(Point2D p) {
		//TODO: implement
		return 0.0;
	}
	
	/**
	 * Returns the intersection point between this line and another algebraic line L2
	 * @param L2 another AlgebraicLine
	 * @return intersection Point2D between this and another line
	 */
	Point2D itnersect(AlgebraicLine L2) {
		//TODO: implement
		return null;
	}

}
