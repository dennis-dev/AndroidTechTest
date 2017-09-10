package com.techtest.githubrepobrowser.presenter;

import android.content.Context;

import com.techtest.githubrepobrowser.RepoListActivity;
import com.techtest.githubrepobrowser.RepoListAdapter;

public interface IRepoListPresenter {
    public void setUrl(String url);
    public void onCreate(RepoListActivity activity);
    public void loadNextPage();

    public void onNetworkStatusChanged(Context context);

    String getLastError();
}
