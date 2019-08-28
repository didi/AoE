package com.didi.aoe.examples.demo.features.recognize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.didi.aoe.examples.demo.R;
import com.didi.aoe.examples.demo.features.BaseFeartureFragment;
import com.didi.aoe.extensions.parcel.kryo.KryoParcelImpl;
import com.didi.aoe.features.recognize.RecognizeMnnInterpreter;
import com.didi.aoe.library.core.AoeClient;

import java.io.FileNotFoundException;

public class RecognizeFeartureFragment extends BaseFeartureFragment {
    private static final int SELECT_IMAGE = 1;
    private static final String TAG = "Frag.squeeze";
    private AoeClient mClient;

    private TextView infoResult;
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new AoeClient(requireContext(), "recognize-mnn",
                new AoeClient.Options()
                        .setInterpreter(RecognizeMnnInterpreter.class)
                        .setParceler(KryoParcelImpl.class)
                        .useRemoteService(false),
                "recognize-mnn");
        int resultCode = mClient.init();
        Log.d(TAG, "AoeClient init: " + resultCode);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_squeeze, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoResult = view.findViewById(R.id.tv_result);
        infoResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        imageView = view.findViewById(R.id.iv_sketch);

        Button buttonImage = view.findViewById(R.id.btn_image_pick);
        buttonImage.setOnClickListener(arg0 -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, SELECT_IMAGE);
        });

    }

    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.release();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            if (requestCode == SELECT_IMAGE) {
                try {
                    Bitmap bitmap = decodeUri(selectedImage);

                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    // resize to 227x227
                    yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false);

                    rgba.recycle();

                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFoundException");
                    return;
                }
            }

            if (yourSelectedImage == null)
                return;

            Object result = mClient.process(yourSelectedImage);

            if (result instanceof String) {
                infoResult.setText((String) result);

            } else {
                infoResult.setText("detect failed");
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (width_tmp / 2 >= REQUIRED_SIZE
                && height_tmp / 2 >= REQUIRED_SIZE) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
