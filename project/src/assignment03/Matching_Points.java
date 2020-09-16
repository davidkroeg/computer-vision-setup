package assignment03;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.ShapeRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.color.RandomColorGenerator;
import imagingbook.lib.math.Matrix;
import imagingbook.pub.corners.Corner;
import imagingbook.pub.geometry.basic.Point;
import imagingbook.pub.corners.HarrisCornerDetector;
import imagingbook.pub.geometry.fitting.AffineFit2D;
import imagingbook.pub.geometry.fitting.LinearFit2D;

public class Matching_Points implements PlugInFilter {

	private static int cornerSize = 2;					// size of cross-markers
	private static Color cornerColor = Color.green;		// color of cross markers
	
	private static double topSpacing = 45;
	private static double bottomSpacing = 70;
	private static double sideSpacing = 125;
	private static double midSpacing = 10;
	
	static int cornerStrength = 200000;
    private static int cornerStrengthForDrawing = 130000;
	
	ImagePlus im;
	
	@Override
	public int setup(String arg, ImagePlus im) {
		this.im = im;
        return DOES_ALL + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		if (!getUserInput()) {
			return;
		}
		
		final int width = ip.getWidth();
		final int height = ip.getHeight();
		
		//run corner detector
		HarrisCornerDetector cd = new HarrisCornerDetector(ip);
		List<Corner> corners = cd.getCorners();
		
		//split into 2 point clouds left and right
		List<Corner> leftCorners =  new ArrayList<Corner>();
		List<Corner> rightCorners =  new ArrayList<Corner>();
		
		for (Corner c : corners) {
			double x = c.getX();
			double y = c.getY();
			
			if(y > topSpacing & y < height - bottomSpacing) {
				if(x > sideSpacing & x  < width/2 - midSpacing) {
					leftCorners.add(c);
				} else if (x > width/2 + midSpacing & x < width - sideSpacing) {
					rightCorners.add(c);
				}
			}
		}
		
		//calculate centroids
		Point leftCentroid = getCentroid(leftCorners);
		Point rightCentroid = getCentroid(rightCorners);
		
		IJ.log("Left Centroid: " + leftCentroid.toString());
		IJ.log("Right Centroid: " + rightCentroid.toString());
		
		
		//calculate initial transformation A 
		RealMatrix tInit = createInitialTransformationMatrix(leftCentroid, rightCentroid);
		IJ.log("Initial Trans Matrix: " + tInit.toString());		
		
		//apply A on right point cloud
		 List<Corner> leftTransformedCorners = applyAffineTransformation(leftCorners, tInit);
		
		 List<Corner> closest = findClosestCorners(leftTransformedCorners, rightCorners);
		 
		 int counter = 0;

	     double error = getError(leftTransformedCorners, closest); //(initial error)
	     IJ.log(String.format("initial error: \u03B5 = %.6f", error));
	     
	     double prevError = Double.MAX_VALUE;
	 
	     //execute Affine Fitter	
	     while (Math.abs(prevError-error) > 0.001) {
	    	 prevError = error;
	    	 
	    	 LinearFit2D fitter = new AffineFit2D(mapToPoints(leftTransformedCorners), mapToPoints(closest));
	 		 double[][] Af = fitter.getTransformationMatrix().getData();
	 		 RealMatrix T = createTransformationMatrix(Af);
			 leftTransformedCorners = applyAffineTransformation(leftTransformedCorners, T);
	    	 closest = findClosestCorners(leftTransformedCorners, rightCorners);
			 error = Math.sqrt(fitter.getError());
	    	 counter++;
	     }
	     
		IJ.log(String.format("\nRMS error: \u03B5 = %.6f", error));
		
		//calculate correspondence
		List<List<Corner>> finalCorners = calculateCorrespondence(leftCorners, leftTransformedCorners, closest, 20);
		List<ShapeRoi> arcs = drawArcs(finalCorners.get(0), finalCorners.get(1)); 
		
		Overlay oly = new Overlay();
		ColorProcessor colorIp = ip.convertToColorProcessor();
		ImagePlus newIm = new ImagePlus("Overlay Test", colorIp);
		for(ShapeRoi arc : arcs) {
			oly.add(arc);
		}
		newIm.setOverlay(oly);
		
		drawCorners(colorIp, leftCorners, Color.red);
		drawCorners(colorIp, rightCorners, Color.green);
		newIm.show();
	}
	
	private Point[] mapToPoints(List<Corner> corners) {
		List<Point> pointList = new ArrayList<>();
		for(Corner c : corners) {
			pointList.add(Point.create(c.getX(), c.getY()));
		}
		
		return Point.toArray(pointList);
	}
	
	private Point getCentroid(List<Corner> pointCloud) {
		double dx = 0.0;
		double dy = 0.0;
		
		for(Point p : pointCloud) {
			dx += p.getX();
			dy += p.getY();
		}
		
		dx /= pointCloud.size();
		dy /= pointCloud.size();
		
		return Point.create(dx, dy);
	}
	
	private RealMatrix createInitialTransformationMatrix(Point centroidLeft, Point centroidRight) {
		RealMatrix A = MatrixUtils.createRealMatrix(new double[][] 
				{{ 1,  0, centroidRight.getX() - centroidLeft.getX()}, 
				 { 0,  1,  centroidRight.getY() - centroidLeft.getY()}, 
				 { 0,  0,  1}});
		return A;
	}
	
