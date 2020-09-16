package assignment08;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * This plugin demonstrates the calculation of the log-polar mapping. The
 * mapping is displayed in a separate window that is redrawn when the mouse is
 * moved.
 * 
 * @author W. Burger
 * @version 05/2019
 */
public class Log_Polar_Mapping_Continuous implements PlugInFilter, MouseListener, MouseMotionListener {

	static int ANGULAR_STEPS = 100;
	static int RADIAL_STEPS = 60;
	static double LOG_BASE = 1.1;

	ImageProcessor sourceIp;
	ImagePlus sourceIm;
	ImageCanvas canvas;
	ImageProcessor logPolarIp;
	ImagePlus logPolarIm;

	boolean active = false;

	public int setup(String arg0, ImagePlus img) {
		this.sourceIm = img;
		return DOES_8G + DOES_RGB + NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		if (!getUserInput()) {
			return;
		}
		sourceIp = ip;
		sourceIp.setInterpolationMethod(ImageProcessor.BICUBIC);
		makeDxDyTable();
		active = false;
		canvas = sourceIm.getCanvas(); // sourceIm.getWindow().getCanvas();
		canvas.addMouseListener(this);

		// IJ.log("Click left in image to start/terminate log-polar mapping");

	}

	double[][][] dXdYTable; // dXdYTable[r][a][xy]

	void makeDxDyTable() {
		dXdYTable = new double[RADIAL_STEPS][ANGULAR_STEPS][2];
		final double da = 2 * Math.PI / ANGULAR_STEPS;
		for (int ri = 0; ri < RADIAL_STEPS; ri++) {
			double radius = Math.pow(LOG_BASE, ri);
			for (int ai = 0; ai < ANGULAR_STEPS; ai++) {
				double angle = da * ai;
				dXdYTable[ri][ai][0] = radius * Math.cos(angle);
				dXdYTable[ri][ai][1] = radius * Math.sin(angle);
			}
		}
	}

	void repaintLogPolarImage(double xc, double yc) {
		if (logPolarIp == null) {
			logPolarIp = sourceIp.createProcessor(RADIAL_STEPS, ANGULAR_STEPS);// new ByteProcessor(RADIAL_STEPS,
																				// ANGULAR_STEPS);
			logPolarIm = new ImagePlus("Log Polar Image", logPolarIp);
			logPolarIm.show();
		}

		for (int ri = 0; ri < RADIAL_STEPS; ri++) {
			for (int ai = 0; ai < ANGULAR_STEPS; ai++) {
				double x = xc + dXdYTable[ri][ai][0];
				double y = yc + dXdYTable[ri][ai][1];
				int p = sourceIp.getPixelInterpolated(x, y);
				logPolarIp.putPixel(ri, ai, p);
			}
		}
		logPolarIm.updateAndDraw();
	}

	void activate() {
		if (!active) {
			canvas.addMouseMotionListener(this);
			active = true;
		}
	}

	void terminate() {
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		// IJ.log("terminated.");
	}

	// --------- mouse event handling --------------------

	public void mouseClicked(MouseEvent e) {
		if (active)
			terminate();
		else
			activate();
	}

	public void mouseMoved(MouseEvent e) {
		if (active) {
			double x = canvas.offScreenXD(e.getX());
			double y = canvas.offScreenYD(e.getY());
			repaintLogPolarImage(x, y);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	// -------- User Dialog ------------------------------------

	boolean getUserInput() {
		GenericDialog gd = new GenericDialog("Log-Polar Mapping");
		gd.addNumericField("Radial steps", RADIAL_STEPS, 0);
		gd.addNumericField("Angular steps", ANGULAR_STEPS, 0);
		gd.addNumericField("Log base", LOG_BASE, 2);
		gd.addMessage("Click left in image\n to start/terminate\n log-polar mapping");
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		RADIAL_STEPS = (int) gd.getNextNumber();
		ANGULAR_STEPS = (int) gd.getNextNumber();
		LOG_BASE = gd.getNextNumber();
		return true;
	}

}
