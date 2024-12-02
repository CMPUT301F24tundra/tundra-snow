package com.example.tundra_snow_app.Activities;

import android.content.Intent;
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

import com.example.tundra_snow_app.EventActivities.MyEventDetailActivity;
import com.example.tundra_snow_app.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


/**
 * QrScanActivity handles scanning QR codes to allow users to sign up for events
 * based on the scanned QR data. The activity integrates with Firebase Firestore
 * to validate QR codes against event data and to manage user sign-ups.
 *
 * Features:
 * - Camera-based QR code scanning using Android CameraX and ML Kit.
 * - Firebase integration for validating scanned QR codes.
 * - Automatic user sign-up for events linked to QR codes.
 * - Navigation to event details upon successful sign-up.
 *
 * This class extends {@link AppCompatActivity}.
 */
@OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
public class QrScanActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean isProcessing = false;

    private PreviewView previewView;
    private Button scanButton;
    private ImageButton backButton;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String currentUserID;
    private boolean isScanning = false;
    private FirebaseFirestore db;


    /**
     * Initializes the activity, UI components, and camera permissions.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_activity); // Set your layout

        previewView = findViewById(R.id.previewView);
        scanButton = findViewById(R.id.scanButton);
        backButton = findViewById(R.id.qrBackButton);

        db = FirebaseFirestore.getInstance();

        // Check for camera permission
        fetchSessionUser(() -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        });


        scanButton.setOnClickListener(view -> {
            try {
                startQrScan();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("QrScanActivity", "Error starting QR scan", e);
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


    /**
     * Starts the camera and binds it to the lifecycle of the activity.
     */
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Binds the camera preview to the UI and sets up the CameraX pipeline.
     *
     * @param cameraProvider The {@link ProcessCameraProvider} instance.
     */
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        // Set up camera selector (back camera by default)
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    /**
     * Handles the scanned QR code data by looking up the corresponding event in Firestore
     * and initiating the user sign-up process.
     *
     * @param qrData The raw data extracted from the scanned QR code.
     */
    public void handleScannedQRCode(String qrData) {
        Log.d("QrScanActivity", "QR Code scanned: " + qrData);

        // Look for the event with the corresponding QR hash in Firestore
        db.collection("events")
                .whereEqualTo("qrHash", qrData)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String eventId = document.getString("eventID");

                        if (eventId != null) {
                            Log.d("QrScanActivity", "Found matching event with eventID: " + eventId);
                            signUpForEvent(eventId);
                        } else {
                            Log.e("QrScanActivity", "eventID not found in the document for the QR code.");
                            Toast.makeText(this, "Invalid QR code. EventID is missing.", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                        }
                    } else {
                        Log.w("QrScanActivity", "No event found for this QR code.");
                        Toast.makeText(this, "No event found for this QR code.", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QrScanActivity", "Error looking up event in Firestore", e);
                    Toast.makeText(this, "Failed to process QR code.", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });
    }

    /**
     * Signs up the current user for the specified event and updates Firestore.
     *
     * @param eventId The ID of the event to sign up for.
     */
    private void signUpForEvent(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");
                        List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                        List<String> declinedList = (List<String>) documentSnapshot.get("declinedList");

                        if (entrantList != null && entrantList.contains(currentUserID)) {
                            // User is already signed up
                            Log.d("QrScanActivity", "User " + currentUserID + " is already signed up for event " + eventId);
                            Toast.makeText(this, "You are already signed up for this event.", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            navigateToEventDetails(eventId);
                            return;
                        }

                        if (chosenList != null && chosenList.contains(currentUserID)) {
                            Log.d("QrScanActivity", "User " + currentUserID + " has already been chosen for event " + eventId);
                            Toast.makeText(this, "You have already been chosen for this event.", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            navigateToEventDetails(eventId);
                            return;
                        }

                        if (declinedList != null && declinedList.contains(currentUserID)) {
                            Log.d("QrScanActivity", "User " + currentUserID + " has been rejected from event " + eventId);
                            Toast.makeText(this, "You have already been rejected from this event.", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            navigateToEventDetails(eventId);
                            return;
                        }

                        // Proceed with signing up the user
                        db.collection("events").document(eventId)
                                .update("entrantList", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("QrScanActivity", "Successfully added " + currentUserID + " to entrantList.");
                                    Toast.makeText(this, "Signed up for the event successfully!", Toast.LENGTH_SHORT).show();
                                    navigateToEventDetails(eventId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("QrScanActivity", "Error signing up for event", e);
                                    Toast.makeText(this, "Failed to sign up for the event.", Toast.LENGTH_SHORT).show();
                                    isProcessing = false;
                                });
                    } else {
                        Log.w("QrScanActivity", "Event " + eventId + " not found.");
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QrScanActivity", "Error fetching event details for sign-up check", e);
                    Toast.makeText(this, "Failed to check sign-up status.", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });
    }

    /**
     * Navigates to the event details page for the specified event.
     *
     * @param eventId The ID of the event to view.
     */
    private void navigateToEventDetails(String eventId) {
        Intent intent = new Intent(this, MyEventDetailActivity.class);
        intent.putExtra("eventID", eventId);
        startActivity(intent);
        finish();
    }

    /**
     * Fetch the userId of the current user from the latest session in the "sessions" collection.
     * @param onComplete Runnable to execute after fetching the userId
     */
    private void fetchSessionUser(@NonNull Runnable onComplete) {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }

    /**
     * Handles the result of the camera permission request.
     *
     * @param requestCode  The request code for the permission.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Starts the QR scanning process using CameraX and ML Kit Barcode Scanner.
     *
     * @throws ExecutionException   If the camera provider cannot be retrieved.
     * @throws InterruptedException If the thread is interrupted while retrieving the camera provider.
     */
    private void startQrScan() throws ExecutionException, InterruptedException {
        if (isProcessing) {
            return; // Exit if a QR code is already being processed
        }
        isProcessing = true;

        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (image.getImage() == null) {
                    image.close();
                    return;
                }

                InputImage inputImage = InputImage.fromMediaImage(
                        Objects.requireNonNull(image.getImage()),
                        image.getImageInfo().getRotationDegrees()
                );

                barcodeScanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null) {
                                    handleScannedQRCode(rawValue);
                                    stopScanning();
                                    return;
                                }
                            }
                            image.close();
                            isScanning = false;
                        })
                        .addOnFailureListener(e -> {
                            Log.e("QrScanActivity", "Error processing QR code", e);
                            isProcessing = false;
                        })
                        .addOnCompleteListener(task -> {
                            image.close();
                        });
            }
        });

        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    /**
     * Stops the QR scanning process by unbinding all camera use cases.
     */
    private void stopScanning() {
        // Unbind the camera pipeline to stop further QR code scanning
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            cameraProvider.unbindAll(); // Stops camera use cases, including image analysis
        } catch (ExecutionException | InterruptedException e) {
            Log.e("QrScanActivity", "Error stopping camera pipeline", e);
        }
        isProcessing = false; // Reset processing state for future scans
    }

    public void simulateScan(String qrHash) {
        // Simulate passing the scanned data to the handler
        handleScannedQRCode(qrHash);
    }
}

