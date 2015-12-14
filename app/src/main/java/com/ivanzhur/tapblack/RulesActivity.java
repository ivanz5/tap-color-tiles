package com.ivanzhur.tapblack;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class RulesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.activity_rules);
    }

    private boolean isActivityStarted(Intent activityIntent)
    {
        try
        {
            startActivity(activityIntent);
            return true;
        }
        catch (ActivityNotFoundException ex)
        {
            return false;
        }
    }

    public void onRateClick(View view)
    {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW);
        rateIntent.setData(Uri.parse("market://details?id=com.ivanzhur.tapblack"));
        if (!isActivityStarted(rateIntent)) {
            rateIntent.setData(Uri.parse("https://play.google.com/store/apps/details?com.ivanzhur.tapblack"));
            if (!isActivityStarted(rateIntent))
                Toast.makeText(getApplicationContext(), getString(R.string.could_not_open_play_store), Toast.LENGTH_SHORT).show();
        }
    }

    public void onFeedbackClick(View view)
    {
        final Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
        feedbackIntent.setType("text/html");
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.mail_feedback_email)});
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        startActivityForResult(Intent.createChooser(feedbackIntent, getString(R.string.mail_feedback_chooser_title)), 1);
    }
}
