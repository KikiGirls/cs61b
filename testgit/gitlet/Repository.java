package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.branch.nowBranch;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {

    // <editor-fold desc="定义文件夹">

    /** 当前工作目录 */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** 主文件夹 */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** 存放提交的文件夹 */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");

    /** 用来当作文件的缓存区 */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File STAGTORM_DIR = join(STAGING_DIR, "rm");

    /** 保存文件快照 */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    /** 保存日志 */
    public static final File LOGS_DIR = join(GITLET_DIR, "logs");

    /** 这个目录保存所有分支的引用 */
    public static final File HEADS_DIR = join(GITLET_DIR, "heads");

    /** 保存头指针 */
    public static final File HEAD = join(GITLET_DIR, "head");

    // </editor-fold>


    /** 初始化gitlet,创建新的仓库 */
    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
            //失败案例：如果当前目录中已经存在 Gitlet 版本控制系统，则它应该中止。
            // 它不应该用新系统覆盖现有系统。应该打印错误消息
            // 当前目录中已存在 Gitlet 版本控制系统。'']]]}
        }

        //创建文件夹
        List<File> dirs = List.of(GITLET_DIR, COMMIT_DIR, STAGING_DIR, HEADS_DIR, LOGS_DIR, BLOBS_DIR, STAGTORM_DIR);
        dirs.forEach(File::mkdirs);

        //初始化提交
        Commit initialCommit = new Commit();

        //创建新分支master
        File masterFile = join(HEADS_DIR, "master");
        masterFile.createNewFile();
        HEAD.createNewFile();//头指针
        //得到uid然后写入
        writeContents(masterFile, initialCommit.getUid());
        writeContents(HEAD, masterFile.toString());

    }

    /** 得到头指针的commit */
    public static Commit headCommit() throws IOException {
        String commitFileUid = readContentsAsString(join(readContentsAsString(HEAD)));
        File commitFile = join(COMMIT_DIR, commitFileUid);
        return readObject(commitFile, Commit.class);
    }

    /** 添加到缓存区 */
    public static void addFile(String[] args) throws IOException {
        String filename = args[1];//args = add [file name]
        File currFile = join (CWD, filename);
        File stagingFile = join(STAGING_DIR, filename);
        if (!currFile.exists()) {
            throw error("File does not exist: ");
            //失败案例：如果文件不存在，则打印错误信息File does not exit。并退出而不做任何改变。
        }
            //判断这个文件在不在上一次提交中
        if (headCommit().getFields().containsKey(filename)) {
            //获取hash值比较是否修改
            String currFileHash = sha1(readContentsAsString(currFile));
            String fieldHash = headCommit().getFields().get(filename);
            if (currFileHash.equals(fieldHash)) {
                // 如果它已经存在，则将其从暂存区域中删除
                if (stagingFile.exists()) {
                    restrictedDelete(stagingFile);
                }
                return;
            }
        }
        Files.copy(currFile.toPath(), stagingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        stagingFile.createNewFile();
    }

    /** 从父提交里面继承然后提交新的 */
    public static void commitFile(String[] args) throws IOException {
        //把缓存文件夹里面的文件变为数组,然后遍厉
        String message = args[1];//// args = commit [message]
        List<String> stagingFiles = plainFilenamesIn(STAGING_DIR);
        List<String> toRm = plainFilenamesIn(STAGTORM_DIR);
        HashMap<String, String> newFields = new HashMap<>(headCommit().getFields());
        if (stagingFiles != null) {
            for (String stagingFileName : stagingFiles) {
                String value = sha1(readContentsAsString(join(STAGING_DIR,stagingFileName)));
                newFields.put(stagingFileName, value);
                File blob = join(BLOBS_DIR,value);
                Files.copy(join(STAGING_DIR,stagingFileName).toPath(), blob.toPath());
            }
        }
        if (toRm != null) {
            //删除文件
            for (String stagingFileName : toRm) {
                newFields.remove(stagingFileName);
            }
        }
        String[] Lists = new String[2];
        Lists[0] = headCommit().getUid();
        Commit newCommit = new Commit(message, Lists, newFields);
        writeContents(join(HEADS_DIR, nowBranch()), newCommit.getUid());
    }

    /** 删除文件 */
    public static void rmFile(String[] args) throws IOException {
        String fileName = args[1];//args =rm [file name]
        File stagingFile = join(STAGING_DIR, fileName);
        if (stagingFile.exists()) {
            //如果文件当前已暂存以供添加，则取消暂存该文件
            restrictedDelete(stagingFile);
        } else if (headCommit().getFields().containsKey(fileName)) {
            //如果在当前提交中跟踪该文件，则将其暂存以进行删除，并从工作目录中删除该文件
            File currFile = join(CWD, fileName);
            File rmfile = join(STAGTORM_DIR, fileName);
            Files.copy(currFile.toPath(), rmfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            currFile.delete();
        } else {
            //失败情况：如果文件既没有被暂存，也没有被头提交跟踪，则打印错误消息No Reason to remove the file。
            throw Utils.error("No reason to remove the file.");
        }
    }

    /**
     * 清空缓存的文件
     */
    public static void deleteStagingFile(){
        //清空缓存区
        List<String> strings = plainFilenamesIn(STAGING_DIR);
        if (strings != null && !strings.isEmpty()) {
            for (String string : strings) {
                join(STAGING_DIR, string).delete();
            }
        }
        //清空删除区
        List<String> stringsToRm = plainFilenamesIn(STAGTORM_DIR);
        if (stringsToRm != null && !stringsToRm.isEmpty()) {
            for (String string : stringsToRm) {
                join(STAGTORM_DIR, string).delete();
            }
        }

    }

    /** 签出分支 */
    public static void checkoutBranch(String[] args) throws IOException {
        //获取给定分支头部提交中的所有文件，并将它们放入工作目录中，覆盖已存在的文件版本（如果存在）。
        // 此外，在此命令结束时，给定分支现在将被视为当前分支 (HEAD)。
        // 当前分支中跟踪但不存在于签出分支中的任何文件都将被删除。
        // 暂存区域将被清除，除非签出的分支是当前分支（请参阅下面的失败案例）。
        String branchName = args[1];//args = checkout [branch name]
        if (!join(HEADS_DIR, branchName).exists()) {
            //如果不存在具有该名称的分支，则打印不存在这样的分支。
            throw Utils.error("No such branch exists.");
        } else if (branchName.equals(nowBranch())) {
            //如果该分支是当前分支，则打印不需要签出当前分支。
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        } else if (hasUntrackedFiles()) {
            // 如果当前分支中的工作文件未跟踪并且将被检出覆盖，
            // 则打印 There is an untracked file in the way;删除它，或者先添加并提交。
            // 并退出；在执行其他操作之前执行此检查。不要更改 CWD。
            Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        } else {
            deleteCwdAllFile();//删除工作区文件
            deleteStagingFile();//删除缓存区文件
            File branchFile = join(HEADS_DIR, branchName);
            Commit branchCommit = readObject(join(branchFile), Commit.class);
            checkoutFilesFromCommit(branchCommit);//从提交中恢复文件到cwd
        }
    }

    /**
     * 获取给定提交中存在的文件版本并将其放入工作目录中
     */
    public static void checkoutCommitFile(String[] args) throws IOException {
        // checkout [commit id] -- [file name]
        // 获取具有给定 id 的提交中存在的文件版本，并将其放入工作目录中，
        // 覆盖已存在的文件版本（如果存在）。该文件的新版本未暂存。
        String fileName = args[3];
        String commitUid = args[1];
        // 如果不存在具有给定 id 的提交，则打印 No commit with that id exits。
        File commitFile = join(COMMIT_DIR, commitUid);
        if (!commitFile.exists()) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit commit = readObject(commitFile, Commit.class);
            if (!commit.getFields().containsKey(fileName)) {
                // 否则，如果给定提交中不存在该文件，则打印与失败情况 1 相同的消息
                throw error("File does not exist in that commit.");
            } else {
                // 获取具有给定 id 的提交中存在的文件版本，并将其放入工作目录中，
                replaceCwdFile(fileName, commit);
            }

        }
    }

    /**
     * 获取头提交中存在的文件版本并将其放入工作目录中
     */
    public static void checkoutFile(String[] args) throws IOException {
        //args = checkout -- [file name]
        // 获取头提交中存在的文件版本并将其放入工作目录中，
        // 覆盖已存在的文件版本（如果存在）。该文件的新版本未暂存。
        String filename = args[2];
        String[] newArgs  = {"checkout", " -- ", headCommit().getUid(), filename};
        checkoutCommitFile(newArgs);
    }

    /**
     * 删除工作区的全部文件
     */
    public static void deleteCwdAllFile() {
        List<String> files = plainFilenamesIn(CWD);
        for (String file : files) {
            join(CWD, file).delete();
        }
    }

    /**
     * 将文件替换成提交中的版本
     */
    public static void replaceCwdFile(String fileName, Commit commit) throws IOException {
        File nweflie = join(CWD, fileName);
        Files.copy(join(BLOBS_DIR, commit.getFields().get(fileName)).toPath(), nweflie.toPath(), StandardCopyOption.REPLACE_EXISTING );

    }

    /** 判断有没有未跟踪的文件 */
    public static boolean hasUntrackedFiles() throws IOException {
        List<String> files = plainFilenamesIn(CWD);
        // 遍历当前工作目录中的所有文件
        for (String fileName : files) {
            // 检查文件是否在当前提交中
            if (!headCommit().getFields().containsKey(fileName)) {
                return true; // 找到未跟踪的文件，立即返回 true
            }
        }
        return false; // 所有文件都已跟踪，返回 false
    }

    /**
     * 描述：检查给定提交跟踪的所有文件。删除该提交中不存在的跟踪文件。
     * 还将当前分支的头移动到该提交节点。请参阅简介，了解使用重置后头指针会发生什么情况的示例。
     * [commit id] 可以缩写为结账。暂存区已清理完毕。
     * 该命令本质上是检出任意提交，该提交也更改了当前分支头。
     */
    public static void reset(String[] args) throws IOException {
        //args = reset [commit id]
        String commitUid = args[1];//需要倒回的id
        File commitFile = join(COMMIT_DIR, commitUid);//相应的提交
        if (!commitFile.exists()) {
            //失败情况：如果不存在给定 id 的提交，则打印 No commit with that id exits。
            throw error("No commit with that id exists.");
        } else if (hasUntrackedFiles()) {
            // 则打印 `There is an untracked file in the way;删除它，或者先添加并提交它。`
            //如果当前分支中的工作文件未被跟踪并且将被重置覆盖
            message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        } else {
            Commit commit = readObject(commitFile, Commit.class);//读取提交
            deleteCwdAllFile();//清空工作区
            deleteStagingFile();//清空缓存区
            checkoutFilesFromCommit(commit);//恢复文件到CWD
            writeContents(join(HEADS_DIR, nowBranch()), commitUid);//更改指针
        }
    }

    /** 从提交中恢复文件到cwd */
    public static void checkoutFilesFromCommit(Commit commit) throws IOException {
        for (Map.Entry<String, String> entry : commit.getFields().entrySet()) {
            File newfile = join(CWD, entry.getKey());
            Files.copy(join(BLOBS_DIR,entry.getValue()).toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }






}