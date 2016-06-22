package me.arunpadiyan.netaccess;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by narendra1 on 04/05/16.
 */
public class NATextView extends TextView {

    private MyApplication mApp;

    public NATextView(Context context) {
        super(context);
        init(null);
    }

    public NATextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NATextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NATextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mApp = (MyApplication) getContext().getApplicationContext();

        if (attrs != null) {
            TypedArray a = mApp.obtainStyledAttributes(attrs, R.styleable.CabTextView);
            int fontName = a.getInteger(R.styleable.CabTextView_fontName, Utils.FONT_FJORD_REGULAR);
            switch (fontName) {
                case Utils.FONT_NORMAL_REGULAR:
                    break;
                case Utils.FONT_FJORD_REGULAR:
                    setTypeface(mApp.getFjordOneRegular());
                    break;

            }
            a.recycle();
        }
    }
}
