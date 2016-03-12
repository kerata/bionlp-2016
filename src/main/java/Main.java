import Models.Document;
import Models.Habitat;
import Models.Ontology;
import Utils.Categorizer;
import Utils.LEXParser;
import Utils.NERecognizer;
import Utils.Parser;
import com.sun.tools.javac.util.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kerata on 28/02/16.
 */
public class Main {

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static String DATA_PATH = "src/main/resources/data";
    public static List<Document> documents;

    public static Ontology ontology;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
//        ontology = Parser.buildOntology("src/main/resources/OntoBiotope_BioNLP-ST-2016.obo");
//        ontology.buildDependencyTrees();

        // Iterates over given files and constructs document objects.
        File[] listOfFiles = (new File(DATA_PATH)).listFiles();
        assert listOfFiles != null;

        documents = new ArrayList<>();
        for(File file : listOfFiles){
            if(!file.getName().endsWith(".txt")) continue;
            Document doc = new Document(file.getName().replace(".txt", ""),
                                            new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));

            // Extracts habitats from given files.
            // TODO the process is temporarily in main method.
            String NEFileName = file.getPath().replace(".txt", ".a1");
            doc.setHabitatList(NERecognizer.init().getHabitats(
                                            new String(Files.readAllBytes(Paths.get(NEFileName)), StandardCharsets.UTF_8)));

            // Extracts categories from given files.
            String CATFileName = file.getPath().replace(".txt", ".a2");
            doc.setCategories(Categorizer.init().splitCategories(
                                            new String(Files.readAllBytes(Paths.get(CATFileName)), StandardCharsets.UTF_8)));

            documents.add(doc);
        }

        // Tags a document as an example and prints the tagged text line by line.
//        documents[0].posTagger();
//        documents[0].printTaggedDocument();

        // Parses the document as an example and prints parse tree.
//        documents[0].printParseTree();

        // Prints habitats of a document.
//        documents[0].printHabitats();

        int tp = 0, fp = 0, fn = 0;
        for (Document document: documents) {
            System.out.println(ANSI_BLACK + document.getId() + ANSI_BLACK);
            for(Habitat habitat : document.getHabitats()) {
                // Categorizes the given habitat by using 'Categorizer' class.
                String  decidedCategory = Categorizer.init().categorize(habitat.getEntity());

                if(decidedCategory.equals("Not categorized")){
                    fn++;
                    System.out.println(ANSI_YELLOW + habitat.getId() + " " + habitat.getEntity() + " : " + decidedCategory + ANSI_YELLOW);
                    continue;
                }

                List<String> categoryList = document.getCategories().get(habitat.getId());
                if(categoryList.contains(decidedCategory)){
                    tp++;
                    System.out.println(ANSI_BLUE + habitat.getId() + " " + habitat.getEntity() + " : " + decidedCategory + ANSI_BLUE);
                } else {
                    fp++;
                    System.out.println(ANSI_RED + habitat.getId() + " " + habitat.getEntity() + " : " + decidedCategory + ANSI_RED);
                }
            }
            System.out.println();
        }
        double precision = 1.0 * tp / (tp + fp), recall = 1.0 * tp / (tp + fn);
        System.out.println(ANSI_BLACK + "True Positive : " + tp  + ANSI_BLACK);
        System.out.println("False Positive : " + fp);
        System.out.println("False Negative : " + fn);
        System.out.println("Precision : " + precision);
        System.out.println("Recall : " + recall);
    }
}