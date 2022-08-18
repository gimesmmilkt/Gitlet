package gitlet;



import java.io.IOException;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args)  {
        // TODO: what if args is empty?
        if (args.length == 0){
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];


        switch(firstArg) {
            case "init":
                if (GITLET_DIR.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                // TODO: handle the `init` command
                try {
                    Repository.setUpPersistence();
                    Repository.initialCommit();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                checkInit();
                // TODO: handle the `add [filename]` command
                try {
                    Repository.add(args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            // TODO: FILL THE REST IN
            case "commit":
                checkInit();
                String m = (args[1]);
                if (m.length() == 0) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(m);
                break;
            case "rm":
                checkInit();
                String torm = (args[1]);
                Repository.rm(torm);
                break;
            case "log":
                checkInit();
                Repository.log();
                break;
            case "checkout":
                checkInit();
                if ((args.length > 3) && (!args[2].equals("--"))) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if(args[1].equals("--")) {
                    Repository.checkoutFN(args[2]);
                } else if ((args.length > 2) && (args[2].equals("--"))) {
                    Repository.checkoutFNCID(args[1],args[3]);
                } else {
                    Repository.checkoutBR(args[1]);
                }
                break;
            case "global-log":
                checkInit();
                Repository.globalLog();
                break;
            case "find":
                checkInit();
                String tofind = args[1];
                Repository.find(tofind);
                break;
            case "branch":
                checkInit();
                String branchName = args[1];
                    Repository.branch(branchName);
                break;
            case "status":
                checkInit();
                Repository.status();
                break;
            case "rm-branch":
                checkInit();
                String tormBr = args[1];
                Repository.rmBranch(tormBr);
                break;
            case "reset":
                checkInit();
                String resetId = args[1];
                Repository.reset(resetId);
                break;
            case "merge" :
                checkInit();
                String branch = args[1];
                Repository.merge(branch);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
    public static void checkInit() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
