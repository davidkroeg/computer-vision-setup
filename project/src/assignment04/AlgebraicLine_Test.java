package assignment04;

import java.awt.Color;
import java.awt.geom.Point2D;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class AlgebraicLine_Test implements PlugIn {

	private static double a = 0.650720;
	private static double b = 0.759318;
	private static double c = -157.162936;
	private static int lineWidth = 1;
	
	@Override
	public void run(String arg) {
		if (!getUserInput()) {
			return;
		}
		
		ImageProcessor ip = new ByteProcessor(300, 200);
		ip.setColor(Color.white);
		ip.fill();
		
		//create line with 2 Points
		Point2D upperLeft = new Point2D.Double(0, 0);
		Point2D bottomRight = new Point2D.Double(300, 200);	
		AlgebraicLine customLine = new AlgebraicLine(upperLeft, bottomRight);
		
		//create line with parameters
		AlgebraicLine testLine = new AlgebraicLine(a, b, c);
		
		//create Point at ip center
		Point2D centerPoint = new Point2D.Double(150, 100);
		
		ImageProcessor cp = ip.convertToColorProcessor();
		
		//draw lines
		cp.setColor(Color.blue);
		testLine.draw(cp, lineWidth);
		cp.setColor(Color.green);
		customLine.draw(cp, lineWidth);
		cp.setColor(Color.red);
		cp.drawDot((int)centerPoint.getX(), (int)centerPoint.getY());
		
		
		//test Utility methods
		IJ.log("Distance to center: " + Double.toString(testLine.distance(centerPoint)));
		
		Point2D intersectionPoint = testLine.intersect(customLine);
		cp.setColor(Color.cyan);
		cp.drawOval((int)intersectionPoint.getX() - 10, (int)intersectionPoint.getY() - 10, 20, 20);
		
		ImagePlus im = new ImagePlus("Algebraic Line Test", cp);
		im.show();
	}
	
	private boolean getUserInput() {
		GenericDialog gd = new GenericDialog("Draw Algebraic Line");
		gd.addNumericField("Parameter a", a, 6);
		gd.addNumericField("Parameter b", b, 6);
		gd.addNumericField("Parameter c", c, 6);
		gd.addNumericField("Line Width", lineWidth, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		a = (double) gd.getNextNumber();
		b = (double) gd.getNextNumber();
		c = (double) gd.getNextNumber();
		lineWidth = (int) gd.getNextNumber();
		return true;
	}

}
