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

public class SelectPropertyActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;

    private String mPropId;
    private String mPropType;
    private String mPropValue;
    private String mPropAcreage;
    private String mPropLocation;

    private TextView mTypeBox;
    private TextView mValueBox;
    private TextView mAcreageBox;
    private TextView mLocationBox;

    private Button mPropertyEditButton;
    private Button mPropertyDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_property);

        mPropId = getIntent().getStringExtra("PROP_ID");
        mPropType = getIntent().getStringExtra("PROP_TYPE");
        mPropValue = getIntent().getStringExtra("PROP_VALUE");
        mPropAcreage = getIntent().getStringExtra("PROP_ACREAGE");
        mPropLocation = getIntent().getStringExtra("PROP_LOCATION");

        mTypeBox = (TextView) findViewById(R.id.property_list_item_type_text);
        mValueBox = (TextView) findViewById(R.id.property_list_item_value_text);
        mAcreageBox = (TextView) findViewById(R.id.property_list_item_acreage_text);
        mLocationBox = (TextView) findViewById(R.id.property_list_item_location_text);

        // Set TextView field information to what was passed in via the Property List activity
        mTypeBox.setText(mPropType);
        mValueBox.setText(mPropValue);
        mAcreageBox.setText(mPropAcreage);
        mLocationBox.setText(mPropLocation);

        mPropertyEditButton = (Button) findViewById(R.id.PropertyEditButton1);
        mPropertyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Passes information on to the Property Edit activity
                Intent intent = new Intent(SelectPropertyActivity.this, PropertyEditActivity.class);
                intent.putExtra("PROP_ID", mPropId);
                intent.putExtra("PROP_TYPE", mPropType);
                intent.putExtra("PROP_VALUE", mPropValue);
                intent.putExtra("PROP_ACREAGE", mPropAcreage);
                intent.putExtra("PROP_LOCATION", mPropLocation);
                startActivity(intent);
            }
        });

        mPropertyDeleteButton = (Button) findViewById(R.id.PropertyDeleteButton1);
        mPropertyDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HTTP request, DELETE to
                // property id URL
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/property/" + mPropId;
                HttpUrl reqUrl = HttpUrl.parse(Url);
                Request request = new Request.Builder()
                        .url(reqUrl)
                        .delete()
                        .build();
                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Intent intent = new Intent(SelectPropertyActivity.this, PropertyListActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
