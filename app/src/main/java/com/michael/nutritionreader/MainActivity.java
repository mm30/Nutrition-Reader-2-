package com.michael.nutritionreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 4;
    private static final int CAMERA_TAKE_PICTURE = 2;
    int PICK_IMAGE_REQUEST = 5;
    int CAMERA_INTENT_REQUEST_CODE = 3;
    Uri ActualFileUri;
    Uri CamerafileUri;
    String defaultCameraPackage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PackageManager packageManager = MainActivity.this.getPackageManager();
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        int listSize = list.size();
        for (int n = 0; n < listSize; n++) {
            if ((list.get(n).flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Camera")) {
                    defaultCameraPackage = list.get(n).packageName;

                }
            }
        }
    }




    public void importFromGallery(View view){
        openGallery();
    }

    public void openGallery(){

        int permissionWriteStorage = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionReadStorage = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED && permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            Utilities.requestWritePermissionStorage(MainActivity.this, MainActivity.this, REQUEST_WRITE_STORAGE_PERMISSION);

        }

        if (permissionWriteStorage == PackageManager.PERMISSION_GRANTED && permissionReadStorage == PackageManager.PERMISSION_GRANTED) {
            pickImages();
        }

    }


    private void pickImages() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }


    public void onTakePhotoClick(View view){
        callCamera();
    }


    public void callCamera(){

        int permissionCheckCamera = ContextCompat.checkSelfPermission((MainActivity.this),
                Manifest.permission.CAMERA);
        int permissionStorage = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheckCamera == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED) {

            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = Utilities.getOutputImageMediaFile();
            if(file != null){

                ActualFileUri = Uri.fromFile(file);
                CamerafileUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationContext().getPackageName() + ".NutritionProvider", file);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, CamerafileUri);
                intentCamera.setPackage(defaultCameraPackage);
                startActivityForResult(intentCamera, CAMERA_INTENT_REQUEST_CODE);

            }
        }
        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED || permissionStorage != PackageManager.PERMISSION_GRANTED) {
            Utilities.requestPermissionCamera(MainActivity.this, MainActivity.this, REQUEST_CAMERA_PERMISSION);
        }



    }


    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case REQUEST_WRITE_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCamera();
                }
                break;
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String selectedImagePath;

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            final Uri uri = data.getData();

                selectedImagePath = getRealPathFromURI(uri);
                Intent displayImageIntent = new Intent(MainActivity.this,DisplayImage.class);
                displayImageIntent.putExtra("SourceType","gallery");
                displayImageIntent.putExtra("ImagePath",selectedImagePath);
                startActivity(displayImageIntent);


        } else if (requestCode == CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {

            if(ActualFileUri != null){
                Uri uri = Uri.parse(ActualFileUri.getEncodedPath());
                final String imagePath = uri.toString();

                if (!imagePath.isEmpty()) {

                    Intent displayImageIntent = new Intent(MainActivity.this,DisplayImage.class);
                    displayImageIntent.putExtra("SourceType","camera");
                    displayImageIntent.putExtra("ImagePath",imagePath);
                    displayImageIntent.putExtra("Uri",  CamerafileUri.toString());
                    startActivity(displayImageIntent);

                }
            }
            }
        }

}
