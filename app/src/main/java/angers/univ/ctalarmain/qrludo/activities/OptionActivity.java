package angers.univ.ctalarmain.qrludo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import angers.univ.ctalarmain.qrludo.R;
import angers.univ.ctalarmain.qrludo.utils.FileDowloader;

import static java.lang.String.valueOf;

/**
 * Created by Corentin Tarlarmain on 28/04/17.
 * Modified by Florian Lherbeil
 */

public class OptionActivity extends AppCompatActivity {

    SeekBar sb_speedSpeech;
    Spinner spin_language;
    TextView tv_SSValue;
    SeekBar sb_SRValue;
    TextView tv_SRValue;
    SeekBar sb_MDTValue;
    TextView tv_MDTValue;
    Button b_valider;
    Button b_supprimer;
    SharedPreferences settings;
    List<String> languages;
    ArrayList<String> langs;
    ArrayList<String> countries;
    LinearLayout OptionLayout;
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
        sb_SRValue  = (SeekBar) findViewById(R.id.sb_SRTime);
        tv_SRValue = (TextView) findViewById(R.id.tv_SRValue);
        sb_MDTValue = (SeekBar) findViewById(R.id.sb_MDTTime);
        tv_MDTValue = (TextView) findViewById(R.id.tv_MDTValue);

        Intent intent = getIntent();
        boolean enforced = intent.getBooleanExtra("DefaultsEnforced", true);
        Bundle languages = intent.getBundleExtra("languages");

        spin_language.setEnabled(enforced);
        sb_speedSpeech.setEnabled(enforced);

        settings = getSharedPreferences(MainActivity.PREFS_NAME,0);


        float speedSpeech = settings.getFloat("speechSpeed",MainActivity.SPEEDSPEECH_DEFAULT);
        int progressSS = (int)((speedSpeech-0.8)*5);
        sb_speedSpeech.setProgress(progressSS);
        tv_SSValue.setText(String.format(Locale.ENGLISH,"%.2f",sb_speedSpeech.getProgress()*0.2 + 0.8));


        float mdt = settings.getFloat("MDTime",MainActivity.DEFAULT_MULTIPLE_DETECTION_TIME);
        sb_MDTValue.setProgress((int)mdt*2-2);
        tv_MDTValue.setText(String.format(Locale.ENGLISH,"%.2f",mdt));

        float sr = settings.getFloat("resetTime2",MainActivity.DEFAULT_CONTENT_RESET_TIME);
        sb_SRValue.setProgress((int)(sr/10-3));
        tv_SRValue.setText(String.format(Locale.ENGLISH,"%.2f",sr));


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

        sb_MDTValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tv_MDTValue.setText(String.format(Locale.ENGLISH,"%.2f",((float)sb_MDTValue.getProgress()+2)/2));
                seekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_SRValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tv_SRValue.setText(String.format(Locale.ENGLISH,"%.2f",((float)sb_SRValue.getProgress()+3)*10));
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




         //Ajout d'un bouton pour vider les fichiers stockés par l'application sur le téléphone

        b_supprimer = (Button) findViewById(R.id.b_supprimer);
        b_supprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                builder.setTitle("Suppression");
                builder.setMessage("Les données téléchargées vont être supprimées");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(FileDowloader.viderMemoire())
                                Toast.makeText(OptionActivity.this, "Les données ont bien été supprimées", Toast.LENGTH_LONG).show();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });



        b_valider = (Button) findViewById(R.id.b_valider);
        b_valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Float.parseFloat(tv_SRValue.getText().toString()) > Float.parseFloat(tv_MDTValue.getText().toString())) {
                    SharedPreferences.Editor edit = settings.edit();

                    edit.putFloat("speechSpeed", Float.parseFloat(tv_SSValue.getText().toString()));

                    edit.putString("speechCountry", countries.get(spin_language.getSelectedItemPosition()));
                    Log.d("Country", countries.get(spin_language.getSelectedItemPosition()));
                    edit.putString("speechLanguage", langs.get(spin_language.getSelectedItemPosition()));
                    Log.d("Language", langs.get(spin_language.getSelectedItemPosition()));


                    edit.putFloat("resetTime2",Float.parseFloat(tv_SRValue.getText().toString()));
                    edit.putFloat("MDTime", Float.parseFloat(tv_MDTValue.getText().toString()));

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
