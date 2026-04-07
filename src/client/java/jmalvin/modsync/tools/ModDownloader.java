package jmalvin.modsync.tools;

import jmalvin.modsync.ModSync;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ModDownloader {
    private static Git gitDir;

    public ModDownloader() {
        try {
            if (new File(".git").exists())
                gitDir =  Git.open(new File(""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Git getGitDir() {
        return gitDir;
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
                gitDir.add().setAll(true).call();
                gitDir.stashCreate().call();
                gitDir.pull().call();
                gitDir.stashApply().call();
                return true;
            } catch (Exception e) {
                String message = e.getMessage().contains("cannot open git-upload-pack") ? "Internet connection error" : e.getMessage();
                throw new RuntimeException(message);
            }
        }
        return false;
    }

    public boolean setupRepo(String repo) throws IOException{
       try {
           Git.lsRemoteRepository()
                   .setRemote(repo)
                   .call();
       } catch (GitAPIException e) {
           throw new IOException(e.getMessage().contains("connection failed") ? "Internet connection failure" : "That repository is invalid or does not exist");
       }

       File gitFolder = new File(".git");
        if (gitDir != null || gitFolder.exists()) {
            try {
                DirCache cache = gitDir.getRepository().readDirCache();
                RmCommand rm = gitDir.rm();
                for (int i = 0; i < cache.getEntryCount(); i++) {
                    String path = cache.getEntry(i).getPathString();
                    rm.addFilepattern(path);
                }
                rm.call();
                if (gitFolder.exists()) ModDownloader.deleteDirectory(gitFolder.toPath());
            } catch (Exception e) {
                throw new IOException("There was an error removing git files");
            }
        }

        try {
            if (new File("modsync").exists())
                deleteDirectory(Path.of("modsync"));
            Git git = Git.cloneRepository().setURI(repo).setDirectory(new File("modsync")).call();
            git.close();
        } catch (Exception e) {
            throw new IOException("Could not clone repository");
        }

        Path modlist = Path.of("modsync");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modlist)) {
            for (Path file : stream) {;
                if (Files.isDirectory(file)) {
                    moveDirectory(file, Path.of(""));
                } else {
                    Files.move(file, file.getFileName());
                }
            }
            deleteDirectory(modlist);
            gitDir =  Git.open(new File(""));
            return true;
        } catch (Exception e) {
            throw new IOException(/*"There was an error extracting mods"*/e);
        }
    }

    public static void moveDirectory(Path dir, Path dst) throws IOException {
        if (!Files.isDirectory(dir))
            throw new IllegalArgumentException("Not a directory");

        File newDir = dst.resolve(dir.getFileName()).toFile();
        if (newDir.mkdir() || newDir.exists()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    if (Files.isDirectory(file)) {
                        moveDirectory(file, newDir.toPath());
                    } else {
                        Files.move(file, newDir.toPath().resolve(file.getFileName()));
                    }
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        Files.delete(dir);
    }

    public static void deleteDirectory(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    deleteDirectory(file);
                } else {
                    Files.delete(file);
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        Files.delete(dir);
    }
}
