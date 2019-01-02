package com.michael.nutritionreader;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;



public class CropActivity extends AppCompatActivity implements CropImageView.OnCropImageCompleteListener {

    String uriString;
    Bitmap bitmap;
    public static final String KEY_BITMAP_STRING = "key_bitmap_string";
    public static final String KEY_FULL_BITMAP_STRING = "key_fullbitmap_string";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        final CropImageView cropView = findViewById(R.id.crop_view);
        cropView.setOnCropImageCompleteListener(this);
        final ImageView cropButton = findViewById(R.id.crop);
        ImageView cancel = findViewById(R.id.cross);
        ImageView rotateLeft = findViewById(R.id.rotateLeft);
        ImageView rotateRight = findViewById(R.id.rotateRight);
        ImageView autoImprove = findViewById(R.id.autoimprove);

        bitmap = getBitmap(KEY_FULL_BITMAP_STRING);
        cropView.setImageBitmap(bitmap);

        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bitmap = RotateBitmap(bitmap,-90);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cropView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
        });

        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bitmap = RotateBitmap(bitmap,90);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cropView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });


            }
        });

        autoImprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bitmap = Utilities.improveImage(bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cropView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
        });


        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropView.getCroppedImageAsync();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });

    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



    public void saveBitmap(Bitmap bitmap,String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        bitmap.recycle();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, bitmapStr);
        editor.apply();
    }

    public Bitmap getBitmap(String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String bitmapStr = sharedPreferences.getString(key,"");
        Bitmap bitmap = null;
        if (!bitmapStr.isEmpty()) {
            byte[] bytes = Base64.decode(bitmapStr, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
        }
        return bitmap;
    }


    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        if(result.isSuccessful()){
            saveBitmap(view.getCroppedImage(),KEY_BITMAP_STRING);
            finish();
        }
    }
}
