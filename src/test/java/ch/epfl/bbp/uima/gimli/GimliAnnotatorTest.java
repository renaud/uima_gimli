package ch.epfl.bbp.uima.gimli;

import static ch.epfl.bbp.uima.testutils.UimaTests.getTokenizedTestCas;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;
import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import ch.epfl.bbp.uima.types.Protein;
import ch.epfl.bbp.uima.typesystem.Prin;

public class GimliAnnotatorTest {

    @Test
    public void test() throws Exception {

        JCas jcas = getTokenizedTestCas("This is a dummy sentence about CAMKII. BRCA1 and BRCA2 are human genes that belong"
                + " to a class of genes known as tumor suppressors.");
        AnalysisEngine gimli = createPrimitive(GimliAnnotator.class);
        runPipeline(jcas, gimli);

        Collection<Protein> prots = select(jcas, Protein.class);
        assertEquals(3, prots.size());
        Prin.t(prots);

        Iterator<Protein> it = prots.iterator();
        Protein prot = it.next();
        assertEquals("CAMKII", prot.getCoveredText());
        prot = it.next();
        assertEquals("BRCA1", prot.getCoveredText());
        prot = it.next();
        assertEquals("BRCA2", prot.getCoveredText());
    }
}
