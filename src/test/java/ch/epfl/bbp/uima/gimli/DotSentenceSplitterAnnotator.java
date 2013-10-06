package ch.epfl.bbp.uima.gimli;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

import ch.epfl.bbp.typesystem.Sentence;

/**
 * Splits an input text into {@link Sentence}s at each dot.
 * 
 * @author renaud.richardet@epfl.ch
 */
public class DotSentenceSplitterAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {

        String text = jcas.getDocumentText();

        if (text.indexOf('.') == -1) {// no dots, return whole sentence
            Sentence sentence = new Sentence(jcas, 0, text.length());
            sentence.addToIndexes();

        } else {
            int i = 0;
            for (String sentenceText : text.split("\\.")) {
                Sentence sentence = new Sentence(jcas, i, i
                        + sentenceText.length());
                sentence.addToIndexes();
                i += sentenceText.length();
            }
        }
    }
}