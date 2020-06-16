package assignment07;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.LogStream;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * This ImageJ plugin demonstrates how to process a stack of float (32-bit) images.
 * The input stack is not supposed to be modified.
 * 
 * @author W. Burger
 * @version 2018/04/10
 *
 */
public class Float_Stack_Demo implements PlugInFilter {
	
	static {
		LogStream.redirectSystem();		// to enable System.out.println() etc.
	}
	
	private ImagePlus imp;
	
    public int setup(String arg, ImagePlus imp) {
    	this.imp = imp;
        return DOES_32 + STACK_REQUIRED + NO_CHANGES;
    }
    
    public void run(ImageProcessor ignored) {
    	ImageStack stack = imp.getImageStack();
    	int n = stack.getSize();
    	if (n < 1) {
    		IJ.error("Stack is too small!");
    		return;
    	}
    	
    	int w = stack.getWidth();		// all stack images have the same size
    	int h = stack.getHeight();
    	
    	for (int i = 0; i < n; i++) {
    		FloatProcessor ip = (FloatProcessor) stack.getProcessor(i + 1);	// this must be a FloatProcessor!
    		float[] pixels = (float[]) ip.getPixels();
    		// ...
    	}
    	
    }
    
}
