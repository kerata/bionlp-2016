package BB3.Utils;

import BB3.Models.Document;
import BB3.Models.Stat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by hakansahin on 28/05/16.
 */
public class Evaluator {

    public interface EvaluationListener {
        void onDocumentEvaluationStart(String documentId);
        void onHabitatEvaluated(String documentId, String habitatId, String predictedCategoryId, List<String> referenceCategoryId);
        void onDocumentEvaluated(String documentId, Stat stat);
        void onFolderEvaluated(Stat folderStat, List<Stat> folderStatList);
    }

    public static void evaluateResultsFromMap(String documentId, Map<String, List<String> > predictedCategoryMap, Map<String, List<String>> referenceCategoryMap, Stat folderStat, List<Stat> folderStatList, EvaluationListener listener) throws IOException {

        Stat documentStat = new Stat();
        predictedCategoryMap.forEach((habitatId, predictedCategoryIdList) -> {

            List<String> referenceCategoryIdList = referenceCategoryMap.getOrDefault(habitatId, new ArrayList<>());
            List<String> notFoundCategoryIdList = new ArrayList<>(referenceCategoryIdList);
            notFoundCategoryIdList.removeAll(predictedCategoryIdList);

            predictedCategoryIdList.forEach(predictedCategoryId -> {

                if(referenceCategoryIdList.contains(predictedCategoryId)){
                    documentStat.truePostiveCnt++;
                    if (listener != null)
                        listener.onHabitatEvaluated(documentId, habitatId, predictedCategoryId, null);
                } else {
                    documentStat.falsePositiveCnt++;
                    if (listener != null)
                        listener.onHabitatEvaluated(documentId, habitatId, predictedCategoryId, notFoundCategoryIdList);
                }


            });

            if (!notFoundCategoryIdList.isEmpty()) {
                if (listener != null)
                    listener.onHabitatEvaluated(documentId, habitatId, "", notFoundCategoryIdList);
                documentStat.falseNegativeCnt += notFoundCategoryIdList.size();
            }
        });

        folderStat.addStats(documentStat);
        folderStatList.add(documentStat);
        if (listener != null) listener.onDocumentEvaluated(documentId, documentStat);
    }

    public static void evaluateResultsFromFile(File predictionFile, File referenceFile, Stat folderStat, List<Stat> folderStatList, EvaluationListener listener) throws IOException {

        Map<String, List<String>> predictedCategoryMap = Parser.buildCategoryList(
                new String(Files.readAllBytes(predictionFile.toPath()), StandardCharsets.UTF_8));

        Map<String, List<String>> referenceCategoryMap = Parser.buildCategoryList(
                new String(Files.readAllBytes(referenceFile.toPath()), StandardCharsets.UTF_8));

        String documentId = predictionFile.getName().replace(".a2","");
        evaluateResultsFromMap(documentId, predictedCategoryMap, referenceCategoryMap, folderStat, folderStatList, listener);
    }

    public static void evaluateResultsOfAllDocuments(Map<String, Document> documentMap, String referenceFolderPath, EvaluationListener listener) throws IOException {

        Stat folderStat = new Stat();
        List<Stat> folderStatList = new ArrayList<>();
        for(String documentId : documentMap.keySet()){
            if (listener != null) listener.onDocumentEvaluationStart(documentId);

            Map<String, List<String>> referenceCategoryMap = Parser.buildCategoryList(
                    new String(Files.readAllBytes(new File(referenceFolderPath, documentId.concat(".a2")).toPath()), StandardCharsets.UTF_8));

            evaluateResultsFromMap(documentId, documentMap.get(documentId).getCategoryList(), referenceCategoryMap, folderStat, folderStatList, listener);
        }
        if (listener != null) listener.onFolderEvaluated(folderStat, folderStatList);
    }

    public static void evaluateResultsOfAllFiles(String predictionFolderPath, String referenceFolderPath, EvaluationListener listener) throws IOException {

        String[] CATFileNameList = (new File(predictionFolderPath)).list((dir, name) -> name.endsWith(".a2"));
        assert CATFileNameList != null;

        Stat folderStat = new Stat();
        List<Stat> folderStatList = new ArrayList<>();
        for(String CATFileName : CATFileNameList){
            if (listener != null) listener.onDocumentEvaluationStart(CATFileName.replace(".a2",""));
            evaluateResultsFromFile(new File(predictionFolderPath, CATFileName), new File(referenceFolderPath, CATFileName), folderStat, folderStatList, listener);
        }
        if (listener != null) listener.onFolderEvaluated(folderStat, folderStatList);
    }

}
