package com.michael.nutritionreader;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

 /**
	Entire module retrieved from: https://github.com/djbb7/node-ocr-server/blob/master/Android/OpenOCRanges/app/src/main/java/fi/aalto/openoranges/project2/openocranges/TessOCR.java
	retrieved on:
	10 November 2018
 */

public class TessOCR {
    private TessBaseAPI mTess;

    public TessOCR() {

        mTess = new TessBaseAPI();

        String datapath = Environment.getExternalStorageDirectory() + "/ImageMagicksOCR/";
        String language = "eng";
        File dir = new File(datapath + "/tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        mTess.init(datapath, language);
    }

    public String getOCRResult(Bitmap bitmap) {

        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();

        return result;
}

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }

}
