package efrei.android.lab2.authactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {


    Button authenticateBtn;
    EditText username;
    EditText password;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get elements
        this.authenticateBtn = (Button) findViewById(R.id.login_btn_submit);
        this.username = (EditText) findViewById(R.id.login_username);
        this.password = (EditText) findViewById(R.id.login_password);
        this.result = (TextView) findViewById(R.id.login_result);

        // assign the method authenticate to btn authenticate
        this.authenticateBtn.setOnClickListener(view -> authenticate());

    }


    private void authenticate() {
        // Exercise 4 uncomment to see the result
        // new Thread(() -> fetchURL("https://android.com/")).start();

        // Exercise 5
        // fetch with credential
        new Thread(() -> fetchURL("https://httpbin.org/basic-auth/bob/sympa",
                this.username.getText().toString(),
                this.password.getText().toString())).start();
    }

    /**
     * Fetch data from an url and log back with JFL key
     *
     * @param urlPath the url to fetch
     */
    private void fetchURL(String urlPath) {
        URL url = null;
        try {
            url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String s = readStream(in);
                Log.i("JFL", s);
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch data from an url and log back with JFL key
     * WITH AUTHORIZATION (username:password)
     *
     * @param urlPath  the url to fetch
     * @param username the username to access resource
     * @param password the password to access resource
     */
    private void fetchURL(String urlPath, String username, String password) {

        // set basic auth
        String basicAuth = "Basic " + Base64.encodeToString(concatCredential(username, password).getBytes(), Base64.NO_WRAP);
        URL url;
        try {
            url = new URL("https://httpbin.org/basic-auth/bob/sympa");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // add authorization
            urlConnection.setRequestProperty("Authorization", basicAuth);
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String s = readStream(in);
                Log.i("JFL", s);
                // start thread to update render view
                updateResultView(s);
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean extractJsonResult(String result) {
        try {
            JSONObject object = new JSONObject(result);
            return object.getBoolean("authenticated");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateResultView(String result) {

        // get json result
        boolean res = extractJsonResult(result);
        // with inner class | uncomment to test it and comment line 137
        // runOnUiThread(new CustomRunnable(res));
        // with attribute
        runOnUiThread(() -> this.result.setText(res ? "true" : "false"));
    }

    private String concatCredential(String username, String password) {
        return username + ':' + password;
    }


    /**
     * Read an input stream and return string
     *
     * @param in the input stream
     * @return the input stream reading
     * @throws IOException throw if error during reading
     */
    private String readStream(InputStream in) throws IOException {
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining());
    }

    /**
     * Custom runnable class to test rendering view
     */
    class CustomRunnable implements Runnable {

        boolean res;
        TextView result;

        CustomRunnable(boolean res) {
            super();
            this.res = res;
            this.result = (TextView) findViewById(R.id.login_result);
        }

        @Override
        public void run() {
            this.result.setText(res ? "true" : "false");
        }
    }

}