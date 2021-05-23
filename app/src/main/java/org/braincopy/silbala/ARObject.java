package org.braincopy.silbala;

import android.graphics.Bitmap;

/**
 * class for AR Object, which has information showed on the display of android
 * device.
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.8
 * 
 */
public class ARObject {
	private Bitmap image;

	private Point point;

	/**
	 * for touch event
	 */
	private boolean touched;

	private String objName;

	/**
	 * 
	 * @return center point
	 */
	public Point getPoint() {
		return point;
	}

	/**
	 * need to set center point of the AR Object.
	 * 
	 * @param point
	 */
	public void setPoint(Point point) {
		this.point = point;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public boolean isTouched() {
		return touched;
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String name) {
		this.objName = name;
	}
}
