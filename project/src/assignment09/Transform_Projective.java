package assignment09;



import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagingbook.lib.image.ImageMapper;
import imagingbook.lib.interpolation.InterpolationMethod;
import imagingbook.pub.geometry.basic.Point;
import imagingbook.pub.geometry.mappings.Mapping2D;
import imagingbook.pub.geometry.mappings.linear.ProjectiveMapping2D;

/**
 * This plugin demonstrates the use of geometric mappings, as implemented
 * in the imagingbook library.
 * A {@link ProjectiveMapping} (transformation) is specified by 4 corresponding point
 * pairs, given by P and Q.
 * The mapping defines the target-to-source transformation.
 * The actual pixel transformation is performed by an {@link ImageMapper} object.
 * Try on the "bridge" test image and check if the image corners (P) are
 * mapped to the points specified in Q.
 * 
 * @author WB
 *
 */
public class Transform_Projective implements PlugInFilter {
	//transformation for 4 point matches
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {
    	int w = ip.getWidth();
    	int h = ip.getHeight();
    	
    	//imageJ Point data structure
		Point p1 = Point.create(0, 0);
		Point p2 = Point.create(w, 0);
		Point p3 = Point.create(w, h);
		Point p4 = Point.create(0, h);

		Point q1 = Point.create(0, 60);
		Point q2 = Point.create(400, 20);
		Point q3 = Point.create(300, 400);
		Point q4 = Point.create(30, 200);
		
		Point[] P = {p1, p2, p3, p4};		// source points
		Point[] Q = {q1, q2, q3, q4};		// target points

		// We need the target-to source mapping, i.e. Q -> P. There are 2 alternatives:
		Mapping2D imap = ProjectiveMapping2D.fromPoints(P, Q).getInverse();		// P -> Q, then invert
//		Mapping2D imap = ProjectiveMapping2D.fromPoints(Q, P);		// Q -> P = inverse mapping by swapping point sets (less computation)
		
		// Now we apply the geometric mapping to the input image:
		ImageMapper mapper = new ImageMapper(imap, InterpolationMethod.Bicubic);
		mapper.map(ip);
		
		//copy pixels over after doing transformation
	}
}
