package com.j32productions.balloon;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditBox extends EditText {
    public EditBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public EditBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditBox(Context context) {
        super(context);
        init();
    }

    private void init() {
    	/*
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
            setTypeface(tf);
        }*/
    }
}
