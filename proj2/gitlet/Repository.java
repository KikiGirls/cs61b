package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static gitlet.Commit.getCommitByUid;
import static gitlet.Commit.getCurrCommit;
import static gitlet.Utils.*;
import static gitlet.Branch.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author syWang
 */

public class Repository {

    /**
     * 主文件夹
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    //存放提交和快照文件
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");

    //    存放分支
    public static final File REFS_OIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_OIR, "heads");

    //    存放头文件
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    //    存放缓存文件
    public static final File STAGES_DIR = join(GITLET_DIR, "stage");
    public static final File ADD_DIR = join(STAGES_DIR, "addstage");
    public static final File REMOVE_DIR = join(STAGES_DIR, "removestage");


    /*初始化gitlet*/
    public static void init() {

        setrope();
        creatBranch("master");
        setHEAD("master");
        //        创建初始提交
        Commit initcommit = new Commit();
        initcommit.add();

        //        创建初始分支
    }

    private static void setrope() {
        //        检查一下是否存在仓库
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0); //退出
        }

        //        创建文件夹初始化仓库
        List<File> dirs = List.of(GITLET_DIR, COMMITS_DIR, STAGES_DIR, HEADS_DIR, BLOBS_DIR, ADD_DIR, REMOVE_DIR);
        dirs.forEach(File::mkdirs);
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addFile2Stage(String[] args) {
        String filename = args[1]; //args = add [file name]

        addFile2Stage(filename);


    }

    public static void addFile2Stage(String filename) {
        File currFile = join(CWD, filename);
        File stagingFile = join(ADD_DIR, filename);


        if (!currFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        if (flieInRemoveStage(filename)) {
            join(REMOVE_DIR, filename).delete();

        } else if (flieInCurrCommit(filename)) {
            String currFileUid = sha1(readContentsAsString(currFile));

            String uid = fileUidInCurrCommit(filename);

            if (uid.equals(currFileUid) && stagingFile.exists()) {
                stagingFile.delete();
            } else if (!uid.equals(currFileUid)) {
                try {
                    Files.copy(currFile.toPath(), stagingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            try {
                Files.copy(currFile.toPath(), stagingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addFile2Stage(String filename, String blobuid) {
        File f = join(BLOBS_DIR, blobuid);
        File stagingFile = join(ADD_DIR, filename);
        try {
            Files.copy(f.toPath(), stagingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commit(String[] args) {
        String message = args[1]; //// args = commit [message]
        if (message.equals("")) { // args = commit
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit commit = new Commit(message);
        commit.add();
    }

    private static String fileUidInCurrCommit(String filename) {
        Commit currCommit = getCurrCommit();
        return currCommit.getBlobs().get(filename);
    }

    private static boolean flieInCurrCommit(String filename) {
        Commit currCommit = getCurrCommit();
        return currCommit.getBlobs().containsKey(filename);
    }

    public static void deleteStage() {
        deleteFiles(ADD_DIR);
        deleteFiles(REMOVE_DIR);
    }

    public static void deleteFiles(File dir) {
        List<String> filesName = plainFilenamesIn(dir);
        for (String fileName : filesName) {
            join(dir, fileName).delete();
        }
    }

    public static String creatBlobFromADDstage(String filename) {
        File f = join(ADD_DIR, filename);
        String fUid = sha1(readContentsAsString(f));
        File blob = join(BLOBS_DIR, fUid);
        try {
            Files.copy(f.toPath(), blob.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fUid;
    }

    public static void rmFile2Stage(String[] args) {
        String fileName = args[1]; //args =rm [file name]

        if (fileInAddstage(fileName)) {
            join(ADD_DIR, fileName).delete();
        } else if (flieInCurrCommit(fileName)) {
            deleteCWDfile(fileName);
            File filetorm = join(REMOVE_DIR, fileName);
            try {
                filetorm.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }

    public static void deleteCWDfile(String fileName) {
        File f = join(CWD, fileName);
        if (f.exists()) {
            f.delete();
        }
    }

    private static boolean fileInAddstage(String fileName) {
        return fileIn(ADD_DIR, fileName);
    }

    private static boolean flieInRemoveStage(String fileName) {
        return fileIn(REMOVE_DIR, fileName);
    }


    private static boolean fileIn(File addDir, String fileName) {
        List<String> toAdd = plainFilenamesIn(addDir);
        if (toAdd != null) {
            return toAdd.contains(fileName);
        }
        return false;
    }

    public static void findCommit(String[] args) {
        String message = args[1]; //args = find [commit message]
        boolean finded = false;
        List<String> commitList = plainFilenamesIn(COMMITS_DIR);
        for (String commitUid : commitList) {
            Commit commit = getCommitByUid(commitUid);
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.getuid());
                finded = true;
            }


        }
        if (!finded) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }

    }

    static void checkoutFile(String[] args) {
        //args = checkout -- [file name]
        // 获取头提交中存在的文件版本并将其放入工作目录中，
        // 覆盖已存在的文件版本（如果存在）。该文件的新版本未暂存。
        String filename = args[2];
        String blobuid = getCurrCommit().getFileUid(filename);
        checkoutBlob2CWD(filename, blobuid);
    }

    private static File getBlobFromCurrCommit(String filename) {
        Commit commit = getCurrCommit();
        return getBlobFromCommit(filename, commit);

    }

    private static File getBlobFromCommit(String filename, Commit commit) {
        HashMap<String, String> blobs = commit.getBlobs();
        if (!blobs.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobUid = blobs.get(filename);
        return getBlobByUid(blobUid);
    }

    public static String getBlobUidFromCommit(String filename, Commit commit) {
        return commit.getFileUid(filename);
    }

    static File getBlobByUid(String blobUid) {
        File blob = join(BLOBS_DIR, blobUid);
        return blob;
    }

    public static void checkoutCommitFile(String[] args) {
        // checkout [commit id] -- [file name]
        // 获取具有给定 id 的提交中存在的文件版本，并将其放入工作目录中，
        // 覆盖已存在的文件版本（如果存在）。该文件的新版本未暂存。
        String fileName = args[3];
        String commitUid = args[1];

        Commit commit = getCommitByUid(commitUid);
        String blobuid = commit.getFileUid(fileName);
        if (blobuid == null) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        checkoutBlob2CWD(fileName, blobuid);

    }

    public static void checkoutBlob2CWD(String fileName, String blobuid) {
        File blob = getBlobByUid(blobuid);
        File f = join(CWD, fileName);
        try {
            Files.copy(blob.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkoutBranch(String[] args) {
        //获取给定分支头部提交中的所有文件，并将它们放入工作目录中，覆盖已存在的文件版本（如果存在）。
        // 此外，在此命令结束时，给定分支现在将被视为当前分支 (HEAD)。
        // 当前分支中跟踪但不存在于签出分支中的任何文件都将被删除。
        // 暂存区域将被清除，除非签出的分支是当前分支（请参阅下面的失败案例）。
        String branchName = args[1]; //args = checkout [branch name]
        checkoutBranch(branchName);

    }

    public static void checkoutBranch(String branchName) {
        if (Objects.equals(branchName, getCurrBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File branchFile = getBranchFileByName(branchName);
        if (branchFile == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Commit commit = getCurrCommitInBranch(branchName);
        checkoutAllblobFromCommit2CWD(commit);
        setHEAD(branchName);
    }

    static void setHEAD(String branchName) {
        writeContents(HEAD, branchName);
    }

    private static void checkoutAllblobFromCommit2CWD(Commit commit) {
        Map<String, String> blobs = commit.getBlobs();
        Map<String, String> currCommitBlobs = getBlobsFromCurrCommit();
        List<String> cwdFiles = getCWDFiles();

        List<String> allfilesname = allfilesname(blobs.keySet(), currCommitBlobs.keySet(), cwdFiles);
        for (String fileName : allfilesname) {

            if (blobs.containsKey(fileName)
                    && !currCommitBlobs.containsKey(fileName)
                    && cwdFiles.contains(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            } else if (blobs.containsKey(fileName) && currCommitBlobs.containsKey(fileName)) {
                checkoutBlob2CWD(fileName, blobs.get(fileName));
            } else if (!blobs.containsKey(fileName) && currCommitBlobs.containsKey(fileName)) {
                deleteCWDfile(fileName);
            } else if (blobs.containsKey(fileName) && !currCommitBlobs.containsKey(fileName)) {
                checkoutBlob2CWD(fileName, blobs.get(fileName));
            }

        }

        deleteStage();
    }


    public static List<String> allfilesname(Set<String> blobsFiles,
                                            Set<String> currCommitFiles, List<String> cwdFiles) {
        // 使用一个 Set 来去重所有的文件名
        Set<String> allFilesSet = new HashSet<>();

        // 将所有文件名加入 Set（Set 自动去重）
        allFilesSet.addAll(blobsFiles);
        allFilesSet.addAll(currCommitFiles);
        allFilesSet.addAll(cwdFiles);

        // 将 Set 转为 List 并返回
        return new ArrayList<>(allFilesSet);
    }

    public static List<String> allfilesname(Set<String> blobsFiles, Set<String> currCommitFiles, Set<String> cwdFiles) {
        // 使用一个 Set 来去重所有的文件名
        Set<String> allFilesSet = new HashSet<>();

        // 将所有文件名加入 Set（Set 自动去重）
        allFilesSet.addAll(blobsFiles);
        allFilesSet.addAll(currCommitFiles);
        allFilesSet.addAll(cwdFiles);

        // 将 Set 转为 List 并返回
        return new ArrayList<>(allFilesSet);
    }


    private static List<String> getCWDFiles() {
        List<String> cwdfiles = plainFilenamesIn(CWD);
        return cwdfiles;
    }

    private static Map<String, String> getBlobsFromCurrCommit() {
        Commit currCommit = getCurrCommit();
        return currCommit.getBlobs();
    }

    static Commit getCurrCommitInBranch(String branchName) {
        String commitUid = getCommitUidInBranch(branchName);
        Commit commit = getCommitByUid(commitUid);
        return commit;
    }

    public static String getCommitUidInBranch(String branchName) {
        File branchFile = getBranchFileByName(branchName);
        String commitUid = readContentsAsString(branchFile);
        return commitUid;
    }

    public static void reset(String[] args) {
        //args = reset [commit id]
        String commitUid = args[1];
        Commit commit = getCommitByUid(commitUid);
        checkoutAllblobFromCommit2CWD(commit);
        setCurrBranchHead(commitUid);
        deleteStage();
    }

    public static boolean stageisNull() {
        return isNull(ADD_DIR) && isNull(REMOVE_DIR);
    }

    private static boolean isNull(File dir) {
        List<String> files = plainFilenamesIn(dir);
        return files.isEmpty();
    }


}

