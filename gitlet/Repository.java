package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.TreeMap;

import static gitlet.Utils.*;
public class Repository implements Serializable {
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Folder for the stage area*/
    public static final File STAGING =  join(GITLET_DIR, "staging");
    /** File for a commit head which store the commit's id to locate the current commit*/
    public static final File HEAD = join(GITLET_DIR, "head");
    /** File to store all the branches*/
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    /** File to store the addition map*/
    public static final File ADDITION = join(STAGING, "addition");
    /** File to store the removal map*/
    public static final File REMOVAL = join(STAGING, "removal");
    /** File to store all of commits */
    public static final File COMMITSHOME = join(GITLET_DIR, "Commitshome");
    /** File for a brance head which store the commit's id to locate the current branch*/
    public static final File BRHEAD = join(GITLET_DIR, "Brhead");
    protected static TreeMap<String, Blobs> addition = new TreeMap<>(); // Treemap for addition
    protected static TreeMap<String, Blobs> removal = new TreeMap<>(); // Treemap for removal
    protected static TreeMap<String, Commit> commitsHome = new TreeMap<>(); // Treemap for all commits
    protected static TreeMap<String, String> brHead = new TreeMap<>(); // Treemap for current head commit
    private static String head;
    private static boolean mergeswitch = false;


    /**set up persistence*/
    public static void setUpPersistence() throws IOException {
        GITLET_DIR.mkdir();
        STAGING.mkdir();
        REMOVAL.createNewFile();
        ADDITION.createNewFile();
        HEAD.createNewFile();
        COMMITSHOME.createNewFile();
        BRANCHES.createNewFile();
        BRHEAD.createNewFile();
    }
    /** helper to put a commit into map */
    private static void gohome(Commit c) {
        commitsHome.put(c.getCommitid(), c);
    }
    /** helper to find a commit from map */
    private static Commit findhead() {
        head = readObject(HEAD, String.class);
        return commitsHome.get(head);
    }
    private static void saveALl() {
        writeObject(BRHEAD, brHead);
        writeObject(COMMITSHOME, commitsHome);
    }
    private static void readAll() {
        brHead = readObject(BRHEAD, TreeMap.class);
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
    }
    private static void readStage() {
        addition = readObject(ADDITION, TreeMap.class);
        removal = readObject(REMOVAL, TreeMap.class);
    }
    private static void saveStage() {
        writeObject(REMOVAL, removal);
        writeObject(ADDITION, addition);
    }
    /** method for initialCommit */
    public static void initialCommit() throws IOException {
        // timestampe for commit
        Date timestamp = new Date(0);
        Commit initialC = new Commit(timestamp,
                "initial commit",
                null); // create the first commit with given input
        gohome(initialC);
        writeObject(BRANCHES, "master");
        brHead.put("master", initialC.getCommitid());
        saveALl();

    }
    /** method for add command */
    public static void add(String fileName) throws IOException {
        if (ADDITION.length() != 0) {
            addition = readObject(ADDITION, TreeMap.class);
        }
        if (REMOVAL.length() != 0) {
            removal = readObject(REMOVAL, TreeMap.class);
        }
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        File toAdd = new File(fileName);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blobs myBlob = new Blobs(toAdd, fileName); // convert the file into blob
        Commit h = findhead();
        if (removal.containsKey(fileName)) {
            removal.remove(fileName);
            writeObject(REMOVAL, removal);
        }
        if (h.blobmap.containsKey(fileName)) {
            if (h.findsha1(fileName).equals(myBlob.getsha1())) {
                return;
            } else {
                addition.put(fileName, myBlob);
            }
        } else {
            addition.put(fileName, myBlob);
        }
        saveStage();

    }
    /** a helper to check if the same filename exists */
    private static Commit headFinder(String s) {
        String id = brHead.get(s);
        return commitsHome.get(id);
    }
    /** method fot the commit command */
    public static void commit(String message) {
        if (ADDITION.length() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        addition = readObject(ADDITION, TreeMap.class);
        head = readObject(HEAD, String.class);
        readAll();
        String currentBr = readObject(BRANCHES, String.class);
        Date current = new Date();
        Commit ch = headFinder(currentBr);
        Commit c = new Commit(current, message, ch);
        c.blobmap.putAll(c.getParent().blobmap);
        commitHelper(c, currentBr);
    }

    /**
     * a helper to remove same filename blob from the commit blobmap if the bolb in the removal as well
     * @param c the commit need to be checked
     * @param currentBr current branch head commit id
     */
    private static void commitHelper(Commit c, String currentBr) {
        c.blobmap.putAll(addition);
        gohome(c);
        brHead.put(currentBr, c.getCommitid());
        if (REMOVAL.length() != 0) {
            removal = readObject(REMOVAL, TreeMap.class);
            for (Object s : removal.keySet()) {
                c.blobmap.remove(s);
            }
        }
        addition.clear();
        removal.clear();
        saveStage();
        saveALl();
    }
    /** method for the rm command
     * remove a file by its fileName */
    public static void rm(String fileName) {
        if (ADDITION.length() == 0) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        head = readObject(HEAD, String.class);
        readAll();
        readStage();
        String currentBr = readObject(BRANCHES, String.class);
        Commit headc = headFinder(currentBr);

        if ((!addition.containsKey(fileName))
               && (!headc.blobmap.containsKey(fileName))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Blobs b = addition.remove(fileName);

        if (headc.blobmap.containsKey(fileName)) {
            removal.put(fileName, b);
            if (plainFilenamesIn(CWD).contains(fileName)) {
                restrictedDelete(fileName);
            }
        }
        saveStage();
        writeObject(COMMITSHOME, commitsHome);
    }
    /** method for the log command
     * display all commit info in order*/
    public static void log() {

        String currBr = readObject(BRANCHES, String.class);
        readAll();
        Commit c; // set c as head to loop
        c = headFinder(currBr);
        while (c != null) {
            if (c.parenthelper()) {
                System.out.println("===");
                System.out.println("commit " + c.getSha1code());
                System.out.println("Merge: " + c.mergeidHelper());
                System.out.println("Date: " + c.getTimestamp());
                System.out.println(c.getMessage());
                System.out.println();
            } else {
                System.out.println("===");
                System.out.println("commit " + c.getSha1code());
                System.out.println("Date: " + c.getTimestamp());
                System.out.println(c.getMessage());
                System.out.println();
            }
            c = c.getParent();
        }
    }
    /**
     * display all commits in all branches, order does not matter*/
    public static void globalLog() {
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        for (Object s : commitsHome.keySet()) {
            Commit c = commitsHome.get(s);
            System.out.println("===");
            System.out.println("commit " + c.getSha1code());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage());
            System.out.println();
        }
    }
    /**
     * find a commit by its commit message*/
    public static void find(String m) {
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        int count = 0;
        for (Object s : commitsHome.keySet()) {
            Commit c = commitsHome.get(s);
            if (c.getMessage().equals(m)) {
                System.out.println(c.getSha1code());
                count += 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** find the blob in the target commit by its fileName*/
    private static Blobs blobHelper(Commit c, String f) {
        return c.blobmap.get(f);
    }


    /** method for the checkout command
     * Takes the version of the file as it exists in the head commit
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one*/
    public static void checkoutFN(String fileName) {

        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        Commit hc = findhead();
        Blobs b = blobHelper(hc, fileName);
        if (b == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File inFile = new File(fileName);
        Utils.writeContents(inFile, b.getcontent());
        inFile.renameTo(CWD);
    }
    /**
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one*/
    public static void checkoutFNCID(String commitID, String fileName) {
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        Commit c = commitsHome.get(commitID.substring(0, 6));
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Blobs  b = blobHelper(c, fileName);
        if (b == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File inFile = new File(fileName);
        Utils.writeContents(inFile, b.getcontent());
        inFile.renameTo(CWD);
    }
    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.*/
    public static void checkoutBR(String branchName) {
        readAll();
        String currbr = readObject(BRANCHES, String.class);
        if (currbr.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!brHead.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        Commit headc =  headFinder(currbr);
        Commit headcc = headFinder(branchName);
        List dirList = plainFilenamesIn(CWD);
        for (Object s : dirList) {
            if ((!headc.blobmap.containsKey(s))
                    & (headcc.blobmap.containsKey(s))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        String s = brHead.get(branchName);
        Commit c = commitsHome.get(s);
        List foldList = plainFilenamesIn(CWD);
        for (Object fn : foldList) {
            restrictedDelete((String) fn);
        }
        if (!(c.blobmap.isEmpty())) {
            for (Object i : c.blobmap.keySet()) {
                Blobs b = c.blobmap.get(i);
                File inFile = new File((String) i);
                Utils.writeContents(inFile, b.getcontent());
                inFile.renameTo(CWD);
            }
        }

        writeObject(BRANCHES, branchName);
    }
    /**
     * Creates a new branch with the given name, and points it at the current head commit*/
    public static void branch(String branchName) {
        brHead = readObject(BRHEAD, TreeMap.class);
        if (brHead.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        head = readObject(HEAD, String.class);
        Commit c = commitsHome.get(head);
        brHead.put(branchName, c.getCommitid());
        writeObject(BRHEAD, brHead);
    }
    /**
     * Displays what branches currently exist, and marks the current branch with a '*'
     * */
    public static void status() {
        String branchName = readObject(BRANCHES, String.class);
        brHead = readObject(BRHEAD, TreeMap.class);

        System.out.println("=== Branches ===");
        for (Object s : brHead.keySet()) {
            if (s.equals(branchName)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();


        System.out.println("=== Staged Files ===");
        if (ADDITION.length() != 0) {
            addition = readObject(ADDITION, TreeMap.class);

            for (Object s : addition.keySet()) {
                System.out.println(s);
            }
        }
        System.out.println();


        System.out.println("=== Removed Files ===");
        if (REMOVAL.length() != 0) {
            removal = readObject(REMOVAL, TreeMap.class);
            for (Object s : removal.keySet()) {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
    /**
     * Deletes the branch with the given name*/
    public static void rmBranch(String branchName) {
        brHead = readObject(BRHEAD, TreeMap.class);
        String currBr = readObject(BRANCHES, String.class);
        if (!brHead.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        }
        if (currBr.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        }
        brHead.remove(branchName);
        writeObject(BRHEAD, brHead);
    }
    /**
     *  Checks out all the files tracked by the given commit.
     *  Removes tracked files that are not present in that commit*/
    public static void reset(String id) {
        readAll();
        String currBr = readObject(BRANCHES, String.class);
        addition = readObject(ADDITION, TreeMap.class);
        if (!commitsHome.containsKey(id.substring(0, 6))) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = commitsHome.get(id.substring(0, 6));
        Commit h = headFinder(currBr);
        List<String> dirList = plainFilenamesIn(CWD);
        for (Object s : dirList) {
            if ((!h.blobmap.containsKey(s)) && (c.blobmap.containsKey(s))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (Object k : c.blobmap.keySet()) {
            Blobs b = c.blobmap.get(k);
            File inFile = new File((String) k);
            Utils.writeContents(inFile, b.getcontent());
            inFile.renameTo(CWD);
        }
        addition.clear();
        brHead.remove(currBr);
        brHead.put(currBr, id.substring(0, 6));
        writeObject(BRHEAD, brHead);
        writeObject(ADDITION, addition);
    }
    /**
     * find the common parent of all merge*/
    private static Commit splitHelper(Commit currH, Commit givenH) {
        ArrayList<String> given = new ArrayList<>();
        TreeMap<String, Commit> curr = new TreeMap<>();
        while (givenH != null) {
            given.add(givenH.getCommitid());
            givenH = givenH.getParent();
        }
        while (currH != null) {
            curr.put(currH.getCommitid(), currH);
            currH = currH.getParent();
        }
        for (Object s : given) {
            if (curr.containsKey(s)) {
                return  curr.get(s);
            }
        }
        return null;
    }
    /**
     * merge two branches into one,
     * keep or leave each commit by spcific rules
     * most time keep commit in the current branch
     * */
    public static void merge(String branchName) {
        String currBr = readObject(BRANCHES, String.class);
        if (currBr.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        brHead = readObject(BRHEAD, TreeMap.class);
        if (!brHead.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        commitsHome = readObject(COMMITSHOME, TreeMap.class);
        Commit currC = headFinder(currBr);
        Commit givenBrC = headFinder(branchName);
        Commit splitPointC = splitHelper(currC, givenBrC);
        if (currC.parenthelper()) {
            currC = currC.getParent();
        }

        if (ADDITION.length() != 0) {
            addition = readObject(ADDITION, TreeMap.class);
        }
        if (REMOVAL.length() != 0) {
            removal = readObject(REMOVAL, TreeMap.class);
        }
        if (addition.size() != 0 || removal.size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        List dirList = plainFilenamesIn(CWD);
        for (Object s : dirList) {
            if ((!currC.blobmap.containsKey(s))
                    && (givenBrC.blobmap.containsKey(s))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (currC.equals(splitPointC)) {
            checkoutBR(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (givenBrC.equals(splitPointC)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        for (String s : splitPointC.blobmap.keySet()) {
            moredetail(splitPointC, currC, givenBrC, s);
        }
        for (String s : currC.blobmap.keySet()) {
            currChelper(splitPointC, currC, givenBrC, s);
        }
        for (String s : givenBrC.blobmap.keySet()) {
            givenChelper(splitPointC, currC, givenBrC, s);
        }
        String message = "Merged " + branchName + " into " + currBr + ".";
        Commit c = new Commit(new Date(), message, currC, givenBrC);
        commitHelper(c, currBr);
        List dir = plainFilenamesIn(CWD);
        for (Object s : dir) {
            if (!c.blobmap.containsKey(s)) {
                restrictedDelete((String) s);
            }
        }

        if (mergeswitch) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    /**
     * check if the branch has been merged or not */
    private static boolean conflictorNot() {
        mergeswitch = true;
        return true;
    }
    /**
     * helper to generate status message*/
    private static String messageHelper(Blobs c, Blobs g, Blobs s) {
        conflictorNot();
        String message;
        if ((c != null) && (g != null)) {
            message = "<<<<<<< HEAD\n"
                    + c.getcontent()
                    + "=======\n"
                    + g.getcontent()
                    + ">>>>>>>\n";
            return message;
        } else if (c == null) {
            message = "<<<<<<< HEAD\n=======\n"
                    + g.getcontent()
                    + ">>>>>>>\n";
            return message;
        } else if (g == null) {
            message = "<<<<<<< HEAD\n"
                    + c.getcontent()
                    + "=======\n>>>>>>>\n";
            return message;
        }
        return null;
    }
    /**
     * helper for detail behavior of merge
     * */
    private static void moredetail(Commit spC, Commit currC, Commit givenC, String fileName) {
        Blobs currB = currC.blobmap.get(fileName);
        Blobs givenB = givenC.blobmap.get(fileName);
        Blobs spB = spC.blobmap.get(fileName);
        if ((currC.blobmap.containsKey(fileName))
                && (givenC.blobmap.containsKey(fileName))) {
            if (!currB.getsha1().equals(givenB.getsha1())) {
                if (currB.getsha1().equals(spB.getsha1())) {
                    checkoutFNCID(givenC.getSha1code(), fileName);
                    additionHelper(fileName);
                } else if (givenB.getsha1().equals(spB.getsha1())) {
                    checkoutFNCID(currC.getSha1code(), fileName);
                    additionHelper(fileName);
                } else if (!currB.getsha1().equals(spB.getsha1())) {
                    String message = messageHelper(currB, givenB, spB);
                    currB.overwriteContent(message);
                    File f = new File(fileName);
                    writeContents(f, message);
                    Blobs b = new Blobs(f, fileName);
                    addition.put(fileName, b);
                }
            }
        } else if ((currC.blobmap.containsKey(fileName))
                && (!givenC.blobmap.containsKey(fileName)))  {
            if (currB.getsha1().equals(spB.getsha1())) {
                currC.blobmap.remove(fileName);
            } else {
                String message = messageHelper(currB, givenB, spB);
                currB.overwriteContent(message);
                File f = new File(fileName);
                writeContents(f, message);
                Blobs b = new Blobs(f, fileName);
                addition.put(fileName, b);
            }
        }
    }
    /**
     * put a file into a blob
     * */
    private static void additionHelper(String fileName) {
        File f = new File(fileName);
        Blobs b = new Blobs(f, fileName);
        addition.put(fileName, b);
    }

    private static void currChelper(Commit spC, Commit currC, Commit givenC, String fileName) {
        if ((!givenC.blobmap.containsKey(fileName))
                && (!spC.blobmap.containsKey(fileName))) {
            checkoutFNCID(currC.getSha1code(), fileName);
            additionHelper(fileName);
        }
    }
    private static void givenChelper(Commit spC, Commit currC, Commit givenC, String fileName) {
        if ((!spC.blobmap.containsKey(fileName))
            && (!currC.blobmap.containsKey(fileName))) {
            checkoutFNCID(givenC.getSha1code(), fileName);
            additionHelper(fileName);
        }
    }
}
