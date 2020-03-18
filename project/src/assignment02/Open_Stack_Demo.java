package assignment02;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * This IJ plugin opens an image stack (TIF) that is assumed to contain 
 * at least 2 images of identical size.
 * 
 * @author WB
 *
 */
public class Open_Stack_Demo implements PlugInFilter {
	
	ImagePlus im = null;
	
	public int setup(String arg, ImagePlus im) {
		this.im = im;	// keep a reference to im
		return DOES_8G + STACK_REQUIRED;
	}

	public void run(ImageProcessor ip) {
		int n = im.getStackSize();
		if (n < 2) {
			IJ.error("stack with 2 images required");
			return;
		}
		
		// iterate over all frames of the stack:
		ImageStack stack = im.getStack();
		for (int k = 1 ; k <= n; k++) {	// NOTE: k starts from 1!!
			ImageProcessor ipk = stack.getProcessor(k);
			ipk.invert();	// for demo only!
		}
	}
}
