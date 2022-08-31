package com.volley.library.model;


public class QueueEntity implements Comparable<QueueEntity>, Cloneable, MultiItemEntity {
    private long latestRefreshTime;
    private long stayTime;
    private long sameCount;
    private boolean needResentRemove;
    private int itemType;
    private boolean allowSame;

    public long getLatestRefreshTime() {
        return latestRefreshTime;
    }

    public void setLatestRefreshTime(long latestRefreshTime) {
        this.latestRefreshTime = latestRefreshTime;
    }

    @Override
    public int compareTo(QueueEntity o) {
        return (int) (this.getLatestRefreshTime() - o.getLatestRefreshTime());
    }

    public long getStayTime() {
        return stayTime;
    }

    public void setStayTime(long stayTime) {
        this.stayTime = stayTime;
    }

    public long getSameCount() {
        return sameCount;
    }

    public void setSameCount(long sameCount) {
        this.sameCount = sameCount;
    }

    public boolean isNeedResentRemove() {
        return needResentRemove;
    }

    public void setNeedResentRemove(boolean needResentRemove) {
        this.needResentRemove = needResentRemove;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public boolean isAllowSame() {
        return allowSame;
    }

    public void setAllowSame(boolean allowSame) {
        this.allowSame = allowSame;
    }
}
