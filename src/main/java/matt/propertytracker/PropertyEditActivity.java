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

public class PropertyEditActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;

    private String mPropId;
    private String mPropType;
    private String mPropValue;
    private String mPropAcreage;
    private String mPropLocation;

    private String mPropTypeToSubmit;
    private String mPropValueToSubmit;
    private String mPropAcreageToSubmit;
    private String mPropLocationToSubmit;

    private EditText mTypeBox;
    private EditText mValueBox;
    private EditText mAcreageBox;
    private EditText mLocationBox;

    private Button mPropertyEditButton;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_edit);

        mPropId = getIntent().getStringExtra("PROP_ID");
        mPropType = getIntent().getStringExtra("PROP_TYPE");
        mPropValue = getIntent().getStringExtra("PROP_VALUE");
        mPropAcreage = getIntent().getStringExtra("PROP_ACREAGE");
        mPropLocation = getIntent().getStringExtra("PROP_LOCATION");

        mTypeBox = (EditText) findViewById(R.id.property_list_item_type_text);
        mValueBox = (EditText) findViewById(R.id.property_list_item_value_text);
        mAcreageBox = (EditText) findViewById(R.id.property_list_item_acreage_text);
        mLocationBox = (EditText) findViewById(R.id.property_list_item_location_text);

        // Set EditText field information to what was passed in via the Select Property activity
        mTypeBox.setText(mPropType);
        mValueBox.setText(mPropValue);
        mAcreageBox.setText(mPropAcreage);
        mLocationBox.setText(mPropLocation);

        mPropertyEditButton = (Button) findViewById(R.id.PropertyEditButton1);
        mPropertyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPropTypeToSubmit = ((EditText) findViewById(R.id.property_list_item_type_text)).getText().toString();
                mPropValueToSubmit = ((EditText) findViewById(R.id.property_list_item_value_text)).getText().toString();
                mPropAcreageToSubmit = ((EditText) findViewById(R.id.property_list_item_acreage_text)).getText().toString();
                mPropLocationToSubmit = ((EditText) findViewById(R.id.property_list_item_location_text)).getText().toString();

                // HTTP request, default is PATCH
                // Update specific property at property id
                // in NDB
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/property/" + mPropId;
                HttpUrl reqUrl = HttpUrl.parse(Url);

                String json = "{" +
                        "\"type\":" + "\"" + mPropTypeToSubmit + "\", " +
                        "\"value\":" + "\"" + mPropValueToSubmit + "\", " +
                        "\"acreage\":" + "\"" + mPropAcreageToSubmit + "\", " +
                        "\"location\":" + "\"" + mPropLocationToSubmit + "\"" +
                        "}";
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(reqUrl)
                        .patch(body)
                        .build();
                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Intent intent = new Intent(PropertyEditActivity.this, PropertyListActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
