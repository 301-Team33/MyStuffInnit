package com.example.dwelventory;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private Button captureBtn;
    private Button retakeBtn;
    private Button confirmBtn;
    private CheckBox displayMainCheck;
    private Camera camera;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        Intent intent = getIntent();

        previewView = findViewById(R.id.preview_view);
        captureBtn = findViewById(R.id.capture_button);
        retakeBtn = findViewById(R.id.retake_button);
        confirmBtn = findViewById(R.id.confirm_button);
        displayMainCheck = findViewById(R.id.checkBox);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPrevew(cameraProvider);
            } catch (ExecutionException | InterruptedException e){
                Log.d("CAMERA", "Unable to open camera");
            }
        }, ContextCompat.getMainExecutor(this));

        ContentValues contentVals = new ContentValues();
        contentVals.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {
                capturedState();
                // show captured image
                // take picture
                imageCapture.takePicture(Executors.newSingleThreadExecutor(),
                        new ImageCapture.OnImageCapturedCallback() {
                            @Override
                            public void onCaptureSuccess(@NonNull ImageProxy image){
                                Log.d("Camera", "Photo captured");
                                confirmBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        image.close();
                                        // somehow save image
                                        // send it back to fragment to display in list
                                        finish();
                                    }
                                });
                                retakeBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        image.close();
                                        uncapturedState();
                                    }
                                });
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Toast.makeText(CameraActivity.this, "Error taking picture", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void bindPrevew(@NonNull ProcessCameraProvider cameraProvider){
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder().build();

        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);
            // bindToLifeCycle returns Camera object
    }

    /***
     * show capture button
     */
    private void uncapturedState(){
        confirmBtn.setVisibility(View.GONE);
        displayMainCheck.setVisibility(View.GONE);
        retakeBtn.setVisibility(View.GONE);
        captureBtn.setVisibility(View.VISIBLE);
    }

    /***
     * show confirm button
     * show option to display in list checkbox (default: unselected)
     */
    private void capturedState(){
        confirmBtn.setVisibility(View.VISIBLE);
        displayMainCheck.setVisibility(View.VISIBLE);
        retakeBtn.setVisibility(View.VISIBLE);
        captureBtn.setVisibility(View.GONE);
    }
}
