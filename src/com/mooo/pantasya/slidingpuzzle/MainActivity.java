package com.mooo.pantasya.slidingpuzzle;

import com.mooo.pantasya.slidingpuzzle.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.InputStream;

public class MainActivity extends Activity {

    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 8;
    private static final int REQ_PICK_IMAGE = 1001;

    private ImageView previewImage;
    private TextView sizeLabel;
    private SeekBar sizeSeekBar;
    private SlidingPuzzleView puzzleView;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewImage = findViewById(R.id.previewImage);
        sizeLabel = findViewById(R.id.sizeLabel);
        sizeSeekBar = findViewById(R.id.sizeSeekBar);
        puzzleView = findViewById(R.id.puzzleView);

        Button changeImageButton = findViewById(R.id.changeImageButton);
        Button shuffleButton = findViewById(R.id.shuffleButton);

        sizeSeekBar.setMax(MAX_SIZE - MIN_SIZE);
        sizeSeekBar.setProgress(1);
        updateSizeLabel();

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSizeLabel();
                if (currentBitmap != null) {
                    puzzleView.setGridSize(getGridSize());
                    puzzleView.setSourceBitmap(currentBitmap);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        changeImageButton.setOnClickListener(v -> pickImage());
        shuffleButton.setOnClickListener(v -> puzzleView.shuffle());

        Bitmap sample = BitmapFactory.decodeResource(getResources(), R.drawable.sample_image);
        currentBitmap = sample;
        previewImage.setImageBitmap(sample);
        puzzleView.setGridSize(getGridSize());
        puzzleView.setSourceBitmap(sample);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    if (in != null) in.close();
                    if (bmp != null) {
                        currentBitmap = bmp;
                        previewImage.setImageBitmap(bmp);
                        puzzleView.setGridSize(getGridSize());
                        puzzleView.setSourceBitmap(bmp);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private int getGridSize() {
        return MIN_SIZE + sizeSeekBar.getProgress();
    }

    private void updateSizeLabel() {
        sizeLabel.setText("Slice size: " + getGridSize() + " x " + getGridSize());
    }
}