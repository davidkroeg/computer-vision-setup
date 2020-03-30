package assignment02;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagingbook.pub.geometry.basic.Point;
import imagingbook.pub.geometry.delaunay.DelaunayTriangulation;
import imagingbook.pub.geometry.delaunay.Triangle;
import imagingbook.pub.geometry.delaunay.guibas.TriangulationGuibas;

public class Structuring_By_Triangulation implements PlugInFilter {

	private static final String title = Structuring_By_Triangulation.class.getSimpleName();
	
	private static Color DelaunayColor = Color.green;
	private static Color PointColor = Color.magenta;
	private static float StrokeWidth = 0.1f;
	private static double PointRadius = 1.5;
	
	private List<Point> currentPoints = new ArrayList<>();
	
	ImagePlus im = null;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.im = imp;
		return DOES_ALL + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		int n = im.getStackSize();
		if (n < 2) {
			IJ.error("stack with 2 images required");
			return;
		}
		
		ImageStack stack = im.getStack();
		ImageProcessor ipStartImage = stack.getProcessor(1);
		ImageProcessor ipEndImage = stack.getProcessor(2);
		
		List<Point> startPoints = collectPoints(ipStartImage);
		List<Point> endPoints = collectPoints(ipEndImage);
		
		DelaunayTriangulation startTriangulation = new TriangulationGuibas(startPoints);
		DelaunayTriangulation endTriangulation = new TriangulationGuibas(endPoints);
		
		Collection<Triangle> startTriangles = startTriangulation.getTriangles();
		Collection<Triangle> endTriangles = endTriangulation.getTriangles();
		
		//test on one triangle
		//TODO: loop through all triangles and repeat
		Triangle startTriangle = startTriangles.iterator().next();
		Triangle  endTriangle = endTriangles.iterator().next();
		RealMatrix A = null;
		Point[] tempPoints1 = startTriangle.getPoints();
		
//		for(int i = 0; i <3; i++) {
//			
//		}
		
		Point[] tempPoints2 = endTriangle.getPoints();
		//do point permutation
		
		A = findAffineTransformation(tempPoints1, tempPoints2);
		
		//TODO: Apply a to all points 
		List<Point> projectedPoints = applyAffineTransformation(startPoints, A);
		
		//TODO: measure the distance of each projected point to its closest point
		double residualError = calculateError(projectedPoints, endPoints);
		IJ.log("Residual Error: " + residualError);
		//if error is smallest then save currentPoints to correspondancePoints
		
		//TODO: memorize the best constellation
		
		//TODO: find the least squares fit for the resulting point match
		//find transformation between realPoints and correspondancePoints 
		
//		showOverlay(ipStartImage, startTriangulation);
//		showOverlay(ipEndImage, endTriangulation);
		
