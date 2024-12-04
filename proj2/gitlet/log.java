package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gitlet.Commit.*;
import static gitlet.Repository.*;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.branch.getCurrBranchName;


public class log {
    public static void printlog() {
        Commit commit = getCurrCommit();
        while (commit != null) {
            if (commit.isMergeCommit()){
                printMergeCommit(commit);
            } else {
                printCommit(commit);
            }
            commit = commit.getPrexCommit();
        }
    }

    public static void printGlobalLog() {
        List<String> commitList = plainFilenamesIn(COMMITS_DIR);
        for (String commituid : commitList) {
            Commit commit = getCommitByUid(commituid);
            printCommit(commit);
        }
    }


    private static void printMergeCommit(Commit commit) {
        String f = commit.getParent()[0].substring(0, 7);
        String s = commit.getParent()[1].substring(0, 7);
        String out = "==="
                + "\ncommit " + commit.getuid()
                + "\nDate: " + dateToTimeStamp(commit.getDate())
                + "\nMerge: " + f + " " + s
                + "\n" + commit.getMessage() + "\n";
        System.out.println(out);
    }

    private static void printCommit(Commit commit) {
        String out = "==="
                + "\ncommit " + commit.getuid()
                + "\nDate: " + dateToTimeStamp(commit.getDate())
                + "\n" + commit.getMessage() + "\n";
        System.out.println(out);
    }

    public static void printStatus() {
        System.out.println("=== Branches ===");
        printBranchName();
        System.out.println("\n=== Staged Files ===");
        printFilesNameIn(ADD_DIR);
        System.out.println("\n=== Removed Files ===");
        printFilesNameIn(REMOVE_DIR);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
        printUntrackedFiles();
    }

    private static void printUntrackedFiles() {
        // Ensure file lists are non-null
        List<String> filelist = plainFilenamesIn(CWD);
        filelist = filelist == null ? new ArrayList<>() : filelist;

        List<String> addlist = plainFilenamesIn(ADD_DIR);
        addlist = addlist == null ? new ArrayList<>() : addlist;

        List<String> removelist = plainFilenamesIn(REMOVE_DIR);
        removelist = removelist == null ? new ArrayList<>() : removelist;

        // Convert commit blobs keySet to a list
        List<String> commitblobs = new ArrayList<>(getCurrCommit().getBlobs().keySet());

        // Track already printed files
        Set<String> printed = new HashSet<>();

        for (String filename : filelist) {
            if (!commitblobs.contains(filename) && !addlist.contains(filename) && !printed.contains(filename)) {
                System.out.println(filename);
                printed.add(filename);
            }

            if (removelist.contains(filename) && !printed.contains(filename)) {
                System.out.println(filename);
                printed.add(filename);
            }
        }
    }

    private static void printBranchName() {
        String currentBranchName = getCurrBranchName();
        System.out.println("*" + currentBranchName);
        List<String> filesName = plainFilenamesIn(HEADS_DIR);
        for (String fileName : filesName) {
            if (!fileName.equals(currentBranchName)) {
                System.out.println(fileName);
            }
        }
    }

    private static void printFilesNameIn(File Dir) {
        List<String> filesName = plainFilenamesIn(Dir);
        printFilesNameInList(filesName);
    }

    private static void printFilesNameInList(List<String> filesName) {
        for (String fileName : filesName) {
            System.out.println(fileName);
        }
    }
}
