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

public class NewUserActivity extends AppCompatActivity {
    //private static final String HostUrl = "http://10.0.2.2:8080";
    private static final String HostUrl = "https://propertytracker2745.appspot.com";
    private static final String TAG = "PropertyTracker";

    private OkHttpClient mOkHttpClient;

    private String mEmail;
    private String mPassword;
    private String mFirstname;
    private String mLastname;
    private String mZip;
    private String mOccupation;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Button mNewUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        mNewUserButton = (Button) findViewById(R.id.NewUserButton1);
        mNewUserButton.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                mEmail = ((EditText) findViewById(R.id.NewUserInput1)).getText().toString();
                mPassword = ((EditText) findViewById(R.id.NewUserInput2)).getText().toString();
                mFirstname = ((EditText) findViewById(R.id.NewUserInput3)).getText().toString();
                mLastname = ((EditText) findViewById(R.id.NewUserInput4)).getText().toString();
                mZip = ((EditText) findViewById(R.id.NewUserInput5)).getText().toString();
                mOccupation = ((EditText) findViewById(R.id.NewUserInput6)).getText().toString();

                String json = "{" +
                        "\"email\":" + "\"" + mEmail + "\", " +
                        "\"password\":" + "\"" + mPassword + "\", " +
                        "\"first\":" + "\"" + mFirstname + "\", " +
                        "\"last\":" + "\"" + mLastname + "\", " +
                        "\"zip\":" + "\"" + mZip + "\", " +
                        "\"occupation\":" + "\"" + mOccupation + "\"" +
                        "}";
                RequestBody body = RequestBody.create(JSON, json);
                // HTTP request, POST request to make new user
                mOkHttpClient = new OkHttpClient();
                String Url = HostUrl + "/user/";
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
                        // Get the response, add an error if necessary, and
                        // go back to the Main activity.
                        String r = response.body().string();
                        Integer HTTPStatus = new Integer(response.code());
                        Intent intent = new Intent(NewUserActivity.this, MainActivity.class);
                        if (HTTPStatus >= 400) {
                            // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
                            intent.putExtra("ERROR_MESSAGE", r);
                        }
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
