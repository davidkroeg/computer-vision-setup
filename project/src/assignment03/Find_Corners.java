package assignment03;

import java.awt.Color;
import java.util.List;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.corners.Corner;
import imagingbook.pub.corners.HarrisCornerDetector;

/**
 * This plugin demonstrates usage of the Harris corner detector.
 * It calculates the corner positions in the current image
 * and shows the result in a new color image.
 * 
 * @version 2020/03/17
 */
public class Find_Corners implements PlugInFilter {
	
	private static int cornerSize = 2;					// size of cross-markers
	private static Color cornerColor = Color.green;		// color of cross markers
	
	ImagePlus im;

    public int setup(String arg, ImagePlus im) {
    	this.im = im;
        return DOES_ALL + NO_CHANGES;
    }
    
    public void run(ImageProcessor ip) {
    		
		HarrisCornerDetector cd = new HarrisCornerDetector(ip);
		List<Corner> corners = cd.getCorners();
		
		ColorProcessor R = ip.convertToColorProcessor();
		drawCorners(R, corners);
		new ImagePlus("Corners from " + im.getShortTitle(), R).show();
    }
	
	//-------------------------------------------------------------------
	
	private void drawCorners(ImageProcessor ip, List<Corner> corners) {
		ip.setColor(cornerColor);
		int n = 0;
		for (Corner c: corners) {
			c.draw(ip, cornerSize);
			n = n + 1;
		}
	}
	
}
