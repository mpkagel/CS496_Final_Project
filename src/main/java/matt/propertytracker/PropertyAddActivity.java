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

public class PropertyAddActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpClientTwo;

    private String mUserId;
    private String mPropType;
    private String mPropValue;
    private String mPropAcreage;
    private String mPropLocation;

    private Button mPropertyAddButton;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_add);

        mPropertyAddButton = (Button) findViewById(R.id.PropertyAddButton2);
        mPropertyAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HTTP request, default is GET
                // Get currently logged in user
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
                        // Get user id by turning response String into a JSONObject
                        String r = response.body().string();
                        try {
                            JSONObject j = new JSONObject(r);
                            mUserId = j.getString("id");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        // Get the values for attributes from the EditText fields
                        mPropType = ((EditText) findViewById(R.id.PropertyAddInput1)).getText().toString();
                        mPropValue = ((EditText) findViewById(R.id.PropertyAddInput2)).getText().toString();
                        mPropAcreage = ((EditText) findViewById(R.id.PropertyAddInput3)).getText().toString();
                        mPropLocation = ((EditText) findViewById(R.id.PropertyAddInput4)).getText().toString();

                        String json = "{" +
                                "\"owner\":" + "\"" + mUserId + "\", " +
                                "\"type\":" + "\"" + mPropType + "\", " +
                                "\"value\":" + "\"" + mPropValue + "\", " +
                                "\"acreage\":" + "\"" + mPropAcreage + "\", " +
                                "\"location\":" + "\"" + mPropLocation + "\"" +
                                "}";
                        RequestBody body = RequestBody.create(JSON, json);
                        // HTTP request, POST to property URL
                        // to make new property owned by user
                        // currently logged in.
                        // Ownership could mean association for this app,
                        // not strictly the titled owner.
                        // For example, a tenant could list properties that
                        // they have stayed in, in the past.
                        mOkHttpClientTwo = new OkHttpClient();
                        String UrlTwo = HostUrl + "/property/";
                        HttpUrl reqUrlTwo = HttpUrl.parse(UrlTwo);
                        Request requestTwo = new Request.Builder()
                                .url(reqUrlTwo)
                                .post(body)
                                .build();
                        mOkHttpClientTwo.newCall(requestTwo).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call callTwo, IOException eTwo) {
                                eTwo.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call callTwo, Response responseTwo) throws IOException {
                                Intent intent = new Intent(PropertyAddActivity.this, PropertyListActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }
}
