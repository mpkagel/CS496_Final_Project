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

public class EditAccountActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpClientTwo;
    private OkHttpClient mOkHttpClientThree;

    private String mUserId;
    private String mFirstName;
    private String mLastName;
    private String mZip;
    private String mOccupation;

    private EditText mFirstNameInput;
    private EditText mLastNameInput;
    private EditText mZipInput;
    private EditText mOccupationInput;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Button mEditAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Make HTTP request, default is GET
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
                // Get response, assign to String
                String r = response.body().string();
                try {
                    // Turn String into JSONObject
                    JSONObject j = new JSONObject(r);
                    // Get user id out of JSONObject
                    mUserId = j.getString("id");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                // Make a subsequent HTTP request within callback
                // Take user id and get user information
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
                        // Get response, assign to String
                        String r = responseTwo.body().string();
                        try {
                            // Turn String into JSONObject
                            JSONObject j = new JSONObject(r);
                            // Get user information from JSONObject
                            mFirstName = j.getString("first_name");
                            mLastName = j.getString("last_name");
                            mZip = j.getString("zip_code");
                            mOccupation = j.getString("occupation");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        // Populate EditText fields with user information
                        mFirstNameInput = (EditText) findViewById(R.id.EditUserInput1);
                        mLastNameInput = (EditText) findViewById(R.id.EditUserInput2);
                        mZipInput = (EditText) findViewById(R.id.EditUserInput3);
                        mOccupationInput = (EditText) findViewById(R.id.EditUserInput4);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mFirstNameInput.setText(mFirstName);
                                mLastNameInput.setText(mLastName);
                                mZipInput.setText(mZip);
                                mOccupationInput.setText(mOccupation);
                            }
                        });
                    }
                });
            }
        });

        mEditAccountButton = (Button) findViewById(R.id.EditButton1);
        mEditAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HTTP request, PATCH
                // Send altered user information to back end to
                // get updated in the NDB
                mOkHttpClientThree = new OkHttpClient();
                String UrlThree = HostUrl + "/user/" + mUserId;
                HttpUrl reqUrlThree = HttpUrl.parse(UrlThree);

                mFirstName = ((EditText) findViewById(R.id.EditUserInput1)).getText().toString();
                mLastName = ((EditText) findViewById(R.id.EditUserInput2)).getText().toString();
                mZip = ((EditText) findViewById(R.id.EditUserInput3)).getText().toString();
                mOccupation = ((EditText) findViewById(R.id.EditUserInput4)).getText().toString();

                String json = "{" +
                        "\"first\":" + "\"" + mFirstName + "\", " +
                        "\"last\":" + "\"" + mLastName + "\", " +
                        "\"zip\":" + "\"" + mZip + "\", " +
                        "\"occupation\":" + "\"" + mOccupation + "\"" +
                        "}";
                RequestBody body = RequestBody.create(JSON, json);
                Request requestThree = new Request.Builder()
                        .url(reqUrlThree)
                        .patch(body)
                        .build();
                mOkHttpClientThree.newCall(requestThree).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call callThree, IOException eThree) {
                        eThree.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call callThree, Response responseThree) throws IOException {
                        Intent intent = new Intent(EditAccountActivity.this, UserOptionsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}