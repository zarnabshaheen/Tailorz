    package com.example.tailorz.register;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.text.Editable;
    import android.text.InputType;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.util.Patterns;
    import android.widget.EditText;
    import android.widget.LinearLayout;
    import android.widget.Toast;

    import com.example.tailorz.adminPanel.AdminMainPanel;
    import com.example.tailorz.customerActivities.CustomerMain;
    import com.example.tailorz.helpers.Prefs;
    import com.example.tailorz.tailorActivities.TailorMain;
    import com.example.tailorz.databinding.ActivityLoginBinding;
    import com.google.firebase.database.*;

    public class Login extends AppCompatActivity {

        private static final String DATABASE_URL = "https://tailorz-aa1b7-default-rtdb.firebaseio.com/";
        private ActivityLoginBinding binding;
        private Prefs prefs;
        private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(DATABASE_URL);

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            prefs = new Prefs(getApplicationContext());

            if (prefs.getLoginStatus()) {
                navigateToDashboard(prefs.getUserCategory());
            }

            binding.tvSignupLink.setOnClickListener(v -> {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
                finish();
            });

            binding.btLogin.setOnClickListener(v -> AuthenticateUser());
            binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
                    binding.tvUsername.addTextChangedListener(emailTextWatcher);

        }

        private void AuthenticateUser() {
            String userName = binding.tvUsername.getText().toString();
            String password = binding.tvPassword.getText().toString();
            String encodedUserName = encodeEmail(userName);

            if (userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please Enter Credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("admin".equalsIgnoreCase(userName) && "admin".equalsIgnoreCase(password)) {
                startActivity(new Intent(Login.this, AdminMainPanel.class));
                finish();
                return;
            }

            databaseReference.child("Users").child(encodedUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(Login.this, "User Does Not Exist", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String databasePass = snapshot.child("password").getValue(String.class);
                    if (databasePass == null || !databasePass.equals(password)) {
                        Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    saveUserData(snapshot, encodedUserName);
                    navigateToDashboard(snapshot.child("category").getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void navigateToDashboard(String category) {
            if ("Customer".equals(category)) {
                startActivity(new Intent(Login.this, CustomerMain.class));
            } else if ("Tailor".equals(category)) {
                startActivity(new Intent(Login.this, TailorMain.class));
            }
            finish();
        }

        private void saveUserData(DataSnapshot snapshot, String encodedUserName) {
            prefs.setUserID(encodedUserName);
            prefs.setUserEmail(snapshot.child("email").getValue(String.class));
            prefs.setUserName(snapshot.child("username").getValue(String.class));
            prefs.setUserCategory(snapshot.child("category").getValue(String.class));
            prefs.setUserWhatsapp(snapshot.child("whatsappNumber").getValue(String.class));
            prefs.setUserAddress(snapshot.child("address").getValue(String.class));
            prefs.setUserTelephone(snapshot.child("telephoneNumber").getValue(String.class));
            prefs.setUserProfileImage(snapshot.child("profile_image_url").getValue(String.class));
            prefs.setTailorCategory(snapshot.child("tailor_category").getValue(String.class));
            prefs.setUserGender(snapshot.child("gender").getValue(String.class));
            prefs.setUserArms(snapshot.child("arms_measurements").getValue(String.class));
            prefs.setUserChest(snapshot.child("chest_measurements").getValue(String.class));
            prefs.setUserLegs(snapshot.child("legs_measurements").getValue(String.class));
            prefs.setUserFull(snapshot.child("full_measurements").getValue(String.class));
            prefs.setUserWaist(snapshot.child("waist_measurements").getValue(String.class));
            prefs.setLoginStatus(true);
        }

        private String encodeEmail(String email) {
            return email.replace(".", ",");
        }

        private void showForgotPasswordDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Forgot Password");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);

            final EditText emailInput = new EditText(this);
            emailInput.setHint("Enter your email");
            emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            layout.addView(emailInput);

            final EditText passwordInput = new EditText(this);
            passwordInput.setHint("Enter new password");
            passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(passwordInput);

            builder.setView(layout);
            builder.setPositiveButton("Reset", (dialog, which) -> resetPassword(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim()));
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        }

        private void resetPassword(String email, String newPassword) {
            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String encodedEmail = encodeEmail(email);
            DatabaseReference usersRef = databaseReference.child("Users");

            usersRef.child(encodedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(Login.this, "Email not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    usersRef.child(encodedEmail).child("password").setValue(newPassword)
                            .addOnSuccessListener(unused -> Toast.makeText(Login.this, "Password Reset Successful!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(Login.this, "Failed to update password", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private final TextWatcher emailTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches() && s.length() > 0) {
                    binding.tvUsername.setError("Enter a valid email");
                } else {
                    binding.tvUsername.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    binding.tvUsername.setError("Required");
                }
            }
        };
    }
