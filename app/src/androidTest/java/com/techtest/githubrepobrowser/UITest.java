package com.techtest.githubrepobrowser;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {

    @Rule
    public final ActivityTestRule<RepoListActivity> activityRule = new ActivityTestRule<>(RepoListActivity.class);

    private MockWebServer server;
    private final Object loaderLock = new Object();

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        activityRule.getActivity().getPresenter().setUrl(server.url("/").toString());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void testScrolling() throws Exception {
        RecyclerView view = (RecyclerView) activityRule.getActivity().findViewById(R.id.repo_list);

        String firstPage = IOUtils.toString(activityRule.getActivity().getResources().openRawResource(R.raw.first_page));
        String secondPage = IOUtils.toString(activityRule.getActivity().getResources().openRawResource(R.raw.second_page));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(firstPage));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(secondPage));

        activityRule.getActivity().runOnUiThread(() -> {
                activityRule.getActivity().getPresenter().loadNextPage();
        });

        synchronized (loaderLock) {
            loaderLock.wait(2000);
        }

        assertTrue(view.getAdapter().getItemCount() == 15);

        onView(withId(R.id.repo_list))
        .perform(RecyclerViewActions.scrollToPosition(11));

        synchronized (loaderLock) {
            loaderLock.wait(2000);
        }

        assertTrue(view.getAdapter().getItemCount() == 15);

        synchronized (loaderLock) {
            loaderLock.wait(2000);
        }

        onView(withId(R.id.repo_list))
                .perform(RecyclerViewActions.scrollToPosition(13));

        synchronized (loaderLock) {
            loaderLock.wait(2000);
        }

        assertTrue(view.getAdapter().getItemCount() > 15);
    }

    @Test
    public void testFailedGettingData() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404));

        activityRule.getActivity().runOnUiThread(() -> {
            activityRule.getActivity().getPresenter().loadNextPage();
        });

        synchronized (loaderLock) {
            loaderLock.wait(2000);
        }

        assertTrue(activityRule.getActivity().getPresenter().getLastError() != null);
    }
}
