package com.techtest.githubrepobrowser;

import com.techtest.githubrepobrowser.entity.RepoInfo;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GitHubService {
    @GET("users/JakeWharton/repos?per_page=15")
    Observable<List<RepoInfo>> listRepos(@Query("page") int page);
}
