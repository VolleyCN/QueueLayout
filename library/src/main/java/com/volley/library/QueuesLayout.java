package com.volley.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.volley.library.adapter.BaseQueueAdapter;
import com.volley.library.model.QueueEntity;
import com.volley.library.model.QueueKey;
import com.volley.library.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by MENG on 2017/3/20.
 */

public class QueuesLayout {
    private Context context;
    private BaseQueueAdapter adapter;
    private long CHECK_TIME = 200;
    private int CHECK_TIME_KEY = 1000;
    private Handler mHandler;
    boolean isStop;
    private Map<Integer, List<QueueEntity>> mapQueues;
    private Map<Integer, QueueKey> mapKeys;
    /**
     * 多行排队模式
     */
    public static final int SHOW_MODE_NORMAL = 1;
    /**
     * 一行可重叠
     * 根据 itemType 进行重叠
     */
    public static final int SHOW_MODE_OVERLAP = 2;
    private int showMode = SHOW_MODE_NORMAL;

    public void setShowMode(int showMode) {
        this.showMode = showMode;
        resetQueueContainer();
    }

    private synchronized void resetQueueContainer() {
        if (mapKeys == null || mapKeys.size() == 0) {
            return;
        }
        Set<Integer> keySet = mapKeys.keySet();
        for (Integer itemType : keySet) {
            QueueKey queueKey = mapKeys.get(itemType);
            if (queueKey == null) {
                break;
            }
            if (showMode == SHOW_MODE_OVERLAP) {
                FrameLayout overlapLayout = new FrameLayout(context);
                overlapLayout.setLayoutTransition(new LayoutTransition());
                mapKeys.get(itemType).setContainer(overlapLayout);
            } else {
                if (normaLayout == null) {
                    normaLayout = new LinearLayout(context);
                    normaLayout.setOrientation(LinearLayout.VERTICAL);
                    normaLayout.setLayoutTransition(new LayoutTransition());
                }
                mapKeys.get(itemType).setContainer(normaLayout);
            }
        }
    }

    /**
     * 开始任务
     */
    public void startAnimation() {
        if (isStop && mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(CHECK_TIME_KEY, CHECK_TIME);
            isStop = false;
        }
    }

