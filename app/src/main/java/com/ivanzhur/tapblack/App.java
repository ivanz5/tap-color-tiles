package com.ivanzhur.tapblack;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class App extends Application {

    public static final String SOUND = "com.ivanzhur.tapblack.sound";
    public static final String PARAMETERS = "com.ivanzhur.tapblack.parameters";
    public static final String HIGH_SCORES = "com.ivanzhur.tapblack.highScores";

    // Variables for sound and color
    public static final String TILE_COLOR = "tileColor";
    public static final String SELECTED_COLOR = "selectedColor";
    public static final String SOUND_BLUE_TILES = "soundBlueTiles";
    public static final String SOUND_WHITE_TILES = "soundWhiteTiles";
    public static final String SOUND_BLUE_SELECTED = "soundBlueSelected";
    public static final String SOUND_WHITE_SELECTED = "soundWhiteSelected";

    static String LEADERBOARD_ID_CLASSIC;
    static final int REQUEST_LEADERBOARD = 1;
    static final int REQUEST_ACHIEVEMENTS = 1;

    static SharedPreferences sound, parameters, highScores;
    static SharedPreferences.Editor editorSound, editorParameters, editorHighScores;
    static AudioManager audioManager;
    static SoundPool soundPool;
    static int soundIdBlue, soundIdWhite;
    static boolean playWhenLoaded;
    private static boolean soundIdsReset = false;
    static Context context;

    @Override
    public void onCreate(){
        super.onCreate();

        context = this;
        LEADERBOARD_ID_CLASSIC = getResources().getString(R.string.leaderboard_classic_mode);

        setSharedPreferences();
        setSound();
        setColor();
    }

    private void setSharedPreferences(){
        sound = getSharedPreferences(SOUND, MODE_PRIVATE);
        parameters = getSharedPreferences(PARAMETERS, MODE_PRIVATE);
        highScores = getSharedPreferences(HIGH_SCORES, MODE_PRIVATE);
        editorSound = sound.edit();
        editorParameters = parameters.edit();
        editorHighScores = highScores.edit();
        editorSound.apply();
        editorParameters.apply();
        editorHighScores.apply();
    }

    @SuppressWarnings("deprecation")
    private void setSound(){
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (!App.sound.contains(SOUND_BLUE_TILES)){
            App.editorSound.putInt(SOUND_BLUE_TILES, R.raw.click_blue_1);
            App.editorSound.putInt(SOUND_WHITE_TILES, R.raw.click_white_1);
            App.editorSound.putInt(SOUND_BLUE_SELECTED, 1);
            App.editorSound.putInt(SOUND_WHITE_SELECTED, 1);
            App.editorSound.apply();
        }

        int soundRawIdBlue = App.sound.getInt(SOUND_BLUE_TILES, 0);
        int soundRawIdWhite = App.sound.getInt(SOUND_WHITE_TILES, 0);

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

        // FIXME: need to test
        // New version of app was installed. Possible changes in sound raw ids
        // Call to soundPool.load() can cause NullPointerException
        try {
            if (soundRawIdBlue != 0) soundIdBlue = soundPool.load(this, soundRawIdBlue, 1);
            else soundIdBlue = -1;
            if (soundRawIdWhite != 0) soundIdWhite = soundPool.load(this, soundRawIdWhite, 1);
            else soundIdWhite = -1;
        }
        catch (Exception ex){ // Try to reset sound raw ids to defaults
            if (!soundIdsReset) resetSoundIds();
            else { // Exception thrown again. Resetting didn't help. Just set sounds to none
                soundIdBlue = -1;
                soundIdWhite = -1;
            }
        }
    }

    private void resetSoundIds(){
        int soundRawIdBlue = R.raw.click_blue_1;
        int soundRawIdWhite = R.raw.click_white_1;
        editorSound.putInt(SOUND_BLUE_TILES, soundRawIdBlue);
        editorSound.putInt(SOUND_WHITE_TILES, soundRawIdWhite);
        editorSound.apply();
        soundIdsReset = true;
        setSound();
    }

    public static void reloadAndPlay(int id, int resId){
        soundPool.unload(id);
        if (resId == 0) return;
        playWhenLoaded = true;
        if (id == soundIdBlue) soundIdBlue = soundPool.load(context, resId, 1);
        if (id == soundIdWhite) soundIdWhite = soundPool.load(context, resId, 1);
    }

    public static void playSound(int soundId){
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actVolume / maxVolume;
        if (soundId != -1) soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    public void setColor(){
        if (!App.parameters.contains(TILE_COLOR)){
            App.editorParameters.putString(TILE_COLOR, "#1248e6");
            App.editorParameters.putInt(SELECTED_COLOR, 0);
            App.editorParameters.apply();
        }
    }
}
