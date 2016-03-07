import Models.Document;
import Models.Ontology;
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
        for(int i=0; i<listOfFiles.length; i++){
            File file = listOfFiles[i];
            if(!file.getName().endsWith(".txt")) continue;
            documents[i] = new Document(new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));
        }

        // Tags a document as an example and prints the tagged text line by line.
        documents[0].posTagger();
        documents[0].printTaggedDocument();

        // Parses the document as an example and prints parse tree.
        documents[0].printParseTree();
    }
}