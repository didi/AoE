package com.didi.aoe.examples.demo.features;

import com.didi.aoe.examples.demo.R;

import java.util.ArrayList;
import java.util.List;

import static com.didi.aoe.examples.demo.ShowcaseApp.getContext;

/**
 * @author noctis
 */
public class FeatureContents {
    private static final List<Feature> FEATURES = new ArrayList<>();

    static {

        FEATURES.add(new Feature(
                R.id.action_featuresFragment_to_mnistFeatureFragment,
                getContext().getString(R.string.title_mnist),
                getContext().getString(R.string.description_mnist),
                R.drawable.bg_mnist));

        FEATURES.add(new Feature(
                R.id.action_featuresFragment_to_squeezeFeartureFragment,
                getContext().getString(R.string.title_squeeze),
                getContext().getString(R.string.description_squeeze),
                R.drawable.bg_squeeze));
        FEATURES.add(new Feature(
                R.id.action_featuresFragment_to_recognizeFeartureFragment,
                getContext().getString(R.string.title_recognize),
                getContext().getString(R.string.description_recognize),
                R.drawable.bg_squeeze));

    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }
}
