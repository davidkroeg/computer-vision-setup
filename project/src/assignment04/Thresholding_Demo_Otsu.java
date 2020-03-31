package assignment04;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.threshold.global.GlobalThresholder;
import imagingbook.pub.threshold.global.OtsuThresholder;

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
public class Thresholding_Demo_Otsu implements PlugInFilter {

	public int setup(String arg, ImagePlus im) {
		return DOES_8G;
	}

	public void run(ImageProcessor ip) {
		IJ.log("Starting");
		GlobalThresholder thr = new OtsuThresholder();
		IJ.log("thr = " + thr);
		int q = thr.getThreshold((ByteProcessor) ip);
		IJ.log("q = " + q);
		if (q < 0) {
			IJ.error("no threshold found");
			return;
		}
		ip.threshold(q);
		ip.invert(); // we want the dark regions as foreground (> 0)!
	}
}