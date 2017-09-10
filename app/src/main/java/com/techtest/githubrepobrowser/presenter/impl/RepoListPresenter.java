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
import com.techtest.githubrepobrowser.events.ItemsLoadedEvent;
import com.techtest.githubrepobrowser.events.NewPageRequiredEvent;
import com.techtest.githubrepobrowser.events.ShowProgressEvent;
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

    private static final String BASE_URL = "https://api.github.com";

    private int page = 1;
    private GitHubService service;
    private Realm mainThreadRealm;
    private RepoListActivity activity;
    private boolean stopRequests = false;
    private Throwable lastError;

    @Override
    public void setUrl(String url) {
        page = 1;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }

    @UiThread
    @Override
    public void onCreate(RepoListActivity activity) {
        this.activity = activity;

        EventBusProvider.getEventBus().register(this);

        setUrl(BASE_URL);

        mainThreadRealm = Realm.getDefaultInstance();

        loadNextPage();
    }

    private List<RepoInfo> readDataFromCache(Realm realm, int page) {
        return realm.where(RepoInfo.class).equalTo("page", page).findAll();
    }

    @Override
    public void loadNextPage() {
        if (stopRequests) return;

        showProgress(true);
        Observable<List<RepoInfo>> loadDataObserver = service.listRepos(page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnEach(this::writeDataToCache)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> showProgress(false));

        loadDataObserver.subscribe(this::displayList, this::onError);
    }

    private void showProgress(boolean show) {
        EventBusProvider.getEventBus().post(new ShowProgressEvent(show));
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
            EventBusProvider.getEventBus().post(new ItemsLoadedEvent(data));
            ++page;
        } else {
            stopRequests = true;
        }
        lastError = null;
    }

    private void onError(Throwable t) {
        lastError = t;
        List<RepoInfo> cachedList = readDataFromCache(mainThreadRealm, page);
        if (cachedList != null && !cachedList.isEmpty()) {
            ++page;
            EventBusProvider.getEventBus().post(new ItemsLoadedEvent(cachedList));
        }
    }

    @Override
    public String getLastError() {
        if (lastError == null) {
            return null;
        } else {
            return lastError.getMessage();
        }
    }
}
