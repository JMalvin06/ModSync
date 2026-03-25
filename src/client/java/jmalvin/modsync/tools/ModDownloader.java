package jmalvin.modsync.tools;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
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
        } catch (Exception ignored) {}
    }

    public Git getGitDir() {
        return gitDir;
    }

    public boolean upToDate() throws IOException {
        return getCommitsAhead().isEmpty();
    }

    public List<RevCommit> getCommitsAhead() throws IOException {
        ArrayList<RevCommit> commits = new ArrayList<>();
        Repository repo = gitDir.getRepository();
        ObjectId localHead = repo.resolve("refs/heads/main");
        ObjectId remoteHead = repo.resolve("refs/remotes/origin/main");
        try (RevWalk revWalk = new RevWalk(repo)) {
            RevCommit localCommit = revWalk.parseCommit(localHead);
            RevCommit remoteCommit = revWalk.parseCommit(remoteHead);
            for (RevCommit commit : gitDir.log().addRange(localCommit, remoteCommit).call()) {
                commits.add(commit);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return commits;
    }

    public RevCommit getCurrentCommit() throws IOException {
        RevCommit commit;
        Repository repo = gitDir.getRepository();
        ObjectId localHead = repo.resolve("refs/heads/main");
        try (RevWalk revWalk = new RevWalk(repo)) {
            commit = revWalk.parseCommit(localHead);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return commit;
    }
    public List<RevCommit> getCommitsBehind() throws IOException {
        ArrayList<RevCommit> commits = new ArrayList<>();
        try {
            for (RevCommit commit : gitDir.log().call()) {
                commits.add(commit);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return commits.subList(1, commits.size());
    }

    public void fetch() {
        if (gitDir != null) {
            try {
                gitDir.fetch().call();
            } catch (Exception e) {
                // TODO handle
                throw new RuntimeException();
            }
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
            } catch (CheckoutConflictException e){
                // TODO handle merge conflict
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public boolean setupRepo(String repo) throws GitAPIException{
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
                throw new RuntimeException(e);
            }
        }

        Git.lsRemoteRepository()
                .setRemote(repo)
                .call();

        Path modDir = Paths.get("mods");
        if (!Files.exists(modDir)) {
            try {
                Files.createDirectory(modDir);
            } catch (Exception e) {
                // TODO Handle exception properly
                throw new RuntimeException(e);
            }
        }

        System.out.println("Cloning repository..");
        Git git = Git.cloneRepository().setURI(repo).setDirectory(new File("mods/modlist")).call();
        git.close();
        System.out.println("Successfully cloned repository!");

        System.out.println("Extracting mods...");
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
            System.out.println("Mods successfully extracted!");
            gitDir =  Git.open(new File("mods"));
            return true;
        } catch (Exception e) {
            // TODO Handle exception properly
            throw new RuntimeException(e);
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
            // TODO Handle exception properly
            throw new RuntimeException(e);
        }
        Files.delete(dir);
    }
}
