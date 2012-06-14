package edu.washington.cs.knowitall.extractor.conf;

import static org.junit.Assert.assertTrue;
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.model.ListEventStream;

import org.junit.Test;

import edu.washington.cs.knowitall.extractor.conf.featureset.BooleanFeatureSet;
import edu.washington.cs.knowitall.extractor.conf.opennlp.OpenNlpConfFunction;

public class OpenNlpClassifierConfFunctionTest {

    @Test
    public void testGetConf() throws Exception {
        BooleanFeatureSet<String> features = StringFeatures.featureSet;
        GISModel model = GIS.trainModel(
                new ListEventStream(StringFeatures.dataSet.getInstances()), 100, 0);
        OpenNlpConfFunction<String> conf = new OpenNlpConfFunction<String>(model, features);
        double janeConf = conf.getConf("jane");
        double ofConf = conf.getConf("of");
        System.out.println(ofConf);
        System.out.println(janeConf);
        assertTrue(ofConf < janeConf);
        assertTrue(ofConf < 1.0);
        assertTrue(0.0 < janeConf);
    }
}
