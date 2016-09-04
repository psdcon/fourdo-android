package com.example.paul.fourdo;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Paul on 26/04/2015.
 */
public class CustomEditText extends EditText {

    private Context context;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // Slide away the edit text
            ((MainActivity)context).disableAddTask();
//            Toast.makeText(context, "Custom back!", Toast.LENGTH_SHORT).show();
//            return true;  // So it is not propagated.

        }
        return super.dispatchKeyEvent(event);
    }
}
