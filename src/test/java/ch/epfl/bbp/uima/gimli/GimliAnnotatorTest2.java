package ch.epfl.bbp.uima.gimli;

import static ch.epfl.bbp.uima.gimli.GimliAnnotator.ANNOTATION_NAMED_ENTITY;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.ANNOTATION_SENTENCE;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.PARAM_FEATURES;
import static ch.epfl.bbp.uima.gimli.GimliAnnotator.PARAM_MODEL;
import static org.junit.Assert.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Test;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;

import ch.epfl.bbp.typesystem.Protein;
import ch.epfl.bbp.typesystem.Sentence;

public class GimliAnnotatorTest2 {

    @Test
    public void testGimli() throws Exception {

        TypeSystemDescription tsd = TypeSystemDescriptionFactory
                .createTypeSystemDescriptionFromPath("src/test/java/test-typesystem.xml");

        JCas jCas = JCasFactory.createJCas(tsd);

        jCas.setDocumentText("Endo H and PNGase F glycosidases (Boehringer Mannheim) were used per the manufacture's protocol with the final concentration of glycosidases at 0.16 U/ml and 13 U/ml, respectively, for 20–24 h at 37 °C. After solubilization with 1.5% N-laurylsarcosine, the lysate was incubated with 20 g/ml DNase I (Boehringer Mannheim, Indianapolis, IN) and 10 mM MgCl. To demonstrate that indeed the shift in molecular mass is due to N-linked glycosylation, oocytes injected with Kv4.2 and HA/DPP10 were injected and incubated with 2.5 mg/ml tunicamycin to prevent glycosylation. Cells were lysed in a buffer containing 50 mM Tris at pH 7.5, 1% Triton X-100, 150 mM NaCl, 5 mM EDTA, 1 μg/ml pepstatin A, 1 μg/ml leupeptin, 2 μg/ml aprotinin, 1 mM PMSF, 0.1 mg/ml benz- 21. Transfected cells were harvested and rinsed twice in the presence of cold PBS (1) supplemented with proteases inhibitors (100 lM Pefabloc, 1 lM leupeptin, 1 lM pepstatin, 0.1 lM aprotinin, 1 mM DTT, and 1 mM EDTA). After the completion of electrophysiological recordings, 10 oocytes were added to 200 ml of homogenization solution (100 mM NaCl, 1% Triton-X-100, 1X protease inhibitor; Roche Diagnostics, Indianapolis, IN, 5 mM EDTA, 5 mM EGTA, and 20 mM Tris-HCl at pH 7.6) in 1.5 ml microcentrifuge tubes on ice. These data were reasonably fit slow time constants from protocols were compiled into a single with a single Boltzmann function having a half-inactivation voltplot (Fig. 3F ) and fit with a two-state model derived from the HH age of 50.4 2.6 mV and a slope factor of 20 2 mV (Fig. 4 B). To examine the binding of contactin to the N and C termini and the intracellular loops of Na 1.3 in vitro, glutathione-Sepharose beads loaded with 0.5 g of protein of GST, serving as a negative control, GST-Na 1.3N, GST-Na 1.3L1, GST-Na 1.3L2, GST-Na 1.3L3, or GST-Na 1.3C were incubated with extracts (500 g) from HEK 293 cells transfected with an expression plasmid encoding either GFP alone or full-length Ctn–GFP, as indicated, in an equal volume of buffer AM (2 mM Tris-HCl, pH 7.9; 0.1 mM EDTA; 10% glycerol; 5 mM MgCl ; 0.5 mM DTT; protease inhibitors) supplemented with 100 mM KCl and 0.5 mg/ml bovine serum albumin.");

        AnalysisEngine sentenceSplitter = createEngine(
                DotSentenceSplitterAnnotator.class, tsd);
        AnalysisEngine gimli = createEngine(GimliAnnotator.class, tsd, //
                PARAM_MODEL, "resources/model/bc2gm_bw_o2.gz",//
                PARAM_FEATURES, "src/main/resources/config/bc.config",//
                ANNOTATION_SENTENCE, Sentence.class.getName(),//
                ANNOTATION_NAMED_ENTITY, Protein.class.getName());
        runPipeline(jCas, sentenceSplitter, gimli);

        Collection<Protein> prots = select(jCas, Protein.class);

        for (Protein protein : prots) {
            System.out.println(protein);
        }
        assertEquals(10, prots.size());
    }
}
