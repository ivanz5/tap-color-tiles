package com.ivanzhur.tapblack;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class SettingsActivity extends Activity implements View.OnClickListener {

    ArrayList<Button> buttons_blue, buttons_white, buttons_color;

    // Variables for sound
    public static final String SOUND_BLUE_TILES = "soundBlueTiles";
    public static final String SOUND_WHITE_TILES = "soundWhiteTiles";
    public static final String SOUND_BLUE_SELECTED = "soundBlueSelected";
    public static final String SOUND_WHITE_SELECTED = "soundWhiteSelected";
    public static final String TILE_COLOR = "tileColor";
    public static final String SELECTED_COLOR = "selectedColor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.activity_settings);

        setAudio();
        setButtons();
        setSelectedButtons();
    }

    public void onClick(View view)
    {
        int id = 0;
        int num = 0;
        String type = "";
        String color = "";
        //region switch (Different buttons)
        switch (view.getId())
        {
            case (R.id.settingsSound): {
                LinearLayout soundL = (LinearLayout)findViewById(R.id.settingsScreenSound);
                LinearLayout colorL = (LinearLayout)findViewById(R.id.settingsScreenColors);
                soundL.setVisibility(View.VISIBLE);
                colorL.setVisibility(View.GONE);
            } break;
            case (R.id.settingsColor): {
                LinearLayout soundL = (LinearLayout)findViewById(R.id.settingsScreenSound);
                LinearLayout colorL = (LinearLayout)findViewById(R.id.settingsScreenColors);
                soundL.setVisibility(View.GONE);
                colorL.setVisibility(View.VISIBLE);
            } break;

            case (R.id.settingsSoundBlueNone): {type="blue";} break;
            case (R.id.settingsSoundBlue1): {id+=R.raw.click_blue_1; type="blue"; num=1;} break;
            case (R.id.settingsSoundBlue2): {id+=R.raw.click_blue_2; type="blue"; num=2;} break;
            case (R.id.settingsSoundBlue3): {id+=R.raw.click_blue_3; type="blue"; num=3;} break;
            case (R.id.settingsSoundBlue4): {id+=R.raw.click_blue_4; type="blue"; num=4;} break;

            case (R.id.settingsSoundWhiteNone): {type="white";} break;
            case (R.id.settingsSoundWhite1): {id+=R.raw.click_white_1; type="white"; num=1;} break;
            case (R.id.settingsSoundWhite2): {id+=R.raw.click_white_2; type="white"; num=2;} break;
            case (R.id.settingsSoundWhite3): {id+=R.raw.click_white_3; type="white"; num=3;} break;
            case (R.id.settingsSoundWhite4): {id+=R.raw.click_white_4; type="white"; num=4;} break;

            case (R.id.settingsColorBlue):      {color = "#1248e6"; type="color"; num = 0;} break;
            case (R.id.settingsColorDarkBlue):  {color = "#16389e"; type="color"; num = 1;} break;
            case (R.id.settingsColorGreen):     {color = "#1b786c"; type="color"; num = 2;} break;
            case (R.id.settingsColorDarkGreen): {color = "#106117"; type="color"; num = 3;} break;
            case (R.id.settingsColorYellow):    {color = "#f4f71d"; type="color"; num = 4;} break;
            case (R.id.settingsColorRed):       {color = "#000000"; type="color"; num = 5;} break;
        }
        //endregion

        //region if (type == "blue")
        if (type.equals("blue"))
        {
            MainActivity.editorSound.putInt(SOUND_BLUE_TILES, id);
            MainActivity.editorSound.putInt(SOUND_BLUE_SELECTED, num);
            for (int i=0; i<5; i++){
                buttons_blue.get(i).setBackgroundResource(R.drawable.xml_ui_button_white);
                buttons_blue.get(i).setTextColor(Color.parseColor("#000000"));
            }

            MainActivity.reloadAndPlay(MainActivity.soundIdBlue, id);
        }
        //endregion
        //region else if (type == "white")
        else if (type.equals("white"))
        {
            MainActivity.editorSound.putInt(SOUND_WHITE_TILES, id);
            MainActivity.editorSound.putInt(SOUND_WHITE_SELECTED, num);
            MainActivity.editorSound.apply();
            for (int i=0; i<5; i++){
                buttons_white.get(i).setBackgroundResource(R.drawable.xml_ui_button_white);
                buttons_white.get(i).setTextColor(Color.parseColor("#000000"));
            }

            MainActivity.reloadAndPlay(MainActivity.soundIdWhite, id);
        }
        //endregion
        //region else (color)
        else if ((view.getId() != R.id.settingsColor) && ((view.getId() != R.id.settingsSound)))
        {
            MainActivity.editorParameters.putString(TILE_COLOR, color);
            MainActivity.editorParameters.putInt(SELECTED_COLOR, num);
            MainActivity.editorParameters.apply();

            for (int i=0; i<buttons_color.size(); i++){
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)buttons_color.get(i).getLayoutParams();
                params.setMargins(0, 1, 0, 0);
                buttons_color.get(i).setLayoutParams(params);
            }

            LinearLayout.LayoutParams paramsSelected = (LinearLayout.LayoutParams)buttons_color.get(num).getLayoutParams();
            paramsSelected.setMargins(10, 10, 10, 10);
            buttons_color.get(num).setLayoutParams(paramsSelected);
        }
        //endregion

        if ((type.equals("blue")) || (type.equals("white"))) {
            view.setBackgroundResource(R.drawable.xml_ui_button_green);
            Button b = (Button) view;
            b.setTextColor(Color.parseColor("#FFFFFF"));
            MainActivity.editorSound.apply();
        }
        else if ((view.getId() == R.id.settingsColor) || ((view.getId() == R.id.settingsSound))) {
            Button soundB = (Button)findViewById(R.id.settingsSound);
            Button colorB = (Button)findViewById(R.id.settingsColor);
            Button thisB = (Button)view;
            soundB.setBackgroundResource(R.drawable.xml_ui_button_white); soundB.setTextColor(Color.parseColor("#000000"));
            colorB.setBackgroundResource(R.drawable.xml_ui_button_white); colorB.setTextColor(Color.parseColor("#000000"));
            thisB.setBackgroundResource(R.drawable.xml_ui_button_red); thisB.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    public void setAudio()
    {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void setButtons()
    {
        buttons_blue = new ArrayList<>();
        buttons_white = new ArrayList<>();
        buttons_color = new ArrayList<>();
        buttons_blue.add((Button) findViewById(R.id.settingsSoundBlueNone));
        buttons_blue.add((Button) findViewById(R.id.settingsSoundBlue1));
        buttons_blue.add((Button) findViewById(R.id.settingsSoundBlue2));
        buttons_blue.add((Button) findViewById(R.id.settingsSoundBlue3));
        buttons_blue.add((Button) findViewById(R.id.settingsSoundBlue4));
        buttons_white.add((Button) findViewById(R.id.settingsSoundWhiteNone));
        buttons_white.add((Button) findViewById(R.id.settingsSoundWhite1));
        buttons_white.add((Button) findViewById(R.id.settingsSoundWhite2));
        buttons_white.add((Button) findViewById(R.id.settingsSoundWhite3));
        buttons_white.add((Button) findViewById(R.id.settingsSoundWhite4));
        buttons_color.add((Button)findViewById(R.id.settingsColorBlue));
        buttons_color.add((Button)findViewById(R.id.settingsColorDarkBlue));
        buttons_color.add((Button)findViewById(R.id.settingsColorGreen));
        buttons_color.add((Button)findViewById(R.id.settingsColorDarkGreen));
        buttons_color.add((Button)findViewById(R.id.settingsColorYellow));
        buttons_color.add((Button)findViewById(R.id.settingsColorRed));

        Button soundBtn = (Button)findViewById(R.id.settingsSound);
        Button colorBtn = (Button)findViewById(R.id.settingsColor);
        soundBtn.setOnClickListener(this);
        colorBtn.setOnClickListener(this);

        for (int i=0; i<5; i++)
        {
            buttons_blue.get(i).setOnClickListener(this);
            buttons_white.get(i).setOnClickListener(this);
        }
        for (int i=0; i<buttons_color.size(); i++)
            buttons_color.get(i).setOnClickListener(this);
    }

    public void setSelectedButtons()
    {
        int selectedBlue = MainActivity.sound.getInt(SOUND_BLUE_SELECTED, 0);
        int selectedWhite = MainActivity.sound.getInt(SOUND_WHITE_SELECTED, 0);
        int selectedColor = MainActivity.parameters.getInt(SELECTED_COLOR, 0);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)buttons_color.get(selectedColor).getLayoutParams();
        params.setMargins(10, 10, 10, 10);
        buttons_color.get(selectedColor).setLayoutParams(params);

        buttons_blue.get(selectedBlue).setBackgroundResource(R.drawable.xml_ui_button_green);
        buttons_blue.get(selectedBlue).setTextColor(Color.parseColor("#FFFFFF"));
        buttons_white.get(selectedWhite).setBackgroundResource(R.drawable.xml_ui_button_green);
        buttons_white.get(selectedWhite).setTextColor(Color.parseColor("#FFFFFF"));
        buttons_color.get(selectedColor).setLayoutParams(params);
    }
}
