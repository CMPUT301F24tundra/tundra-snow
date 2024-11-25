package com.example.tundra_snow_app.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.tundra_snow_app.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class QrScanActivity extends AppCompatActivity {

    private PreviewView previewView;
    private Button scanButton;
    private ImageButton backButton;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_activity); // Set your layout

        previewView = findViewById(R.id.previewView);
        scanButton = findViewById(R.id.scanButton);
        backButton = findViewById(R.id.qrBackButton);

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Trigger QR code scan
                try {
                    startQrScan();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the current activity and return to the previous one
                finish();
            }
        });
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Initialize camera
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        // Set up camera selector (back camera by default)
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // Create the preview use case
        Preview preview = new Preview.Builder().build();

        // Bind the preview to the PreviewView
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Bind the camera to lifecycle
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }

    private void startQrScan() throws ExecutionException, InterruptedException {
        // Initialize the barcode scanner from ML Kit
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

        // Set up ImageAnalysis to process frames from the camera
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class) // may want to change implementation in the future to a more stable version
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();

                // Convert the camera frame to InputImage
                InputImage inputImage = InputImage.fromMediaImage(Objects.requireNonNull(image.getImage()), rotationDegrees);

                // Process the image to scan for barcodes
                barcodeScanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            // Process all the detected barcodes
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null) {
                                    // Hash the raw value of the QR code
                                    String qrHash = getSha256Hash(rawValue);
                                    Log.d("QR Code", "Hash: " + qrHash);
                                    /*
                                    Store hash in the database possibly? Or url/image. Further
                                    information on how system is implemented is needed to proceed
                                     */
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("QR Code", "QR code scanning failed", e);
                        })
                        .addOnCompleteListener(task -> {
                            image.close();
                        });
            }
        });

        // Bind the image analysis use case to the camera lifecycle

        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    /**
     * Method to generate SHA-256 hash of a string
     *
     * @param rawValue the raw value of the QR code
     * @return the SHA-256 hash of the raw value
     */
    private String getSha256Hash(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawValue.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();  // Return the SHA-256 hash as a hex string
        } catch (NoSuchAlgorithmException e) {
            Log.e("QR Hash", "Error hashing the QR value", e);
            return null;
        }
    }

}

