package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/** Commit class to construct commit objects.
 *  @author Austin Ralleta
 */
public class Commit implements Serializable {
    /** Constructs a normal commit object.
     * @param m is the commit message.
     * @param tracking hashmap of files tracked in commit.
     * @param theParent is parent of commit.
     * @param myBranch is branch the commit is on.
     */
    public Commit(String m, HashMap<String, String> tracking,
                  String theParent, String myBranch) {
        timestamp = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").format(
                new Date());
        message = m;
        tracked = tracking;
        parent = theParent;
        commitId = Utils.sha1(timestamp, message, parent,
                blobReferences(tracked));
        isMerge = false;
        branch = myBranch;
    }

    /** Constructs a merge commit object.
     * @param m is the commit message.
     * @param tracking hashmap of files tracked in commit.
     * @param firstParent is first parent of commit.
     * @param secondParent is second parent of commit.
     * @param myBranch is branch the commit is on.
     */
    public Commit(String m, HashMap<String, String> tracking,
                  String firstParent, String secondParent, String myBranch) {
        timestamp = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").format(
                new Date());
        message = m;
        tracked = tracking;
        parent = firstParent;
        parentTwo = secondParent;
        commitId = Utils.sha1(timestamp, message, parent, parentTwo,
                blobReferences(tracked));
        isMerge = true;
        branch = myBranch;
    }

    /** Constructs a normal commit object.
     * @param dateTime is time of commit.
     * @param m is the commit message.
     */
    public Commit(String dateTime, String m) {
        timestamp = dateTime;
        message = m;
        commitId = Utils.sha1(timestamp, message);
        parent = null;
        tracked = new HashMap<String, String>();
        isMerge = false;
        branch = "master";
    }

    /**
     * Turns all the blobs into a string.
     * @param trackedBlobs is the tracked files of the commit
     * @return concatenated SHA-1 of blobs.
     */
    public String blobReferences(HashMap<String, String> trackedBlobs) {
        StringBuilder blobRefs = new StringBuilder();
        for (String ref: trackedBlobs.keySet()) {
            blobRefs.append(ref);
        }
        return blobRefs.toString();
    }

    /**
     * Get the commit id.
     * @return the SHA-1 of the commit.
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * Get the map of tracked files.
     * @return the hashmap of tracked files.
     */
    public HashMap<String, String> getTracked() {
        return tracked;
    }

    /**
     * Get the commit time.
     * @return date and time.
     */
    public String getDateTime() {
        return timestamp;
    }

    /**
     * Get the commit message.
     * @return the messages.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the first parent.
     * @return the SHA-1 of the parent.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Get the second parent.
     * @return the SHA-1 of the parent.
     */
    public String getParentTwo() {
        return parentTwo;
    }

    /**
     * Determine if merge commit.
     * @return true or false.
     */
    public boolean isMerge() {
        return isMerge;
    }

    /**
     * Get the branch of the commit.
     * @return the branch.
     */
    public String getBranch() {
        return branch;
    }

    /** Time committed. */
    private String timestamp;

    /** Commit message. */
    private String message;

    /** SHA-1 of commit. */
    private String commitId;

    /** First parent of commit. */
    private String parent;

    /** Second parent of commit. */
    private String parentTwo;

    /** HashMap of files tracked by commit. */
    private HashMap<String, String> tracked;

    /** Indicates if commit is a merge commit. */
    private boolean isMerge;

    /** Branch of commit. */
    private String branch;
}
