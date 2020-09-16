package assignment04;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import imagingbook.lib.math.Eigensolver2x2;

public class AutoFitLine {
	private AlgebraicLine line;
	private double error;
	List<Point> pointList;
	
	public AutoFitLine(List<Point> pts) {
		pointList = pts;
		line = fitLine();
	}

	public AutoFitLine() {
		pointList = new ArrayList<Point>();
		line = new AlgebraicLine(0,0,0);
		error = 0;
	}
	
	public AlgebraicLine fitLine() {
		double x = 0;
		double y = 0;
		double xx = 0;
		double yy = 0;
		double xy = 0;
		
		for(Point p : pointList) {
			x  += p.getX();
			y  += p.getY();
			xx += Math.pow(p.getX(), 2);
			yy += Math.pow(p.getY(), 2);
			xy += p.getX()*p.getY();
		}
		x /= pointList.size();
		y /= pointList.size();
		xx /= pointList.size();
		yy /= pointList.size();
		xy /= pointList.size();
				
				
		//Solve 2x2 eigenproblem
		Eigensolver2x2 solver = new Eigensolver2x2(
										xx-Math.pow(x, 2),
										xy-x*y,
										xy-x*y, 
										yy-Math.pow(y,2));
		
		
		double[][] eigen_vectors = solver.getEigenvectors();
		double[]   eigen_values = solver.getEigenvalues();
		
		int idx = eigen_values[0] < eigen_values[1] ? 0 : 1;
		
		double a = eigen_vectors[idx][0];
		double b = eigen_vectors[idx][1];
		double c = -x*eigen_vectors[idx][0] - y*eigen_vectors[idx][1];
		
		line = new AlgebraicLine(a,b,c);
		
		error = 0;
		for(Point p : pointList) {
			error += Math.pow(line.distance(p),2);
		}
		
		return line;
	}
	
	public void clear() {
		pointList.clear();
		line = new AlgebraicLine(0,0,0);
		pointList.clear();
		error = 0;
	}
	
	public void addFront(Point p) {
		pointList.add(0, p);
		fitLine();
	}
	
	public void removeFront() {
		pointList.remove(0);
		fitLine();
	}
	
	public void addBack(Point p) {
		pointList.add(p);
		fitLine();
	}
	
	public void removeBack() {
		pointList.remove(pointList.size()-1);
		fitLine();
	}
	
	public Point getPoint(int i) {
		return pointList.get(i);
	}
	
	public double getError() {
		return error;
	}
	
	public AlgebraicLine getLine() {
		return line;
	}
	
	public List<Point> getPoints(){
		return pointList;
	}
}
