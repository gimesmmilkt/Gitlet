package gitlet;
import java.io.Serializable;
import java.util.*;
import static gitlet.Repository.HEAD;
/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private final String timestamp;
    /** The message of this Commit. */
    private final String message;
    private final Commit parent;
    private Commit secParent;
    protected TreeMap<String, Blobs> blobmap;
    private final String sha1code;
    private final String commitid;

    /** constructor of a commit
     * @param t time of the commit
     * @param message message of the commit
     * @param parent parent of a commit, should always be the head except for the initial commit*/
    public Commit(Date t, String message, Commit parent) {

        String sdate = String.format(Locale.US,
                "%ta %tb %te %tH:%tM:%tS %tY %tz ",
                t, t, t, t, t, t, t, t);
        this.timestamp = sdate;
        this.message = message;
        this.parent = parent;
        this.blobmap = new TreeMap<>();
        this.sha1code = Utils.sha1( Utils.serialize(this));
        this.commitid = sha1code.substring(0, 6);
        Utils.writeObject(HEAD, commitid);

    }
    /** constructor of a merged commit
     *@param t time of the commit
     *@param message message of the commit
     *@param parent parent of a commit, should always be the head except for the initial commit
     *@param secParent second parent of a commit for merge condition*/
    public Commit(Date t, String message, Commit parent, Commit secParent) {
        String sdate = String.format(Locale.US,
                "%ta %tb %te %tH:%tM:%tS %tY %tz ",
                t, t, t, t, t, t, t, t);
        this.timestamp = sdate;
        this.message = message;
        this.parent = parent;
        this.secParent = secParent;
        this.blobmap = new TreeMap<String, Blobs>();
        this.sha1code = Utils.sha1(Utils.serialize(this));
        this.commitid = sha1code.substring(0, 6);
        Utils.writeObject(HEAD, commitid);

    }
    public String getTimestamp() {
        return this.timestamp;
    }
    public String getMessage() {
        return this.message;
    }

    public Commit getParent() {
        return this.parent;
    }
    public String getSha1code() {
        return sha1code;
    }

    /**
     * find a blob based on the filename
     * @param s filename
     * @return a target blob
     */
    public String findsha1(String s) {
        if (blobmap.get(s) == null) {
            return null;
        }
        Blobs target = blobmap.get(s);
        return target.getsha1();
    }

    public String getCommitid() {
        return commitid;
    }
    public boolean parenthelper() {
        if (this.secParent != null) {
            return true;
        }
        return false;
    }

    /**
     * used by merge, return the first 7 sha1 of each parent of a commit
     * @return first 7 sha1
     */
    public String mergeidHelper() {
        String front = this.parent.sha1code.substring(0, 7);
        String back = this.secParent.sha1code.substring(0, 7);
        return front + " " + back;
    }
}

