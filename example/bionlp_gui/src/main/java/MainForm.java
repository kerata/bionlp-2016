import BB3.Models.*;
import BB3.Models.Document;
import BB3.Utils.Categorizer;
import BB3.Utils.Commons;
import BB3.Utils.Evaluator;
import BB3.Utils.Parser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Created by Mert Tiftikci on 29/05/16.
 */
public class MainForm extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JPanel ontologyExpanderPanel;
    private JPanel categorizerPanel;
    private JPanel evaluatorPanel;
    private JTextPane output;
    private JButton expandButton;
    private JButton categorizeButton;
    private JButton evaluateButton;
    private JButton doCategorizeButton;
    private JPanel ontologyPathGroup;
    private JPanel developmentSetPathGroup;
    private JPanel testSetFolderPathGroup;
    private JPanel predictionFolderPathGroup;
    private JPanel referenceFolderPathGroup;
    private JPanel OntologyWrapper;

    private JTextField ontologyPathTextField;
    private JTextField developmentSetFolderTextField;
    private JTextField testSetFolderTextField;
    private JTextField predictionFolderTextField;
    private JTextField referenceFolderTextField;

    private JProgressBar ontologyProgress;
    private JProgressBar documentProgress;

    private final JFileChooser fc = new JFileChooser();

    private static final int
            width = 600,
            height = 450,
            MAX_TRIAL_CNT = 1;

    private static String PREDICTION_FOLDER_PATH = "/result";

    private static Map<String, Document> documentMap;
    private static Ontology ontology;
    private boolean isOntologyValid = false;
    private boolean isDocumentMapValid = false;

    private MainForm(String title) throws HeadlessException {
        super(title);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        setBounds((width - MainForm.width) / 2, (height - MainForm.height) / 2, MainForm.width, MainForm.height);
        setMinimumSize(new Dimension(MainForm.width, MainForm.height));
    }

    public static void main(String[] args) {
        PREDICTION_FOLDER_PATH = Paths.get(".").toAbsolutePath().normalize().toString() + PREDICTION_FOLDER_PATH;

        MainForm form = new MainForm("Habitat Categorizer");
        form.fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        form.setContentPane(form.mainPanel);
        form.setVisible(true);
    }

    private void createUIComponents() {
        ontologyPathGroup = new JPanel();
        developmentSetPathGroup = new JPanel();
        testSetFolderPathGroup = new JPanel();
        predictionFolderPathGroup = new JPanel();
        referenceFolderPathGroup = new JPanel();

        expandButton = new JButton();
        categorizeButton = new JButton();
        evaluateButton = new JButton();
        doCategorizeButton = new JButton();

        output = new JTextPane();
        output.setBackground(Color.decode("#1D1E1A"));

        ontologyPathTextField = fillInputGroup(ontologyPathGroup, "Ontology Path", true);
        ontologyPathTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isOntologyValid = false;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isOntologyValid = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isOntologyValid = false;
            }
        });
        JButton inputField = (JButton) getChildWithProperty(
                ontologyPathGroup, "id", "FOLDER_CHOOSER");
        if (inputField != null) {
            inputField.setIcon(UIManager.getIcon("FileView.fileIcon"));
            inputField.putClientProperty("id", "FILE_CHOOSER");
        }
        ontologyProgress = (JProgressBar) getChildWithProperty(
                ontologyPathGroup, "id", "PROGRESS");

        developmentSetFolderTextField = fillInputGroup(developmentSetPathGroup, "Development Set Folder Path", false);
        testSetFolderTextField = fillInputGroup(testSetFolderPathGroup, "Test Set Folder Path", true);
        testSetFolderTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isDocumentMapValid = false;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isDocumentMapValid = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isDocumentMapValid = false;
            }
        });
        documentProgress = (JProgressBar) getChildWithProperty(
                testSetFolderPathGroup, "id", "PROGRESS");
        predictionFolderTextField = fillInputGroup(predictionFolderPathGroup, "Prediction Folder Path", false);
        referenceFolderTextField = fillInputGroup(referenceFolderPathGroup, "Reference Folder Path", false);

        output.setAutoscrolls(true);
        output.setEditable(false);
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        expandButton.addActionListener(this);
        categorizeButton.addActionListener(this);
        evaluateButton.addActionListener(this);
        doCategorizeButton.addActionListener(this);
    }

    private JTextField fillInputGroup(JPanel holder, String title, boolean shouldAddProgressBar) {
        holder.setLayout(new GridBagLayout());

        JLabel prompt = new JLabel(title);
        prompt.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        holder.add(prompt, c);

        JTextField input = new JTextField();
        input.setSelectionColor(Color.YELLOW);
        input.putClientProperty("id", "INPUT");
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 1;
        c.weightx = 1.0;
        holder.add(input, c);

        JButton fileChooserButton = new JButton();
        fileChooserButton.putClientProperty("id", "FOLDER_CHOOSER");
        fileChooserButton.setActionCommand("OPEN_FILE_CHOOSER");
        fileChooserButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        fileChooserButton.addActionListener(this);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        holder.add(fileChooserButton, c);

        if (shouldAddProgressBar) {
            JProgressBar progressBar = new JProgressBar();
            progressBar.putClientProperty("id", "PROGRESS");
            progressBar.setIndeterminate(true);
            progressBar.setVisible(false);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.gridy = 2;
            c.gridwidth = 2;
            holder.add(progressBar, c);
        }

        return input;
    }

    /**
     * callback of ActionListener
     * @param actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        // will be triggered when file choosers are on action
        if (actionEvent.getActionCommand().equals("OPEN_FILE_CHOOSER")) {
            JTextField inputField = (JTextField) getChildWithProperty(
                    ((JComponent) source).getParent(), "id", "INPUT");
            // set chooser mode: for ontology, only files; for others, only folders.
            if (((JComponent) source).getClientProperty("id").equals("FILE_CHOOSER"))
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            else fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            File file = null;
            // open file chooser and get selected file's absolute path
            int returnVal = fc.showOpenDialog((Component) source);
            if (returnVal == JFileChooser.APPROVE_OPTION)
                file = fc.getSelectedFile();

            if (inputField != null && file != null)
                inputField.setText(file.getAbsolutePath());
        }
        else if (source.equals(expandButton)) {
            if (!developmentSetFolderTextField.getText().isEmpty()){
                try {
                    String filePath = ontologyPathTextField.getText();
                    File ontologyFile = new File(filePath);
                    String extendedOntologyFileName = ontologyFile.getName().replace(".obo", "_ext.obo");

                    String developmentFolderPath = developmentSetFolderTextField.getText();
                    if (developmentFolderPath != null && !developmentFolderPath.isEmpty()) {
                        ontology = Parser.expandOntology(filePath, developmentFolderPath);
                    }
                    else {
                        appendToPaneError("Development Set Folder is Empty!");
                        return;
                    }

                    Commons.printToFile(ontologyFile.getParent(), extendedOntologyFileName, ontology.toString());

                    ontologyPathTextField.requestFocus();
                    ontologyPathTextField.setText(new File(ontologyFile.getParent(), extendedOntologyFileName).getAbsolutePath());
                    isOntologyValid = true;
                    int highlightStart = ontologyPathTextField.getText().length() - 8;
                    ontologyPathTextField.select(highlightStart, highlightStart + 4);
                    new java.util.Timer()
                            .schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        output.requestFocus();
                                    }
                                }, 750);

                    appendToPane("Ontology is successfully expanded.");
                    appendToPaneEmphasize(String.format("Expanded ontology is saved into \"%s\"", ontologyFile.getParent()));
                } catch (IOException ex){
                    appendToPaneError("Ontology expansion is not successful: " + ex.getLocalizedMessage());
                }
            }
        }
        else if (source.equals(categorizeButton)) {
            if (!testSetFolderTextField.getText().isEmpty()) {
                if (!isOntologyValid && validateOntology()) {
                    appendToPaneError("There is no valid ontology!");
                    return;
                }
                if (!validateDocumentMap()) {
                    appendToPaneError("Categorization is not successful!");
                    return;
                }

                categorize(true);
            }
        }
        else if (source.equals(evaluateButton)) {
            if (!isOntologyValid)
                validateOntology();
            if (isDocumentMapValid) {
                new EvaluationWorker() {

                    @Override
                    protected String doInBackground() throws Exception {
                        Evaluator.evaluateResultsOfAllDocuments(
                                documentMap, referenceFolderTextField.getText(), defaultListener);
                        return null;
                    }
                }.execute();
            }
            else {
                validateDocumentMap();
                new EvaluationWorker() {
                    @Override
                    protected String doInBackground() throws Exception {
                        Evaluator.evaluateResultsOfAllFiles(
                                predictionFolderTextField.getText(),
                                referenceFolderTextField.getText(),
                                defaultListener);
                        return null;
                    }
                }.execute();
            }
        }
        else if (source.equals(doCategorizeButton)) {
            if (!validateOntology()) {
                appendToPaneError("Ontology could not be loaded!");
                return;
            }
            if (!validateDocumentMap()) {
                appendToPaneError("Documents could not be loaded!");
                return;
            }
            if (!validatePredictionReferenceFolderPath(predictionFolderTextField)) {
                if (!categorize(false)) {
                    appendToPaneError("Prediction path is not valid nor files could not be created");
                    return;
                }
            }
            if (!validatePredictionReferenceFolderPath(referenceFolderTextField)) {
                appendToPaneError("Reference files could not be loaded!");
                return;
            }

            try {
                output.getDocument().remove(0, output.getDocument().getLength());
            } catch (BadLocationException e) {

            }

            new EvaluationWorker() {

                @Override
                protected String doInBackground() throws Exception {
                    Evaluator.evaluateResultsOfAllDocuments(
                            documentMap, referenceFolderTextField.getText(), defaultListener);
                    return null;
                }
            }.execute();
        }
    }

    /**
     * Searches for specific component within container hierarchy.
     * @param container that contains quarried component
     * @param key that component has
     * @param value that put for given key
     * @return quarried component or null if not found
     */
    private JComponent getChildWithProperty(Container container, Object key, Object value) {
        for (Component component: container.getComponents()) {
            if (component instanceof JPanel) {
                return getChildWithProperty((Container) component, key, value);
            } else if (component instanceof JComponent) {
                JComponent target = (JComponent) component;
                Object val = target.getClientProperty(key);
                if (val != null && val.equals(value))
                    return target;
            }
        }
        return null;
    }

    private void appendToPane(String str) {
        appendToPane(str + "\n", Color.WHITE);
    }

    private void appendToPaneEmphasize(String str) {
        appendToPane(str + "\n", Color.BLUE);
    }

    private void appendToPaneWarning(String str) {
        appendToPane(str + "\n", Color.YELLOW);
    }

    private void appendToPaneError(String str) {
        appendToPane(str + "\n", Color.RED);
    }

    private void appendToPane(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attributeSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        attributeSet = sc.addAttribute(attributeSet, StyleConstants.FontFamily, "Consolas");
        attributeSet = sc.addAttribute(attributeSet, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        attributeSet = sc.addAttribute(attributeSet, StyleConstants.Bold, Boolean.TRUE);

        try {
            output.getDocument().insertString(output.getDocument().getLength(), msg, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private boolean validateOntology() {
        if (isOntologyValid) return true;
        String ontologyPath = ontologyPathTextField.getText();
        if (ontologyPath != null && !ontologyPath.isEmpty()) {
            ontologyProgress.setVisible(true);
            SwingUtilities.invokeLater(() -> {
                ontology = Parser.buildOntology(ontologyPath, false);
                ontologyProgress.setVisible(false);
            });
            isOntologyValid = true;
            return true;
        }
        ontologyPathTextField.requestFocus();
        return false;
    }

    private boolean validateDocumentMap() {
        if (isDocumentMapValid) return true;
        String testSetFolderPath = testSetFolderTextField.getText();
        if (testSetFolderPath != null && !testSetFolderPath.isEmpty()) {
            documentProgress.setVisible(true);
            SwingUtilities.invokeLater(() -> {
                try {
                    documentMap = Parser.buildDocumentList(testSetFolderPath);
                    isDocumentMapValid = true;
                } catch (IOException e) {

                }
                documentProgress.setVisible(false);
            });
            return true;
        }
        testSetFolderTextField.requestFocus();
        return false;
    }

    private boolean validatePredictionReferenceFolderPath(JTextField input) {
        String referenceFolderPath = input.getText();
        if (referenceFolderPath != null && !referenceFolderPath.isEmpty()) {
            File referenceFolder = new File(referenceFolderPath);
            if (referenceFolder.exists() && referenceFolder.canRead()) {
                String[] files = referenceFolder.list((dir, name) -> name.endsWith(".a2"));
                if (files != null && files.length != 0)
                    return true;
            }
        }
        return false;
    }

    private boolean categorize(final Boolean shouldPublishResults) {
        final File predictionFolder = new File(PREDICTION_FOLDER_PATH);
        final boolean predictionFolderReady = predictionFolder.mkdirs();

        new SwingWorker<String, String>() {

            @Override
            protected String doInBackground() throws Exception {
                documentMap.values().forEach(document -> {
                    publish(String.format("%s", document.getId()));
                    Categorizer.categorizeDocument(ontology, document, MAX_TRIAL_CNT,
                            new Categorizer.CategorizationListener() {

                                int categoryCnt = 0;
                                StringBuilder sb = new StringBuilder();

                                @Override
                                public void onCategoryAddedToHabitat(Habitat habitat, Term category) {
                                    categoryCnt++;
                                    String categorizationResult = String.format("N%d\tOntoBiotope Annotation:%s Referent:%s",
                                            categoryCnt, habitat.getId(), category.getId());
                                    publish(categorizationResult);
                                    sb.append(categorizationResult).append("\n");
                                }

                                @Override
                                public void onCategorizationEnded() {
                                    if (predictionFolderReady)
                                        Commons.printToFile(PREDICTION_FOLDER_PATH,
                                                document.getId().concat(".a2"), sb.toString());
                                }
                            });
                });
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                super.process(chunks);
                if (shouldPublishResults)
                    chunks.forEach(result -> appendToPane(result));
            }

            @Override
            protected void done() {
                super.done();
                predictionFolderTextField.setText(predictionFolder.getAbsolutePath());
            }
        }.execute();

        return predictionFolderReady;
    }

    private class Tuple<L, R> {
        L lhs;
        R rhs;

        public Tuple(L lhs, R rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    private abstract class EvaluationWorker extends SwingWorker<String, Tuple<Integer, String>> {

        Evaluator.EvaluationListener defaultListener = new Evaluator.EvaluationListener() {
            int habitatCounter = 0;

            @Override
            public void onDocumentEvaluationStart(String documentId) {
                publish(new Tuple<>(0, documentId));
                habitatCounter = 0;
            }

            @Override
            public void onHabitatEvaluated(String documentId, String habitatId, String predictedCategoryId, List<String> referenceCategoryIdList) {
                String habitatEntity = isDocumentMapValid ?
                        documentMap.get(documentId).getHabitatMap().get(habitatId).getEntity() :
                        String.format("N%d ", ++habitatCounter);
                if(!predictedCategoryId.equals("") && referenceCategoryIdList != null && !referenceCategoryIdList.isEmpty()) {
                    StringBuilder sb1 = new StringBuilder();
                    referenceCategoryIdList.forEach((referenceCategoryId) ->
                            sb1.append(isOntologyValid ? ontology.getTerm(referenceCategoryId).getName() : referenceCategoryId).append(","));
                    publish(new Tuple<>(-1, habitatEntity + " -> " +
                            (isOntologyValid ? ontology.getTerm(predictedCategoryId).getName() : predictedCategoryId) +
                            " - " + sb1.deleteCharAt(sb1.length() - 1).toString()));
                } else if(referenceCategoryIdList != null && !referenceCategoryIdList.isEmpty()) {
                    if(predictedCategoryId.equals("")) {
                        referenceCategoryIdList.forEach(referenceCategoryId ->
                                publish(new Tuple<>(2, habitatEntity + " -> " +
                                        (isOntologyValid ? ontology.getTerm(referenceCategoryId).getName() : referenceCategoryId))));
                    }
                } else {
                    publish(new Tuple<>(1, habitatEntity + " -> " +
                            (isOntologyValid ? ontology.getTerm(predictedCategoryId).getName() : predictedCategoryId)));
                }
            }

            @Override
            public void onDocumentEvaluated(String documentId, Stat stat) {
                publish(new Tuple<>(0, stat.toString()));
            }

            @Override
            public void onFolderEvaluated(Stat folderStat, List<Stat> folderStatList) {
                publish(new Tuple<>(2, "Micro Average"));
                publish(new Tuple<>(0, folderStat.toString()));
                publish(new Tuple<>(2, "Macro Average"));
                double precisionMacro = folderStatList.stream()
                        .mapToDouble(stat -> stat.precision)
                        .average().getAsDouble();
                double recallMacro = folderStatList.stream()
                        .mapToDouble(stat -> stat.recall)
                        .average().getAsDouble();
                publish(new Tuple<>(0, String.format("precision: %f\nrecall: %f",
                        precisionMacro, recallMacro)));
            }
        };

        @Override
        protected void process(List<Tuple<Integer, String>> chunks) {
            super.process(chunks);
            chunks.forEach(chunk -> {
                switch (chunk.lhs) {
                    case 0:
                        appendToPane(chunk.rhs);
                        break;
                    case 1:
                        appendToPaneEmphasize(chunk.rhs);
                        break;
                    case 2:
                        appendToPaneWarning(chunk.rhs);
                        break;
                    default:
                        appendToPaneError(chunk.rhs);
                }
            });
        }
    }
}
