package jmalvin.modsync.tools;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class ModDownloader {
    public static void setupRepo(String repo) throws GitAPIException{
        Git.lsRemoteRepository()
                .setRemote(repo)
                .call();

        Path modDir = Paths.get("mods");
        if (!Files.exists(modDir)) {
            try {
                Files.createDirectory(modDir);
            } catch (Exception e) {
                // TODO Handle exception properly
                return;
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
                if (modName.endsWith("jar")) {
                    Files.move(mod, mod.getParent().getParent().resolve(mod.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } else if (Files.isDirectory(mod)) {
                    deleteDirectory(mod);
                } else {
                    Files.delete(mod);
                }
            }
            Files.delete(modlist);
            System.out.println("Mods successfully extracted!");
        } catch (Exception e) {
            // TODO Handle exception properly
        }
    }

    private static void deleteDirectory(Path dir) throws IOException {
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
