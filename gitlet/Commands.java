package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/** Commmand class of all gitlet command methods called in Main.
 *  @author Austin Ralleta
 */
public class Commands {
    /**
     * Creates a new Gitlet version-control system in the current directory.
     * @throws IOException
     */
    public static void init() throws IOException {
        if (!Utils.join(System.getProperty("user.dir"), ".gitlet").exists()) {
            File dotGitlet = new File(workDir + File.separator + ".gitlet");
            dotGitlet.mkdir();
            File stage = Utils.join(workDir, ".gitlet", "stage");
            stage.mkdir();
            File allStaged = Utils.join(stage, "staged");
            allStaged.createNewFile();
            Utils.writeObject(allStaged, staged);
            File tracked = Utils.join(dotGitlet, "tracked");
            tracked.createNewFile();
            Utils.writeObject(tracked, track);
            File commits = Utils.join(dotGitlet, "commits");
            commits.mkdir();
            File blobs = Utils.join(dotGitlet, "blobs");
            blobs.mkdir();
            File removal = Utils.join(dotGitlet, "removal");
            removal.mkdir();
            File allRemoved = Utils.join(removal, "removed");
            allRemoved.createNewFile();
            Utils.writeObject(allRemoved, markRemoved);
            Commit initCommit = new Commit("Wed Dec 31 16:00:00 1969 -0800",
                    "initial commit");
            File firstCommit = Utils.join(commits, initCommit.getCommitId());
            commitsMap.put(initCommit.getCommitId(), initCommit.getMessage());
            File allCommits = Utils.join(commits, "hashes");
            allCommits.createNewFile();
            Utils.writeObject(allCommits, commitsMap);
            File branches = Utils.join(dotGitlet, "branches");
            branches.mkdir();
            File master = Utils.join(branches, "master");
            master.createNewFile();
            Utils.writeContents(master, initCommit.getCommitId());
            File myBranch = Utils.join(dotGitlet, "currentbranch");
            myBranch.createNewFile();
            Utils.writeContents(myBranch, "master");
            String head = initCommit.getCommitId();
            File current = Utils.join(dotGitlet, "HEAD");
            current.createNewFile();
            Utils.writeContents(current, head);
            Utils.writeObject(firstCommit, initCommit);
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }

    /**
     * Gets the head.
     * @return the current commit.
     */
    public static Commit getHead() {
        String theHead = Utils.readContentsAsString(Utils.join(workDir,
                ".gitlet", "HEAD"));
        File currentCommit = Utils.join(workDir, ".gitlet",
                "commits", theHead);
        return Utils.readObject(currentCommit, Commit.class);
    }

    /**
     * Gets hashmap of currently staged files.
     * @return hashmap of staged.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getStaged() {
        File allStaged = Utils.join(workDir, ".gitlet", "stage", "staged");
        return Utils.readObject(allStaged, HashMap.class);
    }

    /**
     * Get currently tracked files.
     * @return hashmap of tracked files.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> tracked() {
        File tracked = Utils.join(workDir, ".gitlet", "tracked");
        return Utils.readObject(tracked, HashMap.class);
    }

    /**
     * Gets map of files currently marked removed.
     * @return hashmap of removed files.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getMarkRemoved() {
        File allRemoved = Utils.join(workDir, ".gitlet", "removal", "removed");
        return Utils.readObject(allRemoved, HashMap.class);
    }

    /**
     * Gets map of all commit SHA-1.
     * @return hashmap of all commits.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getCommitHashes() {
        File allCommits = Utils.join(workDir, ".gitlet", "commits", "hashes");
        return Utils.readObject(allCommits, HashMap.class);
    }

    /**
     * Gets the branch file.
     * @param branch is branch name.
     * @return file containing branch head.
     */
    public static File getBranchFile(String branch) {
        return Utils.join(workDir, ".gitlet", "branches", branch);
    }

    /**
     * Gets the name of the current branch.
     * @return the branch name.
     */
    public static String getMyBranchName() {
        File myBranch = Utils.join(workDir, ".gitlet", "currentbranch");
        return Utils.readContentsAsString(myBranch);
    }

    /**
     * Gets the blob.
     * @param blobHash is the SHA-1 of the file.
     * @return the blob with file contents.
     */
    public static File getBlob(String blobHash) {
        return Utils.join(workDir, ".gitlet", "blobs", blobHash);
    }

    /**
     * Gets the commit.
     * @param id is the SHA-1 of the commit.
     * @return the commit object.
     */
    public static Commit getCommit(String id) {
        File commitID = Utils.join(workDir, ".gitlet", "commits", id);
        return Utils.readObject(commitID, Commit.class);
    }

    /**
     * Adds the current copy of the file to the staging area.
     * @param file to be added to stage
     */
    public static void add(String file) throws IOException {
        if (!Utils.join(System.getProperty("user.dir"), file).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            File copy = new File(workDir + File.separator + file);
            byte[] content = Utils.readContents(copy);
            String blobHash = Utils.sha1(content);
            String currVersion = getHead().getTracked().get(file);
            HashMap<String, String> currentStaged = getStaged();
            HashMap<String, String> currMarkedRemoved = getMarkRemoved();
            if (currMarkedRemoved.containsKey(file)) {
                currMarkedRemoved.remove(file);
                Utils.writeObject(Utils.join(workDir, ".gitlet",
                        "removal", "removed"), currMarkedRemoved);
                File inRemoval = Utils.join(workDir, ".gitlet", "removal",
                        file);
                inRemoval.delete();
            }
            if (blobHash.equals(currVersion)) {
                if (currentStaged.containsKey(file)) {
                    currentStaged.remove(file);
                    Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                            "staged"), currentStaged);
                    File inStage = Utils.join(workDir, ".gitlet", "stage",
                            file);
                    inStage.delete();
                } else {
                    System.exit(0);
                }
            }
            File blobFile = Utils.join(workDir, ".gitlet", "blobs",
                    blobHash);
            if (!blobFile.exists()) {
                blobFile.createNewFile();
                Utils.writeContents(blobFile, content);
            }
            currentStaged.put(file, blobHash);
            Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                    "staged"), currentStaged);
            File stagedFile = Utils.join(workDir, ".gitlet", "stage",
                    file);
            stagedFile.createNewFile();
            Utils.writeContents(stagedFile, blobHash);
            HashMap<String, String> currentTracked = tracked();
            currentTracked.put(file, blobHash);
            Utils.writeObject(Utils.join(workDir, ".gitlet", "tracked"),
                    currentTracked);
        }
    }

    /**
     * Removes the file if needed and marks it to be removed.
     * @param file is name of file to be removed.
     */
    public static void rm(String file) throws IOException {
        boolean isTracked = getHead().getTracked().containsKey(file);
        HashMap<String, String> currentStaged = getStaged();
        if (!currentStaged.containsKey(file) && !isTracked) {
            System.out.println("No reason to remove the file.");
        }
        File theFile = new File(workDir + File.separator + file);
        if (currentStaged.containsKey(file)) {
            currentStaged.remove(file);
            Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                    "staged"), currentStaged);
            File inStage = Utils.join(workDir, ".gitlet", "stage", file);
            inStage.delete();
        }
        if (isTracked) {
            HashMap<String, String> currMarkedRemoved = getMarkRemoved();
            if (theFile.exists()) {
                Utils.restrictedDelete(theFile);
            }
            String blobHash = tracked().get(file);
            currMarkedRemoved.put(file, blobHash);
            Utils.writeObject(Utils.join(workDir, ".gitlet", "removal",
                    "removed"), currMarkedRemoved);
            File removedFile = Utils.join(workDir, ".gitlet", "removal", file);
            removedFile.createNewFile();
            Utils.writeContents(removedFile, blobHash);
            HashMap<String, String> currentTracked = tracked();
            currentTracked.remove(file);
            Utils.writeObject(Utils.join(workDir, ".gitlet", "tracked"),
                    currentTracked);
        }
    }

    /**
     * Saves a snapshot of certain files that are staged and tracked,
     * creating a new commit.
     * @param msg is the commit message.
     * @throws IOException
     */
    public static void commit(String msg) throws IOException {
        HashMap<String, String> currentStaged = getStaged();
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        if (currentStaged.size() == 0 && currMarkedRemoved.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        HashMap<String, String> tracking = tracked();
        File current = Utils.join(workDir, ".gitlet", "HEAD");
        String theHead = Utils.readContentsAsString(current);
        String myBranchName = getMyBranchName();
        Commit thisCommit = new Commit(msg, tracking, theHead, myBranchName);
        Utils.writeContents(current, thisCommit.getCommitId());
        HashMap<String, String> mapOfCommits = getCommitHashes();
        mapOfCommits.put(thisCommit.getCommitId(), thisCommit.getMessage());
        File allCommits = Utils.join(workDir, ".gitlet", "commits", "hashes");
        Utils.writeObject(allCommits, mapOfCommits);
        File thisBranch = getBranchFile(myBranchName);
        Utils.writeContents(thisBranch, thisCommit.getCommitId());
        File thisCommitFile = Utils.join(workDir, ".gitlet", "commits",
                thisCommit.getCommitId());
        thisCommitFile.createNewFile();
        Utils.writeObject(thisCommitFile, thisCommit);
        if (currentStaged.size() != 0) {
            for (String stagedFile : currentStaged.keySet()) {
                File inStage = Utils.join(workDir, ".gitlet", "stage",
                        stagedFile);
                inStage.delete();
            }
            currentStaged.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                    "staged"), currentStaged);
        }
        if (currMarkedRemoved.size() != 0) {
            for (String inRemoval : currMarkedRemoved.keySet()) {
                File removedFile = Utils.join(workDir, ".gitlet", "removal",
                        inRemoval);
                removedFile.delete();
            }
            currMarkedRemoved.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "removal",
                    "removed"), currMarkedRemoved);
        }
    }

    /**
     * Displays commit information starting from current head
     * and going backwards along the commit tree.
     */
    public static void log() {
        Commit current = getHead();
        System.out.println("===");
        System.out.println("commit " + current.getCommitId());
        System.out.println("Date: " + current.getDateTime());
        System.out.println(current.getMessage());
        while (current.getParent() != null) {
            File nextPath = Utils.join(workDir, ".gitlet",
                    "commits", current.getParent());
            current = Utils.readObject(nextPath, Commit.class);
            System.out.println();
            System.out.println("===");
            System.out.println("commit " + current.getCommitId());
            if (current.isMerge()) {
                System.out.println("Merge: " + current.getParent().substring(
                        0, 7) + " " + current.getParentTwo().substring(0, 7));
            }
            System.out.println("Date: " + current.getDateTime());
            System.out.println(current.getMessage());
        }
    }

    /**
     * Displays all commit information for all commits, regardless of branch.
     */
    public static void globalLog() {
        Set<String> idSet = getCommitHashes().keySet();
        int count = 0;
        for (String id: idSet) {
            File currentPath = Utils.join(workDir, ".gitlet", "commits", id);
            Commit current = Utils.readObject(currentPath, Commit.class);
            System.out.println("===");
            System.out.println("commit " + current.getCommitId());
            if (current.isMerge()) {
                System.out.println("Merge: " + current.getParent().substring(
                        0, 7) + " " + current.getParentTwo().substring(0, 7));
            }
            System.out.println("Date: " + current.getDateTime());
            System.out.println(current.getMessage());
            count++;
            if (count < idSet.size()) {
                System.out.println();
            }
        }
    }

    /**
     * Print commit ids of commits that have the commit message.
     * @param message is the commit message
     */
    public static void find(String message) {
        HashMap<String, String> mapOfCommits = getCommitHashes();
        int idCount = 0;
        for (HashMap.Entry entry: mapOfCommits.entrySet()) {
            if (message.equals(entry.getValue())) {
                System.out.println(entry.getKey());
                idCount++;
            }
        }
        if (idCount == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Displays current existence of branches, staged files, removed files,
     * modifications not staged for commit, and untracked files.
     */
    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branchesList = Utils.plainFilenamesIn(dBranches);
        if (branchesList != null) {
            for (String branch: branchesList) {
                String currentBranch = getMyBranchName();
                if (branch.equals(currentBranch)) {
                    System.out.println("*" + branch);
                } else {
                    System.out.println(branch);
                }
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagedList = Utils.plainFilenamesIn(Utils.join(workDir,
                ".gitlet", "stage"));
        HashMap<String, String> currentStaged = getStaged();
        if (stagedList != null) {
            for (String stagedFile: stagedList) {
                if (!stagedFile.equals("staged")) {
                    System.out.println(stagedFile);
                    File wdStagedPath = Utils.join(workDir, stagedFile);
                    if (!wdStagedPath.exists()) {
                        modsNotStaged.add(stagedFile);
                    } else {
                        byte[] content = Utils.readContents(wdStagedPath);
                        String wdStagedHash = Utils.sha1(content);
                        if (!currentStaged.get(stagedFile).equals(
                                wdStagedHash)) {
                            modsNotStaged.add(stagedFile);
                        }
                    }
                }
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedList = Utils.plainFilenamesIn(Utils.join(workDir,
                ".gitlet", "removal"));
        if (removedList != null) {
            for (String removedFile: removedList) {
                if (!removedFile.equals("removed")) {
                    System.out.println(removedFile);
                }
            }
        }
        System.out.println();
        statusExtra(currentStaged);
        System.out.println("=== Modifications Not Staged For Commit ===");
        Collections.sort(modsNotStaged);
        for (String modFile: modsNotStaged) {
            System.out.println(modFile);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String notTracked: untracked) {
            System.out.println(notTracked);
        }
    }

    /**
     * Helper method for status to get modifications not staged and untracked.
     * @param currentStaged is hashmap of staged files.
     */
    public static void statusExtra(HashMap<String, String> currentStaged) {
        List<String> workDirFiles = Utils.plainFilenamesIn(new File(workDir));
        Commit myCommit = getHead();
        HashMap<String, String> tracking = myCommit.getTracked();
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        for (String trackedFile: tracking.keySet()) {
            File wdTrackedPath = Utils.join(workDir, trackedFile);
            if (!currMarkedRemoved.containsKey(trackedFile)) {
                if (!wdTrackedPath.exists()) {
                    modsNotStaged.add(trackedFile + " (deleted)");
                }
            }
            if (wdTrackedPath.exists()) {
                if (!currentStaged.containsKey(trackedFile)) {
                    String trackHash = tracking.get(trackedFile);
                    if (!Arrays.equals(Utils.readContents(getBlob(trackHash)),
                            Utils.readContents(wdTrackedPath))) {
                        modsNotStaged.add(trackedFile + " (modified)");
                    }
                }
            }
        }
        if (workDirFiles != null) {
            for (String wdFile : workDirFiles) {
                if (!currentStaged.containsKey(wdFile)
                        && !tracking.containsKey(wdFile)) {
                    untracked.add(wdFile);
                }
            }
        }
    }

    /**
     * Make a new branch whose head currently points at the current head commit.
     * @param name is branch name.
     */
    public static void branch(String name) {
        File theBranch = Utils.join(workDir, ".gitlet", "branches", name);
        if (theBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            Utils.writeContents(theBranch, Utils.readContentsAsString(
                    Utils.join(workDir, ".gitlet", "HEAD")));
        }
    }

    /**
     * Deletes the branch with given name.
     * @param name is branch name.
     */
    public static void rmbranch(String name) {
        if (name.equals(getMyBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File theBranch = Utils.join(workDir, ".gitlet", "branches", name);
        if (!theBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            theBranch.delete();
        }
    }

    /**
     * Takes file from head of branch and overwrites file in working directory.
     * @param fileName is name of file.
     */
    public static void checkout(String fileName) throws IOException {
        String theHead = Utils.readContentsAsString(Utils.join(workDir,
                ".gitlet", "HEAD"));
        checkoutWithin(theHead, fileName);
    }

    /**
     * Takes file from given commit and overwrites file in working directory.
     * @param id is commit id.
     * @param fileName is name of file.
     */
    public static void checkoutWithin(String id, String fileName)
            throws IOException {
        if (!Utils.join(workDir, ".gitlet", "commits", id).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit given = getCommit(id);
        HashMap<String, String> inGiven = given.getTracked();
        if (!inGiven.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String version = inGiven.get(fileName);
        byte[] versionContent = Utils.readContents(Utils.join(workDir,
                ".gitlet", "blobs", version));
        File wdFilePath = Utils.join(workDir, fileName);
        if (wdFilePath.exists()) {
            Utils.writeContents(wdFilePath, versionContent);
        } else {
            wdFilePath.createNewFile();
            Utils.writeContents(wdFilePath, versionContent);
        }
    }

    /**
     * Takes file from given branch and overwrites file in working directory.
     * @param branchName is name of branch.
     * @throws IOException
     */
    public static void checkoutBranch(String branchName) throws IOException {
        File branchPath = getBranchFile(branchName);
        if (!branchPath.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(getMyBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit given = getCommit(Utils.readContentsAsString(branchPath));
        HashMap<String, String> inGiven = given.getTracked();
        Utils.writeObject(Utils.join(workDir, ".gitlet", "tracked"),
                inGiven);
        String branchCommitID = Utils.readContentsAsString(
                getBranchFile(getMyBranchName()));
        Commit currBranchCommit = getCommit(branchCommitID);
        HashMap<String, String> currentTracked = currBranchCommit.getTracked();
        Set<String> inGivenFiles = inGiven.keySet();
        for (String givenFile: inGivenFiles) {
            if (Utils.join(workDir, givenFile).exists()
                    && !currentTracked.containsKey(givenFile)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
        for (String givenFile: inGivenFiles) {
            File givenFilePath = Utils.join(workDir, givenFile);
            String givenHash = inGiven.get(givenFile);
            byte[] givenContent = Utils.readContents(Utils.join(workDir,
                    ".gitlet", "blobs", givenHash));
            if (givenFilePath.exists()) {
                Utils.writeContents(givenFilePath, givenContent);
            } else {
                givenFilePath.createNewFile();
                Utils.writeContents(givenFilePath, givenContent);
            }
        }
        for (String currTrackFile: currentTracked.keySet()) {
            if (!inGiven.containsKey(currTrackFile)) {
                Utils.restrictedDelete(Utils.join(workDir, currTrackFile));
            }
        }
        Utils.writeContents(Utils.join(workDir, ".gitlet", "currentbranch"),
                branchName);
        Utils.writeContents(Utils.join(workDir, ".gitlet", "HEAD"),
                Utils.readContents(branchPath));
        clearStagingArea();
    }

    /**
     * Clears the staging area.
     */
    public static void clearStagingArea() {
        HashMap<String, String> currentStaged = getStaged();
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        if (currentStaged.size() != 0) {
            for (String stagedFile : currentStaged.keySet()) {
                File inStage = Utils.join(workDir, ".gitlet", "stage",
                        stagedFile);
                inStage.delete();
            }
            currentStaged.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                    "staged"), currentStaged);
        }
        if (currMarkedRemoved.size() != 0) {
            for (String inRemoval : currMarkedRemoved.keySet()) {
                File removedFile = Utils.join(workDir, ".gitlet", "stage",
                        inRemoval);
                removedFile.delete();
            }
            currMarkedRemoved.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "removal",
                    "removed"), currMarkedRemoved);
        }
    }

    /**
     * Checks out given commit.
     * @param id is commit id.
     */
    public static void reset(String id) throws IOException {
        File commitPath = Utils.join(workDir, ".gitlet", "commits", id);
        if (!commitPath.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit givenCommit = Utils.readObject(commitPath, Commit.class);
        HashMap<String, String> inGiven = givenCommit.getTracked();
        Commit current = getHead();
        HashMap<String, String> currentTracked = current.getTracked();
        Set<String> inGivenFiles = inGiven.keySet();
        for (String givenFile: inGivenFiles) {
            if (Utils.join(workDir, givenFile).exists()
                    && !currentTracked.containsKey(givenFile)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
        for (String givenFile: inGivenFiles) {
            String version = inGiven.get(givenFile);
            byte[] versionContent = Utils.readContents(Utils.join(workDir,
                    ".gitlet", "blobs", version));
            File wdFilePath = Utils.join(workDir, givenFile);
            if (wdFilePath.exists()) {
                Utils.writeContents(wdFilePath, versionContent);
            } else {
                wdFilePath.createNewFile();
                Utils.writeContents(wdFilePath, versionContent);
            }
        }
        for (String currTrackFile: currentTracked.keySet()) {
            if (!inGiven.containsKey(currTrackFile)) {
                Utils.restrictedDelete(Utils.join(workDir, currTrackFile));
            }
        }
        Utils.writeContents(getBranchFile(getMyBranchName()), id);
        Utils.writeContents(Utils.join(workDir, ".gitlet", "HEAD"), id);
        clearStagingArea();
    }

    /**
     * Find latest common ancestor.
     * @param current is the current commit.
     * @param given is the head commit of the branch to be merged with current.
     * @return
     */
    public static Commit splitPoint(Commit current, Commit given) {
        HashSet<String> currentCommitsSet = new HashSet<>();
        currentCommitsSet.add(current.getCommitId());
        while (current.getParent() != null) {
            Commit newCurrent = getCommit(current.getParent());
            currentCommitsSet.add(newCurrent.getCommitId());
            current = newCurrent;
        }
        String givenParent = given.getParent();
        while (givenParent != null
                && !currentCommitsSet.contains(given.getCommitId())) {
            given = getCommit(givenParent);
            givenParent = given.getParent();
        }
        return given;
    }

    /**
     * Deals with merge cases where one branch head is at the split point.
     * @param split is latest common ancestor.
     * @param givenCommit is head of given branch to be merged with.
     * @param currBranchCommit is head of current branch.
     * @param currentBranch is current branch name.
     * @param givenID is commit id of other branch head.
     */
    public static void branchIsAtSplit(Commit split, Commit givenCommit,
                                       Commit currBranchCommit,
                                       String currentBranch, String givenID) {
        if (split.equals(givenCommit)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            System.exit(0);
        }
        if (split.equals(currBranchCommit)) {
            File currBranchPath = getBranchFile(currentBranch);
            Utils.writeContents(currBranchPath, givenID);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /**
     * Checks out the file and prepare to add to the staging area.
     * @param wdFilePath is path to file in working directory.
     * @param versionContent is the content of the file.
     * @param theFile is the given file name.
     * @param theFileHash is the SHA-1 of the file.
     * @throws IOException
     */
    public static void checkoutAndStage(File wdFilePath, byte[] versionContent,
                                        String theFile, String theFileHash)
            throws IOException {
        if (wdFilePath.exists()) {
            Utils.writeContents(wdFilePath, versionContent);
            automaticStage(theFile, theFileHash);
        } else {
            wdFilePath.createNewFile();
            Utils.writeContents(wdFilePath, versionContent);
            automaticStage(theFile, theFileHash);
        }
    }

    /**
     * Adds file to the staging area.
     * @param file is the name of the file.
     * @param blobHash is the SHA-1 of the file.
     * @throws IOException
     */
    public static void automaticStage(String file, String blobHash)
            throws IOException {
        HashMap<String, String> currentStaged = getStaged();
        currentStaged.put(file, blobHash);
        Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                "staged"), currentStaged);
        File stagedFile = Utils.join(workDir, ".gitlet", "stage",
                file);
        stagedFile.createNewFile();
        Utils.writeContents(stagedFile, blobHash);
        HashMap<String, String> currentTracked = tracked();
        currentTracked.put(file, blobHash);
        Utils.writeObject(Utils.join(workDir, ".gitlet", "tracked"),
                currentTracked);
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        if (currMarkedRemoved.containsKey(file)) {
            currMarkedRemoved.remove(file);
            Utils.writeObject(Utils.join(workDir, ".gitlet",
                    "removal", "removed"), currMarkedRemoved);
            File inRemoval = Utils.join(workDir, ".gitlet", "removal",
                    file);
            inRemoval.delete();
        }
    }

    /**
     * Removes the file from the working directory and from tracking.
     * @param file is the file name.
     * @param blobHash is the SHA-1 of the file.
     * @throws IOException
     */
    public static void removeAndUntrack(String file, String blobHash)
            throws IOException {
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        currMarkedRemoved.put(file, blobHash);
        Utils.writeObject(Utils.join(workDir, ".gitlet", "removal",
                "removed"), currMarkedRemoved);
        File removedFile = Utils.join(workDir, ".gitlet", "removal", file);
        removedFile.createNewFile();
        Utils.writeContents(removedFile, blobHash);
        HashMap<String, String> currentTracked = tracked();
        currentTracked.remove(file);
        Utils.writeObject(Utils.join(workDir, ".gitlet", "tracked"),
                currentTracked);
    }

    /**
     * Handles cases where merging cannot occur.
     * @param currentStaged is hashmap of staged files.
     * @param currMarkedRemoved is hashmap of removed files.
     * @param branchName is the name of the branch to be merged in.
     * @param currentBranch is the current branch name
     */
    public static void mergeFails(HashMap<String, String> currentStaged,
                                  HashMap<String, String> currMarkedRemoved,
                                  String branchName, String currentBranch) {
        if (currentStaged.size() > 0 || currMarkedRemoved.size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!getBranchFile(branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * Goes through the files at the split point and adds or deletes them
     * depending on cases for merge.
     * @param splitTracked is the tracked files at the split point.
     * @param currentTrack is the tracked files at the current head.
     * @param inGiven is the the tracked files at the other branch head.
     * @throws IOException
     */
    public static void checkThroughSplit(HashMap<String, String> splitTracked,
                                         HashMap<String, String> currentTrack,
                                         HashMap<String, String> inGiven)
            throws IOException {
        for (String splitFile: splitTracked.keySet()) {
            File splitFilePath = Utils.join(workDir, splitFile);
            String splitFileHash = splitTracked.get(splitFile);
            if (currentTrack.containsKey(splitFile)
                    && !inGiven.containsKey(splitFile)) {
                if (currentTrack.get(splitFile).equals(splitFileHash)) {
                    Utils.restrictedDelete(splitFilePath);
                    removeAndUntrack(splitFile, splitFileHash);
                } else {
                    byte[] content = conflictFile(currentTrack, splitFile,
                            splitFilePath, true);
                    String blobHash = Utils.sha1(content);
                    File blobFile = Utils.join(workDir, ".gitlet", "blobs",
                            blobHash);
                    blobFile.createNewFile();
                    Utils.writeContents(blobFile, content);
                    automaticStage(splitFile, blobHash);
                }
            }
            if (inGiven.containsKey(splitFile)
                    && !currentTrack.containsKey(splitFile)) {
                if (!inGiven.get(splitFile).equals(splitFileHash)) {
                    byte[] content = conflictFile(inGiven, splitFile,
                            splitFilePath, false);
                    String blobHash = Utils.sha1(content);
                    File blobFile = Utils.join(workDir, ".gitlet", "blobs",
                            blobHash);
                    blobFile.createNewFile();
                    Utils.writeContents(blobFile, content);
                    automaticStage(splitFile, blobHash);
                }
            }
            if (currentTrack.containsKey(splitFile)
                    && inGiven.containsKey(splitFile)
                    && !currentTrack.get(splitFile).equals(splitFileHash)
                    && !inGiven.get(splitFile).equals(splitFileHash)
                    && !currentTrack.get(splitFile).equals(
                    inGiven.get(splitFile))) {
                aConflict = true;
                byte[] contInCurrent = Utils.readContents(getBlob(
                        currentTrack.get(splitFile)));
                byte [] givenContent = Utils.readContents(getBlob(
                        inGiven.get(splitFile)));
                Utils.writeContents(splitFilePath, "<<<<<<< HEAD\n",
                        contInCurrent, "=======\n", givenContent, ">>>>>>>\n");
                byte[] content = Utils.readContents(splitFilePath);
                String blobHash = Utils.sha1(content);
                File blobFile = Utils.join(workDir, ".gitlet", "blobs",
                        blobHash);
                blobFile.createNewFile();
                Utils.writeContents(blobFile, content);
                automaticStage(splitFile, blobHash);
            }
        }
    }

    /**
     * Merge the files of the given branch with the current branch.
     * @param branchName is the name of the branch to be merged with current.
     * @throws IOException
     */
    public static void merge(String branchName) throws IOException {
        HashMap<String, String> currentStaged = getStaged();
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        String currentBranch = getMyBranchName();
        mergeFails(currentStaged, currMarkedRemoved, branchName,
                currentBranch);
        String currCommitID = Utils.readContentsAsString(
                getBranchFile(currentBranch));
        Commit currBranchCommit = getCommit(currCommitID);
        HashMap<String, String> currentTracked = currBranchCommit.getTracked();
        String givenID = Utils.readContentsAsString(getBranchFile(branchName));
        Commit givenCommit = getCommit(givenID);
        HashMap<String, String> inGiven = givenCommit.getTracked();
        Set<String> inGivenFiles = inGiven.keySet();
        for (String givenFile: inGivenFiles) {
            if (Utils.join(workDir, givenFile).exists()
                    && !currentTracked.containsKey(givenFile)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
        Commit split = splitPoint(currBranchCommit, givenCommit);
        branchIsAtSplit(split, givenCommit, currBranchCommit, currentBranch,
                givenID);
        HashMap<String, String> splitTracked = split.getTracked();
        for (String givenFile: inGivenFiles) {
            String givenFileHash = inGiven.get(givenFile);
            File wdFilePath = Utils.join(workDir, givenFile);
            if (currentTracked.containsKey(givenFile)
                    && splitTracked.containsKey(givenFile)) {
                String currentFileHash = currentTracked.get(givenFile);
                String splitFileHash = splitTracked.get(givenFile);
                if (!givenFileHash.equals(splitFileHash)
                        && currentFileHash.equals(splitFileHash)) {
                    byte[] versionContent = Utils.readContents(Utils.join(
                            workDir, ".gitlet", "blobs", givenFileHash));
                    checkoutAndStage(wdFilePath, versionContent, givenFile,
                            givenFileHash);
                }
            }
            if (!currentTracked.containsKey(givenFile)
                    && !splitTracked.containsKey(givenFile)) {
                byte[] versionContent = Utils.readContents(Utils.join(
                        workDir, ".gitlet", "blobs", givenFileHash));
                checkoutAndStage(wdFilePath, versionContent, givenFile,
                        givenFileHash);
            }
            if (currentTracked.containsKey(givenFile)
                    && !splitTracked.containsKey(givenFile)) {
                if (!givenFileHash.equals(currentTracked.get(givenFile))) {
                    bothConflict(currentTracked, inGiven, givenFile,
                            wdFilePath);
                }
            }
        }
        checkThroughSplit(splitTracked, currentTracked, inGiven);
        commitMerge("Merged " + branchName + " into " + currentBranch + ".",
                currCommitID, givenID, currentBranch);
    }

    /**
     * Makes a merge commit.
     * @param msg is the commit message.
     * @param oneParent is the parent from the current branch.
     * @param twoParent is the parent from the given branch.
     * @param myBranch is the name of the current branch.
     * @throws IOException
     */
    public static void commitMerge(String msg, String oneParent,
                                   String twoParent, String myBranch)
            throws IOException {
        HashMap<String, String> currentStaged = getStaged();
        HashMap<String, String> currMarkedRemoved = getMarkRemoved();
        if (currentStaged.size() == 0 && currMarkedRemoved.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        HashMap<String, String> tracking = tracked();
        File current = Utils.join(workDir, ".gitlet", "HEAD");
        Commit thisCommit = new Commit(msg, tracking, oneParent, twoParent,
                myBranch);
        Utils.writeContents(current, thisCommit.getCommitId());
        HashMap<String, String> mapOfCommits = getCommitHashes();
        mapOfCommits.put(thisCommit.getCommitId(), thisCommit.getMessage());
        File allCommits = Utils.join(workDir, ".gitlet", "commits", "hashes");
        Utils.writeObject(allCommits, mapOfCommits);
        File thisBranch = getBranchFile(myBranch);
        Utils.writeContents(thisBranch, thisCommit.getCommitId());
        File thisCommitFile = Utils.join(workDir, ".gitlet", "commits",
                thisCommit.getCommitId());
        thisCommitFile.createNewFile();
        Utils.writeObject(thisCommitFile, thisCommit);
        if (currentStaged.size() != 0) {
            for (String stagedFile : currentStaged.keySet()) {
                File inStage = Utils.join(workDir, ".gitlet", "stage",
                        stagedFile);
                inStage.delete();
            }
            currentStaged.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "stage",
                    "staged"), currentStaged);
        }
        if (currMarkedRemoved.size() != 0) {
            for (String inRemoval : currMarkedRemoved.keySet()) {
                File removedFile = Utils.join(workDir, ".gitlet", "removal",
                        inRemoval);
                removedFile.delete();
            }
            currMarkedRemoved.clear();
            Utils.writeObject(Utils.join(workDir, ".gitlet", "removal",
                    "removed"), currMarkedRemoved);
        }
        if (aConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Handles merge case where a file present at the split point was modified
     * in one branch and deleted in the other.
     * @param tracked is the hashmap of tracked files.
     * @param splitFile is the name of the file at the split point.
     * @param splitFilePath is the path of the file in the working directory.
     * @param givenEmpty indicates whether the file was deleted in the given
     *                   branch.
     * @return
     */
    public static byte[] conflictFile(HashMap<String, String> tracked,
                                      String splitFile, File splitFilePath,
                                      boolean givenEmpty) {
        byte[] contents = Utils.readContents(getBlob(
                tracked.get(splitFile)));
        if (givenEmpty) {
            Utils.writeContents(splitFilePath, "<<<<<<< HEAD\n",
                    contents, "=======\n", ">>>>>>>\n");
        } else {
            Utils.writeContents(splitFilePath, "<<<<<<< HEAD\n",
                    "=======\n", contents, ">>>>>>>\n");
        }
        aConflict = true;
        return Utils.readContents(splitFilePath);
    }

    /**
     * Handles merge case where file present at the split point is modified
     * differently in both branches.
     * @param cur is the hashmap of tracked files by the current head.
     * @param give is the hashmap of tracked files by the other branch head.
     * @param theFile is the name of the file.
     * @param theFilePath is the path to the file in the working directory.
     * @throws IOException
     */
    public static void bothConflict(HashMap<String, String> cur,
                                    HashMap<String, String> give,
                                    String theFile, File theFilePath)
            throws IOException {
        byte[] contInCurrent = Utils.readContents(getBlob(
                cur.get(theFile)));
        byte [] givenContent = Utils.readContents(getBlob(
                give.get(theFile)));
        Utils.writeContents(theFilePath, "<<<<<<< HEAD\n",
                contInCurrent, "=======\n", givenContent, ">>>>>>>\n");
        byte[] content = Utils.readContents(theFilePath);
        String blobHash = Utils.sha1(content);
        File blobFile = Utils.join(workDir, ".gitlet", "blobs",
                blobHash);
        blobFile.createNewFile();
        Utils.writeContents(blobFile, content);
        automaticStage(theFile, blobHash);
        aConflict = true;
    }

    /** Initial hashmap of all commits. */
    private static HashMap<String, String> commitsMap = new HashMap<>();

    /** Initial hashmap of staged files. */
    private static HashMap<String, String> staged = new HashMap<>();

    /** My current working directory. */
    private static String workDir = System.getProperty("user.dir");

    /** Initial hashmap of removed files. */
    private static HashMap<String, String> markRemoved = new HashMap<>();

    /** Initial hashmap of tracked files. */
    private static HashMap<String, String> track = new HashMap<>();

    /** All files with modifications not staged. */
    private static ArrayList<String> modsNotStaged = new ArrayList<>();

    /** All untracked files. */
    private static ArrayList<String> untracked = new ArrayList<>();

    /** Indicates whether there exists a merge conflict. */
    private static boolean aConflict = false;

    /** Path to /branches. */
    private static File dBranches = Utils.join(workDir, ".gitlet", "branches");
}
