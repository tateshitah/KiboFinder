package org.braincopy.silbala;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Please extend this class to create your ARView and override onDraw() method.
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.6
 * 
 */
public class ARView extends View {
	/**
	 * <P>
	 * DISTANCE expresses the virtual distance between eye and screen plane.
	 * this value should be calculated by using view angle of the camera of
	 * android device. The unit is "pixel", because the unit of screen plane is
	 * pixel on the actual screen of the device. If think about horizontal
	 * direction,
	 * </P>
	 * 
	 * DISTANCE * Math.tan(0.5 * view angle) = 0.5 * width of canvas
	 * 
	 * <P>
	 * It should depends on each device. The easier method for this calibration
	 * should be prepared later.
	 * </P>
	 * 
	 */
	private static final float DISTANCE = 1350f;

	/**
	 * [deg]
	 */
	protected float lat, lon;

	protected Paint paint;

	/**
	 * canvas size
	 */
	int width = 0, height = 0;

	/**
	 * [degree]
	 */
	protected float direction;

	/**
	 * [degree]
	 */
	protected float pitch;

	/**
	 * [degree]
	 */
	protected float roll;

	private String statusString = "put some message related to status";

	/**
	 * the plane of display screen of smart-phone.
	 */
	private Plane screenPlane;

	/**
	 * temporary line object
	 */
	protected Line line;

	/**
	 * temporary Point object
	 */
	protected Point point;

	/**
	 * radius of the earth. [m]
	 */
	public static final double RADIUS_OF_EARTH = 6378137.0;

	/**
	 * array of AR Objects. When you want to use these objects, please create
	 * them by yourself. They will not be created automatically.
	 */
	protected ARObject[] arObjs;

	public ARView(Context context, AttributeSet attrs){
		super(context, attrs);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		paint.setStyle(Paint.Style.STROKE);
		screenPlane = new Plane(0, 0, 0, 0);
		setDrawingCacheEnabled(true);
	}

	public ARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		paint.setStyle(Paint.Style.STROKE);
		screenPlane = new Plane(0, 0, 0, 0);
		setDrawingCacheEnabled(true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawText("Direction: " + direction, 50, 50, paint);
		canvas.drawText("Pitch: " + pitch, 50, 100, paint);
		canvas.drawText("Roll: " + roll, 50, 150, paint);
		canvas.drawText("Lat: " + lat, 50, 200, paint);
		canvas.drawText("Lon: " + lon, 50, 250, paint);

		drawDirection(canvas, paint);
		drawStatus(canvas, paint);

	}

	public void drawScreen(float[] orientation, float lat_, float lon_) {
		direction = ((float) Math.toDegrees(orientation[0]) + 360) % 360;
		pitch = (float) Math.toDegrees(orientation[1]);
		roll = (float) Math.toDegrees(orientation[2]);
		this.lat = lat_;
		this.lon = lon_;

		screenPlane.setParam(
				(float) (Math.cos(orientation[1]) * Math.sin(orientation[0])),
				-(float) Math.sin(orientation[1]),
				(float) (Math.cos(orientation[1]) * Math.cos(orientation[0])),
				DISTANCE);

		invalidate();
	}

	/**
	 * This method provides a function to calculate the point on the screen from
	 * azimuth and elevation.
	 * 
	 * @param azimuth
	 *            of target [degree]
	 * @param elevation
	 *            of target [degree]
	 * @return point on the screen of android device. This point is on the
	 *         display screen coordinate. When there is no point on the screen,
	 *         return null.
	 */
	public Point convertAzElPoint(float azimuth, float elevation) {
		Point result = null;

		/*
		 * create Line object which starts from the original point (0 ,0 ,0) and
		 * whose directional vector is calculated by input azimuth and
		 * elevation.
		 */
		float ce = (float) Math.cos(elevation / 180 * Math.PI);
		float se = (float) Math.sin(elevation / 180 * Math.PI);
		float ca = (float) Math.cos(azimuth / 180 * Math.PI);
		float sa = (float) Math.sin(azimuth / 180 * Math.PI);
		line = new Line(ce * sa, -se, ce * ca);

		/*
		 * acquired point is on the real world coordinate.
		 */
		point = this.screenPlane.getIntersection(line);

		/*
		 * convert to display screen coordinate
		 */
		if (point != null) {
			// the order of rotations is important.
			point.rotateY(direction);
			point.rotateX(pitch);
			point.rotateZ(roll);
			result = new Point(0.5f * width + point.x, 0.5f * height + point.y,
					0);
		}
		return result;
	}

