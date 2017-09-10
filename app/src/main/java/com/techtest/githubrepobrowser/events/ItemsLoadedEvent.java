package com.techtest.githubrepobrowser.events;

import com.techtest.githubrepobrowser.entity.RepoInfo;

import java.util.List;

public class ItemsLoadedEvent {
    private final List<RepoInfo> items;

    public ItemsLoadedEvent(List<RepoInfo> items) {
        this.items = items;
    }

    public List<RepoInfo> getItems() {
        return items;
    }
}
