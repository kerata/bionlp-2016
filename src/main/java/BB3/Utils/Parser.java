package BB3.Utils;

import BB3.Models.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kerata on 28/02/16.
 */
public class Parser {

    public static Pattern
            habitatPattern          = Pattern.compile("(T\\d*)\\W*(\\w*)\\W*((?:\\d+\\W*\\d+;*)+)\\W*(.*)"),
            habitatScopePattern     = Pattern.compile("(\\d+)\\W*(\\d+)"),
            categoryPattern         = Pattern.compile("(N\\d*)\\W*(\\w*)\\W*.*:(T\\d*)\\W*.*?:(.*)");

    public static Ontology buildOntology(String path, boolean isForExpansion) {
        Ontology ontology = new Ontology();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

            String line;
            Term term = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                if (line.contains("Term")) {
                    if (term != null)
                        ontology.addTerm(term);

                    String id = reader.readLine().substring(4);
                    String name = reader.readLine().substring(6);
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
                    term.addRelation(content[0].trim(), content[1].trim());
                }
            }
            if (term != null)
                ontology.addTerm(term);

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(!isForExpansion) ontology.computeTfIdfValues();

        return ontology;
    }

    public static Ontology expandOntology(String ontologyPath, String developmentSetPath) throws IOException {

        Ontology ontology = buildOntology(ontologyPath, true);

        // Iterates over given files and constructs document objects.
        File[] NEFileList = (new File(developmentSetPath)).listFiles((dir, name) -> name.endsWith(".a1"));
        assert NEFileList != null;

        for(File NEFile : NEFileList){

            // Extracts habitats from given file.
            Document document = buildDocument(NEFile.getName().replace(".a1",""),
                    new String(Files.readAllBytes(NEFile.toPath()), StandardCharsets.UTF_8));


            //Extracts categories from given file.
            String CATFileName = NEFile.getPath().replace(".a1", ".a2");
            Map<String, List<String>> categoryList = buildCategoryList(
                    new String(Files.readAllBytes(Paths.get(CATFileName)), StandardCharsets.UTF_8));

            for (Habitat habitat : document.getHabitatMap().values()) {
                List<String> categoryListForHabitat = categoryList.get(habitat.getId());
                for(String categoryId : categoryListForHabitat) {
                    try {
                        Term category = ontology.getTerm(categoryId);
                        category.addSynonym(new Synonym(Synonym.Type.RELATED, habitat.getEntity()));
                    } catch (NullPointerException e){
                        Commons.printRed("There is no term with id : " + categoryId + " to be expanded");
                    }
                }
            }
        }

        ontology.computeTfIdfValues();
        return ontology;
    }

    public static Map<String, Document> buildDocumentList(String path) throws IOException {
        Map<String, Document> documentMap = new HashMap<>();

        File[] listOfFiles = (new File(path)).listFiles();
        assert listOfFiles != null;

        for(File file : listOfFiles){
            if(!file.getName().endsWith(".a1")) continue;

            // Extracts habitats from given files.
            Document document = buildDocument(file.getName().replace(".a1",""),
                    new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));

            documentMap.put(file.getName().replace(".a1",""), document);
        }

        return documentMap;
    }

    public static Document buildDocument(String documentId, String text){

        Document document = new Document(documentId);

        Matcher m = habitatPattern.matcher(text);
        while (m.find()) {
            String  id      = m.group(1),
                    type    = m.group(2),
                    scope   = m.group(3),
                    entity  = m.group(4);

            Matcher mScope = habitatScopePattern.matcher(scope);
            String start = "", end = "";
            if (mScope.find()) {
                start = mScope.group(1);
                do {
                    end = mScope.group(2);
                } while (mScope.find());
            }

            switch (type){
                case "Title" :
                    document.setTitle(entity);
                    break;
                case "Paragraph" :
                    document.setParagraph(entity);
                    break;
                case "Habitat" :
                    document.addHabitat(new Habitat(id, start, end, entity));
            }
        }

        return document;
    }

    public static Map<String, List<String> > buildCategoryList(String text){

        Matcher m = categoryPattern.matcher(text);

        Map<String, List<String> > categoryList = new HashMap<>();
        while(m.find()){
            String  type        = m.group(2),
                    id          = m.group(3),
                    category    = m.group(4);

            if(!type.equals("OntoBiotope")) continue;

            if(!categoryList.containsKey(id))
                categoryList.put(id, new ArrayList<>());

            categoryList.get(id).add(category);
        }

        return categoryList;
    }

}
