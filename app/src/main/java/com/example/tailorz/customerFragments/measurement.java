package com.example.tailorz.customerFragments;

import android.Manifest;                          // To request and check camera permission
import android.app.ProgressDialog;                 // To show a loading dialog while processing images
import android.content.Intent;                      // To launch camera intent for capturing photo
import android.content.pm.PackageManager;          // To check if camera permission is granted
import android.graphics.Bitmap;                     // To handle image bitmaps
import android.os.Bundle;                           // For fragment lifecycle and passing data
import android.provider.MediaStore;                 // To capture images from device camera
import android.text.Editable;                        // For text change listeners
import android.text.TextWatcher;                     // To watch and modify text in EditTexts
import android.util.Log;                             // To log debug/error messages
import android.view.LayoutInflater;                 // To inflate fragment's UI layout
import android.view.View;                            // Represents UI components
import android.view.ViewGroup;                       // Container for the fragment's UI
import android.widget.EditText;                      // For input fields for measurements
import android.widget.Toast;                         // To show short messages to user

import androidx.annotation.NonNull;                  // For compile-time null safety
import androidx.annotation.Nullable;                 // For compile-time null safety
import androidx.core.app.ActivityCompat;             // For requesting permissions
import androidx.core.content.ContextCompat;          // For checking permissions
import androidx.fragment.app.Fragment;                // Base class for fragment component

import com.example.tailorz.databinding.FragmentMeasurementBinding;  // View binding for XML layout
import com.example.tailorz.helpers.Prefs;                        // Custom helper to store local preferences
import com.google.firebase.database.FirebaseDatabase;           // Firebase Realtime Database instance
import com.google.mlkit.vision.common.InputImage;                // ML Kit input image wrapper
import com.google.mlkit.vision.pose.Pose;                        // ML Kit pose detection result
import com.google.mlkit.vision.pose.PoseDetection;               // ML Kit pose detection API
import com.google.mlkit.vision.pose.PoseDetector;                // ML Kit pose detector client
import com.google.mlkit.vision.pose.PoseLandmark;                // Specific body landmarks from pose detection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions; // Default options for pose detector

import org.opencv.android.OpenCVLoader;                         // To initialize OpenCV library
import org.opencv.core.CvType;                                   // For OpenCV image types
import org.opencv.core.Mat;                                      // OpenCV matrix class for image processing
import org.opencv.imgproc.Imgproc;                               // OpenCV image processing functions

import java.util.HashMap;                                        // For creating maps to save data
import java.util.Map;                                            // For defining Map interface


public class measurement extends Fragment {

    private FragmentMeasurementBinding binding;
    private Prefs prefs;
    private FirebaseDatabase database;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private ProgressDialog progressDialog;

    private boolean pose1Captured = false;
    private Bitmap pose1Image, pose2Image;

    public measurement() {
        // Required empty public constructor
    }

    public static measurement newInstance() {
        return new measurement();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout using view binding
        binding = FragmentMeasurementBinding.inflate(inflater, container, false);

        prefs = new Prefs(requireActivity());
        database = FirebaseDatabase.getInstance();
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed.");
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.");
        }
        addMeasurementTextWatcher(binding.etChestMeasurement);
        addMeasurementTextWatcher(binding.etWaistMeasurement);
        addMeasurementTextWatcher(binding.etLegsMeasurement);
        addMeasurementTextWatcher(binding.etArmMeasurement);
        addMeasurementTextWatcher(binding.etFullMeasurement);
        // Load Saved Measurements
        loadSavedMeasurements();

        // Set click listeners
        binding.btnOpenCamera.setOnClickListener(v -> checkCameraPermission());
        binding.btnSaveMeasurement.setOnClickListener(v -> saveMeasurement());

