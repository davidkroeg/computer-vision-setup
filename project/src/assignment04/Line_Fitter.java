package assignment04;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagingbook.lib.math.Eigensolver2x2;

public class Line_Fitter implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		List<Point> pointList = collectPoints(ip);
		
		//calculate matrix values
		double x = 0;
		double y = 0;
		double xx = 0;
		double yy = 0;
		double xy = 0;
		
		for(Point p : pointList) {
			x += p.getX();
			y += p.getY();
			xx += Math.pow(p.getX(), 2);
			yy += Math.pow(p.getY(), 2);
			xy += p.getX()*p.getY();
		}
		
		x /= pointList.size();
		y /= pointList.size();
		xx /= pointList.size();
		yy /= pointList.size();
		xy /= pointList.size();
		
		//solve eigenproblem
		Eigensolver2x2 solver = new Eigensolver2x2(
				xx - Math.pow(x, 2),
				xy - x * y,
				xy - x * y,
				yy - Math.pow(y, 2)
				);
		
		double[][] eigenVectors = solver.getEigenvectors();
		double [] eigenValues = solver.getEigenvalues();
		
		//pick the lower eigenvalue, the smaller one minimizes the error
		int lowerValueIndex = eigenValues[0] < eigenValues[1] ? 0 : 1;
		
		double a = eigenVectors[lowerValueIndex][0];
		double b = eigenVectors[lowerValueIndex][1];
		double c = -x * eigenVectors[lowerValueIndex][0] - y * eigenVectors[lowerValueIndex][1];
		
		//create line with parameters
		AlgebraicLine resultLine = new AlgebraicLine(a, b, c);
		
		ImageProcessor cp = ip.convertToColorProcessor();
		cp.setColor(Color.blue);
		resultLine.draw(cp, 1);
		
		//calculate final error
		double distance = 0;
		for(Point p : pointList) {
			distance += Math.pow(resultLine.distance(p), 2);
		}
		
		IJ.log("Fitted Line parameters: a("+Double.toString(resultLine.getA())+"), b("+Double.toString(resultLine.getB())+"), c("+Double.toString(resultLine.getC())+")");
		IJ.log("Total error: " + distance);
		
		new ImagePlus("Line fitter", cp).show();
	}
	
	private List<Point> collectPoints(ImageProcessor ip) {
		List<Point> pointList = new ArrayList<Point>();
		
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				if(ip.getPixel(x, y) != 1) {
					pointList.add(new Point(x, y));
				}
			}
		}
		return pointList;
	}

}
