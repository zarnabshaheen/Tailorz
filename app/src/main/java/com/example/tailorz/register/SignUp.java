package com.example.tailorz.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.apachat.loadingbutton.core.customViews.CircularProgressButton;
import com.example.tailorz.R;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    private EditText email_txt, mobileNo_txt, userName_txt, password_txt, confirmPassword_txt, address_txt;
    RadioGroup genderRadioGroup, categoryRadioGroup, tailorCategoryRadioGroup;
    RadioButton genderRadioBtn, categoryRadioBtn, tailorCategory;

    private static final String DATABASE_URL = "https://tailorz-aa1b7-default-rtdb.firebaseio.com/";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(DATABASE_URL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email_txt = findViewById(R.id.emailtxt);
        mobileNo_txt = findViewById(R.id.mobileNotxt);
        userName_txt = findViewById(R.id.usernametxt);
        password_txt = findViewById(R.id.passwordtxt);
        confirmPassword_txt = findViewById(R.id.confirmPasswordtxt);
        address_txt = findViewById(R.id.tv_address);
        genderRadioGroup = findViewById(R.id.genderRadioGrpId);
        categoryRadioGroup = findViewById(R.id.categoryRadioGrp);
        tailorCategoryRadioGroup = findViewById(R.id.TailorcategoryRadioGrp);

        TextView clickHere_txt = findViewById(R.id.clickheretxt);
        CircularProgressButton signUp_Btn = findViewById(R.id.signUpBtn);

        clickHere_txt.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(SignUp.this, Login.class));
        });
        signUp_Btn.setOnClickListener(v -> {
            signUp_Btn.startAnimation();

            new Handler().postDelayed(() -> {
                final String email = email_txt.getText().toString().trim();
                final String encodedEmail = encodeEmail(email);
                final String phoneNumber = mobileNo_txt.getText().toString().trim();
                final String password = password_txt.getText().toString().trim();
                final String conPass = confirmPassword_txt.getText().toString().trim();
                final String userName = userName_txt.getText().toString().trim();
                final String address = address_txt.getText().toString().trim();

                // VALIDATION CHECK
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
                    email_txt.setError("Enter a valid Gmail address (@gmail.com)");
                    email_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (phoneNumber.isEmpty() || phoneNumber.length() != 11) {
                    mobileNo_txt.setError("Phone number must be 11 digits");
                    mobileNo_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Phone number must be 11 digits", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (userName.isEmpty() || !userName.matches("^[a-zA-Z0-9]+$")) {
                    userName_txt.setError("Only letters and numbers allowed");
                    userName_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Username should contain only letters and numbers", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (password.isEmpty()) {
                    password_txt.setError("Password is required");
                    password_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Password is required", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (conPass.isEmpty()) {
                    confirmPassword_txt.setError("Confirm Password is required");
                    confirmPassword_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (!password.equals(conPass)) {
                    password_txt.setError("Passwords do not match");
                    confirmPassword_txt.setError("Passwords do not match");
                    password_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (address.isEmpty()) {
                    address_txt.setError("Address is required");
                    address_txt.requestFocus();
                    Toast.makeText(SignUp.this, "Address is required", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                int selectedCategoryId = categoryRadioGroup.getCheckedRadioButtonId();
                int selectedTailorCategoryId = tailorCategoryRadioGroup.getCheckedRadioButtonId();

                if (selectedGenderId == -1) {
                    Toast.makeText(SignUp.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (selectedCategoryId == -1) {
                    Toast.makeText(SignUp.this, "Please select a category", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                if (selectedTailorCategoryId == -1) {
                    Toast.makeText(SignUp.this, "Please select a tailor category", Toast.LENGTH_SHORT).show();
                    signUp_Btn.revertAnimation();
                    return;
                }

                // Username Existence Check
                checkIfUsernameExists(userName, exists -> {
                    if (exists) {
                        Toast.makeText(SignUp.this, "Username already exists! Please choose another one.", Toast.LENGTH_SHORT).show();
                        signUp_Btn.revertAnimation();
                    } else {
                        // Check if the user already exists
                        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(encodedEmail)) {
                                    Toast.makeText(SignUp.this, "User Already Exists!", Toast.LENGTH_SHORT).show();
                                    signUp_Btn.revertAnimation();
                                } else {
                                    // Save user data in Firebase
                                    databaseReference.child("Users").child(encodedEmail).child("username").setValue(userName);
                                    databaseReference.child("Users").child(encodedEmail).child("password").setValue(password);
                                    databaseReference.child("Users").child(encodedEmail).child("email").setValue(email);
                                    databaseReference.child("Users").child(encodedEmail).child("telephoneNumber").setValue(phoneNumber);
                                    databaseReference.child("Users").child(encodedEmail).child("whatsappNumber").setValue(phoneNumber);
                                    databaseReference.child("Users").child(encodedEmail).child("address").setValue(address);
                                    databaseReference.child("Users").child(encodedEmail).child("profile_image_url").setValue("");
                                    databaseReference.child("Users").child(encodedEmail).child("arms_measurements").setValue("");
                                    databaseReference.child("Users").child(encodedEmail).child("legs_measurements").setValue("");
                                    databaseReference.child("Users").child(encodedEmail).child("waist_measurements").setValue("");
                                    databaseReference.child("Users").child(encodedEmail).child("full_measurements").setValue("");
                                    databaseReference.child("Users").child(encodedEmail).child("chest_measurements").setValue("");

                                    genderRadioBtn = findViewById(selectedGenderId);
                                    categoryRadioBtn = findViewById(selectedCategoryId);
                                    tailorCategory = findViewById(selectedTailorCategoryId);

                                    String genderSelected = genderRadioBtn.getText().toString();
                                    String categorySelected = categoryRadioBtn.getText().toString();
                                    String tailorCategorySelected = tailorCategory.getText().toString();

                                    databaseReference.child("Users").child(encodedEmail).child("gender").setValue(genderSelected);
                                    databaseReference.child("Users").child(encodedEmail).child("category").setValue(categorySelected);
                                    databaseReference.child("Users").child(encodedEmail).child("tailor_category").setValue(tailorCategorySelected);

                                    // Success Animation
                                    Drawable drawable = getResources().getDrawable(R.drawable.checkbutton);
                                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                    signUp_Btn.doneLoadingAnimation(R.color.white, bitmap);

                                    Prefs prefs = new Prefs(getApplicationContext());
                                    prefs.setUserID(encodedEmail);

                                    Toast.makeText(SignUp.this, "Your Account Has Been Created Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUp.this, Login.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SignUp.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                signUp_Btn.revertAnimation();
                            }
                        });
                    }
                });

            }, 300);
        });



        // Add Text Watchers
        email_txt.addTextChangedListener(emailTextWatcher);
        mobileNo_txt.addTextChangedListener(phoneTextWatcher);
        userName_txt.addTextChangedListener(usernameTextWatcher);
    }

    private void checkIfUsernameExists(String userName, final UsernameCheckCallback callback) {
        databaseReference.child("Users").orderByChild("username").equalTo(userName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onUsernameChecked(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignUp.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onUsernameChecked(false);
                    }
                });
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    interface UsernameCheckCallback {
        void onUsernameChecked(boolean exists);
    }
    private final TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String email = s.toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                email_txt.setError("Enter a valid email");
            } else if (!email.endsWith("@gmail.com")) {
                email_txt.setError("Only Gmail addresses (@gmail.com) are allowed");
            } else {
                email_txt.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().isEmpty()) {
                email_txt.setError("Required");
            }
        }
    };

    private final TextWatcher phoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0 && s.length() != 11) {
                mobileNo_txt.setError("Phone number must be 11 digits");
            } else {
                mobileNo_txt.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().isEmpty()) {
                mobileNo_txt.setError("Required");
            }
        }
    };

    private final TextWatcher usernameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().matches("^[a-zA-Z0-9]+$") && s.length() > 0) {
                userName_txt.setError("Only letters and numbers allowed");
            } else {
                userName_txt.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().isEmpty()) {
                userName_txt.setError("Required");
            }
        }
    };
}
