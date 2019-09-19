package com.didi.aoe.examples.demo.features;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

/**
 * @author noctis
 */
public class Feature {
    @IdRes
    private int id;
    private String title;
    private String content;
    @DrawableRes
    private int backgroundId;

    Feature(@IdRes int id, String title, String content, int backgroundId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.backgroundId = backgroundId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }
}
