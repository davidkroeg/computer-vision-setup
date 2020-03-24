package assignment03;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * This ImageJ plugin demonstrates the use of vector overlays applied
 * to a given image. Images with overlays can be saved (and reopened
 * with ImageJ) in TIFF format.
 * See http://rsbweb.nih.gov/ij/plugins/index.html for additional examples.
 * 
 * @author WB
 * @version 2017/03/21
 */
public class Overlay_Demo implements PlugIn {
	
	
	public void run(String arg0) {
		ImageProcessor ip = new ByteProcessor(200, 200);
		ip.setColor(Color.white);
		ip.fill();
		
		ImagePlus im = new ImagePlus("Overlay Test", ip);
		
		// create an empty overlay
		Overlay oly = new Overlay();
		
		// Example 0: polyline with integer coordinates
		int[] xPntsI = { 26, 120, 88, 161 };
		int[] yPntsI = { 38, 44, 108, 158 };
		Polygon p = new Polygon(xPntsI, yPntsI, xPntsI.length);
		Roi roi0 = new PolygonRoi(p, Roi.POLYLINE);
		roi0.setStrokeColor(Color.green);
		roi0.setStrokeWidth(0.5f);
		oly.add(roi0);
		
		// Example 1: a line with floating-point coordinates
		Path2D line = new Path2D.Double();
		line.moveTo(91.5, 37.5);
		line.lineTo(10.5, 123.5);
		ShapeRoi roi1 = new ShapeRoi(line);
		roi1.setStrokeWidth(0.2f);
		roi1.setStrokeColor(Color.blue);
		oly.add(roi1);
		
		// Example 2: closed polygon with floating-point coordinates
		double[] xPntsD = { 56.5, 170.5, 138.5, 211.5 };
		double[] yPntsD = { 88.5, 94.5, 158.5, 208.5 };
		Path2D path = new Path2D.Double();
		path.moveTo(xPntsD[0], yPntsD[0]);
		for (int i = 1; i < xPntsD.length; i++) {
			path.lineTo(xPntsD[i], yPntsD[i]);
		}
		path.closePath();
		Roi roi2 = new ShapeRoi(path);
		roi2.setStrokeColor(Color.yellow);
		roi2.setStroke(new BasicStroke(0.25f));	// many more stroke options available
		oly.add(roi2);
		
		// Example 3: a red circle
		Roi roi3 = new OvalRoi(100, 120, 50, 50);
		roi3.setStrokeColor(Color.red);
		roi3.setStrokeWidth(2.5f);
		oly.add(roi3);
		
		// Example 4: a quadratic curve
		Shape curve = new QuadCurve2D.Double(30, 150, 100, 90, 140, 140);
		ShapeRoi roi4 = new ShapeRoi(curve);
		roi4.setStrokeWidth(0.8f);
		roi4.setStrokeColor(Color.cyan);
		oly.add(roi4);
		
		// Example 5: some text
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
		Roi roi5 = new TextRoi(40, 60, "This drawing is simply amazing!", font);
		roi5.setStrokeColor(Color.magenta);
		oly.add(roi5);
		
		im.setOverlay(oly);
		im.show();
	}



}
