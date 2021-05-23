/*
Copyright (c) 2021 Hiroaki Tateshita
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package org.braincopy.kibofinder;

import org.braincopy.silbala.ARView;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.7.2
 * 
 */
public class ISSARView extends ARView {

	private Satellite[] satellites;

	public ISSARView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}
	public ISSARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}

	public ISSARView(Fragment fragment) {
		super(fragment.getActivity());
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawAzElLines(canvas, paint, 8);
		drawSatellites(canvas, paint);
	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawSatellites(Canvas canvas, Paint paint) {
		float dx = 0;
		float dy = 0;
		Matrix matrix = new Matrix();
		float scale = 0.2f;
		matrix.postScale(scale, scale);
		// if (this.satellites != null) {
		if (this.arObjs != null) {
			// for (int i = 0; i < satellites.length; i++) {
			for (int i = 0; i < arObjs.length; i++) {
				// if (satellites[i] != null) {
				if (arObjs[i] != null) {
					dx = arObjs[i].getImage().getWidth() / 2 * scale;
					dy = arObjs[i].getImage().getHeight() / 2 * scale;
					// point = convertAzElPoint(satellites[i].getAzimuth(),
					// satellites[i].getElevation());
					point = convertAzElPoint(
							((Satellite) arObjs[i]).getAzimuth(),
							((Satellite) arObjs[i]).getElevation());
					((Satellite) arObjs[i]).setPoint(point);
					if (((Satellite) arObjs[i]).getImage() != null
							&& point != null) {
						matrix.postTranslate(point.x - dx, point.y - dy);
						canvas.drawBitmap(((Satellite) arObjs[i]).getImage(),
								matrix, paint);
						canvas.drawText(
								((Satellite) arObjs[i]).getDescription(),
								point.x + 30, point.y, paint);
						matrix.postTranslate(-point.x + dx, -point.y + dy);
					}
				}
			}
		}
	}

	public Satellite[] getSatellites() {
		return (Satellite[]) arObjs;
	}

	public void setSatellites(Satellite[] satellites) {
		// this.satellites = satellites;
		this.arObjs = satellites;
	}

}
