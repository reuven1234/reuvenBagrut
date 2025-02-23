package com.example.reuvenbagrut;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void addUser(User user, FirebaseCallback<User> callback) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(user.getName())
                                        .build();
                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                callback.onSuccess(user);
                                            } else {
                                                callback.onError(task1.getException().getMessage());
                                            }
                                        });
                            }
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getUser(User user, FirebaseCallback<User> callback) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(user);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    // ✅ UPDATE USER METHOD
    public void updateUser(String newName, String newEmail, String newPassword, Uri profilePictureUri, FirebaseCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        // Update Profile (Name or Profile Picture)
        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
        boolean hasProfileUpdate = false;

        if (newName != null && !newName.isEmpty()) {
            profileUpdates.setDisplayName(newName);
            hasProfileUpdate = true;
        }

        if (profilePictureUri != null) {
            profileUpdates.setPhotoUri(profilePictureUri);
            hasProfileUpdate = true;
        }

        if (hasProfileUpdate) {
            user.updateProfile(profileUpdates.build()).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onError("Failed to update profile");
                    return;
                }
            });
        }

        // Update Email
        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
            user.updateEmail(newEmail).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onError("Failed to update email: " + task.getException().getMessage());
                    return;
                }
            });
        }

        // Update Password
        if (newPassword != null && !newPassword.isEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onError("Failed to update password: " + task.getException().getMessage());
                    return;
                }
            });
        }

        callback.onSuccess(null);
    }


    // ✅ DELETE USER METHOD
    public void deleteUser(FirebaseCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Delete user data from Firestore
            firestore.collection("users").document(userId)
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.delete()
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            callback.onSuccess(null);
                                        } else {
                                            callback.onError("Failed to delete user account");
                                        }
                                    });
                        } else {
                            callback.onError("Failed to delete user data");
                        }
                    });
        } else {
            callback.onError("User not logged in");
        }
    }
}
