package assignment05;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class ShapeHistogram {

	private double[][] _data;
	private int _circles;
	private int _sections;
	private Color _color;
	private Rectangle _bounding_box;
	
	public ShapeHistogram(int circles, int sections) {
		_circles = circles;
		_sections = sections;
		_data = new double[_circles][_sections];
	}
	
	public void setColor(Color c) {
		_color = c;
	}
	
	public void setBoundingBox(Rectangle bb) {
		_bounding_box = bb;
	}
	
	public Rectangle getBoundingBox() {
		return _bounding_box;
	}
	
	public Color getColor() {
		return _color;
	}
	
	public int getNumCircles() {
		return _circles;
	}
	
	public int getNumSections() {
		return _sections;
	}
	
	public double get(int circle, int section) {
		if(circle > _circles || section > _sections) {
			return -1;
		}
		return _data[circle][section];
	}
	
	public void increaseCount(int circle, int section) {
		if(circle < _circles || section < _sections) {
			_data[circle][section]++;
		}
		else {
			IJ.log("shit, it does not work");
		}
	}
	
	public void normalize() {
		//calculate normalization factor by getting sum of all elements
		double sum = 0;
		for(int idx_circles = 0; idx_circles < _circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < _sections; idx_sections++) {
				sum += _data[idx_circles][idx_sections];
			}
		}
		
		//apply normalization factor by dividing all elements by normalization factor
		for(int idx_circles = 0; idx_circles < _circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < _sections; idx_sections++) {
				_data[idx_circles][idx_sections] /= sum;
			}
		}
	}
	
	public void display(String title) {
		ImageProcessor hist = new ColorProcessor(_sections*20, _circles*20);
		hist.invert();
		
		double max = 0;
		for(int i = 0; i < _circles; i++) {
			DoubleSummaryStatistics stat = Arrays.stream(_data[i]).summaryStatistics();
			if(stat.getMax() > max) {
				max = stat.getMax();
			}
		}
		
		for(int idx_circles = 0; idx_circles < _circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < _sections; idx_sections++) {
				for(int y = 0; y < 20; y++) {
					for(int x = 0; x < 20; x++) {
						int rgb_val = (int)((_data[idx_circles][idx_sections]*1.0)/(max*1.0) * 255);
						Color c = new Color(rgb_val, rgb_val, rgb_val);
						hist.setColor(c);
						hist.drawDot(idx_sections*20+x, idx_circles*20+y);
					}
				}
			}
		}
		
		new ImagePlus(title, hist).show();
	}
	
	public boolean equals(ShapeHistogram sh, double tolerance) {
		if(_circles != sh.getNumCircles() || _sections != sh.getNumSections()) {
			return false;
		}
		
		
		for(int idx_circles = 0; idx_circles < _circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < _sections; idx_sections++) {
				if(Math.abs(_data[idx_circles][idx_sections] - sh.get(idx_circles, idx_sections)) > tolerance) {
					return false;
				}
			}
		}
		
		return true;
	}
	
}
