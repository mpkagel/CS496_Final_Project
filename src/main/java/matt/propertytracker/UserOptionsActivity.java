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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

public class UserOptionsActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpClientTwo;

    private String mUserId;
    private String mFirstname;
    private String mLastname;
    private String mTitleText;

    private TextView mTitle;

    private Button mPropertyListButton;
    private Button mLogoutButton;
    private Button mEditAccountButton;
    private Button mDeleteAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_options);

        mTitle = (TextView) findViewById(R.id.UserOptionsTitle1);

        // HTTP Request, default is GET
        mOkHttpClient = new OkHttpClient();
        String Url = HostUrl + "/user/";
        HttpUrl reqUrl = HttpUrl.parse(Url);
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Get user id by turning response String into JSONObject
                String r = response.body().string();
                try {
                    JSONObject j = new JSONObject(r);
                    mUserId = j.getString("id");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                // HTTP request, default is GET
                // Make GET request to specific user URL
                mOkHttpClientTwo = new OkHttpClient();
                String UrlTwo = HostUrl + "/user/" + mUserId;
                HttpUrl reqUrlTwo = HttpUrl.parse(UrlTwo);
                Request requestTwo = new Request.Builder()
                        .url(reqUrlTwo)
                        .build();
                mOkHttpClientTwo.newCall(requestTwo).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call callTwo, IOException eTwo) {
                        eTwo.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call callTwo, Response responseTwo) throws IOException {
                        // Get user first name and last name from response by turning
                        // String into a JSONObject.
                        // Take first name and last name and update User Options heading text.
                        String r = responseTwo.body().string();
                        try {
                            JSONObject j = new JSONObject(r);
                            mFirstname = j.getString("first_name");
                            mLastname = j.getString("last_name");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        mTitleText = mFirstname + " " + mLastname + "'s Account";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTitle.setText(mTitleText);
                            }
                        });
                    }
                });
            }
        });

        mPropertyListButton = (Button) findViewById(R.id.PropertyListButton1);
        mPropertyListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserOptionsActivity.this, PropertyListActivity.class);
                startActivity(intent);
            }
        });

        mLogoutButton = (Button) findViewById(R.id.LogoutButton1);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserOptionsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mEditAccountButton = (Button) findViewById(R.id.EditAccountButton1);
        mEditAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserOptionsActivity.this, EditAccountActivity.class);
                startActivity(intent);
            }
        });

        mDeleteAccountButton = (Button) findViewById(R.id.DeleteAccountButton1);
        mDeleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HTTP request, default is GET
                // Get the user currently logged in
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/user/";
                HttpUrl reqUrl = HttpUrl.parse(Url);
                Request request = new Request.Builder()
                        .url(reqUrl)
                        .build();
                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // Get user id from response by turning String into a JSONObject
                        String r = response.body().string();
                        try {
                            JSONObject j = new JSONObject(r);
                            mUserId = j.getString("id");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        // HTTP request, DELETE to
                        // specific user URL
                        mOkHttpClientTwo = new OkHttpClient();
                        String UrlTwo = HostUrl + "/user/" + mUserId;
                        HttpUrl reqUrlTwo = HttpUrl.parse(UrlTwo);
                        Request requestTwo = new Request.Builder()
                                .url(reqUrlTwo)
                                .delete()
                                .build();
                        mOkHttpClientTwo.newCall(requestTwo).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call callTwo, IOException eTwo) {
                                eTwo.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call callTwo, Response responseTwo) throws IOException {
                                // Go back to the Main activity after user is deleted
                                Intent intent = new Intent(UserOptionsActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }
}
