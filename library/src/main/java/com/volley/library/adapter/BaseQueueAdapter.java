package com.volley.library.adapter;

import android.animation.AnimatorSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.volley.library.model.QueueEntity;
import com.volley.library.viewholder.BaseViewHolder;

public abstract class BaseQueueAdapter<T extends QueueEntity, VH extends BaseViewHolder> {
    private SparseArray<Integer> layouts;
    private OnItemClickListener onItemClickListener;

    protected synchronized void addItemType(int itemType, @LayoutRes int layoutResId) {
        if (layouts == null) {
            layouts = new SparseArray();
        }
        layouts.append(itemType, layoutResId);
    }

    public VH onCreateHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, viewType);
    }

    private VH createBaseViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = new BaseViewHolder(viewType, getItemView(parent, layouts.get(viewType)));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        baseViewHolder.itemView.setLayoutParams(params);
        return (VH) baseViewHolder;
    }

    public abstract void onBindViewHolder(@NonNull VH holder, T t);

    private View getItemView(ViewGroup parent, @LayoutRes int layoutResId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
    }

    private OnComboListener onComboListener;

    /**
     * 更新
     *
     * @param view
     * @param o    原礼物对象
     * @param t    新礼物对象
     * @return
     */
    public View onUpdate(View view, T o, T t) {
        return view;
    }

    /**
     * 礼物展示结束，可能由于送礼者过多，轨道被替换导致结束
     *
     * @param bean
     * @return
     */
    public void onKickEnd(T bean) {
        if (onComboListener != null) {
            onComboListener.onKickEnd(bean);
        }
    }

    /**
     * 礼物连击结束,即被系统自动清理时回调
     *
     * @param bean
     * @return
     */
    public void onComboEnd(T bean) {
        if (onComboListener != null) {
            onComboListener.onComboEnd(bean);
        }
    }


    public void onComboEndAll() {
        if (onComboListener != null) {
            onComboListener.onComboEndAll();
        }
    }

    /**
     * 添加进入动画
     *
     * @param view
     * @param queueEntity
     */
    public AnimatorSet getIntAnim(View view, QueueEntity queueEntity) {
        return null;
    }

    /**
     * 添加退出动画
     *
     * @param view
     * @param queueEntity
     * @return
     */
    public AnimatorSet getOutAnim(View view, QueueEntity queueEntity) {
        return null;
    }

    /**
     * 鉴别礼物唯一性，
     *
     * @param o 已存在的礼物bean
     * @param t 新传入的礼物bean
     * @return 返回比对后的结果
     */
    public boolean onCheckUnique(T o, T t) {
        return false;
    }

    public T onGenerateEntity(T bean) {
        return null;
    }

    public void setOnComboListener(OnComboListener onComboListener) {
        this.onComboListener = onComboListener;
    }

    public void onComboStart(QueueEntity queueEntity) {
        if (onComboListener != null) {
            onComboListener.onComboStart(queueEntity);
        }
    }

    public void onAddViewHolderClick(BaseViewHolder holder, T t) {
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(t, v));
        }
    }

    public interface OnComboListener<T> {
        void onComboEndAll();

        default void onComboEnd(T queueEntity) {
        }

        default void onKickEnd(T queueEntity) {
        }

        default void onComboStart(T queueEntity) {
        }
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(QueueEntity queueEntity, View view);
    }
}
