package com.ivanzhur.tapblack;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;


public class MainActivity extends Activity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static int RC_SIGN_IN = 9001;
    static int RC_SIGN_IN_LB = 9002;
    static int RC_SIGN_IN_ACH = 9003;
    static GoogleApiClient mGoogleApiClient;
    static int connectionMode;
    private boolean mResolvingConnectionFailure = false;
    static boolean mSignInClicked = false;

    String TAG = "MainActivity";

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        connectionMode = RC_SIGN_IN; // Just sign in

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
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }
    
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.sign_in_button: {
                mSignInClicked = true;
                mGoogleApiClient.connect();
            } break;
            case R.id.sign_out_button: {
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();

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
                if (mGoogleApiClient.isConnected())
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                            App.LEADERBOARD_ID_CLASSIC), App.REQUEST_LEADERBOARD);
                else if (!mGoogleApiClient.isConnecting()) {
                    connectionMode = RC_SIGN_IN_LB; // Show leaderboard when connected
                    mSignInClicked = true;
                    mGoogleApiClient.connect();
                }
            } break;
            case R.id.achievementsButton: {
                if (mGoogleApiClient.isConnected())
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), App.REQUEST_ACHIEVEMENTS);
                else if (!mGoogleApiClient.isConnecting()) {
                    connectionMode = RC_SIGN_IN_ACH; // Show achievements when connected
                    mSignInClicked = true;
                    mGoogleApiClient.connect();
                }
            } break;
        }

        if (intent != null) startActivity(intent);
    }

    // region Play Game services connection
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected: connected");
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        if (connectionMode == RC_SIGN_IN_LB){ // Show leaderboard
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    App.LEADERBOARD_ID_CLASSIC), App.REQUEST_LEADERBOARD);
        }
        else if (connectionMode == RC_SIGN_IN_ACH){ // Show achievements
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), App.REQUEST_ACHIEVEMENTS);
        }

        connectionMode = RC_SIGN_IN;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.toString());

        if (mResolvingConnectionFailure) return;

        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    connectionMode, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
                connectionMode = RC_SIGN_IN;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + intent);

        if (requestCode == RC_SIGN_IN || requestCode == RC_SIGN_IN_LB || requestCode == RC_SIGN_IN_ACH) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in failed
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.fail);
            }
        }
    }
    //endregion
}
