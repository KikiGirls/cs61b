package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static gitlet.Commit.*;
import static gitlet.Repository.*;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.branch.getCurrBranchName;


public class log {
    public static void printlog() throws IOException {
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

    public static void printGlobalLog() throws IOException {
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

    public static void printStatus() throws IOException {
        System.out.println("=== Branches ===");
        printBranchName();
        System.out.println("\n=== Staged Files ===");
        printFilesNameIn(ADD_DIR);
        System.out.println("\n=== Removed Files ===");
        printFilesNameIn(REMOVE_DIR);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    private static void printBranchName() throws IOException {
        System.out.println("*" + getCurrBranchName());
        List<String> filesName = plainFilenamesIn(HEADS_DIR);
        for (String fileName : filesName) {
            if (fileName.equals( getCurrCommitName())){
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
