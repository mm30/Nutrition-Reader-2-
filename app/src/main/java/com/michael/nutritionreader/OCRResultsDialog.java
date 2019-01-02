package com.michael.nutritionreader;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



public class OCRResultsDialog extends Dialog {

    Context context;
    String resultData;

    public OCRResultsDialog(@NonNull Context context,String resultData) {
        super(context);
        this.context = context;
        this.resultData = resultData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_result_popup);
        TextView resultTextView = (TextView) findViewById(R.id.textViewResults);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());
        resultTextView.setText(resultData);
        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        ImageView copyContentButton = findViewById(R.id.copyButton);
        ImageView shareButton = findViewById(R.id.shareButton);
        copyContentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", resultData);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context,"copied",Toast.LENGTH_SHORT).show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, resultData);
                shareIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(shareIntent, "Share results to"));
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
