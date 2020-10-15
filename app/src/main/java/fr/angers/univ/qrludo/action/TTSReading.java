package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class TTSReading extends Action {
    private String TextToRead;

    public TTSReading(MainActivity mainActivity, int ID, String TextToRead){
        super(mainActivity,ID);
        this.TextToRead = TextToRead;
    }

    public String getTextToRead() {return TextToRead;}
}
