package demos;

import ij.IJ;
import ij.ImagePlus;
import ij.io.LogStream;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class My_First_Plugin implements PlugInFilter {
	
	static {
		 LogStream.redirectSystem();	// to make System.out.println() work
	}

	public int setup(String arg, ImagePlus im) {
		return DOES_8G;
	}

	public void run(ImageProcessor ip) {
		IJ.log("Text output to the ImageJ console.");
		
		int w = ip.getWidth();
		int h = ip.getHeight();

		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				int p = ip.getPixel(u, v);
				ip.putPixel(u, v, 255 - p);
			}
		}
		
		System.out.println("Done.");
	}
}