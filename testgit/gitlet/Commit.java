package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.log.dateLog;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;//信息
    private Date date;//日期
    private String[] parent;//父文件
    private HashMap<String, String> fields;//文件和他的哈希值
    private String uid;//提交本身的哈希值

    //`初始构造函数`
    public Commit() throws IOException {
        this.message = "initial commit";
        this.date = new Date();
        this.parent = new String[2];
        this.fields = new HashMap<>();
        this.uid = Utils.sha1(this.message, new Date(0).toString());
        File commitFile = Utils.join(COMMIT_DIR, uid);//将初始提交提交到文件夹里面
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);//写入对象
    }

    //构造函数
    public Commit(String message, String[] parent, HashMap<String, String> fields) throws IOException {
        this.message = message;
        this.date = new Date();
        this.parent = parent;
        this.fields = fields;
        this.uid = Utils.sha1(fields.toString(), message, date.toString(), parent[0]);
        if (checkCommit()) {
            System.exit(0);
        }
        File commitFile = Utils.join(COMMIT_DIR, uid);//创建提交文件,名字为uid
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);
        deleteStagingFile();//提交后暂存区会被清除。
        System.out.println(fields);

    }

    // <editor-fold desc="返回属性">
    public String getMessage() {
        return message;
    }

    public String getUid() {
        return uid;
    }

    public Date getDate() {
        return date;
    }

    public String[] getParent() {
        return parent;
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    // </editor-fold


    /**失败案例：如果没有文件已暂存，则中止。打印消息没有添加任何更改到提交。
     * 每次提交都必须有一条非空消息。如果没有，请打印错误消息 Please input a commit message。
     * 跟踪文件从工作目录中丢失或在工作目录中发生更改并不是故障。
     * 只需完全忽略 .gitlet 目录之外的所有内容即可。*/
    public boolean checkCommit() throws IOException {
        if ((Utils.plainFilenamesIn(STAGING_DIR)).isEmpty()) {
            Utils.message("No changes added to the commit.");
            return true;
        }
        return false;
    }


    /**
     * 描述：打印出具有给定提交消息的所有提交的 ID，每行一个。
     * 如果有多个这样的提交，它会在单独的行上打印 id。
     * 提交消息是单个操作数；要指示多字消息，请将操作数放在引号中，就像下面的提交命令一样。
     * 提示：该命令的提示与 global-log 的提示相同。
     */
    public static void findCommit(String[] args) throws IOException {
        String message = args[1];//args = find [commit message]
        List<String> files = Utils.plainFilenamesIn(COMMIT_DIR);//返回提交的列表
        boolean found = false;
        for (String file : files) {
            Commit currCommit = Utils.readObject(Utils.join(COMMIT_DIR, file), Commit.class);
            if (currCommit.message.equals(message)) {
                System.out.println(currCommit.uid);
                found = true;
            }
        }
        if (!found) {
             throw Utils.error("Found no commit with that message");
        }
    }
}
