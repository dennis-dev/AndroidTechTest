package com.techtest.githubrepobrowser;

import com.techtest.githubrepobrowser.entity.RepoInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UnitTests {
    @Test
    public void checkAdapterInsertingLogic() throws Exception {
        RepoListAdapter adapter = new RepoListAdapter();
        List<RepoInfo> list = new ArrayList<>(1);
        RepoInfo repoInfo = new RepoInfo();
        repoInfo.setId(1L);
        list.add(repoInfo);
        // NPE appears because adapter is now assigned to view
        try { adapter.addItems(list); } catch (NullPointerException e) { }
        try { adapter.addItems(list); } catch (NullPointerException e) { }
        assertTrue(adapter.getItemCount() == 1);

        repoInfo.setId(2L);
        list.add(repoInfo);
        try { adapter.addItems(list); } catch (NullPointerException e) { }
        assertTrue(adapter.getItemCount() == 2);
    }
}