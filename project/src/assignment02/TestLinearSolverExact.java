package assignment02;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * Solves the linear system A.x = b (for the unknown vector x).
 * Yields either an exact solution or a least-squares solution if the
 * system is over-determined.
 */
public class TestLinearSolverExact {

	public static void main(String[] args) {
		RealMatrix A = MatrixUtils.createRealMatrix(new double[][] 
				{{ 2,  3, -2}, 
				 {-1,  7,  6}, 
				 { 4, -3, -5}});
		RealVector b = MatrixUtils.createRealVector(new double[] {1, -2, 1});
		DecompositionSolver s = new SingularValueDecomposition(A).getSolver();
		
		// Solve the system of equations:
		RealVector x = s.solve(b);
		
		System.out.println("Solution: x = " + x.toString()); // = {-0.3698630137; 0.1780821918; -0.602739726}
		
		// Verify that A.x = b:
		RealVector bb = A.operate(x);
		System.out.println("Check: A.x = " + bb.toString());
	}
	
}
