package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Commit.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Branch {

    public static void creatBranch(String[] args)  {
        String branchName = args[1]; //args = branch [branch name]
        String uid = getCurrCommit().getuid();
        creatBranch(branchName);
        setBranchHead(branchName, uid);
    }

    private static void setBranchHead(String branchName, String uid) {
        File f = join(HEADS_DIR, branchName);
        writeContents(f, uid);
    }

    public static void setCurrBranchHead(String uid)  {
        File currBranchFile = getCurrBranchFile();
        writeContents(currBranchFile, uid); //更新头文件
        setHEAD(getCurrBranchName());
    }


    public static void deleteBranch(String[] args)  {
        String branchName = args[1]; //args = rm-branch [branch name]
        if (Objects.equals(branchName, getCurrBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File f = getBranchFileByName(branchName);
        if (f == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            f.delete();
        }


    }

    public static  void  creatBranch(String branchName)  {
        File branch = join(HEADS_DIR, branchName);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
        }
        try {
            branch.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getCurrBranchName()  {
        String branchName = readContentsAsString(HEAD);
        return branchName;
    }


    public static File getCurrBranchFile()  {
        String branchName = getCurrBranchName();
        return getBranchFileByName(branchName);
    }

    static File getBranchFileByName(String branchName) {
        File branch = join(HEADS_DIR, branchName);
        if (!branch.exists()) {
            return null;
        }
        return branch;
    }

    public static void mergeBranch(String[] args)  {
        String branchName = args[1]; //args = merge [branch name]

        if (!stageisNull()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        File branch = getBranchFileByName(branchName);
        if (branch == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currBranchName = getCurrBranchName();
        if (Objects.equals(branchName, currBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit spc = getSplitPointCommit(branchName, currBranchName);

        if (Objects.equals(spc.getuid(), getCurrCommitName())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        if (Objects.equals(spc.getuid(), getCommitUidInBranch(branchName))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        changeCWD(spc, branchName);

        String messsgae = "Merged " + branchName + " into " + getCurrBranchName();
        Commit commit = new Commit(messsgae, currBranchName, branchName);
        commit.add();

    }

    //    private static void changeCWD(Commit spc, String branchName)  {
    //        boolean conflict = false;
    //
    //        HashMap<String,String> SP = spc.getBlobs();
    //        HashMap<String,String> GB = getCurrCommitInBranch(branchName).getBlobs();
    //        HashMap<String,String> CC = getCurrCommit().getBlobs();
    //
    //        List<String> filesName = ALLfilesName(SP.keySet(),CC.keySet(),GB.keySet());
    //
    //        for (String fileName : filesName) {
    //
    //            if (GB.containsKey(fileName) && SP.containsKey(fileName)
    //            && GB.get(fileName).equals(SP.get(fileName)) &&
    //                    !CC.containsKey(fileName) && CWDhave(fileName)) {
    //                error("There is an untracked file in the way; delete it, or add and commit it first.");
    //                System.exit(0);}
    //
    //            if (SP.containsKey(fileName)) {
    //                if (GB.containsKey(fileName) && CC.containsKey(fileName)) {
    //                    if (GB.get(fileName).equals(CC.get(fileName))) {
    //                        addFile2Stage(fileName,GB.get(fileName));
    //                        checkoutBlob2CWD(fileName,GB.get(fileName));
    //                    } else if (GB.get(fileName).equals(SP.get(fileName))
    //                    && !CC.get(fileName).equals(SP.get(fileName))) {
    //                        addFile2Stage(fileName,CC.get(fileName));
    //                        checkoutBlob2CWD(fileName,CC.get(fileName));
    //                    } else if (CC.get(fileName).equals(SP.get(fileName)) &&
    //                    !GB.get(fileName).equals(SP.get(fileName))) {
    //                        addFile2Stage(fileName,GB.get(fileName));
    //                        checkoutBlob2CWD(fileName,GB.get(fileName));
    //                    } else if (!CC.get(fileName).equals(GB.get(fileName))) {
    //                        replaceCWDFile(fileName, CC.get(fileName), GB.get(fileName));
    //                        addFile2Stage(fileName);
    //                        conflict = true;
    //                    }
    //                } else if (!GB.containsKey(fileName) && CC.containsKey(fileName)) {
    //                    if (CC.get(fileName).equals(SP.get(fileName))) {
    //                        deleteCWDfile(fileName);
    //                    } else {
    //                        replaceCWDFile(fileName, CC.get(fileName), null);
    //                        addFile2Stage(fileName);
    //                        conflict = true;
    //                    }
    //                } else if (!CC.containsKey(fileName) && GB.containsKey(fileName)) {
    //                    if (!SP.get(fileName).equals(GB.get(fileName))) {
    //                        replaceCWDFile(fileName, null, GB.get(fileName));
    //                        addFile2Stage(fileName);
    //                        conflict = true;
    //                    }
    //                }
    //            }
    //            if (!SP.containsKey(fileName)) {
    //                if (!GB.containsKey(fileName) && CC.containsKey(fileName)) {
    //                    addFile2Stage(fileName,CC.get(fileName));
    //                    checkoutBlob2CWD(fileName,CC.get(fileName));
    //                } else if (GB.containsKey(fileName) && !CC.containsKey(fileName)) {
    //                    addFile2Stage(fileName,GB.get(fileName));
    //                    checkoutBlob2CWD(fileName,GB.get(fileName));
    //                } else if (!CC.get(fileName).equals(GB.get(fileName))) {
    //                    replaceCWDFile(fileName, CC.get(fileName), GB.get(fileName));
    //                    addFile2Stage(fileName);
    //                    conflict = true;
    //                }
    //            }
    //
    //        }
    //
    //        if (conflict) {
    //            message("Encountered a merge conflict.");
    //        }
    //
    //    }

    private static boolean cwdhave(String fileName) {
        List<String> cwdfiles = plainFilenamesIn(CWD);
        return cwdfiles.contains(fileName);
    }

    private static void replaceCWDFile(String flieName, String blobuid1, String blobuid2) {
        String a = stringblob(blobuid1);
        String b = stringblob(blobuid2);
        String c = "<<<<<<< HEAD\n"
                + a
                + // HEAD 的内容紧跟在标志后
                "=======\n"
                +
                b
                + // 对方分支内容紧跟在标志后
                ">>>>>>>\n";
        File f = join(CWD, flieName);
        writeContents(f, c);

    }

    private static String stringblob(String blobuid2) {
        if (blobuid2 == null) {
            return "";

        } else {
            File blob = getBlobByUid(blobuid2);
            return readContentsAsString(blob).stripTrailing() + "\n";
        }
    }

    //    private static Commit getSplitPointCommit(String branchName, String currBranchName)  {
    //        // 获取两条分支的 commit 链表
    //        Vector<String> A = getCommitlinkedList(branchName);
    //        Vector<String> B = getCommitlinkedList(currBranchName);
    //
    //        // 找公共祖先
    //        String splitPointUid = null;
    //        int i = 0;
    //        while (i < A.size() && i < B.size()) {
    //            String commitA = A.get(A.size() - 1 - i); // 从尾部向前遍历
    //            String commitB = B.get(B.size() - 1 - i); // 从尾部向前遍历
    //            if (Objects.equals(commitA, commitB)) {
    //                splitPointUid = commitA; // 更新公共祖先
    //            } else {
    //                break; // 一旦不同就停止
    //            }
    //            i++;
    //        }
    //
    //        // 如果没有公共祖先，返回 null 或抛出异常
    //        if (splitPointUid == null) {
    //            System.out.println("No common ancestor found between branches.");
    //            System.exit(0);
    //        }
    //
    //        return getCommitByUid(splitPointUid);
    //    }

    private static Commit getSplitPointCommit(String branchName, String currBranchName) {
        // 用于存储第一个分支的所有访问过的提交
        Set<String> visited = new HashSet<>();

        // 遍历第一个分支，记录所有提交 UID
        Queue<Commit> queue = new LinkedList<>();
        queue.add(getCurrCommitInBranch(branchName));
        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            if (commit == null) {
                continue;
            }
            visited.add(commit.getuid()); // 将提交的 UID 添加到集合
            for (Commit parent : commit.getParents()) { // 遍历所有父提交
                if (parent != null && !visited.contains(parent.getuid())) {
                    queue.add(parent);
                }
            }
        }

        // 遍历第二个分支，找到第一个公共祖先
        queue.add(getCurrCommitInBranch(currBranchName));
        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            if (commit == null) {
                continue;
            }
            if (visited.contains(commit.getuid())) {
                return commit; // 找到第一个公共祖先
            }
            for (Commit parent : commit.getParents()) { // 遍历所有父提交
                if (parent != null) {
                    queue.add(parent);
                }
            }
        }

        // 如果未找到公共祖先，抛出异常
        throw new IllegalArgumentException("No common ancestor found between branches.");
    }
    private static Vector<String> getCommitlinkedList(String branchName)  {
        Commit commit = getCurrCommitInBranch(branchName);
        Vector<String> linkedList = new Vector<>();
        while (commit != null) {
            linkedList.add(commit.getuid());
            commit = commit.getPrexCommit();
        }
        return linkedList;
    }

    private static void changeCWD(Commit splitPoint, String branchName)  {
        boolean conflict = false;

        // 获取三个提交的 Blob 映射
        HashMap<String, String> spBlobs = splitPoint.getBlobs(); // 分裂点提交
        HashMap<String, String> branchBlobs = getCurrCommitInBranch(branchName).getBlobs(); // 分支当前提交
        HashMap<String, String> currBlobs = getCurrCommit().getBlobs(); // 当前分支提交

        // 收集所有文件名
        List<String> fileNames = collectAllFileNames(spBlobs.keySet(), currBlobs.keySet(), branchBlobs.keySet());

        // 遍历所有文件并处理逻辑
        for (String fileName : fileNames) {
            // 检查是否有未跟踪的冲突文件
            if (hasUntrackedConflict(branchBlobs, spBlobs, currBlobs, fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            // 处理合并逻辑，返回是否发生冲突
            if (handleMergeCases(spBlobs, branchBlobs, currBlobs, fileName)) {
                conflict = true;
            }
        }

        // 如果发生冲突，输出提示信息
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    // 处理文件合并逻辑，返回是否发生冲突
    private static boolean handleMergeCases(HashMap<String, String> spBlobs, HashMap<String, String> branchBlobs,
                                            HashMap<String, String> currBlobs, String fileName)  {
        boolean conflict = false;

        if (spBlobs.containsKey(fileName)) {
            // 如果分裂点提交包含该文件
            conflict = handleFileWithSplitPoint(spBlobs, branchBlobs, currBlobs, fileName);
        } else {
            // 如果分裂点提交不包含该文件
            conflict = handleFileWithoutSplitPoint(branchBlobs, currBlobs, fileName);
        }

        return conflict;
    }

    // 处理分裂点存在该文件的情况
    private static boolean
        handleFileWithSplitPoint(HashMap<String, String> spBlobs,
                                 HashMap<String, String> branchBlobs,
                                 HashMap<String, String> currBlobs,
                                 String fileName) {
        boolean conflict = false;

        String spBlob = spBlobs.get(fileName); // 分裂点版本
        String branchBlob = branchBlobs.get(fileName); // 分支版本
        String currBlob = currBlobs.get(fileName); // 当前版本

        if (branchBlobs.containsKey(fileName) && currBlobs.containsKey(fileName)) {
            // 如果分支和当前提交都包含该文件
            if (branchBlob.equals(currBlob)) {
                // 分支和当前版本一致
                addFile2Stage(fileName, branchBlob);
                checkoutBlob2CWD(fileName, branchBlob);
            } else if (branchBlob.equals(spBlob)) {
                // 分支版本与分裂点一致，当前版本已修改
                addFile2Stage(fileName, currBlob);
                checkoutBlob2CWD(fileName, currBlob);
            } else if (currBlob.equals(spBlob)) {
                // 当前版本与分裂点一致，分支版本已修改
                addFile2Stage(fileName, branchBlob);
                checkoutBlob2CWD(fileName, branchBlob);
            } else {
                // 发生冲突
                replaceCWDFile(fileName, currBlob, branchBlob);
                addFile2Stage(fileName);
                conflict = true;
            }
        } else if (!branchBlobs.containsKey(fileName) && currBlobs.containsKey(fileName)) {
            // 分支不存在，当前提交存在
            if (currBlob.equals(spBlob)) {
                deleteCWDfile(fileName);
            } else {
                replaceCWDFile(fileName, currBlob, null);
                addFile2Stage(fileName);
                conflict = true;
            }
        } else if (!currBlobs.containsKey(fileName) && branchBlobs.containsKey(fileName)) {
            // 当前提交不存在，分支存在
            if (!spBlob.equals(branchBlob)) {
                replaceCWDFile(fileName, null, branchBlob);
                addFile2Stage(fileName);
                conflict = true;
            }
        }

        return conflict;
    }

    // 处理分裂点不存在该文件的情况
    private static boolean handleFileWithoutSplitPoint(HashMap<String, String> branchBlobs,
                                                       HashMap<String, String> currBlobs, String fileName)  {
        boolean conflict = false;

        String branchBlob = branchBlobs.get(fileName); // 分支版本
        String currBlob = currBlobs.get(fileName); // 当前版本

        if (!branchBlobs.containsKey(fileName) && currBlobs.containsKey(fileName)) {
            // 分支不存在，当前提交存在
            addFile2Stage(fileName, currBlob);
            checkoutBlob2CWD(fileName, currBlob);
        } else if (branchBlobs.containsKey(fileName) && !currBlobs.containsKey(fileName)) {
            // 当前提交不存在，分支存在
            addFile2Stage(fileName, branchBlob);
            checkoutBlob2CWD(fileName, branchBlob);
        } else if (currBlob != null && !currBlob.equals(branchBlob)) {
            // 发生冲突
            replaceCWDFile(fileName, currBlob, branchBlob);
            addFile2Stage(fileName);
            conflict = true;
        }

        return conflict;
    }

    // 检查是否存在未跟踪文件冲突
    private static boolean hasUntrackedConflict(HashMap<String, String> branchBlobs, HashMap<String, String> spBlobs,
                                                HashMap<String, String> currBlobs, String fileName) {
        if (!cwdhave(fileName)) {
            return false;
        }

        if (currBlobs.containsKey(fileName)) {
            return false;
        }

        if (branchBlobs.containsKey(fileName) && !spBlobs.containsKey(fileName)) {
            return true;
        }

        return false;


    }

    // 收集所有涉及的文件名
    private static List<String> collectAllFileNames(Set<String>... blobKeySets) {
        Set<String> allFiles = new HashSet<>();
        for (Set<String> keys : blobKeySets) {
            allFiles.addAll(keys);
        }
        return new ArrayList<>(allFiles);
    }



}
