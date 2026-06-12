package jmalvin.modsync.tools;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ModDownloader {
    private static Git gitDir;

    public ModDownloader() {
        try {
            if (new File(".git").exists())
                gitDir =  Git.open(new File(""));
        } catch (IOException e) {
            throw new RuntimeException("Error trying to open the .git file, please delete it and reload Minecraft");
        }
    }

    public Git getGitDir() {
        return gitDir;
    }

    public void resetGit() {
        gitDir = null;
    }

    public boolean upToDate() throws IOException {
        try {
            return getCommitsAhead().isEmpty();
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    public List<RevCommit> getCommitsAhead() throws GitAPIException, IOException {
        ArrayList<RevCommit> commits = new ArrayList<>();
        Repository repo = gitDir.getRepository();
        ObjectId localHead = repo.resolve("refs/heads/main");
        ObjectId remoteHead = repo.resolve("refs/remotes/origin/main");
        RevWalk revWalk = new RevWalk(repo);
        RevCommit localCommit = revWalk.parseCommit(localHead);
        RevCommit remoteCommit = revWalk.parseCommit(remoteHead);
        for (RevCommit commit : gitDir.log().addRange(localCommit, remoteCommit).call()) {
            commits.add(commit);
        }
        return commits;
    }

    public RevCommit getCurrentCommit() throws IOException {
        RevCommit commit;
        Repository repo = gitDir.getRepository();
        ObjectId localHead = repo.resolve("refs/heads/main");
        RevWalk revWalk = new RevWalk(repo);
        commit = revWalk.parseCommit(localHead);
        return commit;
    }
    public List<RevCommit> getCommitsBehind() throws GitAPIException {
        ArrayList<RevCommit> commits = new ArrayList<>();
        for (RevCommit commit : gitDir.log().call()) {
            commits.add(commit);
        }
        return commits.subList(1, commits.size());
    }

    public void fetch() throws GitAPIException {
        if (gitDir != null) {
            gitDir.fetch().call();
        }
    }

    public boolean pull() {
        if (gitDir != null) {
            try {
                fetch();
                gitDir.pull().setRebase(true).call();

                if (SystemUtils.IS_OS_WINDOWS) {
                    Set<String> toDelete = gitDir.status().call().getRemoved();
                    ModSyncClient.CONFIG.setConfig("to_delete", new ArrayList<>(toDelete));
                }

                if (ModSyncClient.CONFIG.getListConfig("ignored") != null) {
                    removeIgnoredFolders();
                }
                return true;
            } catch (Exception e) {
                System.out.println("Modsync encountered an error while pulling! => " + e.getMessage());
                String message = e.getMessage().contains("cannot open git-upload-pack") ? "Internet connection error" : e.getMessage();
                throw new RuntimeException(message);
            }
        }
        return false;
    }

    public void removeIgnoredFolders() throws IOException {
        ArrayList<String> paths = new ArrayList<>(Arrays.asList(ModSyncClient.CONFIG.getListConfig("ignored")));
        for (Path file : ModSyncClient.DOWNLOADER.getTrackedFiles()) {
            if (file.getName(0).toFile().isDirectory() && paths.contains(file.getName(0).toString())) {
                try {
                    ModSyncClient.DOWNLOADER.getGitDir().rm().addFilepattern(file.toString()).call();
                } catch (GitAPIException e) {
                    throw new IOException(e);
                }
            }
        }
    }


    public boolean setupRepo(String repo) throws IOException {
        // TODO: Fix https:// requirement?
        try {
            Git.lsRemoteRepository()
                    .setRemote(repo)
                    .call();
        } catch (GitAPIException e) {
            throw new IOException(e.getMessage().contains("connection failed") ? "Internet connection failure" : "That repository is invalid or does not exist");
        }

        File gitFolder = new File(".git");
        try {
            if (gitDir != null || gitFolder.exists()) {
                gitDir.remoteSetUrl()
                        .setRemoteUri(new URIish(repo))
                        .setRemoteName("origin")
                        .call();
                System.out.println("Set uri");
                gitDir.fetch()
                        .call();
                System.out.println("fetched");
                gitDir.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef("origin/main")
                        .call();
                System.out.println("reset");

                ArrayList<String> remainingMods = new ArrayList<>();
                if (gitDir.status().call().getUntracked() != null) {
                    for (String fileName : gitDir.status().call().getUntracked()) {
                        //System.out.println(fileName);
                        if (fileName.startsWith("mods")) {
                            System.out.println("adding.." + fileName);
                            remainingMods.add(fileName);
                        }
                    }
                }
                ModSyncClient.CONFIG.setConfig("to_delete", remainingMods);
            } else {
                InitCommand init = Git.init();
                if (SystemUtils.IS_OS_WINDOWS)
                    init.setDirectory(new File("modsync_temp"));
                gitDir = init.call();
                gitDir.remoteAdd()
                        .setUri(new URIish(repo))
                        .setName("origin")
                        .call();
                gitDir.fetch()
                        .setRemote("origin")
                        .call();
                gitDir.checkout()
                        .setForced(true)
                        .setCreateBranch(true)
                        .setName("main")
                        .setStartPoint("origin/main")
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            }
        } catch (Exception e) {
            throw new IOException("Could not set new remote: " + e);
        }
        return true;
    }

    public List<Path> getTrackedFiles() throws IOException {
        if (gitDir == null)
            return null;
        ArrayList<Path> tracked = new ArrayList<>();
        try {
            DirCache cache = gitDir.getRepository().readDirCache();
            for (int i = 0; i < cache.getEntryCount(); i++) {
                tracked.add(Path.of(cache.getEntry(i).getPathString()));
            }
        } catch (IOException e) {
            throw new IOException("Could not parse paths: " + e);
        }
        return tracked;
    }

    public List<Path> getTrackedFolders() throws IOException {
        List<Path> files = getTrackedFiles();
        ArrayList<Path> folders = new ArrayList<>();
        for (Path file : files) {
            Path root = file.getName(0);
            if (root.toFile().isDirectory() && !folders.contains(root)) {
                folders.add(root);
            }
        }
        return folders;
    }
}
