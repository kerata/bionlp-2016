package BB3;

import BB3.Models.Document;
import BB3.Models.Ontology;
import BB3.Utils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by kerata on 28/02/16.
 */
public class BB3Runner {

    public static String DATA_PATH = "src/main/resources/dev-data";
    public static List<Document> documents;

    public static Ontology ontology;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
//        StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
        ontology = Parser.buildOntology("src/main/resources/OntoBiotope_BioNLP-ST-2016_tra_ext.obo");
        ontology.buildDependencyTrees();

        // Iterates over given files and constructs document objects.
        File[] listOfFiles = (new File(DATA_PATH)).listFiles();
        assert listOfFiles != null;

        int cnt = 0;
        documents = new ArrayList<>();
        for(File file : listOfFiles){
            if(!file.getName().endsWith(".txt")) continue;
            Document doc = new Document(file.getName().replace(".txt", ""),
                                            new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));
//            lemmatizer.lemmatize(doc.getText());
            // Extracts habitats from given files.
            // TODO the process is temporarily in main method.
            String NEFileName = file.getPath().replace(".txt", ".a1");
            doc.setHabitatList(NERecognizer.init().getHabitats(
                                            new String(Files.readAllBytes(Paths.get(NEFileName)), StandardCharsets.UTF_8)));

//             Extracts categories from given files.
            String CATFileName = file.getPath().replace(".txt", ".a2");
            doc.setCategories(Categorizer.splitCategories(
                                            new String(Files.readAllBytes(Paths.get(CATFileName)), StandardCharsets.UTF_8)));

            for(List str : doc.getCategories().values())
                cnt += str.size();
            documents.add(doc);
        }

        ontology.computeTfIdfValues();
        StringBuilder sb = new StringBuilder();
        for (Document document: documents) {
            Commons.printBlack(document.getId());
            sb.append(Categorizer.categorizeDocument(document));
            Commons.printBlack("");
        }

        Commons.printToFile("stats", "FalsePositives.txt", sb.toString());
        double precision = 1.0 * Commons.TP / (Commons.TP + Commons.FP), recall = 1.0 * Commons.TP / (Commons.TP + Commons.FN);

        Commons.printBlack("True Positive : " + Commons.TP);
        Commons.printBlack("False Positive : " + Commons.FP);
        Commons.printBlack("False Negative : " + Commons.FN);
        Commons.printBlack("Precision : " + precision);
        Commons.printBlack("Recall : " + recall);
        Commons.printBlack("Total category : " + cnt);
        Commons.printBlack("Trial : " + Commons.trial);
    }
}