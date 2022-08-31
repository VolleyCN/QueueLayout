package com.volley.library.model;

import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public class QueueKey implements Comparable<QueueKey>, Cloneable, MultiItemEntity {
    private int itemType;
    private int maxLimit;
    private int count;
    private WeakReference<ViewGroup> container;

    public QueueKey(int itemType, int maxLimit, ViewGroup innerView) {
        this.itemType = itemType;
        this.maxLimit = maxLimit;
        this.container = new WeakReference<>(innerView);
    }

    public void setContainer(ViewGroup innerView) {
        if (innerView != null) {
            innerView.clearAnimation();
            innerView.removeAllViews();
        }
        this.container = new WeakReference<>(innerView);
    }

    @Override
    public int compareTo(QueueKey o) {
        return this.itemType - o.itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueueKey)) return false;
        QueueKey queueKey = (QueueKey) o;
        return getItemType() == queueKey.getItemType();
    }

    @Override
    public int hashCode() {
        return getItemType();
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getChildCount() {
        if (container == null) {
            return 0;
        }
        ViewGroup innerView = container.get();
        return innerView != null ? innerView.getChildCount() : 0;
    }

    public ViewGroup getInnerView() {
        if (container == null) {
            return null;
        }
        return container.get();
    }

    public View getChildAt(int i) {
        if (container == null) {
            return null;
        }
        ViewGroup innerView = container.get();
        return innerView != null ? innerView.getChildAt(i) : null;
    }
}
