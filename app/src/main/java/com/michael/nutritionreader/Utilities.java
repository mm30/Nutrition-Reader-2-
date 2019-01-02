package com.michael.nutritionreader;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {

    public static boolean isImageBlur(Bitmap image) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int l = CvType.CV_8UC1; //8-bit grey scale image
        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

        Mat dst2 = new Mat();
        Utils.bitmapToMat(image, dst2);
        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
        Mat laplacianImage8bit = new Mat();
        laplacianImage.convertTo(laplacianImage8bit, l);

        image = Bitmap.createBitmap(laplacianImage8bit.cols(), laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(laplacianImage8bit, image);
        int[] pixels = new int[image.getHeight() * image.getWidth()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        int maxLap = -16777216; // 16m
        for (int pixel : pixels) {
            if (pixel > maxLap)
                maxLap = pixel;
        }

        int soglia = -6118750;
        if (maxLap <= soglia) {
            System.out.println("is blur image");
            image = null;
            return true;

        } else {
            image = null;
            return false;
        }
    }

    public static Bitmap improveImage(Bitmap src){
        int value = 10;

        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int width = src.getWidth();
        int height = src.getHeight();
        double contrast = Math.pow((100 + value) / 100, 2);

        Canvas c = new Canvas();
        c.setBitmap(dest);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int pixelColor = src.getPixel(x, y);
                int pixelAlpha = Color.alpha(pixelColor);
                int pixelRed = (int) ((((((Color.red(pixelColor) + 50)/255.0) - 0.5) * contrast) + 0.5) * 255.0);
                int pixelGreen = (int) ((((((Color.green(pixelColor) + 50)/255.0) - 0.5) * contrast) + 0.5) * 255.0);
                int pixelBlue = (int) ((((((Color.blue(pixelColor) + 50)/255.0) - 0.5) * contrast) + 0.5) * 255.0);

                if(pixelRed > 255){
                    pixelRed = 255;
                }
                else if(pixelRed < 0){
                    pixelRed = 0;
                }
                if(pixelGreen > 255){
                    pixelGreen = 255;
                }
                else if(pixelGreen < 0){
                    pixelGreen = 0;
                }

                if(pixelBlue > 255){
                    pixelBlue = 255;
                }
                else if(pixelBlue < 0){
                    pixelBlue = 0;
                }

                int newPixel = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue);
                dest.setPixel(x, y, newPixel);
            }
        }
        return dest;
    }


    public static Bitmap adjustBrightness(Bitmap src){

        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int width = src.getWidth();
        int height = src.getHeight();


        for(int x = 0; x < width; x++){
        for(int y = 0; y < height; y++){
        int pixelColor = src.getPixel(x, y);
        int pixelAlpha = Color.alpha(pixelColor);
        int pixelRed = Color.red(pixelColor) + 50;
        int pixelGreen = Color.green(pixelColor) + 50;
        int pixelBlue = Color.blue(pixelColor) + 50;
        if(pixelRed > 255){
            pixelRed = 255;
        }
        else if(pixelRed < 0){
        pixelRed = 0;
        }
        if(pixelGreen > 255){
            pixelGreen = 255;
        }
        else if(pixelGreen < 0){
            pixelGreen = 0;
        }

        if(pixelBlue > 255){
            pixelBlue = 255;
        }
        else if(pixelBlue < 0){
            pixelBlue = 0;
        }

        int newPixel = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue);
        dest.setPixel(x, y, newPixel);

        }

    }

    return dest;
    }

    public static Bitmap adjustedContrast(Bitmap src)
    {
        double value = 50;
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap

        // create a mutable empty bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));


        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    public static boolean isResolutionGood(Bitmap bitmap){

        if(bitmap.getWidth() > 120 && bitmap.getHeight() > 140){
            return true;
        }

        return false;
    }

    public static ProgressDialog showProgressBar(Context context, String message){

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }




    public static boolean isBitmapDark(Bitmap bitmap){

        if(bitmap == null)
        {
            return false;
        }

        int[] brightnessHistogram = new int[256];

        for(int i = 0; i < bitmap.getHeight(); i++) {
            for(int j = 0; j < bitmap.getWidth(); j++) {
                int pixel = bitmap.getPixel(j,i);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
                brightnessHistogram[brightness]++;
            }
        }

        int allPixelsCount = bitmap.getWidth() * bitmap.getHeight();

        //counting pixels with brightness less than 10
        int darkPixelCount = 0;
        for(int i = 0; i < 10; i++)
        {
            darkPixelCount += brightnessHistogram[i];
        }

        //if more than 70% pixels are too dark then image is too dark
        return darkPixelCount > allPixelsCount * 0.25;
    }







    public static void requestWritePermissionStorage(Activity activity, Context context, int REQUEST_WRITE_STORAGE_PERMISSION) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION
            );
        }
    }


    public static void requestPermissionCamera(Activity activity, Context context, int REQUEST_CAMERA_PERMISSION) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION
            );

        }
    }



    public static Bitmap convertImagePathToBitmap(String path,int width,int height) {


        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmpFactoryOptions);

        /*if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }*/


        bmpFactoryOptions.inSampleSize = calculateInSampleSize(bmpFactoryOptions,width,height);
        bmpFactoryOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmpFactoryOptions);
        return bitmap;
    }





    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {

            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;
        } catch (Exception e) {
            return null;
        }

    }



    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }








    public static File getOutputImageMediaFile() {

        String IMAGE_DIRECTORY_NAME = "ImageMagicks";

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
        Log.d("File Directory", "" + mediaStorageDir);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("", "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");

                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }







    /*private boolean isPhotoDark(Bitmap bitmap){

        int histogram[] = new int[256];

        for (int i=0;i<256;i++) {
            histogram[i] = 0;
        }

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for(int y = 0; y < bitmap.getHeight(); y++) {
                int color = a.getRGB(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (int) (0.2126*r + 0.7152*g + 0.0722*b);
                histogram[brightness]++;
            }
        }

        int allPixelsCount = a.getWidth() * a.getHeight();

// Count pixels with brightness less then 10
        int darkPixelCount = 0;
        for (int i=0;i<10;i++) {
            darkPixelCount += histogram[i];
        }

        if (darkPixelCount > allPixelCount * 0.25){
            return true;
        }
        else {
            return false;
        }*/

}

