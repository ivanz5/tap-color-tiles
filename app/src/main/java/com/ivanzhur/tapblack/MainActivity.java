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

    private static String LEADERBOARD_ID_CLASSIC;
    private static final int REQUEST_LEADERBOARD = 1;
    private static final int REQUEST_ACHIEVEMENTS = 1;

    // Variables for sound and color
    public static final String TILE_COLOR = "tileColor";
    public static final String SELECTED_COLOR = "selectedColor";
    static Context context;

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

        LEADERBOARD_ID_CLASSIC = getResources().getString(R.string.leaderboard_classic_mode);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        findViewById(R.id.sign_out_button).setVisibility(View.GONE);

        context = this;
        setColorIfNotExist();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            beginUserInitiatedSignIn();
        }
        else if (view.getId() == R.id.sign_out_button) {
            signOut();
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
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

    public void onPlayClassicBtnClick(View view){
        Intent playClassicIntent = new Intent(getApplicationContext(), PlayClassic.class);
        startActivity(playClassicIntent);
    }

    public void onPlayTimeBtnClick(View view){
        Intent playTimeIntent = new Intent(getApplicationContext(), PlayTime.class);
        startActivity(playTimeIntent);
    }

    public void onSettingsBtnClick(View view){
        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void onRulesBtnClick(View view){
        Intent rulesIntent = new Intent(getApplicationContext(), RulesActivity.class);
        startActivity(rulesIntent);
    }

    public void onLeaderboardButtonClick(View view){
        if (getApiClient().isConnected())
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                LEADERBOARD_ID_CLASSIC), REQUEST_LEADERBOARD);
        else if (!getApiClient().isConnecting())
            getApiClient().connect();
    }

    public void onAchievementsButtonClick(View view){
        if (getApiClient().isConnected())
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
        else if (!getApiClient().isConnecting())
            getApiClient().connect();
    }

    public void setColorIfNotExist(){
        if (!App.parameters.contains(TILE_COLOR)){
            App.editorParameters.putString(TILE_COLOR, "#1248e6");
            App.editorParameters.putInt(SELECTED_COLOR, 0);
            App.editorParameters.apply();
        }
    }
}
