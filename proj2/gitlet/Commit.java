package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Branch.*;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author syWang
 */
public class Commit implements Serializable {
    private String message; //信息
    private Date date; //日期
    private String[] parent; //父文件
    private HashMap<String, String> blobs; //文件和他的哈希值
    private String uid; //提交本身的哈希值
    static String u = "shfjjjjjjjjjjjjj";

    //初始提交
    public Commit() {
        this.message = "initial commit";
        this.date = new Date(0);
        this.parent = new String[2];
        this.blobs = new HashMap<String, String>();
        this.uid = sha1(message, dateToTimeStamp(date), blobs.toString(), parent.toString());
    }


    /*
    * 创建提交*/
    public Commit(String message)  {
        Commit commit = getCurrCommit();
        this.message = message;
        this.date = new Date();
        this.parent = new String[2];
        parent[0] = commit.uid;
        this.blobs = updetaBolbs(commit.getBlobs());
        this.uid = sha1(message, dateToTimeStamp(date), blobs.toString(), parent.toString());
    }

    public Commit(String message, String currBranchName, String branchName)  {
        this.message = message;
        this.date = new Date();
        this.parent = new String[2];
        this.blobs = new HashMap<>();
        parent[0] = getCommitUidInBranch(currBranchName);
        parent[1] = getCommitUidInBranch(branchName);
        this.blobs = updetaBolbs(blobs);
        this.uid = sha1(message, dateToTimeStamp(date), blobs.toString(), parent.toString());
    }

    private HashMap<String, String> updetaBolbs(HashMap<String, String> currCommitBlobs)  {
        HashMap<String, String> updetaBolbs = currCommitBlobs;

        List<String> toAdd = plainFilenamesIn(ADD_DIR);
        List<String> toRm = plainFilenamesIn(REMOVE_DIR);

        if (toAdd.isEmpty() && toRm.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else {
            for (String filename : toAdd) {
                String blobuid = creatBlobFromADDstage(filename);
                updetaBolbs.put(filename, blobuid);
            }

        }
        for (String filename : toRm) {
            updetaBolbs.remove(filename);
        }
        deleteStage();
        return updetaBolbs;
    }





    //将时间转换为时间戳

    static String dateToTimeStamp(Date date) {
        // 将 Date 转换为 ZonedDateTime
        ZonedDateTime zonedDateTime = date.toInstant().atZone(java.time.ZoneId.systemDefault());
        // 定义正确的格式化模式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE MMM d HH:mm:ss yyyy xx", Locale.US);
        // 返回格式化后的字符串
        return formatter.format(zonedDateTime);
    }

    public void add()  {
        File commitFile = join(COMMITS_DIR, uid);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(commitFile, (Serializable) this);
        setCurrBranchHead(this.uid);
        //写入对象
        //
    }



    public String getuid() {
        return uid;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public Date getDate() {
        return date;
    }

    public static Commit getCurrCommit()  {
        String headCommitName = getCurrCommitName();
        File commitFlie = join(COMMITS_DIR, headCommitName);
        Commit currCommit = readObject(commitFlie, Commit.class);
        return currCommit;
    }

    static String getCurrCommitName()  {
        String currBranch = getCurrBranchName();
        String currCommitName = getCommitUidInBranch(currBranch);
        return currCommitName;

    }

    public boolean isMergeCommit() {
        return parent[1] != null;
    }


    public String getMessage() {
        return message;
    }


    public String[] getParent() {
        return parent;
    }

    public Commit getPrexCommit()  {

        String firstParent = parent[0];
        if (parent[0] == null) {
            return null;
        }
        return getCommitByUid(firstParent);
    }

    public static Commit getCommitByUid(String commitUid)  {

        if (commitUid.length() < u.length()) {
            return getCommitByShortUid(commitUid);
        }
        File commitFlie = join(COMMITS_DIR, commitUid);
        if (!commitFlie.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(commitFlie, Commit.class);
    }

    private static Commit getCommitByShortUid(String commitUid) {
        List<String> commitUidlist = plainFilenamesIn(COMMITS_DIR);
        for (String uid : commitUidlist) {
            if (commitUid.equals(uid.substring(0, commitUid.length()))) {
                File commitFlie = join(COMMITS_DIR, uid);
                return readObject(commitFlie, Commit.class);
            }

        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }

    public boolean have(String fileName) {
        return blobs.containsKey(fileName);
    }

    public String getFileUid(String fileName) {
        if (!blobs.containsKey(fileName)) {
            return null;
        }
        return blobs.get(fileName);
    }

    public Commit[] getParents() {
        Commit[] parents = new Commit[2];
        if (parent[0] != null) {
            parents[0] = getCommitByUid(parent[0]); // 通过 UID 获取父提交
        }
        if (parent[1] != null) {
            parents[1] = getCommitByUid(parent[1]); // 通过 UID 获取父提交
        }
        return parents; // 返回父提交数组
    }
}
