package com.yunwltn98.youtubeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yunwltn98.youtubeapp.model.Video;

public class VideoImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_image);

        imageView = findViewById(R.id.imageView);

        String highUrl = getIntent().getStringExtra("highUrl");

        Glide.with(VideoImageActivity.this).load(highUrl).into(imageView);

    }
}