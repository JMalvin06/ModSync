package jmalvin.modsync.tools;

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
            gitDir =  Git.open(new File("mods"));
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

        if (gitDir != null) {
            try {
                DirCache cache = gitDir.getRepository().readDirCache();
                RmCommand rm = gitDir.rm();
                for (int i = 0; i < cache.getEntryCount(); i++) {
                    String path = cache.getEntry(i).getPathString();
                    rm.addFilepattern(path);
                }
                rm.call();
                Path modsFolder = Path.of("mods/.git");
                if (modsFolder.toFile().exists()) ModDownloader.deleteDirectory(modsFolder);
            } catch (Exception e) {
                throw new IOException("There was an error removing git files");
            }
        }

        Path modDir = Paths.get("mods");
        if (!Files.exists(modDir)) {
            try {
                Files.createDirectory(modDir);
            } catch (Exception e) {
                throw new IOException("There was an error creating the mods directory");
            }
        }

        try {
            Git git = Git.cloneRepository().setURI(repo).setDirectory(new File("mods/modlist")).call();
            git.close();
        } catch (GitAPIException e) {
            throw new IOException("Could not clone repository");
        }

        Path modlist = Paths.get("mods/modlist");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modlist)) {
            for (Path mod : stream) {
                String modName = mod.getFileName().toString();
                if (modName.endsWith("jar") || modName.equals(".git")) {
                    if (modName.equals(".git") && (new File("mods/.git").exists())) {
                        deleteDirectory(new File("mods/.git").toPath());
                    }
                    Files.move(mod, mod.getParent().getParent().resolve(mod.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } else if(Files.isDirectory(mod)) {
                    deleteDirectory(mod);
                } else {
                    Files.delete(mod);
                }
            }
            Files.delete(modlist);
            gitDir =  Git.open(new File("mods"));
            return true;
        } catch (Exception e) {
            throw new IOException("There was an error extracting mods");
        }
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
