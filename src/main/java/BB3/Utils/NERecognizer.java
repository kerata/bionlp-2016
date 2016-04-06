package BB3.Utils;

import BB3.Models.Habitat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hakansahin on 11/03/16.
 */
public class NERecognizer {

    public static NERecognizer me;
    private Pattern pattern, scopePattern;

    public NERecognizer(){
        this.pattern = Pattern.compile("(T\\d*)\\W*(\\w*)\\W*((?:\\d+\\W*\\d+;*)+)\\W*(.*)");
        this.scopePattern = Pattern.compile("(\\d+)\\W*(\\d+)");
    }
    public static NERecognizer init(){
        return (me == null) ? me = new NERecognizer() : me;
    }

    public List<Habitat> getHabitats(String text){
        Matcher m = pattern.matcher(text);
        List<Habitat> habitatList = new ArrayList<>();
        while (m.find()) {
            String  id      = m.group(1),
                    type    = m.group(2),
                    scope   = m.group(3),
                    entity  = m.group(4);

            Matcher mScope = scopePattern.matcher(scope);
            String start = "", end = "";
            if (mScope.find()) {
                start = mScope.group(1);
                do {
                    end = mScope.group(2);
                } while (mScope.find());
            }

            if(type.equals("Habitat"))
                habitatList.add(new Habitat(id, start, end, entity));
        }

        return habitatList;
    }
}
