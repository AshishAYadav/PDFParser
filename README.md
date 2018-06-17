# PDFParser
##PDF parser implements PDFBox-Android API by Tom-Rous and Material File Picker

This readme file gives explanation of the work

PDFParser can read data from the text version PDFs 

It reads ###Meta Data and ###Contents of the file

Below are the few snapshots of the app

<img src="https://github.com/AshishAYadav/PDFParser/blob/master/Screenshot_2018-06-18-03-33-18-115_com.google.android.packageinstaller.png " width="200" height="400" />
<img src="https://github.com/AshishAYadav/PDFParser/blob/master/Screenshot_2018-06-18-03-33-24-564_com.android.PDFParser.png " width="200" height="400" />
<img src="https://github.com/AshishAYadav/PDFParser/blob/master/Screenshot_2018-06-18-03-33-30-265_com.android.PDFParser" width="200" height="400" />
<img src="https://github.com/AshishAYadav/PDFParser/blob/master/Screenshot_2018-06-18-03-34-00-601_com.android.PDFParser.png " width="200" height="400" />




#CODE


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

        //grant permission in Android versions higher than Marshmallow
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        //To minimize the parsing time 
        PDFBoxResourceLoader.init(this);
       
       
        btnPdf = (Button)findViewById(R.id.pdf_btn);
        btnPdfRead = (Button)findViewById(R.id.read_pdf_btn);

//Open Material File Picker
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

//When File is selected. This method is automatically called
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

//Executes the Async Task on Click

    protected void LetsParse(final String filePath){
        PDFtv.setText("Selected File: "+ filePath);
        PDFBoxResourceLoader.init(this);

        new ParsePDFTask().execute(filePath);
        mDataProgress.setVisibility(View.VISIBLE);


    }

//Check if permission granted for External Storage

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


//Async Task to load the Data in background

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

