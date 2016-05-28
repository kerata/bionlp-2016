package BB3;

import BB3.Models.*;
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

    public static String
            DATA_PATH = "src/main/resources/data",
            DEV_DATA_PATH = "src/main/resources/dev-data",
            ONTOLOGY_PATH = "src/main/resources/OntoBiotope_BioNLP-ST-2016.obo",
            PREDICTION_FOLDER_PATH = "src/main/resources/output/result";

    public static Map<String, Document> documentMap;
    public static Ontology ontology;

    public static final int MAX_TRIAL_CNT = 2;

    public static void main(String[] args) throws IOException {

//        ontology = Parser.expandOntology(ONTOLOGY_PATH, DEV_DATA_PATH);
        ontology = Parser.buildOntology(ONTOLOGY_PATH, false);
        documentMap = Parser.buildDocumentList(DATA_PATH);

        documentMap.values().forEach(document -> {
            Commons.printRed(document.getId());
            Categorizer.categorizeDocument(document, (habitat, category) -> Commons.printBlack(habitat.getId() + " : " + category.getId()));
        });

        Evaluator.evaluateResultsOfAllFiles(PREDICTION_FOLDER_PATH, DATA_PATH, new Evaluator.EvaluationListener() {

            @Override
            public void onDocumentEvaluationStart(String documentId) {
                Commons.printBlack(documentId);
            }

            @Override
            public void onHabitatEvaluated(String documentId, String habitatId, String predictedCategoryId, List<String> referenceCategoryIdList) {
                String habitatEntity = documentMap.get(documentId).getHabitatMap().get(habitatId).getEntity();

                if (!predictedCategoryId.equals("") && referenceCategoryIdList != null){
                    StringBuilder sb = new StringBuilder();
                    referenceCategoryIdList.forEach(referenceCategoryId -> sb.append(ontology.getTerm(referenceCategoryId).getName()).append(","));
                    Commons.printRed(habitatEntity + " -> " + ontology.getTerm(predictedCategoryId).getName() + " - " + sb.deleteCharAt(sb.length()-1).toString());
                } else if (referenceCategoryIdList == null)
                    Commons.printBlue(habitatEntity + " -> " + ontology.getTerm(predictedCategoryId).getName());
                else if (predictedCategoryId.equals(""))
                    for (String referenceCategoryId : referenceCategoryIdList)
                        Commons.printYellow(habitatEntity + " -> " + ontology.getTerm(referenceCategoryId).getName());
            }

            @Override
            public void onDocumentEvaluated(String documentId, Stat stat) {
                Commons.printBlack(stat.toString());
            }

            @Override
            public void onFolderEvaluated(Stat stat) {
                Commons.printBlack(stat.toString());
            }
        });
    }
}