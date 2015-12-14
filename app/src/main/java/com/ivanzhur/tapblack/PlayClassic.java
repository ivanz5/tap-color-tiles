package com.ivanzhur.tapblack;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
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
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;


public class PlayClassic extends BaseGameActivity {

    //region Declaring vars
    Random random;
    ArrayList<Button> buttons;
    Map<Integer, String> idColor;

    // Variables for UI components
    Button restartButton, shareButton, leaderboardButton;
    TextView timerTextView, scoreTextView, bestScoreTextView;
    LinearLayout scoreLayout;

    // Variables for game
    int intervalBase = 370;
    int intervalAmplitude = 1300;
    double beta = -0.06;
    int tilesInterval = 1700;
    int stopAddOnTapInterval = 900;
    int tilesAddOverTwo = 200;
    int wasBlack = 0;
    int numBlack = 0;
    int tiles = 0;
    long startTime = 0;
    boolean gameStarted = false;
    String tileColor = "#1248e6";

    // Variables for animation
    final Animation fadeIn = new AlphaAnimation(0, 1);

    // Variables for local high score and sounds
    public static final String HIGH_SCORE_CLASSIC = "highScoreClassic";
    public static final String NUMBER_GAMES_PLAYED = "gamesPlayed";
    public static final String RATE_ASKED = "rateAsked";
    public static final String TILE_COLOR = "tileColor";
    final Context context = this;

    // Variables for Plus Leaderboard
    private static String LEADERBOARD_ID_CLASSIC;
    private static final int REQUEST_LEADERBOARD = 1;
    //endregion

