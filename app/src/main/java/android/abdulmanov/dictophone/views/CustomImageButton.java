package android.abdulmanov.dictophone.views;

import android.abdulmanov.dictophone.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

public class CustomImageButton extends AppCompatImageButton {

    public CustomImageButton(Context context) {
        super(context);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context
                .obtainStyledAttributes(attrs, R.styleable.CustomImageButton);
        setEnabled(attributes.getBoolean(R.styleable.CustomImageButton_enabled, true));
        attributes.recycle();
    }
}
