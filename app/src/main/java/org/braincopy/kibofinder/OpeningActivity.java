package org.braincopy.kibofinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class OpeningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        //View openingImageView = (View)findViewById(R.id.opening_imageView);
        VideoView openingVideoView = (VideoView)findViewById(R.id.openingVideoView);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.opening);
        openingVideoView.setVideoURI(uri);
        openingVideoView.start();

        openingVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}