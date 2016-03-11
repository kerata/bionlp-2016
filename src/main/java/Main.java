import Models.Document;
import Models.Ontology;
import Utils.LEXParser;
import Utils.NERecognizer;
import Utils.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by kerata on 28/02/16.
 */
public class Main {

    public static String DATA_PATH = "src/main/resources/data";
    public static Document[] documents;

    public static Ontology ontology;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        ontology = Parser.buildOntology("src/main/resources/OntoBiotope_BioNLP-ST-2016.obo");
        ontology.buildDependencyTrees();

        // Iterates over given files and constructs document objects.
        File[] listOfFiles = (new File(DATA_PATH)).listFiles();
        documents = new Document[listOfFiles.length];
        int docCnt = 0;
        for(File file : listOfFiles){
            if(!file.getName().endsWith(".txt")) continue;
            documents[docCnt] = new Document(file.getName().replace(".txt", ""),
                                            new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));

            // Extracts habitats from given files
            // TODO temporarily the process is in main method.
            String NEFileName = file.getPath().replace(".txt", ".a1");
            documents[docCnt].setHabitatList(NERecognizer.init().getHabitats(
                                            new String(Files.readAllBytes(Paths.get(NEFileName)), StandardCharsets.UTF_8)));
            docCnt++;
        }

        // Tags a document as an example and prints the tagged text line by line.
//        documents[0].posTagger();
//        documents[0].printTaggedDocument();

        // Parses the document as an example and prints parse tree.
//        documents[0].printParseTree();

        // Prints habitats of a document.
        System.out.println(documents[0].getId());
        documents[0].printHabitats();
    }
}