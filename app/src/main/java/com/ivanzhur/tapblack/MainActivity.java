package com.ivanzhur.tapblack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
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
    public static final String SOUND = "com.ivanzhur.tapblack.sound";
    public static final String PARAMETERS = "com.ivanzhur.tapblack.parameters";
    public static final String HIGH_SCORES = "com.ivanzhur.tapblack.highScores";
    public static final String SOUND_BLUE_TILES = "soundBlueTiles";
    public static final String SOUND_WHITE_TILES = "soundWhiteTiles";
    public static final String SOUND_BLUE_SELECTED = "soundBlueSelected";
    public static final String SOUND_WHITE_SELECTED = "soundWhiteSelected";
    public static final String TILE_COLOR = "tileColor";
    public static final String SELECTED_COLOR = "selectedColor";
    static SharedPreferences sound, parameters, highScores;
    static SharedPreferences.Editor editorSound, editorParameters, editorHighScores;
    static AudioManager audioManager;
    static SoundPool soundPool;
    static int soundIdBlue, soundIdWhite;
    static boolean playWhenLoaded;
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
        setSoundIfNotExist();
        setColorIfNotExist();

        highScores = getSharedPreferences(HIGH_SCORES, MODE_PRIVATE);
        editorHighScores = highScores.edit();
        editorHighScores.apply();
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

    @SuppressWarnings("deprecation")
    public void setSoundIfNotExist(){
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        sound = context.getSharedPreferences(SOUND, MODE_PRIVATE);
        editorSound = sound.edit();
        if (!sound.contains(SOUND_BLUE_TILES)){
            editorSound.putInt(SOUND_BLUE_TILES, R.raw.click_blue_1);
            editorSound.putInt(SOUND_WHITE_TILES, R.raw.click_white_1);
            editorSound.putInt(SOUND_BLUE_SELECTED, 1);
            editorSound.putInt(SOUND_WHITE_SELECTED, 1);
            editorSound.apply();
        }

        int soundRawIdBlue = sound.getInt(SOUND_BLUE_TILES, 0);
        int soundRawIdWhite = sound.getInt(SOUND_WHITE_TILES, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        else { // Old APIs
            soundPool = new SoundPool(2, AudioManager.STREAM_DTMF, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (playWhenLoaded) playSound(sampleId);
                playWhenLoaded = false;
            }
        });

        playWhenLoaded = false;
        if (soundRawIdBlue != 0) soundIdBlue = soundPool.load(context, soundRawIdBlue, 1);
        else soundIdBlue = -1;
        if (soundRawIdWhite != 0) soundIdWhite = soundPool.load(context, soundRawIdWhite, 1);
        else soundIdWhite = -1;
    }

    public static void reloadAndPlay(int id, int resId){
        soundPool.unload(id);
        if (resId == 0) return;
        playWhenLoaded = true;
        if (id == MainActivity.soundIdBlue) MainActivity.soundIdBlue = soundPool.load(context, resId, 1);
        if (id == MainActivity.soundIdWhite) MainActivity.soundIdWhite = soundPool.load(context, resId, 1);
    }

    public static void playSound(int soundId){
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actVolume / maxVolume;
        if (soundId != -1) soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    public void setColorIfNotExist(){
        parameters = getSharedPreferences(PARAMETERS, MODE_PRIVATE);
        editorParameters = parameters.edit();
        if (!parameters.contains(TILE_COLOR)){
            editorParameters.putString(TILE_COLOR, "#1248e6");
            editorParameters.putInt(SELECTED_COLOR, 0);
            editorParameters.apply();
        }
    }
}