        return binding.getRoot();
    }

    private void loadSavedMeasurements() {
        binding.etChestMeasurement.setText(prefs.getUserChest());
        binding.etWaistMeasurement.setText(prefs.getUserWaist());
        binding.etLegsMeasurement.setText(prefs.getUserLegs());
        binding.etArmMeasurement.setText(prefs.getUserArms());
        binding.etFullMeasurement.setText(prefs.getUserFull());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == requireActivity().RESULT_OK) {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Processing Image...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            // ✅ Run Pose Detection on the captured image
            detectHumanPose(imageBitmap);
        }
    }
    private float getDistance(PoseLandmark p1, PoseLandmark p2) {
        if (p1 == null || p2 == null) return 0;
        return (float) Math.sqrt(
                Math.pow(p1.getPosition().x - p2.getPosition().x, 2) +
                        Math.pow(p1.getPosition().y - p2.getPosition().y, 2));
    }




    private static final float DEFAULT_USER_HEIGHT_INCHES = 64f; // 5ft 4in assumed user height

    private void processBothImages() {
        new Thread(() -> {
            try {
                InputImage image1 = InputImage.fromBitmap(pose1Image, 0);
                InputImage image2 = InputImage.fromBitmap(pose2Image, 0);

                PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();

                PoseDetector poseDetector = PoseDetection.getClient(options);

                poseDetector.process(image1)
                        .addOnSuccessListener(pose1 -> {
                            poseDetector.process(image2)
                                    .addOnSuccessListener(pose2 -> {

                                        // Height in pixels = Shoulder to Ankle
                                        float pixelHeight = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
                                        );

                                        if (pixelHeight == 0) pixelHeight = 500f; // fallback to avoid division error

                                        float scale = DEFAULT_USER_HEIGHT_INCHES / pixelHeight;

                                        float chestPixels = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                                                pose1.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                                        );

                                        float waistPixels = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_HIP),
                                                pose1.getPoseLandmark(PoseLandmark.RIGHT_HIP)
                                        );

                                        float armPixels = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                                        );

                                        float legsPixels = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_HIP),
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                                        );

                                        float fullPixels = getDistance(
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                                                pose1.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
                                        );

                                        float chestInches = chestPixels * scale;
                                        float waistInches = waistPixels * scale;
                                        float armInches = armPixels * scale;
                                        float legsInches = legsPixels * scale;
                                        float fullInches = fullPixels * scale;

                                        requireActivity().runOnUiThread(() -> {
                                            binding.etChestMeasurement.setText(String.format("%.1f inches", chestInches));
                                            binding.etWaistMeasurement.setText(String.format("%.1f inches", waistInches));
                                            binding.etLegsMeasurement.setText(String.format("%.1f inches", legsInches));
                                            binding.etArmMeasurement.setText(String.format("%.1f inches", armInches));
                                            binding.etFullMeasurement.setText(String.format("%.1f inches", fullInches));
                                            progressDialog.dismiss();
                                            Toast.makeText(requireContext(), "Measurements calculated!", Toast.LENGTH_SHORT).show();
                                        });

                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), "Failed to process pose 2", Toast.LENGTH_SHORT).show();
                                    });

                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "Failed to process pose 1", Toast.LENGTH_SHORT).show();
                        });

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e("PoseProcessing", "Exception: " + e.getMessage());
            }
        }).start();
    }




    private void saveMeasurement() {
        binding.btnSaveMeasurement.setEnabled(false);

        String chest = binding.etChestMeasurement.getText().toString().replace(" inches", "").trim();
        String waist = binding.etWaistMeasurement.getText().toString().replace(" inches", "").trim();
        String arms = binding.etArmMeasurement.getText().toString().replace(" inches", "").trim();
        String legs = binding.etLegsMeasurement.getText().toString().replace(" inches", "").trim();
        String full = binding.etFullMeasurement.getText().toString().replace(" inches", "").trim();

        // 🔹 Prevent saving if any measurement is empty
        if (chest.isEmpty() || waist.isEmpty() || arms.isEmpty() || legs.isEmpty() || full.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all measurement fields before saving.", Toast.LENGTH_SHORT).show();
            binding.btnSaveMeasurement.setEnabled(true);
            return;
        }

        // ✅ Save in SharedPreferences
        prefs.setUserChest(chest);
        prefs.setUserWaist(waist);
        prefs.setUserArms(arms);
        prefs.setUserLegs(legs);
        prefs.setUserFull(full);
        prefs.setMeasurementSaved(true);

        // ✅ Save in Firebase (against the encoded email)
        String userEmailKey = prefs.getUserEmail().replace(".", ",");
        Map<String, Object> measurements = new HashMap<>();
        measurements.put("chest_measurements", chest);
        measurements.put("waist_measurements", waist);
        measurements.put("arms_measurements", arms);
        measurements.put("legs_measurements", legs);
        measurements.put("full_measurements", full);

        database.getReference("Users").child(userEmailKey).updateChildren(measurements)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Measurements saved successfully", Toast.LENGTH_SHORT).show();
                    binding.btnSaveMeasurement.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save measurements", Toast.LENGTH_SHORT).show();
                    binding.btnSaveMeasurement.setEnabled(true);
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void addMeasurementTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().endsWith(" inches") && !s.toString().isEmpty()) {
                    editText.removeTextChangedListener(this);
                    String text = s.toString().replace(" inches", "");
                    editText.setText(text + " inches");
                    editText.setSelection(text.length());
                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    private void detectHumanPose(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Set options for pose detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();

        PoseDetector poseDetector = PoseDetection.getClient(options);

        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    boolean isHumanDetected = isHumanPose(pose);

                    if (!isHumanDetected) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "No human detected! Please retake the photo.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ First Image Capture
                    if (!pose1Captured) {
                        pose1Image = bitmap;
                        binding.pose1.setImageBitmap(bitmap);
                        pose1Captured = true;
                        progressDialog.dismiss();

                        Toast.makeText(requireContext(), "Pose 1 captured! Now take Pose 2.", Toast.LENGTH_SHORT).show();

                        // ✅ Open the camera again for the second pose
                        openCamera();
                    }
                    // ✅ Second Image Capture
                    else {
                        pose2Image = bitmap;
                        binding.pose2.setImageBitmap(bitmap);
                        progressDialog.dismiss();

                        // ✅ Process both images after capturing the second one
                        processBothImages();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Pose detection failed. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("MLKit", "Pose detection failed: " + e.getMessage());
                });
    }


    private boolean isHumanPose(Pose pose) {

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);

        return (leftShoulder != null && rightShoulder != null &&
                leftHip != null && rightHip != null &&
                leftKnee != null && rightKnee != null);
    }


}
