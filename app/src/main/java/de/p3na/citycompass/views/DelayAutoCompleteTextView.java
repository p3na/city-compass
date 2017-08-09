package de.p3na.citycompass.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Author: Christian Hansen
 * Date: 05.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * Implementation inspired by:<br>
 * <a href="http://makovkastar.github.io/blog/2014/04/12/android-autocompletetextview-with-suggestions-from-a-web-service/">DelayAutoCompleteTextView</a>
 */

public class DelayAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    private static final int TEXT_CHANGED = 100;
    private static final int AUTOCOMPLETE_DELAY = 750;

    private ProgressBar mProgressBar;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {

        if (mProgressBar != null) {
            mProgressBar.setVisibility(VISIBLE);
        }
        mHandler.removeMessages(TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TEXT_CHANGED, text), AUTOCOMPLETE_DELAY);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(GONE);
        }
        super.onFilterComplete(count);
    }
}