    /**
     * 清除队列
     */
    public void clearQueue() {
        try {
            mapQueues.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearQueue(int key) {
        try {
            mapQueues.put(key, null);
            mapKeys.get(key).setCount(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public BaseQueueAdapter getAdapter() {
        return adapter;
    }

    public void setQueuesAdapter(BaseQueueAdapter adapter) {
        this.adapter = adapter;
    }

    private void initTask() {
        try {
            mapQueues = new ConcurrentHashMap<>();
            mapKeys = new ConcurrentHashMap<>();
            mHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    mHandler.sendEmptyMessageDelayed(CHECK_TIME_KEY, CHECK_TIME);
                    clearTimeOutTask();
                    showTask();
                }
            };
            mHandler.sendEmptyMessageDelayed(CHECK_TIME_KEY, CHECK_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void showTask() {
        try {
            Set<Integer> keySet = mapKeys.keySet();
            boolean comboEndAll = true;
            for (Integer integer : keySet) {
                QueueKey queueKey = mapKeys.get(integer);
                List<QueueEntity> entities = mapQueues.get(integer);
                boolean hasValue = entities != null && entities.size() > 0;
                boolean hasView = queueKey != null && queueKey.getChildCount() > 0;
                if (hasValue || hasView) {
                    comboEndAll = false;
                }
                if (hasValue) {
                    QueueEntity queueEntity = entities.get(0);
                    boolean showSuccess = showQueue(queueEntity);
                    if (showSuccess) {
                        entities.remove(queueEntity);
                    }
                }
            }
            if (comboEndAll && adapter != null) {
                mHandler.removeCallbacksAndMessages(null);
                adapter.onComboEndAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QueuesLayout(Context context) {
        this.context = context;
        initTask();
    }


    /**
     * 不相同礼物，如展示位置剩余，直接添加
     * 展示位上相同礼物需要更新礼物数量
     * 移除时间最将快结束的
     *
     * @param newQueueEntity
     */
    private boolean showQueue(QueueEntity newQueueEntity) {
        try {
            View queueView = findSameQueueView(newQueueEntity);
            if (queueView == null) {
                QueueKey queueKey = mapKeys.get(newQueueEntity.getItemType());
                if (queueKey.getCount() < queueKey.getMaxLimit()) {
                    return addQueueViewAnim(newQueueEntity, queueKey);
                } else {
                    removeResentRefreshTimeQueue(queueKey);
                    return false;
                }
            } else {
                QueueEntity oldBean = getViewTag(queueView);
                if (adapter != null && oldBean != null && queueView.isEnabled()) {
                    queueView = adapter.onUpdate(queueView, oldBean, newQueueEntity);
                    oldBean.setLatestRefreshTime(System.currentTimeMillis());
                    queueView.setTag(oldBean);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 如果正在显示的Item的个数超过MAX_COUNT个，那么就移除最后一次更新时间比较长的
     */
    private void removeResentRefreshTimeQueue(QueueKey queueKey) {
        try {
            List<QueueEntity> list = new ArrayList();
            int childCount = queueKey.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = queueKey.getChildAt(i);
                QueueEntity tag = getViewTag(view);
                if (view != null && tag != null && view.isEnabled() && queueKey.getItemType() == tag.getItemType() && tag.isNeedResentRemove()) {
                    list.add(tag);
                }
            }
            // 根据加入时间排序所有child中queueView
            Collections.sort(list);
            if (list.size() == queueKey.getMaxLimit()) {
                QueueEntity queueEntity = list.get(0);
                View sameUserQueueView = findSameQueueView(queueEntity);
                if (sameUserQueueView != null) {
                    removeQueueViewAnim(sameUserQueueView, queueEntity, queueKey, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 手动更新礼物过期时间
     */
    public void updateRefreshTime(QueueEntity queueEntity) {
        updateRefreshTime(queueEntity, 0);
    }

    /**
     * 手动更新礼物过期时间
     *
     * @param queueEntity
     * @param delay
     */
    public void updateRefreshTime(QueueEntity queueEntity, long delay) {
        if (adapter == null) {
            throw new IllegalArgumentException("setAdapter first");
        }
        if (queueEntity == null) {
            return;
        }
        QueueKey queueKey = mapKeys.get(queueEntity.getItemType());
        if (queueKey == null) {
            return;
        }
        int count = queueKey.getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            View view = queueKey.getChildAt(index);
            QueueEntity tag = getViewTag(view);
            if (view != null && tag != null && view.isEnabled() && adapter.onCheckUnique(tag, queueEntity)) {
                if (delay == 0) {
                    if (queueEntity.getLatestRefreshTime() != 0 && queueEntity.getLatestRefreshTime() > tag.getLatestRefreshTime()) {
                        tag.setLatestRefreshTime(queueEntity.getLatestRefreshTime());
                    } else {
                        tag.setLatestRefreshTime(System.currentTimeMillis());
                    }
                } else {
                    tag.setLatestRefreshTime(tag.getLatestRefreshTime() + delay);
                }
            }
        }
    }

    private QueueEntity getViewTag(View view) {
        if (view != null && view.getTag() instanceof QueueEntity) {
            return (QueueEntity) view.getTag();
        }
        return null;
    }


    private synchronized boolean addQueueViewAnim(final QueueEntity queueEntity, QueueKey queueKey) {
        try {
            if (adapter == null || queueEntity == null || queueKey == null) {
                return false;
            }
            ViewGroup innerView = queueKey.getInnerView();
            if (innerView == null) {
                return false;
            }
            BaseViewHolder holder = adapter.onCreateHolder(queueKey.getInnerView(), queueEntity.getItemType());
            adapter.onBindViewHolder(holder, queueEntity);
            adapter.onAddViewHolderClick(holder, queueEntity);
            View queueView = holder.itemView;
            queueEntity.setLatestRefreshTime(System.currentTimeMillis());
            queueView.setTag(queueEntity);
            queueView.setEnabled(true);  // 标记该queueView可用
            innerView.addView(queueView);
            AnimatorSet animatorSet = adapter.getIntAnim(queueView, queueEntity);
            if (animatorSet != null) {
                animatorSet.start();
            }
            queueKey.setCount(queueKey.getCount() + 1);//计数当前ItemType的个数
            adapter.onComboStart(queueEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除指定view的礼物动画
     * comboEnd true 正常结束 false 强行结束
     */
    private void removeQueueViewAnim(final View queueView, QueueEntity queueEntity, QueueKey queueKey, boolean comboEnd) {
        if (queueView == null || adapter == null) {
            return;
        }
        // 标记该view不可用
        queueView.setEnabled(false);
        if (comboEnd) {
            adapter.onComboEnd(queueEntity);
        } else {
            adapter.onKickEnd(getViewTag(queueView));
        }
        AnimatorSet outAnim = adapter.getOutAnim(queueView, queueEntity);
        if (outAnim == null) {
            removeChildQueue(queueView, queueEntity, queueKey);
            return;
        }
        outAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mHandler != null) {
                    mHandler.post(() -> removeChildQueue(queueView, queueEntity, queueKey));
                }
            }
        });
        outAnim.start();
    }

    /**
     * 移除指定view
     */
    private synchronized void removeChildQueue(View view, QueueEntity queueEntity, QueueKey queueKey) {
        try {
            if (view != null && queueEntity != null && queueEntity != null) {
                view.clearAnimation();
                queueKey.setCount(queueKey.getCount() - 1);
                ViewGroup innerView = queueKey.getInnerView();
                if (innerView != null) {
                    innerView.removeView(view);
                }
                view = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 找出唯一识别的礼物
     * 已经展示的是否存在改礼物
     *
     * @param target
     * @return
     */
    private synchronized View findSameQueueView(QueueEntity target) {
        try {
            if (adapter == null || target == null || !target.isAllowSame()) {
                return null;
            }
            QueueKey queueKey = mapKeys.get(target.getItemType());
            if (queueKey == null) {
                return null;
            }
            int childCount = queueKey.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = queueKey.getChildAt(i);
                QueueEntity originQueue = getViewTag(view);
                if (originQueue != null && adapter.onCheckUnique(originQueue, target) && view.isEnabled()) {
                    return view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onPause() {
        try {
            stopAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        try {
            startAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            stopAnimation();
            clearQueue();
            mapKeys.clear();
            adapter = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LinearLayout normaLayout = null;

    public void setMaxLimit(int itemType, int max) {
        if (mapKeys == null) {
            return;
        }
        if (showMode == SHOW_MODE_OVERLAP) {
            FrameLayout overlapLayout = new FrameLayout(context);
            overlapLayout.setLayoutTransition(new LayoutTransition());
            mapKeys.put(itemType, new QueueKey(itemType, max, overlapLayout));
        } else {
            if (normaLayout == null) {
                normaLayout = new LinearLayout(context);
                normaLayout.setOrientation(LinearLayout.VERTICAL);
                normaLayout.setLayoutTransition(new LayoutTransition());
            }
            mapKeys.put(itemType, new QueueKey(itemType, max, normaLayout));
        }
    }

    public QueueKey getQueueKey(int itemType) {
        if (mapKeys == null) {
            return null;
        }
        return mapKeys.get(itemType);
    }

    /**
     * 放入队列
     */
    public void put(QueueEntity queueEntity) {
        try {
            if (isStop) {
                startAnimation();
            }
            View queueView = findSameQueueView(queueEntity);
            if (queueView != null) {
                showQueue(adapter.onGenerateEntity(queueEntity));
                return;
            }
            QueueEntity oldQueueModel = checkQueueSame(queueEntity);
            if (oldQueueModel == null) {
                oldQueueModel = adapter.onGenerateEntity(queueEntity);
                if (oldQueueModel == null) {
                    throw new NullPointerException("clone return null");
                }
            }
            int itemType = queueEntity.getItemType();
            List<QueueEntity> entities = mapQueues.get(itemType);
            if (entities == null) {
                entities = new ArrayList<>();
            }
            entities.add(oldQueueModel);
            mapQueues.put(itemType, entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查队列里是否已经存在，存在更新数量
     */
    private QueueEntity checkQueueSame(QueueEntity currentQueueEntity) {
        List<QueueEntity> entities = mapQueues.get(currentQueueEntity.getItemType());
        if (entities == null) {
            return null;
        }
        for (QueueEntity queueEntity : entities) {
            if (adapter.onCheckUnique(queueEntity, currentQueueEntity)) {
                queueEntity.setSameCount(queueEntity.getSameCount() + currentQueueEntity.getSameCount());
                queueEntity.setStayTime(currentQueueEntity.getStayTime());
                return queueEntity;
            }
        }
        return null;
    }

    /**
     * 清除过期item
     */
    private synchronized void clearTimeOutTask() {
        try {
            Set<Integer> keySet = mapKeys.keySet();
            for (Integer itemType : keySet) {
                QueueKey queueKey = mapKeys.get(itemType);
                clearTimeOutTask(queueKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearTimeOutTask(QueueKey queueKey) {
        ViewGroup innerView = queueKey.getInnerView();
        if (innerView == null) {
            return;
        }
        int count = innerView.getChildCount();
        long nowTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            View removeView = innerView.getChildAt(i);
            QueueEntity queueEntity = getViewTag(removeView);
            if (removeView == null || queueEntity == null || !removeView.isEnabled()) {
                break;
            }
            if ((nowTime - queueEntity.getLatestRefreshTime()) >= queueEntity.getStayTime()) {
                removeQueueViewAnim(removeView, queueEntity, queueKey, true);
            }
        }
    }

    public void stopAnimation(int itemType) {
        try {
            QueueKey queueKey = mapKeys.get(itemType);
            if (queueKey == null) {
                return;
            }
            int childCount = queueKey.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = queueKey.getChildAt(i);
                if (view == null) {
                    break;
                }
                QueueEntity tag = getViewTag(view);
                if (tag == null && tag.getItemType() == itemType) {
                    removeQueueViewAnim(view, tag, queueKey, false);
                    queueKey.setCount(queueKey.getCount() - 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 暂停当前动画
     */
    public void stopAnimation() {
        try {
            isStop = true;
            Set<Integer> keySet = mapKeys.keySet();
            for (Integer itemType : keySet) {
                QueueKey queueKey = mapKeys.get(itemType);
                queueKey.setCount(0);
                stopAnimation(queueKey);
            }
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAnimation(QueueKey queueKey) {
        ViewGroup innerView = queueKey.getInnerView();
        if (innerView == null) {
            return;
        }
        for (int i = 0; i < innerView.getChildCount(); i++) {
            View view = innerView.getChildAt(i);
            if (view == null) {
                break;
            }
            view.clearAnimation();
        }
        innerView.removeAllViews();
    }

    public void bindParentView(ViewGroup parentView) {
        if (parentView == null) {
            throw new IllegalArgumentException("parentView may not be null");
        }
        parentView.removeAllViews();
        Set<Integer> keySet = mapKeys.keySet();
        for (Integer itemType : keySet) {
            QueueKey queueKey = mapKeys.get(itemType);
            if (queueKey == null) {
                break;
            }
            ViewGroup innerView = queueKey.getInnerView();
            if (innerView == null || innerView.getParent() != null) {
                break;
            }
            parentView.addView(innerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
}
