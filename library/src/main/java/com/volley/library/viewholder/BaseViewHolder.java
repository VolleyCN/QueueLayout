package com.volley.library.viewholder;

import android.graphics.Canvas;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

public class BaseViewHolder {
    private final int itemType;
    private SparseArray<View> views = new SparseArray<>();
    public View itemView;

    public int getItemType() {
        return itemType;
    }

    public BaseViewHolder(int itemType, View itemView) {
        this.itemType = itemType;
        if (itemView == null) {
            throw new IllegalArgumentException("itemView may not be null");
        }
        this.itemView = itemView;
    }

    public void measure(int widthMeasureSpec, int heightMeasureSpec) {
        this.itemView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    public void layout(int l, int t, int r, int b) {
        this.itemView.layout(l, t, r, b);
    }

    public void draw(Canvas canvas) {
        this.itemView.draw(canvas);
    }

    public <T extends View> T getView(@IdRes int viewId) {
        if(viewId!=0){
            return itemView.findViewById(viewId);
        }
        View view = views.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (T) view;
    }

    public void setText(@IdRes int viewId, @StringRes int strId) {
        TextView textView = getView(viewId);
        textView.setText(strId);
    }

    public void setText(@IdRes int viewId, CharSequence str) {
        TextView textView = getView(viewId);
        textView.setText(str);
    }

    public void setBackgroundResource(@IdRes int viewId, @DrawableRes int resId) {
        View view = getView(viewId);
        view.setBackgroundResource(resId);
    }

    public void setImageResource(@IdRes int viewId, @DrawableRes int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
    }

    public void setVisibility(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void setGone(@IdRes int viewId, boolean gone) {
        View view = getView(viewId);
        view.setVisibility(gone ? View.GONE : View.VISIBLE);
    }
}
