package com.didi.aoe.examples.demo.features.mnist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.didi.aoe.examples.demo.R;
import com.didi.aoe.examples.demo.features.BaseFeartureFragment;
import com.didi.aoe.features.mnist.MnistInterpreter;
import com.didi.aoe.features.mnist.model.SketchModel;
import com.didi.aoe.features.mnist.render.SketchRenderer;
import com.didi.aoe.features.mnist.widget.SketchView;
import com.didi.aoe.library.core.AoeClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author noctis
 */
public class MnistFeatureFragment extends BaseFeartureFragment {
    private static final String TAG = "Frag.mnist";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private AoeClient mClient;
    private TextView mResultTextView;
    private SketchModel mSketchModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new AoeClient(requireContext(), "mnist",
                new AoeClient.Options()
                        .setInterpreter(MnistInterpreter.class)/*
                        .useRemoteService(false)*/,
                "mnist");
        int resultCode = mClient.init();
        Log.d(TAG, "AoeClient init: " + resultCode);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mnist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SketchView sketchView = view.findViewById(R.id.sv_sketch);
        mSketchModel = new SketchModel(28, 28);
        sketchView.setRenderer(new SketchRenderer(mSketchModel));
        sketchView.setLifecycleOwner(this);

        mResultTextView = view.findViewById(R.id.tv_result);

        view.findViewById(R.id.btn_recongnize).setOnClickListener(v -> mExecutor.execute(() -> {
            if (mClient != null) {
                Object result = mClient.process(mSketchModel.getPixelData());
                if (result instanceof Integer) {
                    int num = (int) result;
                    Log.d(TAG, "num: " + num);
                    mResultTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            mResultTextView.setText((num == -1) ? "Not recognized." : String.valueOf(num));
                        }
                    });
                }

            }
        }));

        view.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            mSketchModel.clear();
            sketchView.resetAndInvalidate();
            mResultTextView.setText("");
        });
    }

    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.release();
        }
        super.onDestroy();
    }
}