	private RealMatrix createTransformationMatrix(double[][] Af) {
		RealMatrix T = MatrixUtils.createRealMatrix(Af);
		return T;
	}
	
	private List<Corner> applyAffineTransformation(List<Corner> cornerList, RealMatrix M) {
		List<Corner> transformedCorners = new ArrayList<Corner>();
		for (Corner c : cornerList) {
			//b*M = a
			RealVector a = MatrixUtils.createRealVector(new double[]{c.getX(), c.getY(), 1});
			RealVector b = M.operate(a);
			
			Corner resultPoint = new Corner((float)b.getEntry(0), (float)b.getEntry(1), (float)c.getQ());
			
			transformedCorners.add(resultPoint);
		}
		
		return transformedCorners;
	}
	
    List<Corner> findClosestCorners(List<Corner> left, List<Corner> right) {
        List<Corner> closestCorners = new ArrayList<>();
        double[] distances = new double[left.size()];

        for(int i = 0; i < left.size(); i++) {
            Corner closest = right.get(0);
            double dist = Double.MAX_VALUE;

            for(int j = 0; j < right.size(); j++) {
                double distX = Math.abs(left.get(i).getX() - right.get(j).getX());
                double distY = Math.abs(left.get(i).getY() - right.get(j).getY());
                double distance = Math.sqrt(distX*distX + distY*distY);

                if(distance < dist) {
                    dist = distance;
                    closest = right.get(j);
                }
            }

            closestCorners.add(closest);
            distances[i] = dist;
        }

        return closestCorners;
    }
    
    private List<List<Corner>> calculateCorrespondence(List<Corner> initialCorners, List<Corner> calculatedCorners, List<Corner> closestCorners, double distanceThreshold) {
    	List<Corner> cornersLeftExisting = new ArrayList<>();
    	List<Corner> cornersNoOutliers = new ArrayList<>();
    	List<List<Corner>> correspondence = new ArrayList<>();
    	
    	for(int i = 0; i < initialCorners.size(); i++) {
    		if(distance(calculatedCorners.get(i), closestCorners.get(i)) < distanceThreshold) {
    			if(initialCorners.get(i).getQ() > cornerStrengthForDrawing) {
					cornersLeftExisting.add(initialCorners.get(i));
        			cornersNoOutliers.add(closestCorners.get(i));
				}
    			
    		}
    	}
    	
    	correspondence.add(cornersLeftExisting);
    	correspondence.add(cornersNoOutliers);
    	return correspondence;
    }
    
    double getError(List<Corner> transformed, List<Corner> original) {

        int n = transformed.size();
        int m = original.size();
        int difference = Math.abs(n-m);

        double squaredError = 0;
        double[] squaredDistance = new double[transformed.size()];

        for(int i = 0; i < transformed.size(); i++){
            double distX = Math.abs(transformed.get(i).getX()- original.get(i).getX());
            double distY = Math.abs(transformed.get(i).getY()- original.get(i).getY());
            squaredDistance[i] = distX*distX + distY*distY;
        }

        //remove biggest outstanding values if there is a difference in the number of compared corners
        for(int i = 0; i < difference; i++) {
            double max = 0;
            int index = i;

            for(int j = 0; j < squaredDistance.length; j++){
                if(squaredDistance[j] > max) {
                    max = squaredDistance[j];
                    index = j;
                }
            }

            squaredDistance[index] = 0.0;
        }

        for(int i = 0; i < squaredDistance.length; i++){
            squaredError += squaredDistance[i];
        }
        return squaredError;
    }
    
    private double distance(Corner first, Corner second) {
    	double distX = Math.abs(first.getX() - second.getX());
        double distY = Math.abs(first.getY() - second.getY());
        double distance = Math.sqrt(distX*distX + distY*distY);
    	return distance;
    }
	
	private void drawCorners(ImageProcessor ip, List<Corner> corners, Color color) {
		ip.setColor(color);
		int n = 0;
		for (Corner c: corners) {
			c.draw(ip, cornerSize);
			n = n + 1;
		}
	}
	
	private List<ShapeRoi> drawArcs(List<Corner> leftCorners, List<Corner> rightCorners) {
		RandomColorGenerator rcg = new RandomColorGenerator();
		List<ShapeRoi> arcs = new ArrayList<ShapeRoi>();
		
		for(int i=0; i < leftCorners.size(); i++) {
			Corner left = leftCorners.get(i);
			Corner right = rightCorners.get(i);
			Point ctrlp = Point.create((left.getX()+right.getX())/2, (left.getY() + right.getY())/2 + 40); 
			
			Shape arc = new QuadCurve2D.Double(left.getX(), left.getY(), ctrlp.getX(), ctrlp.getY(), right.getX(), right.getY());
			ShapeRoi arcRoi = new ShapeRoi(arc);
			
			arcRoi.setStrokeWidth(0.8f);
			arcRoi.setStrokeColor(rcg.nextColor());
			arcs.add(arcRoi);
		}
		return arcs;
	}
	
	private boolean getUserInput() {
		GenericDialog gd = new GenericDialog("Match Point Clouds");
		gd.addNumericField("Threshold for Drawing", cornerStrengthForDrawing, 40000);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		cornerStrengthForDrawing = (int) gd.getNextNumber();
		return true;
	}

}
