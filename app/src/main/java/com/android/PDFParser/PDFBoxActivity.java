package com.android.PDFParser;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.pdmodel.common.PDMetadata;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;


public class PDFBoxActivity extends AppCompatActivity {

    private ProgressBar mDataProgress;
    private Button btnPdf,btnPdfRead;
    private TextView PDFtv;
    private String  mData;
    private TextView mData_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfbox);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        PDFBoxResourceLoader.init(this);
        mData_tv = (TextView)findViewById(R.id.tv_data);
        mData_tv.setVerticalScrollBarEnabled(true);
        mData_tv.setMovementMethod(new ScrollingMovementMethod());
        mData_tv = (TextView)findViewById(R.id.tv_data);
        mData_tv.setVerticalScrollBarEnabled(true);
        mData_tv.setMovementMethod(new ScrollingMovementMethod());
        mDataProgress =(ProgressBar)findViewById(R.id.mData_progressbar);

        PDFtv = (TextView)findViewById(R.id.pdf_tv);
        btnPdf = (Button)findViewById(R.id.pdf_btn);
        btnPdfRead = (Button)findViewById(R.id.read_pdf_btn);


        btnPdf.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          new MaterialFilePicker()
                                                  .withActivity(PDFBoxActivity.this)
                                                  .withRequestCode(1000)
                                                  .withFilter(Pattern.compile(".*\\.pdf$")) // Filtering files and directories by file name using regexp
                                                  // .withFilterDirectories(true) // Set directories filterable (false by default)
                                                  .withHiddenFiles(true) // Show hidden files and folders
                                                  .start();
                                      }
                                  }
        );


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            final String  filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            PDFtv.setText("Selected File: "+ filePath);
            btnPdfRead.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  LetsParse(filePath);
                                              }
                                          }
            );
        }
    }

    protected void LetsParse(final String filePath){
        PDFtv.setText("Selected File: "+ filePath);
        PDFBoxResourceLoader.init(this);

        new ParsePDFTask().execute(filePath);
        mDataProgress.setVisibility(View.VISIBLE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode) {
            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private class ParsePDFTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String filePath = params[0];
            try {
                PDFTextStripper mTextStripper = new PDFTextStripper();
                File file = new File(filePath);
                PDDocument mfile = PDDocument.load(file);

                PDDocumentInformation info = mfile.getDocumentInformation();
                PDDocumentCatalog cat = mfile.getDocumentCatalog();
                PDMetadata metadata = cat.getMetadata();

                mData = " Page Count= " + mfile.getNumberOfPages();
                mData = mData+"\n Title= " + info.getTitle();
                mData = mData+"\n Author= " + info.getAuthor();
                mData = mData+"\n Subject= " + info.getSubject();
                mData = mData+"\n Keywords= " + info.getKeywords();
                mData = mData+"\n Creator= " + info.getCreator();
                mData = mData+"\n Producer= " + info.getProducer();
              //  mData = mData+ "\n Creation Date= " + new SimpleDateFormat("MMM dd,yyyy hh:mm a").format( info.getCreationDate());
              //  mData = mData+ "\n Modification Date= " + new SimpleDateFormat("MMM dd,yyyy hh:mm a").format(info.getModificationDate());
                mData = mData+"\n Trapped= " + info.getTrapped();
                if (metadata != null) {
                    mData = mData+ "\n Metadata= " + metadata.getStream();
                }

                mData = mData+ "\n \n \n DATA: \n"+mTextStripper.getText(mfile);
                mfile.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return mData;
        }


        @Override
        protected void onPreExecute() {
            mData_tv.setText("");
            mDataProgress.setVisibility(View.INVISIBLE);

        }



        @Override
        protected void onPostExecute(String mdata) {
            mData_tv.setText(mdata);
            PDFtv.setVisibility(View.GONE);
            btnPdfRead.setVisibility(View.GONE);
            btnPdf.setVisibility(View.GONE);
            mDataProgress.setVisibility(View.GONE);
            mData_tv.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onCancelled() {
            mDataProgress.setVisibility(View.GONE);

        }
    }

}



