package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;
import static gitlet.branch.nowBranch;


public class log {

    /**
     * 打印全级日志
     *
     */
    public static void printGlobalLog() throws IOException {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        if (commitList != null) {
            for (String commitName: commitList){
                Commit commit = readObject(join(COMMIT_DIR, commitName), Commit.class);
                dateLog(commit);
            }
        }
        File log = join(LOGS_DIR, "currlog");
        String logs = readContentsAsString(log);
        System.out.println(logs);//打印日志
        log.delete();
    }

    /**
     * 读取头文件然后开始生成日志
     */
    public static void creatlog(Commit commit) throws IOException {
        dateLog(commit);
        if (Arrays.equals(commit.getParent(), new String[2])) {
            return;
        }
        Commit prenatCommit = readObject(join(COMMIT_DIR, commit.getParent()[0]), Commit.class);
        creatlog(prenatCommit);
    }

    /**
     创造提交日志
     */
    public static void dateLog(Commit commit) throws IOException {
        String currLog;
        if (commit.getParent()[1] == null) {
            currLog = "==="
                    + "\ncommit " + commit.getUid()
                    + "\nDate: " + commit.getDate().toString()
                    + "\n" + commit.getMessage() + "\n";
        } else {
            String f = commit.getParent()[0].substring(0, 7);
            String s = commit.getParent()[1].substring(0, 7);
            currLog = "==="
                    + "\ncommit " + commit.getUid()
                    + "\nDate: " + commit.getDate().toString()
                    + "\nMerge: " + f + " " + s
                    + "\n" + commit.getMessage() + "\n";
        }
        File log = join(LOGS_DIR, "currlog");
        if (!log.exists()) {
            log.createNewFile();
            writeContents(log, currLog);
        } else
        {
            String oldLog = readContentsAsString(log);
            currLog = oldLog + "\n" + currLog;
            writeContents(log, currLog);
        }

    }

    /** 打印日志
     */
    public static void printlog() throws IOException {

        creatlog(headCommit());//递归生成日志
        File log = join(LOGS_DIR, "currlog");
        String logs = readContentsAsString(log);
        System.out.println(logs);//打印日志
        log.delete();//删除日志
    }

    /** 打印状态 */
    public static void printStatus() {
        System.out.println("=== Branches ===");
        System.out.println("*" + nowBranch());
        for (String branches : Objects.requireNonNull(plainFilenamesIn(HEADS_DIR))) {
            if (!branches.equals(nowBranch())) {
                System.out.println(branches);
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (String files : Objects.requireNonNull(plainFilenamesIn(STAGING_DIR))) {
            System.out.println(files);
        }
        System.out.println("\n=== Removed Files ===");
        for (String files : Objects.requireNonNull(plainFilenamesIn(STAGTORM_DIR))) {
            System.out.println(files);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

}
