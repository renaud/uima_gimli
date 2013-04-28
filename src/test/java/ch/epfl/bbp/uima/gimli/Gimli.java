package ch.epfl.bbp.uima.gimli;

import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.gimli.external.wrapper.Parser;
import pt.ua.tm.gimli.model.CRFModel;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

public class Gimli {

    public static void main(String[] args) throws GimliException {

        Sentence s = new Sentence(null);

        Parser parser = new GDepParser(true);
        s.parse(parser, "hello");
        

        // CRFModel model = null;
        // String text;
        //
        // InstanceList instances = new
        // InstanceList(model.getCRF().getInputPipe());
        //
        // // text = s.toExportFormat();
        // instances.addThruPipe(new Instance(text, null, i, null));
        //
        // NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(
        // model.getCRF());
        // LabelTag p;
        // for (Instance i : instances) {
        // Sequence input = (Sequence) i.getData();
        // Transducer tran = crfTrainer.getTransducer();
        // Sequence pred = tran.transduce(input);
        //
        // // Get score
        // double logScore = new SumLatticeDefault(model.getCRF(), input, pred)
        // .getTotalWeight();
        // double logZ = new SumLatticeDefault(model.getCRF(), input)
        // .getTotalWeight();
        // double prob = Math.exp(logScore - logZ);
        //
        // for (int j = 0; j < pred.size(); j++) {
        // p = LabelTag.valueOf(pred.get(j).toString());
        // corpus.getSentence(counter).getToken(j).setLabel(p);
        // }
        // corpus.getSentence(counter).addAnnotationsFromTags(prob);
        // counter++;
        // }

    }

}