		//draw result point list on second stack image
		ImageProcessor cp = ipEndImage.convertToColorProcessor();
		cp.setColor(Color.red);
		for (Point p : projectedPoints) {
			cp.drawDot((int)p.getX(), (int)p.getY());
		}
		showImage(cp, "colored dots");
	}
	
	private RealMatrix findAffineTransformation(Triangle startTriangle, Triangle endTriangle) {
		Point[] startPoints = startTriangle.getPoints();
		Point[] endPoints = endTriangle.getPoints();
		//TODO: point permutation
		//remove first point from list and append it 3 times
		
		RealMatrix M = createMatrix(startPoints);
		RealVector b = createVector(endPoints);
		
		//solve for A
		RealVector a = solveEquations(M, b);
		
		RealMatrix A = MatrixUtils.createRealMatrix(new double[][]
				{{a.getEntry(0), a.getEntry(1), a.getEntry(2)},
				 {a.getEntry(3), a.getEntry(4), a.getEntry(5)}
				});
		
		return A;
	}
	
	private RealMatrix findAffineTransformation(Point[] startPoints, Point[] endPoints) {
		RealMatrix M = createMatrix(startPoints);
		RealVector b = createVector(endPoints);
		
		//solve for A
		RealVector a = solveEquations(M, b);
		
		RealMatrix A = MatrixUtils.createRealMatrix(new double[][]
				{{a.getEntry(0), a.getEntry(1), a.getEntry(2)},
				 {a.getEntry(3), a.getEntry(4), a.getEntry(5)}
				});
		
		return A;
	}
	
	private List<Point> applyAffineTransformation(List<Point> pointList, RealMatrix M) {
		List<Point> resultPoints = new ArrayList<Point>();
		for (Point p : pointList) {
			
			RealVector a = MatrixUtils.createRealVector(new double[]{p.getX(), p.getY(), 1});
			
			RealVector b = M.operate(a);
			
			Point resultPoint = Point.create(b.getEntry(0), b.getEntry(1));
			
			resultPoints.add(resultPoint);
		}
		
		return resultPoints;
	}
	
	/**
	 * Run through resulPoints and find each corresponding point in the realPoints which is closest. Calculate the least squares error.
	 * @param resultPoints calculated points after the transformation
	 * @param realPoints the real points of the right image
	 * @return least squares error as a double
	 */
	private double calculateError(List<Point> resultPoints, List<Point> realPoints) {		
		double error = 0.0;
		currentPoints.clear();
		
		for (Point resultPoint : resultPoints) {
			Point correspondancePoint = Point.create(0, 0);
			for (Point realPoint : realPoints) {
				double errorDistance = resultPoint.distance(realPoint);
				if (errorDistance < resultPoint.distance(correspondancePoint)) {
					correspondancePoint = realPoint;
				}
			}
			error += calculateError(resultPoint, correspondancePoint);
			currentPoints.add(correspondancePoint);
		}
		IJ.log("Correspondance Points count: " + currentPoints.size());
		return error;
	}
	
	private double calculateError(Point p, Point x) {
		//take this if p is not the transformed point
//		double error = Math.pow(Math.pow((0.013*p.getX() + 1.088*p.getY() + 18.688 - x.getX()), 2) +
//				Math.pow((-1*p.getX() - 0.05*p.getY() + 127.5 - x.getY()), 2), 0.5);
		double error = Math.pow(Math.pow(p.getX() - x.getX(), 2) +
				Math.pow(p.getY() - x.getY(), 2), 0.5);
		return error;
	}
	
	private RealMatrix createMatrix(Point[] points) {
		RealMatrix M = MatrixUtils.createRealMatrix(new double[][] 
				{{ points[0].getX(),  points[0].getY(), 1, 0, 0, 0}, 
				 { 0, 0, 0, points[0].getX(),  points[0].getY(), 1}, 
				 { points[1].getX(),  points[1].getY(), 1, 0, 0, 0}, 
				 { 0, 0, 0, points[1].getX(),  points[1].getY(), 1},
				 { points[2].getX(),  points[2].getY(), 1, 0, 0, 0}, 
				 { 0, 0, 0, points[2].getX(),  points[2].getY(), 1},
				 });
		
		return M;
	}
	
	private RealVector createVector(Point[] points) {
		RealVector v = MatrixUtils.createRealVector(new double[]
				{points[0].getX(), points[0].getY(),points[1].getX(), points[1].getY(),points[2].getX(), points[2].getY()});
		return v;
	}
	
	private List<Point> collectPoints(ImageProcessor ip) {
		List<Point> vertices = new ArrayList<>();
		int M = ip.getWidth();
		int N = ip.getHeight();
		for (int v = 0; v < N; v++) {
			for (int u = 0; u < M; u++) {
				float val = ip.getPixelValue(u, v);
				if (val > 0) {
					vertices.add(new Point.Imp(u, v));
				}
			}
		}
		return vertices;
	}
	
	private void showOverlay(ImageProcessor ip, DelaunayTriangulation dt) {
		ImageProcessor cp = ip.convertToByteProcessor();
		Overlay overlay = makeOverlay(dt);

		ImagePlus im = new ImagePlus(title, cp);
		im.setOverlay(overlay);
		im.show();
	}
	
	private Overlay makeOverlay(DelaunayTriangulation dt) {
		Collection<Triangle> triangles = dt.getTriangles();
		Collection<Point> allPoints = dt.getPoints();
		Overlay oly = new Overlay();

		double r = PointRadius;
		for (Point p : allPoints) {
			double x = p.getX();
			double y = p.getY();
			Roi roi = new ShapeRoi(new Rectangle2D.Double(x - r, y - r, 2 * r, 2 * r));
			roi.setStrokeColor(PointColor);
			roi.setStrokeWidth(StrokeWidth);
			oly.add(roi);
		}

		Path2D path = new Path2D.Double();
		for (Triangle trgl : triangles) {
			Point[] pts = trgl.getPoints();
			Point a = pts[0];
			Point b = pts[1];
			Point c = pts[2];
			path.moveTo(a.getX(), a.getY());
			path.lineTo(b.getX(), b.getY());
			path.lineTo(c.getX(), c.getY());
			path.lineTo(a.getX(), a.getY());
		}
		Roi roi = new ShapeRoi(path);
		roi.setStrokeColor(DelaunayColor);
		roi.setStrokeWidth(StrokeWidth);
		oly.add(roi);
		
		oly.translate(0.5, 0.5);
		return oly;
	}
	
	//probably relevant for next exercise
	private RealVector solveEquations(RealMatrix A, RealVector b) {
		DecompositionSolver s = new SingularValueDecomposition(A).getSolver();
		IJ.log("Start: b = " + b.toString());
		// Solve the system of equations:
		RealVector x = s.solve(b);
		
		IJ.log("Solution: x = " + x.toString());
		
		// Verify that A.x = b:
		RealVector bb = A.operate(x);
		IJ.log("Check: A.x = " + bb.toString());
		return x;
	}
	
	void showImage(ImageProcessor ip, String title) {
		(new ImagePlus(title, ip)).show();
	}

}
