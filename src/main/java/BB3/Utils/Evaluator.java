package BB3.Utils;

import BB3.Models.Document;
import BB3.Models.Stat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by hakansahin on 28/05/16.
 */
public class Evaluator {

    public interface EvaluationListener {
        void onDocumentEvaluationStart(String documentId);
        void onHabitatEvaluated(String documentId, String habitatId, String predictedCategoryId, List<String> referenceCategoryId);
        void onDocumentEvaluated(String documentId, Stat stat);
        void onFolderEvaluated(Stat stat);
    }

    public static void evaluateResultsFromFile(File predictionFile, File referenceFile, Stat folderStat, EvaluationListener listener) throws IOException {

        Map<String, List<String>> predictedCategoryMap = Parser.buildCategoryList(
                new String(Files.readAllBytes(predictionFile.toPath()), StandardCharsets.UTF_8));

        Map<String, List<String>> referenceCategoryMap = Parser.buildCategoryList(
                new String(Files.readAllBytes(referenceFile.toPath()), StandardCharsets.UTF_8));

        Stat documentStat = new Stat();
        predictedCategoryMap.forEach((habitatId, predictedCategoryIdList) -> {

            List<String> referenceCategoryIdList = referenceCategoryMap.getOrDefault(habitatId, new ArrayList<>());
            List<String> notFoundCategoryIdList = new ArrayList<>(referenceCategoryIdList);
            notFoundCategoryIdList.removeAll(predictedCategoryIdList);

            predictedCategoryIdList.forEach(predictedCategoryId -> {

                if(referenceCategoryIdList.contains(predictedCategoryId)){
                    documentStat.truePostiveCnt++;
                    if (listener != null)
                        listener.onHabitatEvaluated(predictionFile.getName().replace(".a2",""),habitatId, predictedCategoryId, null);
                } else {
                    documentStat.falsePositiveCnt++;
                    if (listener != null)
                        listener.onHabitatEvaluated(predictionFile.getName().replace(".a2",""),habitatId, predictedCategoryId, notFoundCategoryIdList);
                }


            });

            if (!notFoundCategoryIdList.isEmpty())
                if (listener != null)
                    listener.onHabitatEvaluated(predictionFile.getName().replace(".a2",""),habitatId, "", notFoundCategoryIdList);

            documentStat.falseNegativeCnt += notFoundCategoryIdList.size();
        });

        folderStat.addStats(documentStat);
        if (listener != null) listener.onDocumentEvaluated(predictionFile.getName().replace(".a2",""), documentStat);
    }

    public static void evaluateResultsOfAllFiles(String predictionFolderPath, String referenceFolderPath, EvaluationListener listener) throws IOException {

        String[] CATFileNameList = (new File(predictionFolderPath)).list((dir, name) -> name.endsWith(".a2"));
        assert CATFileNameList != null;

        Stat folderStat = new Stat();
        for(String CATFileName : CATFileNameList){
            if (listener != null) listener.onDocumentEvaluationStart(CATFileName.replace(".a2",""));
            evaluateResultsFromFile(new File(predictionFolderPath, CATFileName), new File(referenceFolderPath, CATFileName), folderStat, listener);
        }
        if (listener != null) listener.onFolderEvaluated(folderStat);
    }

}
