package com.techtest.githubrepobrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.techtest.githubrepobrowser.presenter.IRepoListPresenter;
import com.techtest.githubrepobrowser.presenter.impl.RepoListPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoListActivity extends AppCompatActivity {

    @BindView(R.id.repo_list)
    RecyclerView repoList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.repo_list_activity_connection_error_text)
    TextView connectionErrorText;

    private IRepoListPresenter presenter = new RepoListPresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        repoList.setLayoutManager(layoutManager);

        RepoListAdapter adapter = new RepoListAdapter();
        repoList.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(repoList.getContext(),
                layoutManager.getOrientation());
        repoList.addItemDecoration(dividerItemDecoration);

        presenter.onCreate(this, adapter);

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                presenter.onNetworkStatusChanged(context);
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void showConnectionError(boolean enable) {
        if (enable) {
            connectionErrorText.setVisibility(View.VISIBLE);
        } else {
            connectionErrorText.setVisibility(View.GONE);
        }
    }
}
