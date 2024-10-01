package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class branch {
    public static void creatBranch(String[] args) throws IOException {
        String branchName = args[1];
        File newbranch = join(HEADS_DIR, branchName);
        if (newbranch.exists()) {
            throw Utils.error("A branch with that name already exists.");
        } else {
            writeContents(newbranch, headCommit().getUid());
            writeContents(HEAD, newbranch.toString());
            creatSplitPoint();
        }
    }

    public static void deleteBranch(String[] args) throws IOException {
        String branchName = args[1];//args = rm-branch [branch name]
        if (nowBranch().equals(branchName)) {
            // 如果您尝试删除当前所在的分支，则会中止，并打印错误消息“无法删除当前分支”。
            throw Utils.error("Cannot remove the current branch.");
        } else if (!join(HEADS_DIR, branchName).exists()) {
            //如果给定名称的分支不存在，则中止。打印错误消息 具有该名称的分支不存在。
            throw Utils.error("A branch with that name does not exist.");
        } else {
            join(HEADS_DIR, branchName).delete();
            join(LOGS_DIR, "splitpoint").delete();
        }
    }

    /** 返回当前分支 */
    public static String nowBranch() {
        File branch = join(readContentsAsString(HEAD));
        return branch.getName();
    }

    /** 合并分支 */
    public static void mergeBranch(String[] args) throws IOException {
        //描述：将给定分支中的文件合并到当前分支中。
        //args = merge [branch name]
        String branchName = args[1];
        Commit splitPoint = splitPoint();
        Commit commit = getCommit(branchName);

        if (splitPoint.getUid().equals(commit.getUid())) {
            //如果分割点与给定分支是相同的提交，那么我们什么不做；
            // 合并完成，操作结束并显示消息给定分支是当前分支的祖先。
            join(LOGS_DIR, "splitpoint").delete();
            Utils.message("Given branch is an ancestor of the current branch");
        } else if(splitPoint == headCommit()) {
            //如果分割点是当前分支，则效果是检查给定分支，
            // 并且在打印消息 Currentbranchfastforwarded 后操作结束
            checkoutBranch(args);
            join(LOGS_DIR, "splitpoint").delete();
            Utils.message("Current branch fast-forwarded");
        } else {
            //根据上述内容更新文件后，并且分割点不是当前分支或给定分支，合并会自动提交
            HashMap<String, String> fields = replaceFile(branchName);
            //并显示日志消息 Merged [givenbranch name] into [currentbranch name]。
            String message = "Merged " + branchName + " into " + nowBranch() + ".";
            //它们将当前分支的头（称为第一个父级）和命令行上给出的要合并的分支的头记录为父级。
            String[] parents = {headCommit().getUid(), commit.getUid()};
            new Commit(message, parents, fields);
            if (conflict) {
                //然后，如果合并遇到冲突，则打印消息遇到合并冲突。在终端上（不是日志）。合并
                Utils.message("Encountered a merge conflict.");
            }

        }

    }

    /** 更改cwd的文件并且返回跟踪的文件 */
    private static HashMap<String, String> replaceFile(String branchName) throws IOException {
        HashMap<String, String> fields = new HashMap<>();
        HashMap<String, String> splitPointFields = splitPoint().getFields();
        HashMap<String, String> commitFiels = getCommit(branchName).getFields();
        HashMap<String, String> currcommitFiels = headCommit().getFields();
        for (String fieldName : splitPointFields.keySet()) {
            String ValueOfSPF = splitPointFields.get(fieldName);
            String ValueOfCF = commitFiels.get(fieldName);
            String ValueOfCurrF = currcommitFiels.get(fieldName);
            if (!ValueOfCF.equals(ValueOfSPF) && ValueOfCurrF.equals(ValueOfSPF)) {
                //自分割点以来已在给定分支中修改，但自分割点以来未在当前分支中修改的任何文件都应更改为给定分支中的版本（从给定分支前面的提交检出）
                // 。这些文件应该全部自动暂存。
                String[] args1 = {"checkout", " -- ", getCommit(branchName).getUid(), fieldName};
                checkoutCommitFile(args1);
                String[] args2 = {"add", fieldName};
                addFile(args2);
                fields.put(fieldName, ValueOfCF);
            } else if (ValueOfCF.equals(ValueOfSPF) && !ValueOfCurrF.equals(ValueOfSPF)) {
                //自分割点起，在当前分支中已修改但未在给定分支中修改的任何文件都应保持原样。
                fields.put(fieldName, ValueOfCurrF);
            } else if (ValueOfCF.equals(ValueOfSPF)) {
                //在当前分支和给定分支中以相同方式修改的任何文件（即，两个文件现在具有相同的内容或都被删除）在合并时将保持不变。
                // 如果从当前分支和给定分支中删除了一个文件，但工作目录中存在同名文件，则该文件将被单独保留并在合并中继续不存在（不被跟踪或暂存）。
                fields.put(fieldName, ValueOfSPF);

            }

            for



        }


        return fields;
    }

    private static boolean conflict = false;

    private static Commit getCommit(String branchName) {
        File file = join(HEADS_DIR, branchName);
        String commitFileUid = readContentsAsString(file);
        File commitFile = join(COMMIT_DIR, commitFileUid);
        return readObject(commitFile, Commit.class);
    }

    private static Commit splitPoint() {
        File splitPoint = join(LOGS_DIR, "splitpoint");
        String commitFileUid = readContentsAsString(join(readContentsAsString(splitPoint)));
        File commitFile = join(COMMIT_DIR, commitFileUid);
        return readObject(commitFile, Commit.class);
    }

    private static void creatSplitPoint() throws IOException {
        File splitPoint = join(LOGS_DIR, "splitpoint");
        if (!splitPoint.exists()) {
            splitPoint.createNewFile();
        }
        writeContents(splitPoint, headCommit().getUid());
    }
}