package org.braincopy.silbala;

/**
 * Express point (x, y, z) based on real world Coordinates system
 * 
 * <ol>
 * <li>right-handed coordinate system</li>
 * <li>x axis is south direction,</li>
 * <li>y axis is down direction,</li>
 * <li>z axis is east direction,</li>
 * <li>angle increases for clockwise for all axis.</li>
 * <li>original point (0, 0, 0) means the position of android device user.</li>
 * </ol>
 * 
 * @author Hiroaki Tateshita
 * @version 0.2.0
 * 
 */
public class Point {
	public float x, y, z;

	public Point() {
	}

	public Point(float x_, float y_, float z_) {
		this.x = x_;
		this.y = y_;
		this.z = z_;
	}

	/**
	 * 
	 * @param pitch
	 *            [degree]
	 */
	public void rotateX(float pitch) {
		double dy = 0, dz = 0;
		double rad = pitch / 180 * Math.PI;
		dy = this.y * Math.cos(rad) + this.z * Math.sin(rad);
		dz = -this.y * Math.sin(rad) + this.z * Math.cos(rad);
		this.y = (float) dy;
		this.z = (float) dz;
	}

	/**
	 * 
	 * @param dir
	 *            [degree]
	 */
	public void rotateY(float dir) {
		double dz = 0, dx = 0;
		double rad = dir / 180 * Math.PI;
		dz = this.z * Math.cos(rad) + this.x * Math.sin(rad);
		dx = -this.z * Math.sin(rad) + this.x * Math.cos(rad);
		this.z = (float) dz;
		this.x = (float) dx;
	}

	/**
	 * 
	 * @param roll
	 *            [degree]
	 */
	public void rotateZ(float roll) {
		double dx = 0, dy = 0;
		double rad = roll / 180 * Math.PI;
		dx = this.x * Math.cos(rad) + this.y * Math.sin(rad);
		dy = -this.x * Math.sin(rad) + this.y * Math.cos(rad);
		this.x = (float) dx;
		this.y = (float) dy;
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