	/**
	 * This method provides a function to calculate the point on the screen from
	 * the target of latitude, longitude and altitude. In this method, comparing
	 * original position and the inputed information, azimuth and elevation will
	 * be calculated and the result will be inputed to convertAzElPoint().
	 * 
	 * @param lat_t
	 *            latitude of target [deg]
	 * @param lon_t
	 *            longitude of target [deg]
	 * @param alt_t
	 *            altitude of target [m]
	 * @return the point on the screen of android device.
	 */
	public Point convertLatLonPoint(float lat_t, float lon_t, float alt_t) {
		Point result = null;
		/*
		 * [rad]
		 */
		float deltaThetaLat, deltaThetaLon;

		deltaThetaLat = (float) Math.toRadians(lat_t - this.lat);
		deltaThetaLon = (float) Math.toRadians(lon_t - this.lon);

		/*
		 * difference between current position (lat, lon) and target position
		 * (lat_t, lon_t) should be less than 90 degree.
		 */
		if (Math.abs(deltaThetaLat) < 0.5 * Math.PI
				&& Math.abs(deltaThetaLon) < 0.5 * Math.PI) {
			/*
			 * [rad]
			 */
			float deltaTheta = (float) Math.sqrt(deltaThetaLat * deltaThetaLat
					+ deltaThetaLon * deltaThetaLon);

			/*
			 * target should be above horizon.
			 */
			if (RADIUS_OF_EARTH / Math.cos(deltaTheta) < RADIUS_OF_EARTH
					+ alt_t) {
				float az = (float) -Math.toDegrees(Math.atan2(
						Math.tan(deltaThetaLat), Math.tan(deltaThetaLon)));
				float el = (float) Math.toDegrees(Math.atan2(
						(RADIUS_OF_EARTH + alt_t) * Math.cos(deltaTheta)
								- RADIUS_OF_EARTH, (RADIUS_OF_EARTH + alt_t)
								* Math.sin(deltaTheta)));
				result = convertAzElPoint(az, el);
			}
		}

		return result;
	}

	/**
	 * This method provides a function to draw the azimuth and elevation line.
	 * 
	 * @param canvas
	 * @param numOfLines
	 *            number of lines between 0 - 90 degree of elevation.
	 */
	public void drawAzElLines(Canvas canvas, int numOfLines) {

		// create visible points array
		Point[] points = new Point[(numOfLines + 1) * (numOfLines + 1) * 4];
		Point top = convertAzElPoint(0, 90);
		for (int i = 0; i < numOfLines + 1; i++) {
			for (int j = 0; j < (numOfLines + 1) * 4; j++) {
				points[(numOfLines + 1) * 4 * i + j] = convertAzElPoint(j * 90
						/ (numOfLines + 1), i * 90 / (numOfLines + 1));
			}
		}

		// draw horizontal lines
		for (int i = 0; i < numOfLines + 1; i++) {
			for (int j = 1; j < (numOfLines + 1) * 4; j++) {
				if (points[(numOfLines + 1) * 4 * i + j - 1] != null
						&& points[(numOfLines + 1) * 4 * i + j] != null) {
					canvas.drawLine(points[(numOfLines + 1) * 4 * i + j - 1].x,
							points[(numOfLines + 1) * 4 * i + j - 1].y,
							points[(numOfLines + 1) * 4 * i + j].x,
							points[(numOfLines + 1) * 4 * i + j].y, paint);
				}
			}
			if (points[(numOfLines + 1) * 4 * i] != null
					&& points[(numOfLines + 1) * 4 * (i + 1) - 1] != null) {
				canvas.drawLine(points[(numOfLines + 1) * 4 * i].x,
						points[(numOfLines + 1) * 4 * i].y,
						points[(numOfLines + 1) * 4 * (i + 1) - 1].x,
						points[(numOfLines + 1) * 4 * (i + 1) - 1].y, paint);
			}
		}

		// draw vertical lines
		for (int j = 0; j < (numOfLines + 1) * 4; j++) {
			for (int i = 0; i < numOfLines; i++) {
				if (points[(numOfLines + 1) * 4 * i + j] != null
						&& points[(numOfLines + 1) * 4 * (i + 1) + j] != null) {
					canvas.drawLine(points[(numOfLines + 1) * 4 * i + j].x,
							points[(numOfLines + 1) * 4 * i + j].y,
							points[(numOfLines + 1) * 4 * (i + 1) + j].x,
							points[(numOfLines + 1) * 4 * (i + 1) + j].y, paint);
				}
			}
			if (points[(numOfLines + 1) * 4 * numOfLines + j] != null
					&& top != null) {
				canvas.drawLine(
						points[(numOfLines + 1) * 4 * numOfLines + j].x,
						points[(numOfLines + 1) * 4 * numOfLines + j].y, top.x,
						top.y, paint);
			}

		}
	}

