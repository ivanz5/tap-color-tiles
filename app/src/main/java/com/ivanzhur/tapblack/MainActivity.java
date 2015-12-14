package com.ivanzhur.tapblack;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;


public class MainActivity extends BaseGameActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("95FD4A0A356D8AEFEABC5D6262F89B0B")
                .addTestDevice("7534F3BBAEB9896C6078C99653ECDF79")
                .addTestDevice("6D96749D2EA908732F239D4E1C209B11")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.playClassicButton).setOnClickListener(this);
        findViewById(R.id.playTimeButton).setOnClickListener(this);
        findViewById(R.id.settingsButton).setOnClickListener(this);
        findViewById(R.id.leaderboardButton).setOnClickListener(this);
        findViewById(R.id.rulesButton).setOnClickListener(this);
        findViewById(R.id.achievementsButton).setOnClickListener(this);

        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.sign_in_button: {
                beginUserInitiatedSignIn();
            } break;
            case R.id.sign_out_button: {
                signOut();
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            } break;

            case R.id.playClassicButton:
                intent = new Intent(this, PlayClassic.class);
                break;
            case R.id.playTimeButton:
                intent = new Intent(this, PlayTime.class);
                break;
            case R.id.settingsButton:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.rulesButton:
                intent = new Intent(this, RulesActivity.class);
                break;

            case R.id.leaderboardButton: {
                if (getApiClient().isConnected())
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                            App.LEADERBOARD_ID_CLASSIC), App.REQUEST_LEADERBOARD);
                else if (!getApiClient().isConnecting())
                    getApiClient().connect();
            } break;
            case R.id.achievementsButton: {
                if (getApiClient().isConnected())
                    startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), App.REQUEST_ACHIEVEMENTS);
                else if (!getApiClient().isConnecting())
                    getApiClient().connect();
            } break;
        }

        if (intent != null) startActivity(intent);
    }

    public void onSignInSucceeded() {
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onSignInFailed() {
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    }
}
