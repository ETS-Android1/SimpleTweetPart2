package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_lENGTH = 280;

    EditText etCompose;
    Button btnTweet;
    TextView tvCounter;
    TwitterClient client;
    String draftText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ComposeActivity.this);
        String savedDraft = pref.getString("draft", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        tvCounter = findViewById(R.id.tvCounter);

        if (savedDraft!="") {
            etCompose.setText(savedDraft);
            tvCounter.setText(savedDraft.length()+" /280");
            draftText = savedDraft;
        }



        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvCounter.setText(s.toString().length()+" /280");
                if (s.toString().length() > MAX_TWEET_lENGTH) {
                    btnTweet.setBackgroundColor(Color.RED);
                    tvCounter.setTextColor(Color.RED);
                }
                draftText = s.toString();
            }
        });



        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_lENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_SHORT).show();
                    return;
                }
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            draftText = "";
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Publish Tweet: "+ tweet.body);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                        finish();
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ComposeActivity.this);
        if (draftText.length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Save Tweet?")
                    .setMessage("You can save this to send later.")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("draft",draftText);
                            edit.commit();
                            draftText="";
                            finish();
                        }

                    })
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("draft","");
                            edit.commit();
                            draftText="";
                            finish();
                        }

                    })
                    .show();
        }
        else {
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("draft","");
            edit.commit();
            draftText="";
            finish();
        }

    }


}