    //region Handler for adding tiles
    final Handler blackHandler = new Handler();
    Runnable blackRunnable = new Runnable() {
        @Override
        public void run() {
            if (numBlack > 15) {
                gameOver();
                return;
            }
            addTile();
            tilesInterval = intervalBase + (int) (intervalAmplitude * Math.pow(2, beta * tiles));
            blackHandler.postDelayed(this, tilesInterval);
        }
    };
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Fullscreen for API < 16
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_classic);

        setAds(); // Setting ads
        setVariables(); // id-color map, getting TextViews and Layout, Buttons
        setAnimation(); // Setting animation
        setColor(); // Tiles color
        setAudio(); // Audio
        addButtonsToList(); // Adding buttons to List 'buttons'
        setButtonsOnClickListeners(); // Setting onclick listeners for buttons
        setBestScoreResetting(); // Reset best score on click
        addTile(); // Adding first blue tile
    }

    // On sign in succeeded
    public void onSignInSucceeded()
    {
    }

    // On sign in failed
    public void onSignInFailed()
    {
        //Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
    }

    public void setAds()
    {
        AdView mAdView = (AdView) findViewById(R.id.adViewClassic);
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
        random = new Random();
        timerTextView = (TextView)findViewById(R.id.timeTV);
        scoreTextView = (TextView)findViewById(R.id.scoreTV);
        bestScoreTextView = (TextView)findViewById(R.id.bestScoreTV);
        scoreLayout = (LinearLayout)findViewById(R.id.scoreScreenClassic);
        restartButton = (Button)findViewById(R.id.restartButtonClassic);
        shareButton = (Button)findViewById(R.id.shareButtonClassic);
        leaderboardButton = (Button)findViewById(R.id.leaderboardButtonClassic);
        LEADERBOARD_ID_CLASSIC = getResources().getString(R.string.leaderboard_classic_mode);
    }

    public void setAnimation()
    {
        fadeIn.setDuration(500);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                restartButton.setClickable(false);
                shareButton.setClickable(false);
                leaderboardButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                restartButton.setClickable(true);
                shareButton.setClickable(true);
                leaderboardButton.setClickable(true);
                scoreLayout.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setColor()
    {
        tileColor = App.parameters.getString(TILE_COLOR, "#1248e6");
    }

    public void setAudio()
    {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void setButtonsOnClickListeners()
    {
        for (int i=0; i<16; i++)
        {
            idColor.put(buttons.get(i).getId(), "white");

            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle button click
                    // If button is black
                    if (idColor.get(v.getId()).equals("black")) {
                        if (!gameStarted) // If game not started yet
                        {
                            startTime = System.currentTimeMillis();
                            blackHandler.postDelayed(blackRunnable, tilesInterval);
                            gameStarted = true;
                        }
                        if (numBlack > 15) gameOver(); // If all tiles
                        else
                        {
                            if (tiles > tilesAddOverTwo)
                            {
                                if (wasBlack > 2) { addTile(); wasBlack = 0; }
                                else wasBlack++;
                            }
                            else if (tilesInterval < stopAddOnTapInterval)
                            {
                                if (wasBlack > 1) { addTile(); wasBlack = 0; }
                                else wasBlack++;
                            }
                            else
                            {
                                addTile();
                            }

                            changeState(v);
                            numBlack--;
                        }

                        App.playSound(App.soundIdBlue);
                        tiles++;
                        timerTextView.setText(String.valueOf(tiles));
                    }
                    // If button is white
                    else {
                        v.setBackgroundColor(Color.parseColor("#FF0000"));
                        App.playSound(App.soundIdWhite);
                        gameOver();
                    }
                }
            });
        }
    }

    public void setBestScoreResetting()
    {
        bestScoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!App.highScores.contains(HIGH_SCORE_CLASSIC)) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.reset)
                        .setMessage(R.string.reset_classic);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.editorHighScores.remove(HIGH_SCORE_CLASSIC);
                        App.editorHighScores.apply();
                        String bestScoreStr = getResources().getString(R.string.best_score) + getResources().getString(R.string.n_a);
                        bestScoreTextView.setText(bestScoreStr);
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void addTile()
    {
        numBlack++;
        int nb = random.nextInt(16);
        while (idColor.get(buttons.get(nb).getId()).equals("black"))
            nb = random.nextInt(16);
        changeState(buttons.get(nb));
    }

    // Change state (color) of tile
    public void changeState(View view)
    {
        if (idColor.get(view.getId()).equals("black"))
        {
            view.setBackgroundColor(Color.parseColor("#FFFFFF"));
            idColor.put(view.getId(), "white");
        }
        else
        {
            view.setBackgroundColor(Color.parseColor(tileColor));
            idColor.put(view.getId(), "black");
        }
    }

    // Restart button
    public void restartClicked(View view)
    {
        timerTextView.setText("0");
        scoreLayout.clearAnimation();
        scoreLayout.setClickable(false);
        scoreLayout.setVisibility(View.INVISIBLE);
        for (int i=0; i<16; i++) {
            buttons.get(i).setBackgroundColor(Color.parseColor("#FFFFFF"));
            buttons.get(i).setClickable(true);
            idColor.put(buttons.get(i).getId(), "white");
        }
        int rb = random.nextInt(16);
        buttons.get(rb).setBackgroundColor(Color.parseColor(tileColor));
        idColor.put(buttons.get(rb).getId(), "black");
        gameStarted = false;
        tiles = 0;
        numBlack = 1;
    }

    // Share button
    public void shareClicked(View view)
    {
        CharSequence text = getResources().getString(R.string.i_stayed) + scoreTextView.getText() + getResources().getString(R.string.in_tap_black);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    // Leaderboard button
    public void leaderboardClicked(View view)
    {
        if (getApiClient().isConnected())
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                LEADERBOARD_ID_CLASSIC), REQUEST_LEADERBOARD);
        else if (!getApiClient().isConnecting())
            getApiClient().connect();
    }

    // On game over
    public void gameOver()
    {
        for (int i=0; i<16; i++) buttons.get(i).setClickable(false);

        blackHandler.removeCallbacks(blackRunnable);
        scoreTextView.setText(timerTextView.getText());

        scoreLayout.startAnimation(fadeIn);
        scoreLayout.setVisibility(View.VISIBLE);

        // Updating high score
        if (App.highScores.contains(HIGH_SCORE_CLASSIC))
        {
            int currentHigh = App.highScores.getInt(HIGH_SCORE_CLASSIC, 0);
            if (currentHigh < tiles) // If need update
            {
                App.editorHighScores.putInt(HIGH_SCORE_CLASSIC, tiles);
                App.editorHighScores.apply();
            }
        }
        else // If not exist
        {
            App.editorHighScores.putInt(HIGH_SCORE_CLASSIC, tiles);
            App.editorHighScores.apply();
        }

        // Updating number of games played
        if (App.parameters.contains(NUMBER_GAMES_PLAYED))
        {
            int games = App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) + 1;
            App.editorParameters.putInt(NUMBER_GAMES_PLAYED, games);
            App.editorParameters.apply();
        }
        else
        {
            App.editorParameters.putInt(NUMBER_GAMES_PLAYED, 1);
            App.editorParameters.apply();
        }
        //Toast.makeText(getApplicationContext(), App.parameters.getInt(NUMBER_GAMES_PLAYED, 0), Toast.LENGTH_SHORT).show();
        askForRating();

        String bestScoreStr = getResources().getString(R.string.best_score) + App.highScores.getInt(HIGH_SCORE_CLASSIC, 0);
        bestScoreTextView.setText(bestScoreStr);

        // Update leaderboard and achievements
        if (getApiClient().isConnected()) {
            Games.Leaderboards.submitScore(getApiClient(), LEADERBOARD_ID_CLASSIC, tiles);
            if (tiles >= 100) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_beginner));
            if (tiles >= 300) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_expert));
            if (tiles >= 500) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_master_tapper));
            if (tiles == 666) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_devil));
            if (tiles >= 1000) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_cheater));
            if (numBlack > 15) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_no_space));
            if (App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) >= 10) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_newbie));
            if (App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) >= 100) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_busy_fingers));
            if (App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) >= 500) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_serious_play));
            if (App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) >= 1000) Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_geek_gamer));
        }

    }

    // Adding buttons to List 'buttons'
    public void addButtonsToList()
    {
        buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.RowOneBtn1));
        buttons.add((Button) findViewById(R.id.RowOneBtn2));
        buttons.add((Button) findViewById(R.id.RowOneBtn3));
        buttons.add((Button) findViewById(R.id.RowOneBtn4));

        buttons.add((Button) findViewById(R.id.RowTwoBtn1));
        buttons.add((Button) findViewById(R.id.RowTwoBtn2));
        buttons.add((Button) findViewById(R.id.RowTwoBtn3));
        buttons.add((Button) findViewById(R.id.RowTwoBtn4));

        buttons.add((Button) findViewById(R.id.RowThreeBtn1));
        buttons.add((Button) findViewById(R.id.RowThreeBtn2));
        buttons.add((Button) findViewById(R.id.RowThreeBtn3));
        buttons.add((Button) findViewById(R.id.RowThreeBtn4));

        buttons.add((Button) findViewById(R.id.RowFourBtn1));
        buttons.add((Button) findViewById(R.id.RowFourBtn2));
        buttons.add((Button) findViewById(R.id.RowFourBtn3));
        buttons.add((Button) findViewById(R.id.RowFourBtn4));
    }

    public void askForRating()
    {
        if (App.parameters.contains(RATE_ASKED))
        {
            if ((App.parameters.getInt(RATE_ASKED, 0) == -1) && (App.parameters.getInt(NUMBER_GAMES_PLAYED, 0) > 40))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.rate_this_app)
                        .setMessage(R.string.rate_this_app_message);
                //region YES
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Rating
                        Intent rateIntent = new Intent(Intent.ACTION_VIEW);
                        rateIntent.setData(Uri.parse("market://details?id=com.ivanzhur.tapblack"));
                        if (!isActivityStarted(rateIntent)) {
                            rateIntent.setData(Uri.parse("https://play.google.com/store/apps/details?com.ivanzhur.tapblack"));
                            if (!isActivityStarted(rateIntent))
                                Toast.makeText(getApplicationContext(), getString(R.string.could_not_open_play_store), Toast.LENGTH_SHORT).show();
                        }
                        App.editorParameters.putInt(RATE_ASKED, 1);
                    }
                }); //endregion
                //region NO
                builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.editorParameters.putInt(RATE_ASKED, 0);
                    }
                });
                //endregion
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        else
        {
            App.editorParameters.putInt(RATE_ASKED, -1);
        }
        App.editorParameters.apply();
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
}