package org.braincopy.silbala;

/**
 * This class expresses a plane in the real world coordinate system.
 * 
 * <ol>
 * <li>right-handed coordinate system</li>
 * <li>x axis is south direction,</li>
 * <li>y axis is down direction,</li>
 * <li>z axis is east direction,</li>
 * <li>angle increases for clockwise for all axis,</li>
 * <li>original point (0, 0, 0) means the position of android device user.</li>
 * </ol>
 * 
 * a normal vector should be (a, b, c)
 * 
 * @author Hiroaki Tateshita
 * @version 0.2.0
 * 
 */
public class Plane {
	// the equation of a plane: ax + by + cz = d
	float a, b, c, d;

	public Plane(float a_, float b_, float c_, float d_) {
		this.a = a_;
		this.b = b_;
		this.c = c_;
		this.d = d_;
	}

	public void setParam(float a_, float b_, float c_, float d_) {
		this.a = a_;
		this.b = b_;
		this.c = c_;
		this.d = d_;
	}

	/**
	 * This method returns intersection point of this plane and inputed line and
	 * return null when no intersection.
	 * 
	 * When The equation of the line is <br/>
	 * <i>x/la = y/lb = z/lc,</i> <br/>
	 * by using "t", we can see <br/>
	 * <i>x = la * t, y = lb * t, z = lc * t </i><br/>
	 * 
	 * When the equation of this plane is <br/>
	 * <i>pa * x + pb * y + pc * z = d</i> <br/>
	 * to get intersection point, x, y, z are assigned by using <i>t</i><br/>
	 * 
	 * Then we get <br/>
	 * <i> t * (pa * la + pb * lb + pc *lc) = d </i><br/>
	 * <i> t = d / (pa * la + pb * lb + pc *lc) </i><br/>
	 * 
	 * Then we can get the intersection point<br/>
	 * <i>( la * t, lb * t, lc * t) </i><br/>
	 * 
	 * @param line
	 *            this line should start from original point (0,0,0)
	 * @return intersection point of this plane and inputed line. return null
	 *         when no intersection.
	 */
	public Point getIntersection(Line line) {
		Point result = null;

		// if inner-product > 0, this plane and the line should cross.
		if (this.a * line.a + this.b * line.b + this.c * line.c > 0.001) {

			float t = 0;

			t = this.d / (this.a * line.a + this.b * line.b + this.c * line.c);
			result = new Point(line.a * t, line.b * t, line.c * t);
		}

		return result;
	}

	public String toString() {
		return "the equation of this plane: " + a + "x + " + b + "y + " + c
				+ "z = " + d;
	}
}
