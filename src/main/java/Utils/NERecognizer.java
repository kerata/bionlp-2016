package Utils;

import Models.Habitat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hakansahin on 11/03/16.
 */
public class NERecognizer {

    public static NERecognizer me;

    private Pattern pattern;
    public NERecognizer(){
        this.pattern = Pattern.compile("(T\\d*)\\W*(\\w*)\\W*(\\d*)\\W*(\\d*)\\W*(.*)");
    }

    public static NERecognizer init(){
        return (me == null) ? me = new NERecognizer() : me;
    }

    public List<Habitat> getHabitats(String text){

        Matcher m = pattern.matcher(text);
        List<Habitat> habitatList = new ArrayList<>();
        while(m.find()){
            String  id      = m.group(1),
                    type    = m.group(2),
                    start   = m.group(3),
                    end     = m.group(4),
                    entity  = m.group(5);

            if(type.equals("Habitat"))
                habitatList.add(new Habitat(id, start, end, entity));
        }

        return habitatList;
    }
}
