package com.techtest.githubrepobrowser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.techtest.githubrepobrowser.entity.RepoInfo;
import com.techtest.githubrepobrowser.events.NewPageRequiredEvent;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;

    private List<RepoInfo> repoInfoList;
    private boolean showProgress = false;

    RepoListAdapter() {
        this.repoInfoList = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.repo_list_item, parent, false);

            return new ViewHolderItem(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.repo_list_progress_item, parent, false);

            return new ViewHolderProgress(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == repoInfoList.size()) {
            return VIEW_TYPE_PROGRESS;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItem) {
            bindItem((ViewHolderItem) holder, position);
        }
    }

    private void bindItem(ViewHolderItem holder, int position) {
        RepoInfo info = repoInfoList.get(position);
        holder.title.setText(info.getName());
        holder.description.setText(info.getDescription());
        holder.language.setText(info.getLanguage());

        Locale locale = Locale.getDefault();
        holder.forks.setText(String.format(locale, "%d", info.getForks()));
        holder.watchersCount.setText(String.format(locale, "%d", info.getWatchers()));

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", locale);
        String date = sdf.format(info.getUpdated_at());
        String updatedAtStr = holder.updatedAt.getContext().getResources().getString(R.string.repo_list_updated_at_text, date);
        holder.updatedAt.setText(updatedAtStr);

        if (position == repoInfoList.size() - 3) {
            // Must not be called during holder updating
            holder.title.post(() -> EventBusProvider.getEventBus().post(new NewPageRequiredEvent()));
        }
    }

    @Override
    public int getItemCount() {
        return repoInfoList.size() + (showProgress ? 1 : 0);
    }

    public void addItems(List<RepoInfo> items) {
        int lastPos = repoInfoList.size();
        for (RepoInfo item : items) {
            Iterator<RepoInfo> iter = repoInfoList.iterator();
            while (iter.hasNext()) {
                if (iter.next().getId().equals(item.getId())) {
                    iter.remove();
                    break;
                }
            }
        }
        repoInfoList.addAll(items);
        this.notifyItemRangeInserted(lastPos, items.size());
    }

    public void showProgress(boolean enabled) {
        this.showProgress = enabled;
        this.notifyItemChanged(repoInfoList.size());
    }

    static class ViewHolderItem extends RecyclerView.ViewHolder {

        @BindView(R.id.repo_list_item_title_text)
        TextView title;

        @BindView(R.id.repo_list_item_description_text)
        TextView description;

        @BindView(R.id.repo_list_item_language_text)
        TextView language;

        @BindView(R.id.repo_list_item_watchers_text)
        TextView watchersCount;

        @BindView(R.id.repo_list_item_forks_text)
        TextView forks;

        @BindView(R.id.repo_list_item_update_date_text)
        TextView updatedAt;

        ViewHolderItem(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    static class ViewHolderProgress extends RecyclerView.ViewHolder {
        ViewHolderProgress(View v) {
            super(v);
        }
    }

}
