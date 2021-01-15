package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Austin Ralleta
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            if (args.length != 1) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.init();
        } else if (!Utils.join(System.getProperty("user.dir"),
                ".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        } else {
            restOfMain(args);
        }
    }

    /** Continuation of main method.
     * @param args contains <COMMAND> <OPERAND> ....*/
    public static void restOfMain(String... args) throws IOException {
        switch (args[0]) {
        case "add":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.add(args[1]);
            break;
        case "commit":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            if (args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Commands.commit(args[1]);
            break;
        case "rm":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.rm(args[1]);
            break;
        case "log":
            if (args.length != 1) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.log();
            break;
        case "global-log":
            if (args.length != 1) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.globalLog();
            break;
        case "find":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.find(args[1]);
            break;
        case "status":
            if (args.length != 1) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.status();
            break;
        default:
            restOfMainContinued(args);
        }
    }

    /** Continuation of continuation main method.
     * @param args contains <COMMAND> <OPERAND> ....*/
    public static void restOfMainContinued(String... args) throws IOException {
        switch (args[0]) {
        case "branch":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.branch(args[1]);
            break;
        case "rm-branch":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.rmbranch(args[1]);
            break;
        case "checkout":
            if (args.length == 2) {
                Commands.checkoutBranch(args[1]);
                break;
            }
            if (args[1].equals("--")) {
                if (args.length != 3) {
                    System.out.println("Incorrect Operands");
                    System.exit(0);
                }
                Commands.checkout(args[2]);
                break;
            }
            if (args[2].equals("--")) {
                if (args.length != 4) {
                    System.out.println("Incorrect Operands");
                    System.exit(0);
                }
                Commands.checkoutWithin(args[1], args[3]);
                break;
            } else {
                System.out.println("Incorrect Operands");
                System.exit(0);
                break;
            }
        case "reset":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.reset(args[1]);
            break;
        case "merge":
            if (args.length != 2) {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
            Commands.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
