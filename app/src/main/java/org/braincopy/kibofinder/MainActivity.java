package org.braincopy.kibofinder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            NavController navController = navHostFragment.getNavController();
            Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (fragment instanceof MainFragment) {
                navController.navigate(R.id.action_MainFragment_to_SettingFragment);
            } else if (fragment instanceof MapFragment) {
                navController.navigate(R.id.action_MapFragment_to_SettingFragment);
            } else if (fragment instanceof CameraFragment) {
                navController.navigate(R.id.action_CameraFragment_to_SettingFragment);
            } else {
                //no action
            }
            return true;
        } else if (id == R.id.action_close) {
            this.moveTaskToBack(true);
            return true;
        } else if (id == R.id.action_privacy) {
            DialogFragment newFragment = new PrivacyPolicyDialogFragment();
            newFragment.show(getSupportFragmentManager(), "test");
            return true;

        } else if (id == R.id.action_info) {
            DialogFragment newFragment = new InformationDialogFragment();
            newFragment.show(getSupportFragmentManager(), "test");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PrivacyPolicyDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dialog_info, null);
            InputStream is = null;
            BufferedReader br = null;
            String text = "";

            try {
                is = getActivity().getAssets().open("privacy.txt");
                br = new BufferedReader(new InputStreamReader(is));

                String str;
                while ((str = br.readLine()) != null) {
                    text += str + "\n";
                }
                if (is != null)
                    is.close();
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            TextView infoTextView = (TextView) content
                    .findViewById(R.id.infoTextView);
            infoTextView.setText(text);

            builder.setView(content);

            builder.setMessage("Privacy Policy").setNegativeButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

    }

    public static class InformationDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dialog_info, null);
            InputStream is = null;
            BufferedReader br = null;
            String text = "";

            try {
                is = getActivity().getAssets().open("Info.txt");
                br = new BufferedReader(new InputStreamReader(is));

                String str;
                while ((str = br.readLine()) != null) {
                    text += str + "\n";
                }
                if (is != null)
                    is.close();
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            TextView infoTextView = (TextView) content
                    .findViewById(R.id.infoTextView);
            infoTextView.setText(text);

            builder.setView(content);

            builder.setMessage("Information").setNegativeButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
