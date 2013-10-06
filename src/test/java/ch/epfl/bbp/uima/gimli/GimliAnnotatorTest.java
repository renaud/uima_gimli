package ch.epfl.bbp.uima.gimli;

import static ch.epfl.bbp.uima.gimli.GimliAnnotator.ANNOTATION_NAMED_ENTITY;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.ANNOTATION_SENTENCE;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.PARAM_FEATURES;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.PARAM_MODEL;
import static org.junit.Assert.assertEquals;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Test;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;

import ch.epfl.bbp.typesystem.Protein;
import ch.epfl.bbp.typesystem.Sentence;

public class GimliAnnotatorTest {

    @Test
    public void testGimli() throws Exception {

        TypeSystemDescription tsd = TypeSystemDescriptionFactory
                .createTypeSystemDescriptionFromPath("src/test/java/test-typesystem.xml");

        JCas jCas = JCasFactory.createJCas(tsd);
        jCas.setDocumentText("This is a dummy sentence about CAMKII. BRCA1 and BRCA2 are human genes that belong"
                + " to a class of genes known as tumor suppressors.");

        AnalysisEngine sentenceSplitter = createEngine(
                DotSentenceSplitterAnnotator.class, tsd);
        AnalysisEngine gimli = createEngine(GimliAnnotator.class, tsd, //
                PARAM_MODEL, "resources/model/bc2gm_bw_o2.gz",//
                PARAM_FEATURES, "src/main/resources/config/bc.config",//
                ANNOTATION_SENTENCE, Sentence.class.getName(),//
                ANNOTATION_NAMED_ENTITY, Protein.class.getName());
        runPipeline(jCas, sentenceSplitter, gimli);

        Collection<Protein> prots = select(jCas, Protein.class);
        assertEquals(3, prots.size());

        Iterator<Protein> it = prots.iterator();
        Protein prot = it.next();
        assertEquals("CAMKII", prot.getCoveredText());
        prot = it.next();
        assertEquals("BRCA1", prot.getCoveredText());
        prot = it.next();
        assertEquals("BRCA2", prot.getCoveredText());
    }
}
