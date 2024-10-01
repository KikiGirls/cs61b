package gitlet;

import java.io.IOException;

import static gitlet.Commit.findCommit;
import static gitlet.Repository.*;
import static gitlet.branch.*;
import static gitlet.log.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            Utils.message("Please Enter a command");
            System.exit(0);
        }
        String firstArg = args[0];

        switch(firstArg) {
            case "init":
                checkNumOfArgs(args,1);//args = init
                init();
                break;
            case "add":
                checkNumOfArgs(args,2);//args = add [file name]
                addFile(args);
                break;
            case "commit":
                if (args.length == 1) {// args = commit
                    throw Utils.error("Please enter a commit message.");
                } else {// args = commit [message]
                    checkNumOfArgs(args,2);
                }
                commitFile(args);
                break;
            case "log":
                checkNumOfArgs(args,1);//args = log
                printlog();
                break;
            case "find":
                checkNumOfArgs(args,2);//args = find [commit message]
                findCommit(args);
                break;
            case "rm":
                checkNumOfArgs(args,2);//args = rm [file name]
                rmFile(args);
                break;
            case "global-log":
                checkNumOfArgs(args,1);// args = global-log
                printGlobalLog();
                break;
            case "branch":
                checkNumOfArgs(args,2);//args = branch [branch name]
                creatBranch(args);
                break;
            case "rm-branch":
                checkNumOfArgs(args,2);//args = rm-branch [branch name]
                deleteBranch(args);
                break;
            case "checkout":
                //checkout -- [file name]
                //checkout [commit id] -- [file name]
                //checkout [branch name]
                checkout(args);
                break;
            case "status":
                checkNumOfArgs(args,1);//args = status
                printStatus();
                break;
            case "reset":
                checkNumOfArgs(args,2);//args = reset [commit id]
                reset(args);
                break;
            case "merge":
                checkNumOfArgs(args,2);//args = merge [branch name]
                mergeBranch(args);
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);
        }
    }
    /** 检查长度是否合格 */
    private static void checkNumOfArgs(String [] args, Integer n){
        if(args.length != n){
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }

    /** 查看文件 */
    public static void checkout(String[] args) throws IOException {
        if (args.length == 3) {
            //checkout -- [file name]
            checkoutFile(args);
        } else if (args.length == 4) {
            //checkout [commit id] -- [file name]
            checkoutCommitFile(args);
        } else if (args.length == 2) {
            //checkout [branch name]
            checkoutBranch(args);
        } else {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }

}
