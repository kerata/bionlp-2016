package Utils;

import Models.Ontology;
import Models.Relation;
import Models.Synonym;
import Models.Term;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kerata on 28/02/16.
 */
public class Parser {

    public static Ontology buildOntology(String path) {
        Ontology ontology = new Ontology();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

            int counter = 0;
            String line;
            Term term = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                if (line.contains("Term")) {
                    counter++;
                    if (term != null)
                        ontology.addTerm(term);

                    String id = (line = reader.readLine()).substring(4);
                    String name = (line = reader.readLine()).substring(6);
                    term = new Term(id, name);
                }
                else if (line.startsWith("synonym")) {
                    String detail = null;
                    Matcher matcher = Pattern.compile("\".*\"").matcher(line);

                    if (matcher.find()) {
                        detail = matcher.group();
                        detail = detail.substring(1, detail.length() - 1);
                    }

                    Synonym.Type type = line.contains("EXACT") ?
                            Synonym.Type.EXACT :
                            line.contains("RELATED") ?
                                    Synonym.Type.RELATED : null;

                    assert term != null;
                    term.addSynonym(new Synonym(type, detail));
                } else if (line.startsWith("is_a")) {
                    line = line.substring(6);
                    String[] content = line.split("!");

                    assert term != null;
                    term.addRelation(new Relation(
                            content[0].trim(),
                            content[1].trim()
                    ));
                }
            }
            if (term != null)
                ontology.addTerm(term);

            System.out.println("" + counter);

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ontology;
    }
}
