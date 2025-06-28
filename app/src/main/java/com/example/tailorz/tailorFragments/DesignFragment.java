package com.example.tailorz.tailorFragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tailorz.helpers.GenerateDesignID;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.databinding.FragmentDesignBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class DesignFragment extends Fragment {

    private FragmentDesignBinding binding;
    private ActivityResultLauncher<String> launcher;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri uri;
    private Prefs prefs;

    public DesignFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize View Binding
        binding = FragmentDesignBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        prefs = new Prefs(requireActivity().getApplicationContext());
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        String username = prefs.getUserName();

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    long fileSize = getFileSize(result);
                    if (fileSize > 2 * 1024 * 1024) { // Check if file size exceeds 2MB
                        Toast.makeText(getContext(), "Please select an image smaller than 2MB.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    binding.galleryImg.setImageURI(result);
                    uri = result;
                } else {
                    Toast.makeText(getContext(), "No Image Selected!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.addDesignBtn.setOnClickListener(v -> launcher.launch("image/*"));

        binding.uploadImageBtn.setOnClickListener(v -> {
            binding.uploadImageBtn.startAnimation();
            String designName_txt = binding.designNametxt.getText().toString().trim();

            if (designName_txt.isEmpty() || uri == null) {
                Toast.makeText(getContext(), "Please enter a design name and select an image.", Toast.LENGTH_SHORT).show();
                binding.uploadImageBtn.revertAnimation();
                return;
            }

            String designId = new GenerateDesignID().generateRandomID(10);
            StorageReference storageReference = storage.getReference()
                    .child("Design")
                    .child(username)
                    .child(designName_txt);

            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                saveDesignDataToDatabase(username, designName_txt, designId, downloadUri.toString());
                            })
                            .addOnFailureListener(e -> handleError("Failed to get download URL", e)))
                    .addOnFailureListener(e -> handleError("Failed to upload file", e));
        });

        return view;
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
        long fileSize = 0;
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            fileSize = cursor.getLong(sizeIndex);
            cursor.close();
        }
        return fileSize;
    }

    private void saveDesignDataToDatabase(String username, String designName, String designId, String downloadUrl) {
        Map<String, Object> designData = new HashMap<>();
        designData.put("Design_name", designName);
        designData.put("Design_url", downloadUrl);
        designData.put("Design_id", designId);
        designData.put("tailor_username", prefs.getUserName());

        database.getReference().child("Design")
                .child(username)
                .child(designName)
                .setValue(designData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Design uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Drawable drawable = getResources().getDrawable(R.drawable.checkbutton);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    binding.uploadImageBtn.doneLoadingAnimation(R.color.white, bitmap);
                })
                .addOnFailureListener(e -> handleError("Failed to save design data", e));
    }

    private void handleError(String message, Exception e) {
        Log.e("ERROR", message, e);
        Toast.makeText(getContext(), message + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        Drawable drawable = getResources().getDrawable(R.drawable.cancelbutton);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        binding.uploadImageBtn.doneLoadingAnimation(R.color.white, bitmap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
