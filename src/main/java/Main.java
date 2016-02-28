import Models.Ontology;
import Utils.Parser;

/**
 * Created by kerata on 28/02/16.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Ontology ontology = Parser.buildOntology("/Users/kerata/IdeaProjects/bionlp/src/main/resources/OntoBiotope_BioNLP-ST-2016.obo");
        System.out.println(ontology.toString());
    }
}