package com.ivanzhur.tapblack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class PlayTime extends Activity implements AudioManager.OnAudioFocusChangeListener {

    Random random;
    ArrayList<Button> buttons, chooseButtons;
    Map idColor;
    Button restartButton, shareButton, highScoresButton;

    int time = 0;
    int maxSeconds, maxTiles, seconds, tiles;
    long startTime = 0;
    boolean gameStarted = false;
    CharSequence type = "type";

    TextView timerTextView;
    TextView tilesTextView;
    TextView scoreTextView;
    LinearLayout scoreLayout;
    LinearLayout chooseLayout;

    // Variables for audio
    AudioManager audioManager;
    MediaPlayer mediaPlayerBlue, mediaPlayerWhite;

    // Variables for animation
    final Animation fadeIn = new AlphaAnimation(0, 1);
    final Animation fadeOut = new AlphaAnimation(1, 0);

    // Managing local high score
    public static final String HIGH_SCORES = "highScores";
    public static final String HIGH_SCORE_TIME = "highScoreTime";
    public static final String HIGH_SCORE_TILES = "highScoreTiles";
    public static final String SOUND = "sound";
    public static final String SOUND_BLUE_TILES = "soundBlueTiles";
    public static final String SOUND_WHITE_TILES = "soundWhiteTiles";
    public static boolean SOUND_BLUE_NULL = false;
    public static boolean SOUND_WHITE_NULL = false;
    private SharedPreferences highScores, sound;
    private SharedPreferences.Editor editor;
    final Context context = this;

    // Time
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            if (type == "time")
            {
                time = maxSeconds*1000 - (int)millis;
                int secs = (int) Math.floor(time / 1000);
                int centsec = (int) (time/10 - secs*100);
                if (centsec < 0) centsec = 0;
                timerTextView.setText(String.format("%d.%02d", secs, centsec));

                if (time > 0) timerHandler.postDelayed(this, 10);
                else gameOver(tiles);
            }
            else
            {
                time = (int)millis;
                int secs = (int) Math.floor(time / 1000);
                int centsec = (int) (time/10 - secs*100);
                timerTextView.setText(String.format("%d.%02d", secs, centsec));

                timerHandler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.activity_play_time);

        setAds();
        setVariables(); // id-color map, references to Views
        addChooseButtonsToList(); // Adding buttons to List 'chooseButtons' and 'buttons'
        addButtonsToList();
        setAnimation(); // Setting animations
        setAudio();
        setChooseButtonsOnClickListeners(); // Setting onclick listeners for choose buttons
        setGameButtonsOnClickListeners(); // Setting onclick listeners for tile buttons

        int fb = random.nextInt(4);
        buttons.get(fb).setBackgroundResource(R.drawable.xml_big_button_black);
        idColor.put(buttons.get(fb).getId(), "black");
    }

    public void onAudioFocusChange(int focusChange)
    {
        // Do something based on focus change...
    }

    public void setAds()
    {
        AdView mAdView = (AdView) findViewById(R.id.adViewTime);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("95FD4A0A356D8AEFEABC5D6262F89B0B")
                .addTestDevice("7534F3BBAEB9896C6078C99653ECDF79")
                .addTestDevice("6D96749D2EA908732F239D4E1C209B11")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    public void setVariables()
    {
        idColor = new HashMap<>();
        timerTextView = (TextView)findViewById(R.id.timeModeTimeTV);
        tilesTextView = (TextView)findViewById(R.id.timeModeTilesTV);
        scoreTextView = (TextView)findViewById(R.id.timeModeScoreTV);
        scoreLayout = (LinearLayout)findViewById(R.id.scoreScreenTime);
        chooseLayout = (LinearLayout)findViewById(R.id.chooseScreenTime);
        restartButton = (Button)findViewById(R.id.restartButtonTime);
        shareButton = (Button)findViewById(R.id.shareButtonTime);
        highScoresButton = (Button)findViewById(R.id.highScoresButtonTime);
        random = new Random();
        // Local high score
        highScores = getSharedPreferences(HIGH_SCORES, getApplicationContext().MODE_MULTI_PROCESS);
        sound = getSharedPreferences(SOUND, getApplicationContext().MODE_MULTI_PROCESS);
        editor = highScores.edit();
        // Setting tiles and time values
        tiles = 0;
        time = 0;
    }

    public void setAnimation()
    {
        fadeIn.setDuration(400);
        fadeOut.setDuration(250);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                restartButton.setClickable(false);
                shareButton.setClickable(false);
                highScoresButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                restartButton.setClickable(true);
                shareButton.setClickable(true);
                highScoresButton.setClickable(true);
                scoreLayout.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                chooseLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setAudio()
    {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int idBlue = sound.getInt(SOUND_BLUE_TILES, 0);
        int idWhite = sound.getInt(SOUND_WHITE_TILES, 0);
        if (idBlue != 0) mediaPlayerBlue = MediaPlayer.create(getApplicationContext(), idBlue);
        else SOUND_BLUE_NULL = true;
        if (idWhite != 0) mediaPlayerWhite = MediaPlayer.create(getApplicationContext(), idWhite);
        else SOUND_WHITE_NULL = true;

        int audioFocusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
        }
    }

    public void setChooseButtonsOnClickListeners()
    {
        for (int i=0; i<8; i++)
        {
            final int k = i;
            chooseButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setChooseButtons(false);

                    if (k<4)
                    {
                        type = "time";
                        String buttonText = ((Button) v).getText().toString();
                        maxSeconds = Integer.parseInt(buttonText);
                        maxTiles = -1;
                        timerTextView.setText(maxSeconds + ".00");
                    }
                    else
                    {
                        type = "tile";
                        String buttonText = ((Button) v).getText().toString();
                        maxTiles = Integer.parseInt(buttonText);
                        maxSeconds = -1;
                        tilesTextView.setText(maxTiles + "");
                    }

                    chooseLayout.startAnimation(fadeOut);
                    chooseLayout.setClickable(false);
                }
            });
        }
    }

    public void setGameButtonsOnClickListeners()
    {
        for (int i=0; i<4; i++)
        {
            idColor.put(buttons.get(i).getId(), "white");

            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle button click
                    // If button is black
                    if (idColor.get(v.getId()) == "black") {
                        if (gameStarted == false) // If game not started yet
                        {
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            gameStarted = true;
                        }
                        if (type == "tile") // If type 'tiles'
                        {
                            if (tiles >= maxTiles - 1) gameOver(time);
                            tilesTextView.setText(maxTiles - tiles - 1 + "");
                        }
                        else
                        {
                            tilesTextView.setText(tiles + 1 + "");
                        }

                        // Generate new black tile
                        int nb = random.nextInt(4);
                        while (idColor.get(buttons.get(nb).getId()) == "black")
                            nb = random.nextInt(4);
                        changeState(v);
                        changeState(buttons.get(nb));

                        tiles++;
                        if (!SOUND_BLUE_NULL) mediaPlayerBlue.start();
                    }
                    // If button is white
                    else {
                        v.setBackgroundColor(Color.parseColor("#FF0000"));
                        if (!SOUND_WHITE_NULL) mediaPlayerWhite.start();
                        gameOver(-1);
                    }
                }
            });
        }
    }

    public void changeState(View view)
    {
        if (idColor.get(view.getId()) == "black")
        {
            view.setBackgroundResource(R.drawable.xml_big_button_white);
            idColor.put(view.getId(), "white");
        }
        else
        {
            view.setBackgroundResource(R.drawable.xml_big_button_black);
            idColor.put(view.getId(), "black");
        }
    }

    // Restart button
    public void restartClicked(View view)
    {
        timerTextView.setText("0.00");
        tilesTextView.setText("0");
        scoreLayout.clearAnimation();
        scoreLayout.setClickable(false);
        chooseLayout.setVisibility(View.VISIBLE);
        scoreLayout.setVisibility(View.INVISIBLE);
        for (int i=0; i<4; i++) {
            buttons.get(i).setBackgroundResource(R.drawable.xml_big_button_white);
            idColor.put(buttons.get(i).getId(), "white");
        }
        int rb = random.nextInt(4);
        buttons.get(rb).setBackgroundResource(R.drawable.xml_big_button_black);
        idColor.put(buttons.get(rb).getId(), "black");
        gameStarted = false;
        time = 0;
        tiles = 0;
    }

    // Share button
    public void shareClicked(View view)
    {
        CharSequence text = "I stayed for " + scoreTextView.getText() + " in Tap Black! Can you beat me? http://vk.com/i.zhur";
        if (scoreTextView.getText() == getResources().getString(R.string.fail))
        {
            text = getResources().getString(R.string.i_failed);
        }
        else if (type == "time")
        {
            text = getResources().getString(R.string.i_have_clicked) + tiles +
                    getResources().getString(R.string.tiles_in) + maxSeconds + getResources().getString(R.string.seconds_can_more);
        }
        else
        {
            int secs = (int) Math.floor(time / 1000);
            int centsec = (int) (time/10 - secs*100);
            text = getResources().getString(R.string.i_have_clicked) + maxTiles +
                    getResources().getString(R.string.tiles_just_in) + secs + "." + centsec + getResources().getString(R.string.seconds_can_faster);
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void highScoresClicked(View view)
    {
        setChooseButtons(true);
        Button showButton = (Button)findViewById(R.id.TimeShowHighScore);
        showButton.setClickable(false);
        showButton.setVisibility(View.INVISIBLE);
        restartClicked(view);
    }

    public void showHighScoreClicked(View view)
    {
        setChooseButtons(true);
        view.setClickable(false);
        view.setVisibility(View.INVISIBLE);
    }

    public void hideHighScoreClicked(View view)
    {
        Button showButton = (Button)findViewById(R.id.TimeShowHighScore);
        setChooseButtons(false);
        showButton.setVisibility(View.VISIBLE);
        showButton.setClickable(true);
    }

    public void resetHighScoreClicked(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.reset)
                .setMessage(R.string.reset_all_question);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // If YES -> Remove

                setChooseButtons(false);
                for (int i=0; i<4; i++)
                    if (highScores.contains(HIGH_SCORE_TIME + chooseButtons.get(i).getText()))
                        editor.remove(HIGH_SCORE_TIME + chooseButtons.get(i).getText());
                for (int i=4; i<8; i++)
                    if (highScores.contains(HIGH_SCORE_TILES + chooseButtons.get(i).getText()))
                        editor.remove(HIGH_SCORE_TILES + chooseButtons.get(i).getText());
                editor.apply();

                Button showButton = (Button)findViewById(R.id.TimeShowHighScore);
                showButton.setVisibility(View.VISIBLE);
                showButton.setClickable(true);
            }
        });
        builder.setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void gameOver(int result)
    {
        timerHandler.removeCallbacks(timerRunnable);
        if (result == -1)
        {
            scoreTextView.setText(getResources().getString(R.string.fail));
        }
        else if (type == "tile") // TILES mode
        {
            // Showing score
            int secs = (int) Math.floor(result / 1000);
            int centsec = (int) (result/10 - secs*100);
            scoreTextView.setText(String.format("%d.%02d", secs, centsec) + getResources().getString(R.string.second_sign));

            // Updating high score
            if (highScores.contains(HIGH_SCORE_TILES + maxTiles))
            {
                int currentHigh = highScores.getInt(HIGH_SCORE_TILES + maxTiles, 0);
                if (currentHigh > result) // If need update
                {
                    editor.putInt(HIGH_SCORE_TILES + maxTiles, result);
                    editor.apply();
                }
            }
            else // If not exist
            {
                editor.putInt(HIGH_SCORE_TILES + maxTiles, result);
                editor.apply();
            }
        }
        else // TIME mode
        {
            // Showing score
            scoreTextView.setText(result + "");

            if (highScores.contains(HIGH_SCORE_TIME + maxSeconds))
            {
                int currentHigh = highScores.getInt(HIGH_SCORE_TIME + maxSeconds, 0);
                if (currentHigh < result) // If need update
                {
                    editor.putInt(HIGH_SCORE_TIME + maxSeconds, result);
                    editor.apply();
                }
            }
            else // If not exist
            {
                editor.putInt(HIGH_SCORE_TIME + maxSeconds, result);
                editor.apply();
            }
        }

        scoreLayout.startAnimation(fadeIn);
        scoreLayout.setVisibility(View.VISIBLE);
    }

    public void addChooseButtonsToList()
    {
        chooseButtons = new ArrayList<>();
        chooseButtons.add((Button)findViewById(R.id.TimeChooseSeconds1));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseSeconds2));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseSeconds3));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseSeconds4));

        chooseButtons.add((Button)findViewById(R.id.TimeChooseTiles1));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseTiles2));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseTiles3));
        chooseButtons.add((Button)findViewById(R.id.TimeChooseTiles4));
    }

    public void addButtonsToList()
    {
        buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.TwoByTwoTopLeft));
        buttons.add((Button) findViewById(R.id.TwoByTwoTopRight));
        buttons.add((Button) findViewById(R.id.TwoByTwoBottomLeft));
        buttons.add((Button) findViewById(R.id.TwoByTwoBottomRight));
    }

    public void setChooseButtons(boolean highScore)
    {
        ArrayList<String> labels = new ArrayList<>();
        labels.add("10");
        labels.add("20");
        labels.add("30");
        labels.add("45");
        labels.add("20");
        labels.add("40");
        labels.add("60");
        labels.add("90");
        if (highScore)
        {
            for (int i=0; i<4; i++) {
                String newLabel = labels.get(i) + ": ";
                if (!highScores.contains(HIGH_SCORE_TIME + labels.get(i))) newLabel = newLabel + getResources().getString(R.string.n_a);
                else newLabel = newLabel + highScores.getInt(HIGH_SCORE_TIME + labels.get(i), 0);
                labels.set(i, newLabel);
            }
            for (int i=4; i<8; i++) {
                String newLabel = labels.get(i) + ": ";
                if (!highScores.contains(HIGH_SCORE_TILES + labels.get(i))) newLabel = newLabel + getResources().getString(R.string.n_a);
                else newLabel = newLabel + convertTimeFromMillis(highScores.getInt(HIGH_SCORE_TILES + labels.get(i), 0));
                labels.set(i, newLabel);
            }
        }

        for (int i=0; i<8; i++) chooseButtons.get(i).setText(labels.get(i));
    }

    public String convertTimeFromMillis(int millis)
    {
        int sec = (int)Math.floor(millis/1000);
        int cent = (int)((millis - sec*1000)/10);
        return (sec + "." + cent);
    }
}
