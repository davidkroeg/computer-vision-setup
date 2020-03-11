package assignment01;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Circle {

	private Point center;
	private double radius;
	private List<Point> pointsOnCircle = new ArrayList<Point>();
	
	public int pointCount;
	
	public Circle() {
		radius = -1;
		pointCount = 0;
	}
	
	public Circle(Point pt1, Point pt2, Point pt3) {
		radius = -1;
		center = new Point();
		pointCount = 0;
		
		if (!isPerpendicular(pt1, pt2, pt3)) {
			calcCircle(pt1, pt2, pt3);
		} else if (!isPerpendicular(pt1, pt3, pt2)) {
			calcCircle(pt1, pt3, pt2);
		} else if (!isPerpendicular(pt2, pt3, pt1)) {
			calcCircle(pt2, pt3, pt1);
		} else if (!isPerpendicular(pt3, pt2, pt1)) {
			calcCircle(pt3, pt2, pt1);
		} else if (!isPerpendicular(pt3, pt1, pt2)) {
			calcCircle(pt3, pt1, pt2);
		} else {
			radius = -1;
			return;
		}
		
	}
	
	public Point getCenter() {
		return center;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public List<Point> getPointsOnCircle() {
		return pointsOnCircle;
	}
	
	public void checkPointsOnCircle(List<Point> pntlist, double threshold) {
		pointsOnCircle.clear();
		
		for (Point point : pntlist) {
			double distanceToCenter = getCenter().distance(point);
			
			if (Math.abs(distanceToCenter - radius) < threshold) {
				pointCount++;
				pointsOnCircle.add(point);
			}
		}
	}
	
	private boolean isPerpendicular(Point pt1, Point pt2, Point pt3) {
		// check the given point are perpendicular to x or y axis
		double yDeltaA = pt2.getY() - pt1.getY();
		double xDeltaA = pt2.getX() - pt1.getX();
		double yDeltaB = pt3.getY() - pt2.getY();
		double xDeltaB = pt3.getX() - pt2.getX();
		
		// check if the line of the two points are vertical
		if (Math.abs(xDeltaA) <= 0.000000001 && Math.abs(yDeltaB) <= 0.000000001) {
			return false;
		}
		
		if (Math.abs(yDeltaA) <= 0.000000001) {
			return true;
		} else if (Math.abs(yDeltaB) <= 0.000000001) {
			return true;
		} else if (Math.abs(xDeltaA) <= 0.000000001) {
			return true;
		} else if (Math.abs(xDeltaB) <= 0.000000001) {
			return true;
		} else return false;
	}
	
	/**
	 * Calculates the center and the radius of a circle with 3 points and returns the radius
	 * @param pt1 point 1
	 * @param pt2 point 2
	 * @param pt3 point 3
	 * @return radius of the calculated circle as double
	 */
	private double calcCircle(Point pt1, Point pt2, Point pt3) {
		double yDeltaA = pt2.getY() - pt1.getY();
		double xDeltaA = pt2.getX() - pt1.getX();
		double yDeltaB = pt3.getY() - pt2.getY();
		double xDeltaB = pt3.getX() - pt2.getX();
		
		// if lines of points are orthogonal
		if (Math.abs(xDeltaA) <= 0.000000001 && Math.abs(yDeltaB) <= 0.000000001) {
			double xCenter = (pt2.getX() + pt3.getX()) / 2;
			double yCenter = (pt1.getY() + pt2.getY()) / 2;
			center.setLocation(xCenter, yCenter);
			radius = center.distance(pt1);
			return radius;
		}
		
		// isPerpendicular assure that xDeltas are not zero
		double aSlope = yDeltaA / xDeltaA;
		double bSlope = yDeltaB / xDeltaB;
		
		// check if Points are collinear
		if (Math.abs(aSlope -bSlope) <= 0.000000001) {
			return -1;
		}
		
		// calculate center point
		double xCenter = (aSlope * bSlope * (pt1.getY() - pt3.getY()) + bSlope * (pt1.getX() + pt2.getX())
				- aSlope * (pt2.getX() + pt3.getX()) ) / (2 * (bSlope - aSlope));
		double yCenter = -1 * (xCenter - (pt1.getX() + pt2.getX()) / 2) / aSlope + (pt1.getY() + pt2.getY()) / 2;
		center.setLocation(xCenter, yCenter);
		radius = center.distance(pt1);
		return radius;
	}
	
}
