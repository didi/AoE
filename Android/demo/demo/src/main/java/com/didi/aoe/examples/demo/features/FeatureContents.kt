package com.didi.aoe.examples.demo.features

import com.didi.aoe.examples.demo.R
import com.didi.aoe.examples.demo.ShowcaseApp
import java.util.*

/**
 * @author noctis
 */
object FeatureContents {
    private val FEATURES = ArrayList<Feature>()

    val features: List<Feature>
        get() = FEATURES

    init {

        FEATURES.add(Feature(
                R.id.action_featuresFragment_to_mnistFeatureFragment,
                ShowcaseApp.context?.getString(R.string.title_mnist),
                ShowcaseApp.context?.getString(R.string.description_mnist),
                R.drawable.bg_mnist))

        FEATURES.add(Feature(
                R.id.action_featuresFragment_to_pfldFeatureFragment,
                ShowcaseApp.context?.getString(R.string.title_pfld),
                ShowcaseApp.context?.getString(R.string.description_pfld),
                R.drawable.bg_mnist))

        FEATURES.add(Feature(
                R.id.action_featuresFragment_to_classificationFeartureFragment,
                ShowcaseApp.context?.getString(R.string.title_classification),
                ShowcaseApp.context?.getString(R.string.description_classification),
                R.drawable.bg_squeeze))
    }
}
