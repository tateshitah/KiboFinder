package org.braincopy.kibofinder;

import org.braincopy.silbala.ARObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class deals with image, azimuth, and elevation to display on android
 * application.
 * 
 * @author Hiroaki Tateshita
 * @version 0.7.1
 * 
 */
public class Satellite extends ARObject {

	/**
	 * 0-360 [degree] east 0, south 90, west 180, north 270
	 */
	private float azimuth;

	/**
	 * 0 - 90 [degree]
	 */
	private float elevation;

	private String catNo;
	private String description;

	/**
	 * such as "gzss", "galileo", "galileofoc", "gpsBlockIIF"
	 */
	private String gnssStr;

	Satellite(Context context) {
		/*
		 * default
		 */
		Resources res = context.getResources();
//		setImage(BitmapFactory.decodeResource(res, R.drawable.qzss));
		setAzimuth(270);
		setElevation(60);
	}

	Satellite() {

	}

	public float getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

	public float getElevation() {
		return elevation;
	}

	public void setElevation(float elevation) {
		this.elevation = elevation;
	}

	public static Satellite[] createSatellite(Context context) {
		Satellite[] result = new Satellite[1];

		result[0] = new Satellite(context);
		return result;
	}

	public String getCatNo() {
		return catNo;
	}

	public void setCatNo(String catNo) {
		this.catNo = catNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return this.catNo;
	}

	public String getGnssStr() {
		return gnssStr;
	}

	public void setGnssStr(String gnssStr) {
		this.gnssStr = gnssStr;
	}
}


