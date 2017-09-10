package com.techtest.githubrepobrowser.events;

public class ShowProgressEvent {

    private final boolean show;

    public ShowProgressEvent(boolean show) {
        this.show = show;
    }

    public boolean getShow() {
        return show;
    }
}
