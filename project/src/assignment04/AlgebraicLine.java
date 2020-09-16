package assignment04;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import ij.IJ;
import ij.process.ImageProcessor;

public class AlgebraicLine {
	
	private double a;
	private double b;
	private double c;
	
	private final double threshold = 0.0000001;
	
	public AlgebraicLine(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
		normalize();
		
		//Check that a and b are not 0 at the same time
		if (Math.abs(Math.sqrt(Math.pow(this.a,2) + Math.pow(this.b,2)) - 1) > threshold) {
			this.a = 0;
			this.b = 0;
			this.c = 0;
			IJ.log("Invalid input parameters for algebraic line!");
		}
		
	}
	
	public AlgebraicLine(Point2D p1, Point2D p2) {
		a = -p2.getY() + p1.getY();
		b = p2.getX() - p1.getX();
		c = -a*p1.getX() - b*p1.getY();
		normalize();
	}
	
	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getC() {
		return c;
	}

	/**
	 * Runs through all pixels in the imageProcessor and checks if its on the line if yes it colors the pixel in the set color.
	 * @param ip ImageProcessor to draw in.
	 * @param lineWidth
	 */
	void draw(ImageProcessor ip, double lineWidth) {		
		final int w = ip.getWidth();
		final int h = ip.getHeight();
		List<Point> pntlist = new ArrayList<Point>();
		//check if pixel is on line
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				double distance = a*u + b*v + c;
				if(Math.abs(distance) < lineWidth/2) {
					pntlist.add(new Point(u, v));
				}
			}
		}
		
		for (Point p : pntlist) {
			ip.drawDot(p.x, p.y);
		}
	}
	
	/**
	 * Normalize the line
	 */
	void normalize() {
		double commonScaleFactor = 1/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		a *= commonScaleFactor;
		b *= commonScaleFactor;
		c *= commonScaleFactor;
	}
	
	/**
	 * Returns the minimum (perpendicular) distance between this line and the point p;
	 * @param p a 2D point.
	 * @return distance between this line and p as double
	 */
	double distance(Point2D p) {
		return a*p.getX() + b*p.getY() + c;
	}
	
	/**
	 * Returns the intersection point between this line and another algebraic line L2
	 * @param L2 another AlgebraicLine
	 * @return intersection Point2D between this and another line
	 */
	Point2D intersect(AlgebraicLine L2) {
		RealMatrix A = MatrixUtils.createRealMatrix(new double [][] {
			{a, b},
			{L2.getA(), L2.getB()}
		});
		RealVector c = MatrixUtils.createRealVector(new double [] { -this.c, -L2.getC() });
		
		DecompositionSolver s = new SingularValueDecomposition(A).getSolver();
		RealVector p = s.solve(c);
		Point2D intersectionPoint = new Point2D.Double(p.getEntry(0), p.getEntry(1));
		return intersectionPoint;
	}

}
