package matt.propertytracker;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PropertyListActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpClientTwo;

    private String mUserId;

    private Button mPropertyAddButton;
    private Button mPropertyBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_list);

        // HTTP request, default is GET
        // Get user currently logged in
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
                // Get user id from response String by turning into a JSONObject
                String r = response.body().string();
                try {
                    JSONObject j = new JSONObject(r);
                    mUserId = j.getString("id");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                // HTTP request, default is GET
                // Get all properties that user is the owner of
                // Again "owner" refers more to an association with
                // the property
                mOkHttpClientTwo = new OkHttpClient();
                String UrlTwo = HostUrl + "/user/" + mUserId + "/properties";
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
                        // Get response and turn into a String
                        String r = responseTwo.body().string();
                        try {
                            // Turn the response String into a JSONArray
                            JSONArray items = new JSONArray(r);
                            // Make a List of Hash maps that map Strings to other Strings
                            List<Map<String,String>> properties = new ArrayList<Map<String, String>>();
                            for (int i = 0; i < items.length(); i++) {
                                // Make a Hash Map for each property and add to the List
                                HashMap<String, String> m = new HashMap<String, String>();
                                m.put("type", items.getJSONObject(i).getString("type"));
                                m.put("value", items.getJSONObject(i).getString("value"));
                                m.put("acreage", items.getJSONObject(i).getString("acreage"));
                                m.put("location", items.getJSONObject(i).getString("location"));
                                m.put("id", items.getJSONObject(i).getString("id"));
                                properties.add(m);
                            }
                            // Take the List of properties and run through a custom adapter
                            final MyListAdapter propAdapter = new MyListAdapter(
                                    PropertyListActivity.this,
                                    properties
                            );
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Set the adapter to a ListView in the layout for this activity
                                    ((ListView)findViewById(R.id.property_item_list)).setAdapter(propAdapter);
                                }
                            });
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });

        mPropertyBackButton = (Button) findViewById(R.id.BackUserOptionsButton1);
        mPropertyBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyListActivity.this, UserOptionsActivity.class);
                startActivity(intent);
            }
        });

        mPropertyAddButton = (Button) findViewById(R.id.PropertyAddButton1);
        mPropertyAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyListActivity.this, PropertyAddActivity.class);
                startActivity(intent);
            }
        });
    }
}

class MyListAdapter extends ArrayAdapter {
    // The following code was taken and adapted from:
    // http://android.vexedlogic.com/2011/04/02/android-lists-listactivity-and-listview-ii-%E2%80%93-custom-adapter-and-list-item-view/
    // https://www.youtube.com/watch?v=ZEEYYvVwJGY (Android List view with clickable button)

    // This custom adapter inherits from ArrayAdapter
    private static final String TAG = "PropertyTracker";

    private final List<Map<String,String>> properties;
    private String mPropId;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Adapter constructor
    public MyListAdapter(Context context, List<Map<String,String>> objects) {
        super(context, R.layout.list_property_item, objects);
        properties = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        PropertyView propView = null;

        // I'm not entirely sure how the following block of code is working with
        // rowViews and propViews.
        // https://developer.android.com/reference/android/view/LayoutInflater -> explains some
        // If there is a rowView that already exists, then that
        // gets recycled. If there is no rowView, then one gets built using the
        // LayoutInflater object. Each rowView is populated with data from each property
        // in order using the int position index. I do not know how int position gets
        // incremented. I suspect that the setAdapter method of the ListView is
        // calling getView with incremented values of position, but I don't know for sure.
        if(rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.list_property_item, null);

            propView = new PropertyView();
            propView.type = (TextView) rowView.findViewById(R.id.property_list_item_type_text);
            propView.value = (TextView) rowView.findViewById(R.id.property_list_item_value_text);
            propView.acreage = (TextView) rowView.findViewById(R.id.property_list_item_acreage_text);
            propView.location = (TextView) rowView.findViewById(R.id.property_list_item_location_text);
            propView.propSelf = (Button) rowView.findViewById(R.id.PropertySelectButton1);

            rowView.setTag(propView);
        } else {
            propView = (PropertyView) rowView.getTag();
        }

        Map<String,String> currentListing = properties.get(position);
        propView.type.setText(currentListing.get("type"));
        propView.value.setText(currentListing.get("value"));
        propView.acreage.setText(currentListing.get("acreage"));
        propView.location.setText(currentListing.get("location"));
        mPropId = currentListing.get("id");

        // This button stores the property id of each property in the List as setAdapter cycles
        // through the List. That way each button can get the correct information and pass it
        // along to the Select Property activity.
        propView.propSelf.setOnClickListener(new View.OnClickListener () {
            //private static final String HostUrl = "http://10.0.2.2:8080";
            private static final String HostUrl = "https://propertytracker2745.appspot.com";
            private OkHttpClient mOkHttpClient;
            private final String idInsideListener = mPropId;

            @Override
            public void onClick(View v) {
                // HTTP request, default is GET
                // Get property at property id
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/property/" + idInsideListener;
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
                        String r = response.body().string();
                        String mPropIdToPass = "None";
                        String mPropTypeToPass = "None";
                        String mPropValueToPass = "None";
                        String mPropAcreageToPass = "None";
                        String mPropLocationToPass = "None";

                        try {
                            JSONObject j = new JSONObject(r);
                            mPropIdToPass = j.getString("id");
                            mPropTypeToPass = j.getString("type");
                            mPropValueToPass = j.getString("value");
                            mPropAcreageToPass = j.getString("acreage");
                            mPropLocationToPass = j.getString("location");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        Intent intent = new Intent(getContext(), SelectPropertyActivity.class);
                        intent.putExtra("PROP_ID", mPropIdToPass);
                        intent.putExtra("PROP_TYPE", mPropTypeToPass);
                        intent.putExtra("PROP_VALUE", mPropValueToPass);
                        intent.putExtra("PROP_ACREAGE", mPropAcreageToPass);
                        intent.putExtra("PROP_LOCATION", mPropLocationToPass);

                        getContext().startActivity(intent);
                    }
                });
            }
        });

        return rowView;
    }

    // Basic format of each view in rowView for each element of the custom adapter.
    protected static class PropertyView {
        protected TextView type;
        protected TextView value;
        protected TextView acreage;
        protected TextView location;
        protected Button propSelf;
    }
}