package com.tblin.market.ui;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

/**
 * Action item, 每个item里面都有一个ImageView和一个TextView
 */
public class ActionItem {

    private Drawable        icon;
    private String          title;
    private OnClickListener listener;

    /**
     * 构造器
     */
    public ActionItem() {
    }

    /**
     * 带Drawable参数的构造器
     */
    public ActionItem(Drawable icon) {
        this.icon = icon;
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获得标题
     * 
     * @return action title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置图标
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    /**
     * 获得图标
     */
    public Drawable getIcon() {
        return this.icon;
    }

    /**
     * 绑定监听器
     */
    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    /**
     * 获得监听器
     */
    public OnClickListener getListener() {
        return this.listener;
    }
}