package assignment08;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagingbook.lib.image.ImageAccessor;

/**
 * This plugin demonstrates the calculation of the log-polar mapping. The
 * mapping is displayed in a separate window that is redrawn when the left mouse
 * button is clicked.
 * 
 * @author W. Burger
 * @version 05/2019
 *
 */
public class Log_Polar_Mapping_Interactive implements PlugInFilter, MouseListener {

	static int ANGULAR_STEPS = 100;
	static int RADIAL_STEPS = 60;
	static double LOG_BASE = 1.1;

	private ImagePlus img;
	private ImageCanvas canvas;

	private ImageProcessor sourceIp;
	private ImageProcessor logPolarIp;
	private ImagePlus logPolarIm;

	public int setup(String arg0, ImagePlus img) {
		this.img = img;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		sourceIp = ip;
		ip.setInterpolationMethod(ImageProcessor.BILINEAR);
		canvas = img.getWindow().getCanvas();
		canvas.addMouseListener(this);
		IJ.log("Click left in image " + img.getTitle() + " to create Log-Polar-Map.");
		IJ.log("Click Ctrl/left in image " + img.getTitle() + " to terminate.");
	}

	void finish() {
		canvas.removeMouseListener(this);
		IJ.log("done.");
	}

	private void repaintLogPolarImage(double xc, double yc) {
		if (logPolarIp == null) {
			logPolarIp = sourceIp.createProcessor(RADIAL_STEPS, ANGULAR_STEPS);
			logPolarIm = new ImagePlus("Log Polar Image", logPolarIp);
			logPolarIm.show();
		}
		ImageAccessor logPolarIa = ImageAccessor.create(logPolarIp);
		float[][][] L = logPolarMap(sourceIp, xc, yc, RADIAL_STEPS, ANGULAR_STEPS, LOG_BASE);
		for (int i = 0; i < RADIAL_STEPS; i++) {
			for (int j = 0; j < ANGULAR_STEPS; j++) {
				logPolarIa.setPix(i, j, L[i][j]);
			}
		}
		logPolarIm.updateAndDraw();
	}

	private float[][][] logPolarMap(ImageProcessor I, double xc, double yc, int m, int n, double b) {
		ImageAccessor ia = ImageAccessor.create(I);
		float[][][] L = new float[m][n][];
		for (int i = 0; i < m; i++) {
			double xi = i;
			double s = Math.pow(b, xi) - 1;
			for (int j = 0; j < n; j++) {
				double theta = (2 * Math.PI / n) * j;
				double x = s * Math.cos(theta);
				double y = s * Math.sin(theta);
				float[] pa = ia.getPix(xc + x, yc + y);
				L[i][j] = pa;
			}
		}
		return L;
	}

	// --------- mouse event handling --------------------

	public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()) {
			finish();
		} else {
			double x = canvas.offScreenXD(e.getX());
			double y = canvas.offScreenYD(e.getY());
			IJ.log("Mouse pressed: " + x + "," + y);
			IJ.log("Mag = " + canvas.getMagnification());
			repaintLogPolarImage(x, y);
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}
}
