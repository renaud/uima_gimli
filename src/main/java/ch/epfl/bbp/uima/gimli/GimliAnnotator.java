package ch.epfl.bbp.uima.gimli;

import static ch.epfl.bbp.uima.BlueCasUtil.getHeaderDocId;
import static ch.epfl.bbp.uima.BlueUima.BLUE_UIMA_ROOT;
import static ch.epfl.bbp.uima.typesystem.TypeSystem.SENTENCE;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import pt.ua.tm.gimli.annotator.Annotator;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.gimli.model.CRFModel;
import pt.ua.tm.gimli.processing.Abbreviation;
import pt.ua.tm.gimli.processing.Parentheses;
import ch.epfl.bbp.uima.types.Protein;
import ch.epfl.bbp.uima.typesystem.TypeSystem;

/**
 * UIMA wrapper for <a
 * href="http://bioinformatics.ua.pt/support/gimli/doc/index.html">Gimly</a>
 * protein NER.
 * 
 * @author renaud.richardet@epfl.ch
 */
@TypeCapability(inputs = SENTENCE, outputs = TypeSystem.GENERAL_ENGLISH)
public class GimliAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOG = LoggerFactory
            .getLogger(GimliAnnotator.class);

    public static final String GIMLY_ROOT = BLUE_UIMA_ROOT
            + "modules/bluima_gimli/";

    final Parsing parsing = Parsing.FW;
    final EntityType entity = EntityType.protein;;
    final LabelFormat format = LabelFormat.BIO;
    final String modelPath = GIMLY_ROOT
            + "src/main/resources/models/bc2gm_bw_o2.gz";
    final String featuresPath = GIMLY_ROOT
            + "src/main/resources/config/bc.config";

    private GDepParser parser;
    private CRFModel crfModel;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException {
        super.initialize(context);
        try {
            // Load model configuration
            ModelConfig mc = new ModelConfig(featuresPath);
            // Get CRF model
            crfModel = new CRFModel(mc, parsing, modelPath);
            // GDepParser
            parser = new GDepParser(true);
            parser.launch();
            LOG.debug("done initializing Gimly");
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    /** A bean to keep track of the lag */
    private static class MySentence {
        de.julielab.jules.types.Sentence jSentence;
        Sentence sentence;

        public MySentence(Sentence sentence,
                de.julielab.jules.types.Sentence jSentence) {
            this.sentence = sentence;
            this.jSentence = jSentence;
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        // Add sentences to corpus
        Corpus corpus = new Corpus(format, entity);
        // we keep this to keep trac of the lag (see below)
        List<MySentence> mySentences = new ArrayList<MySentence>();

        for (de.julielab.jules.types.Sentence jSentence : JCasUtil.select(jCas,
                de.julielab.jules.types.Sentence.class)) {
            String sentenceText = jSentence.getCoveredText();
            Sentence sentence = new Sentence(corpus);
            try {
                sentence.parse(parser, sentenceText);
                corpus.addSentence(sentence);
                mySentences.add(new MySentence(sentence, jSentence));
            } catch (GimliException e) {
                LOG.info("Error parsing sentence text '" + sentenceText + "'",
                        e);
            }
        }

        // Annotate corpus
        Annotator annotator = new Annotator(corpus);
        annotator.annotate(crfModel);
        // Post-process removing annotations with odd number of parentheses
        Parentheses.processRemoving(corpus);
        // Post-process by adding abbreviation annotations
        Abbreviation.process(corpus);

        // Access Gimly annotations and add to UIMA.
        int lag = 0; // lag, for each sentence
        for (MySentence mySentence : mySentences) {
            // LOG.debug(mySentence.sentence.toExportFormat());

            for (int ai = 0; ai < mySentence.sentence.getNumberAnnotations(); ai++) {
                Annotation annotation = mySentence.sentence.getAnnotation(ai);

                // LOG.debug(annotation.toString());
                // LOG.debug(annotation.getText());

                // indexes do not take into acct empty spaces
                Token tokenStart = mySentence.sentence.getToken(annotation
                        .getStartIndex());
                int start = tokenStart.getStart() + tokenStart.getIndex() + lag;
                Token tokenEnd = mySentence.sentence.getToken(annotation
                        .getEndIndex());
                int end = tokenEnd.getEnd() + tokenEnd.getIndex() + lag + 1;

                // sanity check
                String uimaAnnotationText = jCas.getDocumentText().substring(
                        start, end);
                if (!uimaAnnotationText.equals(annotation.getText().trim())) {
                    String pmId = getHeaderDocId(jCas);
                    LOG.warn(
                            "UIMA annotation not matching: '{}' vs '{}' on pmid {}",
                            new Object[] { uimaAnnotationText,
                                    annotation.getText().trim(), pmId });

                } else {
                    Protein p = new Protein(jCas, start, end);
                    p.addToIndexes();
                    // LOG.debug("adding protein {}", p.getCoveredText());
                }
            }
            lag += mySentence.jSentence.getEnd();
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
        // Terminate parser
        parser.terminate();
    }
}
