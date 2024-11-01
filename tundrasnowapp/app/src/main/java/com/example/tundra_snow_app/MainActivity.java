package com.example.tundra_snow_app;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

//private FirebaseFirestore db = FirebaseFirestore.getInstance();
//private FirebaseStorage storage = FirebaseStorage.getInstance();
//private DocumentReference usersRef;

public class MainActivity extends AppCompatActivity {
    Button entrantButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        entrantButton = findViewById(R.id.entrantButton);

//        usersRef = db.collection("users");



        entrantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EntrantSignupActivity.class);
                startActivity(intent);

            }
        });
    }

//    public void registerUser(View view) {
//        String username = usernameEditText.getText().toString();
//        Integer userID = userIDText.getText().toInteger();
//        String email = emailEditText.getText().toString();
//        String address = addressEditText.getText().toString();
//        // other user information (add as we go along)
//
//        // Handling profile picture upload
//        if (profilePictureUri != null) {
//            // Upload picture to firebase storage
//            StorageReference profilePictureRef = storage.getReference("profilePictures/" + userID);
//            UploadTask uploadTask = profilePictureRef.putFile(profilePictureUri);
//
//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    // Get the download URL for the profile picture
//                    Task<Uri> downloadUrlTask = taskSnapshot.getStorage().getDownloadUrl();
//                    downloadUrlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Store the download URL in the user document
//                            createUserDocument(username, userID, address, uri.toString());
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // Handle errors during download URL retrieval
//                            Log.e("UserRegistration", "Error getting download URL: " + e.getMessage());
//                        }
//                    });
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // Handle errors during profile picture upload
//                    Log.e("UserRegistration", "Error uploading profile picture: " + e.getMessage());
//                }
//            });
//        } else {
//            // If no profile picture is provided, create the user document without it
//            createUserDocument(username, userID, email, address, null);
//            }
//        }
//
//    private void createUserDocument(String username, Integer userID, String email, String address, String profilePictureUrl) {
//        // Create a map to store user data
//        Map<String, Object> user = new HashMap<>();
//        user.put("username", username);
//        user.put("userID", userID);
//        user.put("email", email);
//        user.put("address", address);
//        if (profilePictureUrl != null) {
//            user.put("profilePicture", profilePictureUrl);
//        }
//
//        // Add the user document to the "users" collection
//        usersRef = db.collection("users").document();
//        usersRef.set(user)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("UserRegistration", "DocumentSnapshot added with ID: " + usersRef.getId());
//                        // Handle successful registration (e.g., show a success message)
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("UserRegistration", "Error adding document", e);
//                        // Handle errors during document creation (e.g., show an error message)
//                    }
//                });
//    }
//    }
}