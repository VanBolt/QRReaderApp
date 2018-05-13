package educationalproject.vanja.qrreaderapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import educationalproject.vanja.qrreaderapp.barcode.BarcodeCaptureActivity;

public class MainActivity extends AppCompatActivity {

    private TextView mResultTextView;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final int SEND_BARCODE_REQUEST_CODE = 2;

    public static String bar_text_in = "";
    public static String pass_in = "";
    public static String server = "http://ibolotov-001-site1.atempurl.com/Mobile/GetQR";

    public EditText edit_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scan = (Button) findViewById(R.id.scan_barcode_button);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });

        Button send = (Button) findViewById(R.id.send_btn);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResultTextView = (TextView) findViewById(R.id.result_textview);
                edit_pass = (EditText) findViewById(R.id.edit_pass);

                bar_text_in = mResultTextView.getText().toString();
                pass_in = edit_pass.getText().toString();

                try{
                    new SendData().execute();
                }catch (Exception ex){

                }
            }
        });
    }

    public  void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = (Barcode) data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mResultTextView = (TextView) findViewById(R.id.result_textview);
                    mResultTextView.setText(barcode.displayValue.toString());
                } else
                    mResultTextView.setText(R.string.no_barcode_captured);
            } else
                Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    class SendData extends AsyncTask<Void, Void, Void> {

        String resultString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String myURL = server;

                String parammetrs = "bar=" + bar_text_in + "&pass=" + pass_in;
                byte[] data = null;
                InputStream is = null;

                try {
                    URL url = new URL(myURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    // конвертируем передаваемую строку в UTF-8
                    data = parammetrs.getBytes("UTF-8");

                    OutputStream os = conn.getOutputStream();

                    // передаем данные на сервер
                    os.write(data);
                    os.flush();
                    os.close();
                    data = null;
                    conn.connect();
                    int responseCode = conn.getResponseCode();

                    // передаем ответ сервер
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (responseCode == 200) {    // Если все ОК (ответ 200)
                        is = conn.getInputStream();
                        byte[] buffer = new byte[8192]; // размер буфера
                        // Далее так читаем ответ
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        data = baos.toByteArray();
                        resultString = new String(data, "UTF-8");  // сохраняем в переменную ответ сервера, у нас "OK"
                    } else {
                    }
                    conn.disconnect();
                } catch (MalformedURLException e) {
                    resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {
                    resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {
                    resultString = "Exception:" + e.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void result){
            super.onPostExecute(result);

            Toast toast = Toast.makeText(getApplicationContext(), resultString, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