	/**
	 * This method provides a function to draw a kind of virtual roof of
	 * latitude and longitude grid.
	 * 
	 * @param canvas
	 * @param paint
	 * @param delta
	 *            [deg]
	 * @param heightOfRoof
	 *            [m]
	 * @param numOfLines
	 */
	public void drawRoof(Canvas canvas, Paint paint, float delta,
			float heightOfRoof, int numOfLines) {
		// create points
		Point[][] points = new Point[2 * numOfLines + 1][2 * numOfLines + 1];
		for (int i = 0; i < 2 * numOfLines + 1; i++) {
			for (int j = 0; j < 2 * numOfLines + 1; j++) {
				points[i][j] = convertLatLonPoint(lat + delta
						* (i - numOfLines), lon + delta * (j - numOfLines),
						heightOfRoof);
			}
		}

		// draw points
		for (int i = 0; i < 2 * numOfLines + 1; i++) {
			for (int j = 0; j < 2 * numOfLines; j++) {
				if (points[i][j] != null && points[i][j + 1] != null) {
					canvas.drawLine(points[i][j].x, points[i][j].y,
							points[i][j + 1].x, points[i][j + 1].y, paint);
				}
			}
		}
		for (int i = 0; i < 2 * numOfLines; i++) {
			for (int j = 0; j < 2 * numOfLines + 1; j++) {
				if (points[i][j] != null && points[i + 1][j] != null) {
					canvas.drawLine(points[i][j].x, points[i][j].y,
							points[i + 1][j].x, points[i + 1][j].y, paint);
				}
			}
		}
	}

	private void drawStatus(Canvas canvas, Paint paint2) {
		canvas.drawText(this.statusString, 50, canvas.getHeight() - 50, paint);
	}

	public void drawTest(Canvas canvas, Paint paint, float az, float el) {
		Point point = convertAzElPoint(az, el);
		if (point != null) {
			canvas.drawText("(" + az + "," + el + ")", point.x, point.y, paint);
		}
	}

	/**
	 * This method is to draw EAST, WEST, NORTH, and SOUTH.
	 * 
	 * @param canvas
	 * @param paint
	 */
	public void drawDirection(Canvas canvas, Paint paint) {
		Point textPoint;
		Point pointForLine;

		// draw west
		textPoint = convertAzElPoint(180, 0);
		pointForLine = convertAzElPoint(180, 10);
		if (textPoint != null) {
			canvas.drawText("WEST", textPoint.x, textPoint.y, paint);
			if (pointForLine != null) {
				canvas.drawLine(textPoint.x, textPoint.y, pointForLine.x,
						pointForLine.y, paint);
			}
		}

		// draw south
		textPoint = convertAzElPoint(90, 0);
		pointForLine = convertAzElPoint(90, 10);
		if (textPoint != null) {
			canvas.drawText("SOUTH", textPoint.x, textPoint.y, paint);
			if (pointForLine != null) {
				canvas.drawLine(textPoint.x, textPoint.y, pointForLine.x,
						pointForLine.y, paint);
			}
		}

		// draw east
		textPoint = convertAzElPoint(0, 0);
		pointForLine = convertAzElPoint(0, 10);
		if (textPoint != null) {
			canvas.drawText("EAST", textPoint.x, textPoint.y, paint);
			if (pointForLine != null) {
				canvas.drawLine(textPoint.x, textPoint.y, pointForLine.x,
						pointForLine.y, paint);
			}
		}

		// draw north
		textPoint = convertAzElPoint(270, 0);
		pointForLine = convertAzElPoint(270, 10);
		if (textPoint != null) {
			canvas.drawText("NORTH", textPoint.x, textPoint.y, paint);
			if (pointForLine != null) {
				canvas.drawLine(textPoint.x, textPoint.y, pointForLine.x,
						pointForLine.y, paint);
			}
		}
		drawHorizon(canvas, paint);
	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 */
	public void drawHorizon(Canvas canvas, Paint paint) {
		Point[] points = new Point[12];
		for (int i = 0; i < points.length; i++) {
			points[i] = convertAzElPoint(30 * i, 0);
		}
		for (int i = 0; i < points.length - 1; i++) {
			if (points[i] != null && points[i + 1] != null) {
				canvas.drawLine(points[i].x, points[i].y, points[i + 1].x,
						points[i + 1].y, paint);
			}
		}
		if (points[points.length - 1] != null && points[0] != null) {
			canvas.drawLine(points[points.length - 1].x,
					points[points.length - 1].y, points[0].x, points[0].y,
					paint);
		}
	}

	public void setStatus(String string) {
		this.statusString = string;

	}

	public ARObject[] getArObjs() {
		return arObjs;
	}

	public void updateScreenPlane() {
		screenPlane.setParam((float) (Math.cos(Math.toRadians(pitch)) * Math
				.sin(Math.toRadians(direction))), -(float) Math.sin(Math
				.toRadians(pitch)),
				(float) (Math.cos(Math.toRadians(pitch)) * Math.cos(Math
						.toRadians(direction))), DISTANCE);
	}

}
