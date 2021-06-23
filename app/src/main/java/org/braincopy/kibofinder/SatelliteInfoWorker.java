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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/*
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
*/

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * This class will work for you to get any information related satellite from
 * Internet. In the run() method, this thread object will try to get azimuth and elevation information from a web service.
 *
 * @author Hiroaki Tateshita
 * @version 0.7.2
 */
public class SatelliteInfoWorker extends Thread {
    static final int INITIAL_STATUS = 0;
    //    static final int CONNECTING = 1;
    static final int CONNECTED = 2;
    static final int COMPLETED = 3;
    static final int IMAGE_LOADED = 4;
    // static final int LOADING_IMAGES = 5;
    static final int INFORMATION_LOADED_WO_LOCATION = 6;
    static final int INFORMATION_LOADED = 9;
    // static final int LOCATION_UPDATED = 7;
    // static final int INFORMATION_LOADED_W_LOCATION = 8;
    static final int LOCALIZED = 10;
    static final String TAG = "kibofinder";
    float lat, lon;
    // private MessageListener messageListener;
    private Date currentDate;
    private Satellite[] satArray;
    private int status = SatelliteInfoWorker.INITIAL_STATUS;
//    private String gnssString;

    public SatelliteInfoWorker() {
    }

    public void createSatelliteArray(Document doc) {
        NodeList satObsList = doc.getElementsByTagName("SatObservation");
        NodeList satObs = null;
        NodeList obType1Nodelist = null;
        Satellite[] result = new Satellite[satObsList.getLength()];
        for (int i = 0; i < satObsList.getLength(); i++) {
            satObs = satObsList.item(i).getChildNodes();
            result[i] = new Satellite();
            result[i].setTouched(false);

            for (int k = 0; k < satObs.getLength(); k++) {
                if (satObs.item(k).getNodeName().equals("ObType")) {
                    obType1Nodelist = satObs.item(k).getFirstChild()
                            .getChildNodes();
                    for (int j = 0; j < obType1Nodelist.getLength(); j++) {
                        if (obType1Nodelist.item(j).getNodeName()
                                .equals("Azimuth")) {
                            result[i].setAzimuth((float) (((Float
                                    .parseFloat(obType1Nodelist.item(j)
                                            .getFirstChild().getNodeValue()))
                                    * -1 + 360.0) % 360.0));
                        } else if (obType1Nodelist.item(j).getNodeName()
                                .equals("Elevation")) {
                            result[i].setElevation(Float
                                    .parseFloat(obType1Nodelist.item(j)
                                            .getFirstChild().getNodeValue()));
                        }
                    }
                } else if (satObs.item(k).getNodeName()
                        .equals("SatelliteNumber")) {
                    result[i].setCatNo(satObs.item(k).getFirstChild()
                            .getNodeValue());
                } else if (satObs.item(k).getNodeName().equals("ObTime")){
                    result[i].setDescription(satObs.item(k).getFirstChild()
                            .getNodeValue().substring(0,8));//by substring, time string should be "hh:mm:ss"
                }
            }
        }
        satArray = result;
    }

    @Override
    public void run() {
        final int TIMEOUT_MILLIS = 0;// unit is milli seconds, 0 means infinite.

        final StringBuffer sb = new StringBuffer("");

        HttpURLConnection httpConn = null;
        Document doc = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.encodedAuthority("braincopy.org");
        //builder.scheme("http");
        //builder.encodedAuthority("192.168.11.10:8080");
        builder.path("/tlews/app/az_and_el");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
                Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        builder.appendQueryParameter("dateTime", sdf.format(currentDate));
        builder.appendQueryParameter("lat", Float.toString(lat));
        builder.appendQueryParameter("lon", Float.toString(lon));
        builder.appendQueryParameter("norad_cat_id", "25544");
        builder.appendQueryParameter("term", "5400");
        builder.appendQueryParameter("step", "60");

        try {
            URL url = new URL(builder.build().toString());
            httpConn = (HttpURLConnection) url.openConnection();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = httpConn.getInputStream();
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                doc = docBuilder.parse(is);
                createSatelliteArray(doc);
                setStatus(SatelliteInfoWorker.INFORMATION_LOADED);

            } else {
                Log.e(TAG, "http connection error!!: " + httpConn.getResponseCode());
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "URL format is not correct: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "failed to open connection: " + e);
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "parse config?: " + e);
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e(TAG, "strange xml?: " + e);
            e.printStackTrace();
        } finally {
            httpConn.disconnect();
        }
    }

    public void setLatLon(float _lat, float _lon) {
        this.lat = _lat;
        this.lon = _lon;
    }

    public void setCurrentDate(Date _currentDate) {
        this.currentDate = _currentDate;
    }

    public Satellite[] getSatArray() {
        return this.satArray;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int _status) {
        this.status = _status;
    }

    //   public void setGnssString(String gnssString_) {
    //      this.gnssString = gnssString_;
    //  }
}
