package com.example.chatappprueba3.ui.chats;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import android.app.Fragment;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.R;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class ChatImageFragment extends Fragment {

    private TextView textViewUserName;
    private TextView textViewFecha;
    private ImageView imageView;

    //Para hacer zoom
    //TODO al final
    /*Matrix matrix = new Matrix();
    Float scale = 1f;
    ScaleGestureDetector scaleGestureDetector;*/


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chat_image_fragment, null);

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbarImageFragment);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        textViewUserName = view.findViewById(R.id.textViewImageFragmentName);
        textViewFecha = view.findViewById(R.id.textViewImageFragmentFecha);
        imageView = view.findViewById(R.id.imageViewImageSent);




        //Recuperar datos
        String name = getArguments().getString("Name");
        String date = getArguments().getString("Date");
        String image = getArguments().getString("Image");
        Glide.with(getActivity().getApplicationContext()).load(image).into(imageView);

        textViewUserName.setText(name);
        textViewFecha.setText(date);

        return view;
    }

    /*private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale = scale*detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5f));
            imageView.setImageMatrix(matrix);
            return true;
        }
    }*/



}
