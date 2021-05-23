package org.braincopy.kibofinder;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;

import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

/**
 * Setting page of this application has following functions:
 * <ul>
 * <li>User Location</li>
 * <li>Camera on off (developing)</li>
 * </ul>
 *
 * @author Hiroaki Tateshita
 * @version 0.7.3
 */
public class SettingFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    /**
     * a kind of hashtable for saving setting information.
     */
    private SharedPreferences pref;

    private final String TAG = "kibofinder";
    private EditText latitudeTextView;
    private Switch usingGPSSwitch;
    private EditText longitudeTextView;
    private EditText altitudeTextView;
	private View myView;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings,
                container, false);

        /*
         * initialize setting by loading setting information from shared
         * preference.
         */
        pref = getActivity().getSharedPreferences("kibofinder",
                Activity.MODE_PRIVATE);

        float defaultLat = pref.getFloat("defaultLat", 0);
        latitudeTextView = (EditText) rootView
                .findViewById(R.id.latEditText1);
        latitudeTextView.setText(String.valueOf(defaultLat));

        float defaultLon = pref.getFloat("defaultLon", 0);
        longitudeTextView = (EditText) rootView
                .findViewById(R.id.lonEditText01);
        longitudeTextView.setText(String.valueOf(defaultLon));

        float defaultAlt = pref.getFloat("defaultAlt", 0);
        altitudeTextView = (EditText) rootView
                .findViewById(R.id.altitudeEditText);
        altitudeTextView.setText(String.valueOf(defaultAlt));

        boolean usingGPS = pref.getBoolean("usingGPS", false);
        usingGPSSwitch = (Switch) rootView
                .findViewById(R.id.usingGPSSwitch01);
        usingGPSSwitch.setChecked(usingGPS);
		latitudeTextView.setFocusable(!usingGPS);
		longitudeTextView.setFocusable(!usingGPS);
		altitudeTextView.setFocusable(!usingGPS);

        /*
         * OK button
         */
        Button okButton = (Button) rootView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("usingGPS", usingGPSSwitch.isChecked());
                editor.putFloat("defaultLat",
                        Float.parseFloat(latitudeTextView.getText().toString()));
                editor.putFloat("defaultLon", Float
                        .parseFloat(longitudeTextView.getText().toString()));
                editor.putFloat("defaultAlt",
                        Float.parseFloat(altitudeTextView.getText().toString()));
                editor.commit();

                goBackToHome();
            }
        });

        /*
         * cancel button
         */
        Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goBackToHome();
            }
        });

        return rootView;
    }

    void goBackToHome() {
        NavHostFragment.findNavController(SettingFragment.this)
                .navigate(R.id.action_SettingFragment_to_MainFragment);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myView = view;
        usingGPSSwitch
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {//check permission to use GPS. if no,
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Log.d(TAG, "camera permission not granted!?");
                                // Permission is not granted
                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    Log.d(TAG, "should show explanation?");
                                    Snackbar.make(view, R.string.gpw_access_required,
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
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                }

                            } else {

                            }

                        }
                        latitudeTextView.setFocusable(!isChecked);
                        latitudeTextView.setFocusableInTouchMode(!isChecked);
                        longitudeTextView.setFocusable(!isChecked);
                        longitudeTextView.setFocusableInTouchMode(!isChecked);
                        altitudeTextView.setFocusable(!isChecked);
                        altitudeTextView.setFocusableInTouchMode(!isChecked);
                    }
                });
    }
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String[] permissions, int[] grantResults) {
		Log.d(TAG, "permission request result!?");
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "permission request granted, so what'S next!?");
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					setUsingGPS(true);

				} else {
					Log.d(TAG, "permission request not granted, so show message and move back to main");
					Snackbar.make(myView, R.string.location_access_denied,
							Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							setUsingGPS(false);
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

	private void setUsingGPS(boolean usingGPS) {
		usingGPSSwitch.setChecked(usingGPS);
		latitudeTextView.setFocusable(!usingGPS);
		latitudeTextView.setFocusableInTouchMode(!usingGPS);
		longitudeTextView.setFocusable(!usingGPS);
		longitudeTextView.setFocusableInTouchMode(!usingGPS);
		altitudeTextView.setFocusable(!usingGPS);
		altitudeTextView.setFocusableInTouchMode(!usingGPS);
	}

	/**
     * @param activity
     * @return
     */
    public static String getGNSSString(Activity activity) {
        /*
         * initialize setting by loading setting information from shared
         * preference.
         */
        SharedPreferences pref = activity.getSharedPreferences("gnssfinder",
                Activity.MODE_PRIVATE);
        boolean isGpsBlockIIF = pref.getBoolean("gpsBlockIIF", true);
        boolean isGalileo = pref.getBoolean("galileo", false);
        boolean isQzss = pref.getBoolean("qzss", false);
        String gnssString = "";
        if (isGpsBlockIIF) {
            gnssString += "G";
        }
        if (isGalileo) {
            gnssString += "E";
        }
        if (isQzss) {
            gnssString += "J";
        }
        return gnssString;
    }

}
