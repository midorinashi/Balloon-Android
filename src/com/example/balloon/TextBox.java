package com.example.balloon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

//because fonts are not included in textviews???
// http://www.techrepublic.com/article/pro-tip-extend-androids-textview-to-use-custom-fonts/
public class TextBox extends TextView {

	public TextBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	public TextBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
		
	}
	
	public TextBox(Context context) {
		super(context);
		init(null);
	}
	
	private void init(AttributeSet attrs) {
		if (attrs != null) {
			 TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextBox);
			 String fontName = a.getString(R.styleable.TextBox_fontName);
			 if (fontName!=null) {
				 Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(),
						 "fonts/" + fontName);
				 setTypeface(myTypeface);
			 }
			 a.recycle();
		}
	}

}
