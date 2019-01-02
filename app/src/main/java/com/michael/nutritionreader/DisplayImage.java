package com.michael.nutritionreader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DisplayImage extends AppCompatActivity {

    String imagePath;
    String uri;
    ExifInterface exifInterface;
    int orientation;
    Bitmap bitmap;
    ProgressDialog dialog;
    ImageView imageView;
    public static final String KEY_BITMAP_STRING = "key_bitmap_string";
    public static final String KEY_FULL_BITMAP_STRING = "key_fullbitmap_string";
    public static final String lang = "eng";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/ImageMagicksOCR/";
    TessOCR tessOCR;
    Context context = this;
    boolean isOpenCVConnected = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imageView = (ImageView) findViewById(R.id.imageView);
        /*dialog = Utilities.showProgressBar(DisplayImage.this,"getting your picture ready..");
        dialog.show();
*/
        final Intent intent = getIntent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                savingLanguageDataToExternalPath();
                if (!OpenCVLoader.initDebug()) {

                }
                else
                {
                 mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }

                tessOCR = new TessOCR();

                if(intent.hasExtra("ImagePath")){

                    if(intent.getStringExtra("SourceType").contentEquals("gallery")){

                    imagePath = intent.getStringExtra("ImagePath");
                    String selectedImagePath = getRealPathFromURI(Uri.parse(imagePath));

                        try {
                            exifInterface = new ExifInterface(selectedImagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        bitmap = Utilities.convertImagePathToBitmap(imagePath,imageView.getMeasuredWidth(),imageView.getMeasuredHeight());
                        bitmap = Utilities.rotateBitmap(bitmap,orientation);
                        saveBitmap(bitmap,KEY_FULL_BITMAP_STRING);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog != null && dialog.isShowing()){
                                    dialog.dismiss();
                                }
                                imageView.setImageBitmap(bitmap);
                            }
                        });

                }
                else if(intent.getStringExtra("SourceType").contentEquals("camera")){

                        Log.d("Start","Start");
                        imagePath = intent.getStringExtra("ImagePath");

                        try {
                            exifInterface = new ExifInterface(imagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.d("Height",imageView.getMeasuredHeight() + " Width" + imageView.getMeasuredWidth() + " ");

                        orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        //bitmap = Utilities.improveImage(bitmap);
                        bitmap = Utilities.rotateBitmap(Utilities.convertImagePathToBitmap(imagePath,imageView.getMeasuredWidth(),imageView.getMeasuredHeight()),orientation);
                        saveBitmap(bitmap,KEY_FULL_BITMAP_STRING);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog != null && dialog.isShowing()){
                                    dialog.dismiss();
                                }
                                Log.d("Start","Start");
                                imageView.setImageBitmap(bitmap);
                            }
                        });


                 }

                }
            }
        }).start();


    }

    public void saveNewImageToGallery(View view){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        try (FileOutputStream out = new FileOutputStream(imagePath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        Snackbar.make(findViewById(android.R.id.content),"Saved to Gallery",Snackbar.LENGTH_SHORT).show();

      }
      else {

            Snackbar.make(findViewById(android.R.id.content),"Oops! An error occured",Snackbar.LENGTH_SHORT).show();

        }
    }





    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    isOpenCVConnected = true;

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };






    public void saveBitmap(Bitmap bitmap,String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, bitmapStr);
        editor.apply();
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



    public void imageProcessing(View view){
        dialog = Utilities.showProgressBar(DisplayImage.this,"Processing..");
        if(isOpenCVConnected){

            if(bitmap != null) {


                         if(Utilities.isBitmapDark(bitmap)){

                            runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),"There isn't enough light in this image. Please take another image in bette lighting conditions for optimal" +
                                        " character recognition.",Snackbar.LENGTH_LONG).show();
                            }
                        });

                        }

                        else if(Utilities.isImageBlur(bitmap)){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    Snackbar.make(findViewById(android.R.id.content),"This image is too blurry. Please take another image and focus on the text so it is more clear." +
                                            "Also try to keep the text level on  flat surface.",Snackbar.LENGTH_LONG).show();
                                }
                            });

                        }
                        else {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    performOCR(bitmap);
                                }
                            });

                        }
                    }
                    else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),"Oops! An error occured while processing..",Snackbar.LENGTH_SHORT).show();
                            }
                        });

                    }


        }

    }









    public void cropImage(View view){
        Intent cropIntent = new Intent(DisplayImage.this,CropActivity.class);
        startActivity(cropIntent);

    }


    public void onResume(){
        super.onResume();
        if(getBitmap(KEY_BITMAP_STRING) != null){
            bitmap = Utilities.rotateBitmap(getBitmap(KEY_BITMAP_STRING),orientation);
            imageView.setImageBitmap(bitmap);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sharedPreferences.edit().remove(KEY_BITMAP_STRING).apply();
        }
    }

    public Bitmap getBitmap(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String bitmapStr = sharedPreferences.getString(key, "");
        Bitmap bitmap = null;
        if (!bitmapStr.isEmpty()) {
            byte[] bytes = Base64.decode(bitmapStr, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
        }
        return bitmap;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
    }
    }


    private void savingLanguageDataToExternalPath(){

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v("Main", "ERROR: Creation of directory " + path + " on sdcard failed");
                    break;
                } else {
                    Log.v("Main", "Created directory " + path + " on sdcard");
                }
            }

        }
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();

                InputStream in = assetManager.open(lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                // Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                // Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }

        }
    }



    public void performOCR(final Bitmap bitmap) {
        if (dialog == null) {
            dialog = Utilities.showProgressBar(this,"Performing OCR..");

        }
        else {
            dialog.setMessage("Performing OCR..");
            dialog.show();
        }

        new Thread(new Runnable() {
            public void run() {


                final String result = tessOCR.getOCRResult(bitmap);


                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (result != null && !result.equals("")) {
                            String s = result.trim();
                            OCRResultsDialog ocrResultsDialog = new OCRResultsDialog(DisplayImage.this,s);
                            ocrResultsDialog.setTitle("Recognized Text");
                            if(!((DisplayImage)context).isFinishing()){
                                ocrResultsDialog.show();
                                }

                        }


                        dialog.dismiss();
                    }

                });

            };
        }).start();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tessOCR != null){
            tessOCR.onDestroy();
        }

        bitmap = null;
        dialog = null;
    }



    public void finishActivity(View view){
        finish();
    }

    /*@Override
    protected void onResume() {
        super.onResume();

    }*/
}
