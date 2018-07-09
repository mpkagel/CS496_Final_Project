package matt.propertytracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.MediaType;

// This code was taken and adapted from:
// https://gist.github.com/wolfordj/29353e87cebd97fe1cf13c1ae4b3c7fb, specifically -> APIActivity.java
// Also, the post example source at:
// https://raw.githubusercontent.com/square/okhttp/master/samples/guide/src/main/java/okhttp3/guide/PostExample.java
// was very helpful for handling the json RequestBody object in Java.
// And the OkHttp documentation at: http://square.github.io/okhttp/3.x/okhttp/

public class MainActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpClientTwo;
    private OkHttpClient mOkHttpClientThree;

    private String mUserEmail;
    private String mUserPassword;
    private String mUserId;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Button mLoginButton;
    private Button mNewUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // https://xjaphx.wordpress.com/2011/07/13/auto-close-dialog-after-a-specific-time/
        // If there is an error message passed in from another activity because something
        // went wrong, it gets briefly displayed. The errors displayed here relate to
        // improper user signup or user signin.
        if (getIntent().getStringExtra("ERROR_MESSAGE") != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String s = getIntent().getStringExtra("ERROR_MESSAGE");
            builder.setMessage(s);
            final AlertDialog dialog = builder.create();

            dialog.show();

            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    dialog.dismiss();
                    t.cancel();
                }
            }, 1000);
        }

        // HTTP request, default is GET
        mOkHttpClientThree = new OkHttpClient();
        String UrlThree = HostUrl;
        HttpUrl reqUrlThree = HttpUrl.parse(UrlThree);
        Request requestThree = new Request.Builder()
                .url(reqUrlThree)
                .build();
        mOkHttpClientThree.newCall(requestThree).enqueue(new Callback() {
            @Override
            public void onFailure(Call callThree, IOException eThree) {
                eThree.printStackTrace();
            }

            @Override
            public void onResponse(Call callThree, Response responseThree) throws IOException {
                // There is no response action, the purpose is to ensure that the back end
                // is logged out.
                ;
            }
        });

        mLoginButton = (Button) findViewById(R.id.LoginButton1);
        mLoginButton.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                mUserEmail = ((EditText) findViewById(R.id.LoginInput1)).getText().toString();
                mUserPassword = ((EditText) findViewById(R.id.LoginInput2)).getText().toString();

                String json = "{" +
                        "\"email\":" + "\"" + mUserEmail + "\", " +
                        "\"password\":" + "\"" + mUserPassword + "\"" +
                        "}";
                RequestBody body = RequestBody.create(JSON, json);
                // HTTP request, POST
                // Send user credentials to back end in an attempt to log in
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/user/login";
                HttpUrl reqUrl = HttpUrl.parse(Url);
                Request request = new Request.Builder()
                        .url(reqUrl)
                        .post(body)
                        .build();
                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String r = response.body().string();
                        Integer HTTPStatus = new Integer(response.code());
                        // If there is an error re-start Main activity with error message
                        if (HTTPStatus >= 400) {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
                            intent.putExtra("ERROR_MESSAGE", r);
                            startActivity(intent);
                        } else {
                            // User has logged in, back end authenticates user
                            Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        // Go to new user sign up
        mNewUserButton = (Button) findViewById(R.id.NewUserSignupButton1);
        mNewUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewUserActivity.class);
                startActivity(intent);
            }
        });
    }
}
