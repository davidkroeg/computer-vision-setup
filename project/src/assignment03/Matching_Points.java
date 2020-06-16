package assignment03;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.List;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.corners.Corner;
import imagingbook.pub.corners.HarrisCornerDetector;

public class Matching_Points implements PlugInFilter {

	private static int cornerSize = 2;					// size of cross-markers
	private static Color cornerColor = Color.green;		// color of cross markers
	
	ImagePlus im;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.im = im;
        return DOES_ALL + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
//		HarrisCornerDetector cd = new HarrisCornerDetector(ip);
//		List<Corner> corners = cd.getCorners();
//		
//		ColorProcessor R = ip.convertToColorProcessor();
//		drawCorners(R, corners);
		
		Overlay oly = new Overlay();
		
		// Example 2: closed polygon with floating-point coordinates
		double[] xPntsD = { 109, 759, 759, 109 };
		double[] yPntsD = { 16, 16, 696, 696 };
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
		
		ImagePlus newIm = new ImagePlus("Overlay Test", ip);
		newIm.setOverlay(oly);
		newIm.show();
	}
	
	private void drawCorners(ImageProcessor ip, List<Corner> corners) {
		ip.setColor(cornerColor);
		int n = 0;
		for (Corner c: corners) {
			c.draw(ip, cornerSize);
			n = n + 1;
		}
	}

}
