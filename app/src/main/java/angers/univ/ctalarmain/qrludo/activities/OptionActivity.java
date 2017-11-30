package angers.univ.ctalarmain.qrludo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import angers.univ.ctalarmain.qrludo.R;

import static java.lang.String.valueOf;

/**
 * Created by etudiant on 28/04/17.
 */

public class OptionActivity extends AppCompatActivity {

    SeekBar sb_speedSpeech;
    Spinner spin_language;
    Switch sw_mode;
    TextView tv_SSValue;
    EditText et_SRValue;
    EditText et_MDTValue;
    Button b_deconnexion;
    Button b_valider;
    SharedPreferences settings;
    List<String> languages;
    ArrayList<String> langs;
    ArrayList<String> countries;
    LinearLayout OptionLayout;
    boolean demandeDeconnexion = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        OptionLayout = (LinearLayout)findViewById(R.id.option_layout);
        OptionLayout.requestFocus();
        langs = new ArrayList<>();
        countries = new ArrayList<>();
        languages = Arrays.asList(getResources().getStringArray(R.array.languages_array));


        sb_speedSpeech = (SeekBar) findViewById(R.id.sb_SpeedSpeech);
        tv_SSValue = (TextView) findViewById(R.id.tv_SSValue);
        spin_language = (Spinner) findViewById(R.id.spin_languages);
        sw_mode = (Switch) findViewById(R.id.switch_ajout);
        et_SRValue  = (EditText) findViewById(R.id.et_SRValue);
        et_MDTValue = (EditText) findViewById(R.id.et_MDTValue);

        Intent intent = getIntent();
        boolean enforced = intent.getBooleanExtra("DefaultsEnforced", true);
        Bundle languages = intent.getBundleExtra("languages");

        spin_language.setEnabled(enforced);
        sb_speedSpeech.setEnabled(enforced);

        settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
        float speedSpeech = settings.getFloat("speechSpeed",MainActivity.SPEEDSPEECH_DEFAULT);
        int progress = (int)((speedSpeech-0.8)*5);
        sb_speedSpeech.setProgress(progress);
        tv_SSValue.setText(String.format(Locale.ENGLISH,"%.2f",sb_speedSpeech.getProgress()*0.2 + 0.8));
        sb_speedSpeech.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_SSValue.setText(String.format(Locale.ENGLISH,"%.2f",sb_speedSpeech.getProgress()*0.2 + 0.8));
                seekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        String language = settings.getString("speechCountry",MainActivity.LOCALE_DEFAULT.getCountry());

        int index = 0;
        final ArrayList<String> lang_list = new ArrayList<String>();

        if(languages.containsKey("FR"))
        {
            if(language.equals("FR"))
                index = lang_list.size();
            lang_list.add("Français");
            langs.add("fr");
            countries.add("FR");
        }
        if(languages.containsKey("GB"))
        {
            if(language.equals("GB"))
                index = lang_list.size();
            lang_list.add("Anglais");
            langs.add("en");
            countries.add("GB");
        }
        if(languages.containsKey("DE"))
        {
            if(language.equals("DE"))
                index = lang_list.size();
            lang_list.add("Allemand");
            langs.add("de");
            countries.add("DE");
        }
        if(languages.containsKey("ES"))
        {
            if(language.equals("ES"))
                index = lang_list.size();
            lang_list.add("Espagnol");
            langs.add("es");
            countries.add("ES");
        }
        if(languages.containsKey("IT")){
            if(language.equals("IT"))
                index = lang_list.size();
            lang_list.add("Italien");
            langs.add("it");
            countries.add("IT");
        }
        if(lang_list.isEmpty())
        {
            lang_list.add("Français");
            langs.add(MainActivity.LOCALE_DEFAULT.getLanguage());
            countries.add(MainActivity.LOCALE_DEFAULT.getCountry());
        }


        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lang_list);
        spin_language.setAdapter(spinnerArrayAdapter);
        spin_language.setSelection(index);


        int mode = settings.getInt("speechMode",MainActivity.DEFAULT_MODE);

        if((mode == TextToSpeech.QUEUE_ADD && !sw_mode.isChecked()) || (mode == TextToSpeech.QUEUE_FLUSH && sw_mode.isChecked())){
            sw_mode.toggle();
        }


        et_SRValue.setText(valueOf(settings.getInt("resetTime",MainActivity.DEFAULT_QUESTION_RESET_TIME)));

        et_MDTValue.setText(valueOf(settings.getInt("MDTime",MainActivity.DEFAULT_MULTIPLE_DETECTION_TIME)));

        b_deconnexion = (Button) findViewById(R.id.b_deconnexion);
        b_deconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("test", "demande déconnexion");
                demandeDeconnexion = true;
            }
        });

        b_valider = (Button) findViewById(R.id.b_valider);
        b_valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Integer.parseInt(et_SRValue.getText().toString()) > Integer.parseInt(et_MDTValue.getText().toString())) {
                    SharedPreferences.Editor edit = settings.edit();
                    edit.putFloat("speechSpeed", Float.parseFloat(tv_SSValue.getText().toString()));

                    edit.putString("speechCountry", countries.get(spin_language.getSelectedItemPosition()));
                    Log.d("Country", countries.get(spin_language.getSelectedItemPosition()));
                    edit.putString("speechLanguage", langs.get(spin_language.getSelectedItemPosition()));
                    Log.d("Language", langs.get(spin_language.getSelectedItemPosition()));
                    if (sw_mode.isChecked()) {
                        edit.putInt("speechMode", TextToSpeech.QUEUE_ADD);
                    } else {
                        edit.putInt("speechMode", TextToSpeech.QUEUE_FLUSH);
                    }

                    edit.putInt("resetTime", Integer.parseInt(et_SRValue.getText().toString()));
                    edit.putInt("MDTime", Integer.parseInt(et_MDTValue.getText().toString()));

                    edit.putBoolean("deconnexion", demandeDeconnexion);

                    edit.apply();
                    Intent intent = new Intent();
                    if (getParent() == null) {
                        setResult(RESULT_OK, intent);
                    } else {
                        getParent().setResult(RESULT_OK, intent);
                    }
                    finish();
                }else{
                    Toast.makeText(OptionActivity.this, "Le délai de réinitialisation ne peut être supérieur au temps de détection multiple.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        if(getParent() == null){
            setResult(RESULT_CANCELED);
        }else {
            getParent().setResult(RESULT_CANCELED);
        }
        super.onBackPressed();

    }
}
