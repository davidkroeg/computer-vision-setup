package assignment04;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.regions.Contour;
import imagingbook.pub.regions.RegionContourLabeling;
import imagingbook.pub.regions.RegionLabeling.BinaryRegion;

/**
 * This plugin assumes as input a binary image with values
 * 0 (for background pixels) and >1 (for foreground pixels).
 * The input image is labeled and all detected regions are
 * processed.
 * It uses the binary region segmentation features provided 
 * by the 'imagingbook' library (installed in initial project).
 * 
 * @author WB
 *
 */
public class Region_Demo implements PlugInFilter {
	
	private ImagePlus im;

	public int setup(String arg, ImagePlus im) {
		this.im = im;
		return DOES_8G + NO_CHANGES;
	}

	public void run(ImageProcessor ip) {

		RegionContourLabeling segmenter = new RegionContourLabeling((ByteProcessor) ip);
		
		// This returns a list of regions, sorted by size:
		List<BinaryRegion> regions = segmenter.getRegions(true);
		IJ.log("Number of regions found: " + regions.size());

		ColorProcessor cp = ip.convertToColorProcessor();
		
		// Visit and color the pixels inside of each region:
		cp.setColor(new Color(255, 185, 182));
		for (BinaryRegion R : regions) {
			IJ.log("Region " + R.getLabel() + " has size " + R.getSize());
			IJ.log("   Center : " + R.getCenterPoint().toString());
			for (Point p : R) {
				cp.drawDot(p.x, p.y);
			}
		}
		
		// Color the outer contour pixels of each region:
		cp.setColor(Color.blue);
		for (BinaryRegion R : regions) {
			Contour c = R.getOuterContour();
			for (Point p : c) {
				cp.drawDot(p.x, p.y);
			}
		}
		
		new ImagePlus(im.getShortTitle() + "-colored", cp).show();
	}
}