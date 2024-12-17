package com.tamagochy.dao;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfilePicDAO {

    private final StorageReference storageRef;
    private String downloadURL;

    public ProfilePicDAO() {
        storageRef = FirebaseStorage.getInstance().getReference("pets_images");
    }

    public String getStorageBucket(){return this.storageRef.getBucket();}

    public void uploadProfilePic(Uri photoUri, final UploadCallback callback) {
        // Criar um ID único para a imagem
        String uniqueId = UUID.randomUUID().toString();
        StorageReference photoRef = storageRef.child(uniqueId);  // Usar o uniqueId como nome do arquivo

        // Fazer upload da foto
        photoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Após o upload, passar o uniqueId (não a URL) para o callback
                    callback.onSuccess(uniqueId);
                })
                .addOnFailureListener(e -> {
                    // Tratamento de falha no upload
                    Log.e("UPLOAD_ERROR", "Erro ao fazer upload da foto", e);
                    callback.onError("Erro ao fazer upload da foto.");
                });
    }

    public void deleteProfilePic(String imageUrl, DeletionCallback callback) {
        String imagePath = "gs://" + storageRef.getBucket() + "/pets_images/" + imageUrl;
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);

        photoRef.getMetadata().addOnSuccessListener(metadata -> {
            // If the file exists, delete it
            photoRef.delete().addOnSuccessListener(aVoid -> {
                Log.d("DELETION", "Image deleted successfully.");
                callback.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e("DELETION", "Error deleting image.", e);
                callback.onFailure(e);
            });
        }).addOnFailureListener(e -> {
            Log.e("DELETION", "Image not found in Firebase Storage.", e);
            callback.onFailure(e);
        });
    }

   public StorageReference getStorageRef(){return this.storageRef;}


    public interface DeletionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UploadCallback {
        void onSuccess(String uniqueId); // Passa o uniqueId em vez da URL
        void onError(String errorMessage);
    }

    public interface UriCallback {
        void onSuccess(Uri uri);
        void onFailure(Exception e);
    }

}
