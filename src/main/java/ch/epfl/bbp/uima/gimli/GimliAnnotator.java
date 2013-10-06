package ch.epfl.bbp.uima.gimli;

import static java.lang.Integer.MAX_VALUE;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * UIMA wrapper for <a
 * href="http://bioinformatics.ua.pt/support/gimli/doc/index.html">Gimly</a>
 * protein NER.
 * 
 * @author renaud.richardet@epfl.ch
 */
// @TypeCapability(inputs = , outputs = )
public class GimliAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOG = LoggerFactory
            .getLogger(GimliAnnotator.class);

    public static final String PARAM_PARSING_FORWARD = "parsingForward";
    @ConfigurationParameter(name = PARAM_PARSING_FORWARD, defaultValue = "true", //
    description = "whether to parse forward (true) or backward", mandatory = true)
    private boolean parsingForward;
    private Parsing parsing;

    public static final String PARAM_ENTITY_TYPE = "entityType";
    @ConfigurationParameter(name = PARAM_ENTITY_TYPE, defaultValue = "3", //
    description = "", mandatory = true)
    private int entityType;
    private EntityType entity = EntityType.protein;

    public static final String PARAM_LABEL_FORMAT = "labelFormat";
    @ConfigurationParameter(name = PARAM_LABEL_FORMAT, defaultValue = "0", //
    description = "", mandatory = true)
    private int labelFormat;
    private LabelFormat format;

    public static final String PARAM_MODEL = "model";
    @ConfigurationParameter(name = PARAM_MODEL, description = "path to models, see "
            + "https://github.com/davidcampos/gimli/tree/master/resources/models/gimli", mandatory = true)
    private String modelPath;

    public static final String PARAM_GDEP_FILES = "gDepFilesPath";
    @ConfigurationParameter(name = PARAM_GDEP_FILES, description = "path to "
            + "gdep files, located in resources/tools. must be unzipped first",//
    mandatory = false, defaultValue = "resources/tools/gdep/")
    private String gDepFilesPath;

    public static final String PARAM_FEATURES = "features";
    @ConfigurationParameter(name = PARAM_FEATURES, description = "path to features file", mandatory = true)
    private String featuresPath;

    public static final String ANNOTATION_SENTENCE = "annotationSentence";
    @ConfigurationParameter(name = ANNOTATION_SENTENCE, description = "name of Sentence annotation class", mandatory = true)
    private String sentenceClassName;
    private Class<? extends org.apache.uima.jcas.tcas.Annotation> sentenceClass;

    public static final String ANNOTATION_NAMED_ENTITY = "annotationNE";
    @ConfigurationParameter(name = ANNOTATION_NAMED_ENTITY, description = "name of Named Entity annotation class", mandatory = true)
    private String namedEntityClassName;

    private GDepParser parser;
    private CRFModel crfModel;

    private Class<? extends org.apache.uima.jcas.tcas.Annotation> neClass;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException {
        super.initialize(context);

        if (parsingForward)
            parsing = Parsing.FW;
        else
            parsing = Parsing.BW;

        switch (entityType) {
        case 0:
            entity = EntityType.cell_line;
            break;
        case 1:
            entity = EntityType.cell_type;
            break;
        case 2:
            entity = EntityType.DNA;
            break;
        case 3:
            entity = EntityType.protein;
            break;
        case 4:
            entity = EntityType.RNA;
            break;
        default:
            throw new ResourceInitializationException(new Exception(
                    "Invalid value for EntityType: " + entity));
        }

        switch (labelFormat) {
        case 0:
            format = LabelFormat.BIO;
            break;
        case 1:
            format = LabelFormat.BMEWO;
            break;
        case 2:
            format = LabelFormat.IO;
            break;
        default:
            throw new ResourceInitializationException(new Exception(
                    "Invalid value for LabelFormat: " + format));
        }

        //
        try {
            sentenceClass = (Class<? extends org.apache.uima.jcas.tcas.Annotation>) Class
                    .forName(sentenceClassName);
        } catch (ClassNotFoundException e1) {
            throw new ResourceInitializationException(new Exception(
                    "Could not load class for Sentence " + sentenceClassName));
        }
        try {
            neClass = (Class<? extends org.apache.uima.jcas.tcas.Annotation>) Class
                    .forName(namedEntityClassName);
        } catch (ClassNotFoundException e1) {
            throw new ResourceInitializationException(new Exception(
                    "Could not load class for Named Entity "
                            + namedEntityClassName));
        }

        if (!new File(featuresPath).exists())
            throw new ResourceInitializationException(new Exception(
                    "No file for featuresPath at " + featuresPath));

        if (!new File(modelPath).exists())
            throw new ResourceInitializationException(new Exception(
                    "No file for modelPath at " + modelPath));

        try {
            // Load model configuration
            ModelConfig mc = new ModelConfig(featuresPath);
            // Get CRF model
            crfModel = new CRFModel(mc, parsing, modelPath);

            // GDepParser
            if (!new File(gDepFilesPath).exists()
                    || !new File(gDepFilesPath).isDirectory())
                throw new ResourceInitializationException(new Exception(
                        "No directory for  GDep files at " + gDepFilesPath));
            try {

            } catch (Exception e) {
                throw new ResourceInitializationException(new Exception(
                        "Could not launch parser, check that the gdep script is executable at "
                                + gDepFilesPath));
            }
            parser = new MyGDepParser(true, gDepFilesPath);
            parser.launch();

            LOG.debug("done initializing Gimly");
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    /** A bean to keep track of the lag */
    private static class MySentence {
        int end;
        Sentence sentence;
        int begin;

        public MySentence(Sentence sentence, int begin, int end) {
            this.sentence = sentence;
            this.begin = begin;
            this.end = end;
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        // Add sentences to corpus
        Corpus corpus = new Corpus(format, entity);
        // we keep this to keep trac of the lag (see below)
        List<MySentence> mySentences = new ArrayList<MySentence>();

        for (org.apache.uima.jcas.tcas.Annotation jSentence : JCasUtil.select(
                jCas, sentenceClass)) {
            String sentenceText = jSentence.getCoveredText();
            Sentence sentence = new Sentence(corpus);
            try {
                sentence.parse(parser, sentenceText);
                corpus.addSentence(sentence);
                mySentences.add(new MySentence(sentence, jSentence.getBegin(),
                        jSentence.getEnd()));
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
        for (MySentence mySentence : mySentences) {

            // LOG.warn(mySentence.toString());
            // LOG.warn(mySentence.sentence.toExportFormat());

            for (int ai = 0; ai < mySentence.sentence.getNumberAnnotations(); ai++) {
                Annotation annotation = mySentence.sentence.getAnnotation(ai);
                String annotationText = annotation.getText().trim();

                // LOG.warn(ai
                // + "::"
                // + jCas.getDocumentText().substring(mySentence.begin,
                // mySentence.end));
                // LOG.warn(ai + ":" + annotation.toString());
                // LOG.warn(ai + ":" + annotation.getText());

                // indexes do not take into acct empty spaces
                Token tokenStart = mySentence.sentence.getToken(annotation
                        .getStartIndex());
                int start = tokenStart.getStart() + mySentence.begin;
                Token tokenEnd = mySentence.sentence.getToken(annotation
                        .getEndIndex());
                int end = tokenEnd.getEnd() + mySentence.begin + 1;

                // sanity check
                String uimaAnnotationText = jCas.getDocumentText().substring(
                        start, end);

                // trying exact match
                if (uimaAnnotationText.equals(annotationText)) {
                    add(jCas, start, end);

                } else {

                    String jSentenceText = jCas.getDocumentText().substring(
                            mySentence.begin, mySentence.end);
                    // occurence is always AFTER start, since normalization adds
                    // space
                    int indexOf = jSentenceText.indexOf(annotationText, start
                            - mySentence.begin);
                    List<Integer> starts = new ArrayList<Integer>();
                    while (indexOf > -1) {
                        starts.add(indexOf);
                        indexOf = jSentenceText.indexOf(annotationText,
                                indexOf + 1);
                    }
                    if (starts.size() == 1) {
                        // one single match --> add it
                        add(jCas, mySentence.begin + starts.get(0),
                                mySentence.begin + starts.get(0)
                                        + annotationText.length());

                    } else if (starts.size() > 1) {
                        // pick the one closer to start/end
                        int smallestDelta = MAX_VALUE;
                        int smallestDeltaIndex = -1;
                        for (int i = 0; i < starts.size(); i++) {
                            int delta = starts.get(i)
                                    - (start - mySentence.begin);
                            if (delta < smallestDelta) {
                                smallestDelta = delta;
                                smallestDeltaIndex = i;
                            }
                        }
                        add(jCas,
                                mySentence.begin
                                        + starts.get(smallestDeltaIndex),
                                mySentence.begin
                                        + starts.get(smallestDeltaIndex)
                                        + annotationText.length());

                    } else if (starts.size() == 0) {
                        LOG.warn("UIMA annotation not matching: '{}' vs '{}'",
                                uimaAnnotationText, annotation.getText().trim());
                    }
                }
            }
        }
    }

    private void add(JCas jCas, int start, int end)
            throws AnalysisEngineProcessException {
        try {
            Constructor<? extends org.apache.uima.jcas.tcas.Annotation> constructor;
            constructor = neClass.getConstructor(JCas.class);
            org.apache.uima.jcas.tcas.Annotation uimaAnnotation = constructor
                    .newInstance(jCas);
            uimaAnnotation.setBegin(start);
            uimaAnnotation.setEnd(end);
            uimaAnnotation.addToIndexes();
            // LOG.warn("adding ne {} [{}]", uimaAnnotation.getCoveredText(),
            // uimaAnnotation.getBegin() + " " + uimaAnnotation.getEnd());
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
        // Terminate parser
        parser.terminate();
    }

    /** To overwrite the directory */
    private static class MyGDepParser extends GDepParser {

        public MyGDepParser(boolean doTokenisation, String dirPath) {
            super(doTokenisation);
            dir = new File(dirPath);
        }
    }
}
