package Utils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

import java.io.StringReader;
import java.util.List;

/**
 * Created by hakansahin on 01/03/16.
 */
public class LEXParser {

    public static final String parserModel = "src/main/resources/lexparser/englishPCFG.ser.gz";
    public static LEXParser me;

    private LexicalizedParser lp;
    private TokenizerFactory<CoreLabel> tokenizerFactory;
    private TreebankLanguagePack tlp;
    private GrammaticalStructureFactory gsf;

    public LEXParser(){
        lp = LexicalizedParser.loadModel(parserModel);

        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
        gsf = tlp.grammaticalStructureFactory();
    }

    public static LEXParser initLEXParser(){
        return (me == null) ? me = new LEXParser() : me;
    }

    public Tree parseDocument(String text){

        Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(text));
        List<CoreLabel> rawWords2 = tok.tokenize();
        return lp.apply(rawWords2);


        // You can also use a TreePrint object to print trees and dependencies
//        TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
//        tp.printTree(parse);
    }

    public List<TypedDependency> getTypedDependencies(Tree parse){

        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
        System.out.println();

        return tdl;
    }
}
