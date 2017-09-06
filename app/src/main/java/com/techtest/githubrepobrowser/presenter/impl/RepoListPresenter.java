package com.techtest.githubrepobrowser.presenter.impl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.UiThread;

import com.squareup.otto.Subscribe;
import com.techtest.githubrepobrowser.EventBusProvider;
import com.techtest.githubrepobrowser.GitHubService;
import com.techtest.githubrepobrowser.RepoListActivity;
import com.techtest.githubrepobrowser.RepoListAdapter;
import com.techtest.githubrepobrowser.entity.RepoInfo;
import com.techtest.githubrepobrowser.events.NewPageRequiredEvent;
import com.techtest.githubrepobrowser.presenter.IRepoListPresenter;

import java.util.List;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RepoListPresenter implements IRepoListPresenter {

    private int page = 1;
    private RepoListAdapter adapter;
    private GitHubService service;
    private Realm mainThreadRealm;
    private RepoListActivity activity;
    private boolean stopRequests = false;

    @UiThread
    @Override
    public void onCreate(RepoListActivity activity, RepoListAdapter adapter) {
        this.adapter = adapter;
        this.activity = activity;

        EventBusProvider.getEventBus().register(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);

        mainThreadRealm = Realm.getDefaultInstance();

        loadNextPage();
    }

    private List<RepoInfo> readDataFromCache(Realm realm, int page) {
        return realm.where(RepoInfo.class).equalTo("page", page).findAll();
    }

    @Override
    public void loadNextPage() {
        if (stopRequests) return;

        adapter.showProgress(true);
        Observable<List<RepoInfo>> loadDataObserver = service.listRepos(page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnEach(this::writeDataToCache)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> adapter.showProgress(false));

        loadDataObserver.subscribe(this::displayList, this::onError);
    }

    @Override
    public void onNetworkStatusChanged(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            activity.showConnectionError(false);
            loadNextPage();
        } else {
            activity.showConnectionError(true);
        }
    }

    private void writeDataToCache(Notification<List<RepoInfo>> listNotification) {
        List<RepoInfo> repoInfos = listNotification.getValue();

        if (repoInfos == null || repoInfos.isEmpty()) return;

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(transactionRealm -> {
            for(RepoInfo info : repoInfos) {
                info.setPage(page);
            }
            transactionRealm.copyToRealmOrUpdate(repoInfos);
        });
        realm.close();
    }

    @Subscribe
    public void loadNewPage(NewPageRequiredEvent event) {
        loadNextPage();
    }

    private void displayList(List<RepoInfo> data) {
        if (!data.isEmpty()) {
            adapter.addItems(data);
            ++page;
        } else {
            stopRequests = true;
        }
    }

    private void onError(Throwable t) {
        List<RepoInfo> cachedList = readDataFromCache(mainThreadRealm, page);
        if (cachedList != null && !cachedList.isEmpty()) {
            ++page;
            adapter.addItems(cachedList);
        }
    }
}
