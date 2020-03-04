package demos;

import ij.IJ;
import ij.ImagePlus;
import ij.io.LogStream;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class My_First_Plugin implements PlugInFilter {
	
	static {
		//used to make System.out.print() work
		 LogStream.redirectSystem();	// to make System.out.println() work
	}

	/*
	 * ImagePlus contains meta data for the image like name
	 */
	public int setup(String arg, ImagePlus im) {
		return DOES_8G;
	}
	
	/*
	 * Image Processor is the image data
	 * matrix of pixels
	 * iterate over all pixels read the value and change it to it's invers value
	 */

	public void run(ImageProcessor ip) {
		IJ.log("Changed something.");
		
		int w = ip.getWidth();
		int h = ip.getHeight();

		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				int p = ip.getPixel(u, v);
				ip.putPixel(u, v, 255 - p);
			}
		}
	}
	
	/*
	 * getPixel() can be slow because it just if its inside image
	 * use just 'get()'to be faster but will throw an exception if run outside
	 */
}