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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.braincopy.silbala.ARView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Hiroaki Tateshita
 */
public class CameraFragment extends Fragment implements LocationListener, SensorEventListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    /**
     * a kind of hashtable for saving setting information.
     */
    private SharedPreferences pref;

    private TextureView mTextureView;

    private CameraDevice mCameraDevice;

    //CameraCaptureSession is ...
    CameraCaptureSession mCaptureSession = null;
    //CaptureRequest is ...
    CaptureRequest mPreviewRequest = null;


    private ISSARView arView;
    //    private LocationManager locationManager;
    public float lat, lon, alt;
    //    private GeomagneticField geomagneticField;
    private boolean isUsingGPS = false;
    private View myView;
    private CaptureRequest.Builder mPreviewRequestBuilder;

    String TAG = "kibofinder";
    private String mCameraId;
    private CameraManager mCameraManager;
    private SensorManager mSensorManager;

    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];

    private List<Sensor> mListMag;
    private List<Sensor> mListAcc;
    private GeomagneticField geomagneticField;
    private SatelliteInfoWorker worker;
    private Satellite[] satellites;
    private LocationManager mLocationManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_camera,
                container, false);

        arView = rootView.findViewById(R.id.arview);

        return rootView;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            texture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());

            Surface surface = new Surface(texture);

            // CaptureRequestを生成
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // CameraCaptureSessionを生成
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session setting completed if ready , preview will be started
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // start camera preview and keep it
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //Session setting failed
                            Log.e(TAG, "error");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myView = view;

        mTextureView = view.findViewById(R.id.cam_textureView);

        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        String[] cameraIdList = new String[0];
        try {
            cameraIdList = mCameraManager.getCameraIdList();
            mCameraId = null;
            for (String cameraId : cameraIdList) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        mCameraId = cameraId;
                        break;
                    default:
                }
            }
            startCamera(view);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            worker = new SatelliteInfoWorker();
            worker.setLatLon(lat, lon);
            worker.setCurrentDate(new Date(System.currentTimeMillis()));
            // worker.setGnssString(gnssString);
            startInternetConnection();
        }

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CameraFragment.this)
                        .navigate(R.id.action_CameraFragment_to_MainFragment);
            }
        });
        /*

//todo implementation of camera shutter function:

        ImageButton shutterButton = (ImageButton) findViewById(R.id.cameraShutter);
        shutterButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callbackImple.takePicture();

            }
        });
*/

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mListMag = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        mListAcc = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lat = (float) 35.660994;
        lon = (float) 139.677619;
        alt = (float) 0.0f;


        pref = getActivity().getSharedPreferences("kibofinder", Activity.MODE_PRIVATE);
        isUsingGPS = pref.getBoolean("usingGPS", false);
        lat = pref.getFloat("defaultLat", 35.70000f);
        if(lat == 0.0f){
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("defaultLat", 35.7f);
            editor.commit();
            lat = 35.7f;
        }
        lon = pref.getFloat("defaultLon", 139.70000f);
        if(lon == 0.0f){
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("defaultLon", 139.7f);
            editor.commit();
            lon = 139.7f;
        }
        alt = pref.getFloat("defaultAlt", 0f);


    }

    private void startInternetConnection() {
        worker.start();
        worker.setStatus(SatelliteInfoWorker.CONNECTED);
        arView.setStatus("connected");
    }

    private void startCamera(View view) throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "camera permission not granted!?");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {
                Log.d(TAG, "should show explanation?");
                Snackbar.make(view, R.string.camera_access_required,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                }).show();
            } else {
                // No explanation needed; request the permission
                Log.d(TAG, "request permission");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            //return;
        } else {
            mCameraManager.openCamera(mCameraId, mStateCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        Log.d(TAG, "permission request result!?");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission request granted, so what'S next!?");
                    try {
                        startCamera(myView);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Camera Exception!! "+e);
                        e.printStackTrace();
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.d(TAG, "permission request not granted, so show message and move back to main");
                    Snackbar.make(myView, R.string.camera_access_denied,
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NavHostFragment.findNavController(CameraFragment.this)
                                    .navigate(R.id.action_CameraFragment_to_MainFragment);

                        }
                    }).show();                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "permission request granted, so what'S next!?");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startLocationUpdates();

                } else {
                    Log.d(TAG, "permission request not granted, so show message and move back to main");
                    Snackbar.make(myView, R.string.location_access_denied,
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NavHostFragment.findNavController(CameraFragment.this)
                                    .navigate(R.id.action_CameraFragment_to_MainFragment);

                        }
                    }).show();                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
        }

        if (magneticValues != null && accelerometerValues != null) {
            float[] R = new float[16];
            float[] outR = new float[16];

            float[] I = new float[16];

            SensorManager.getRotationMatrix(R, I, accelerometerValues,
                    magneticValues);
            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_Z, outR);
            float[] actual_orientation = new float[3];
            SensorManager.getOrientation(outR, actual_orientation);

            if (geomagneticField != null) {
                actual_orientation[0] = actual_orientation[0]
                        + geomagneticField.getDeclination();
            }

            actual_orientation[0] = (float) (actual_orientation[0] + Math.PI * 0.5);
            actual_orientation[2] = -1 * actual_orientation[2];

            arView.drawScreen(actual_orientation, lat, lon);
        }
        if (worker != null) {
            if (worker.getStatus() == SatelliteInfoWorker.INFORMATION_LOADED_WO_LOCATION) {
                this.satellites = worker.getSatArray();
                if (loadImages()) {
                    worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
                    arView.setSatellites(satellites);
                    arView.setStatus("getting location...");
                }
            } else if (worker.getStatus() == SatelliteInfoWorker.IMAGE_LOADED) {
                this.satellites = worker.getSatArray();
                if (loadImages()) {
                    worker.setStatus(SatelliteInfoWorker.COMPLETED);
                    arView.setSatellites(satellites);
                }
            }
        }

    }
    private boolean loadImages() {
        boolean result = false;
        Resources resources = this.getResources();
//        InputStream is;
        satellites[0].setImage(BitmapFactory.decodeResource(resources, R.drawable.iis));
        satellites[0].setDescription("International Space Station");
        result = true;
            /*
        try {
            AssetManager assetManager = resources.getAssets();
            is = assetManager.open("satelliteDataBase.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ArrayList<String[]> datalist = new ArrayList<String[]>();
            String buf = null;
            try {
                while ((buf = br.readLine()) != null) {
                    datalist.add(buf.split("\t"));
                }
                String gnssStr = "";
                for (int i = 0; i < satellites.length; i++) {
                    gnssStr = getGnssStr(satellites[i].getCatNo(), datalist);
                    if (gnssStr != null) {
                        satellites[i].setDescription(getSatInfo(
                                satellites[i].getCatNo(), datalist));
                        if (gnssStr.equals("qzss")) {
                            satellites[i]
                                    .setImage(BitmapFactory.decodeResource(
                                            resources, R.drawable.qzss));
                        } else if (gnssStr.equals("galileo")) {
                            satellites[i].setImage(BitmapFactory
                                    .decodeResource(resources,
                                            R.drawable.galileo));
                        } else if (gnssStr.equals("galileofoc")) {
                            satellites[i].setImage(BitmapFactory
                                    .decodeResource(resources,
                                            R.drawable.galileofoc));
                        } else if (gnssStr.equals("gpsBlockIIF")) {
                            satellites[i].setImage(BitmapFactory
                                    .decodeResource(resources, R.drawable.iif));
                        }
                    }
                }
                result = true;
            } catch (IOException e) {
                Log.e("hiro",
                        "failed when to read a line of the satellite database text file."
                                + e);
                e.printStackTrace();
            }
            is.close();
            br.close();
        } catch (FileNotFoundException e) {
            Log.e("hiro", "" + e);
            e.printStackTrace();
        } catch (IOException e1) {
            Log.e("hiro", "" + e1);
            e1.printStackTrace();
        }

  */

        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onResume() {
        super.onResume();
        mLocationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        if(isUsingGPS) {
            startLocationUpdates();
        }

        mSensorManager.registerListener(this, mListMag.get(0),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mListAcc.get(0),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "camera permission not granted!?");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "should show explanation?");
                Snackbar.make(myView, R.string.gpw_access_required,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }).show();
            } else {
                // No explanation needed; request the permission
                Log.d(TAG, "request permission");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        }else {

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // cameraView.stopPreviewAndFreeCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
        // cameraView.stopPreviewAndFreeCamera();

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float)location.getAltitude();

        geomagneticField = new GeomagneticField(lat, lon, alt,
                new Date().getTime());
        if (this.satellites != null && worker != null) {
            if (worker.getStatus() == SatelliteInfoWorker.IMAGE_LOADED) {
                worker = new SatelliteInfoWorker();
                worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
                worker.setLatLon(lat, lon);
                worker.setCurrentDate(new Date(System.currentTimeMillis()));
                worker.start();
            } else if (worker.getStatus() == SatelliteInfoWorker.COMPLETED) {
                this.arView
                        .setStatus("position is updated and information loaded.");
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
