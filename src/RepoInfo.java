public class RepoInfo {
    public String fileRel;
    public String remoteURL;
    public String branch;
    public String revision;

    public RepoInfo(String sFileRel, String sRemoteURL, String sBranch, String sRevision) {
        fileRel = sFileRel;
        remoteURL = sRemoteURL;
        branch = sBranch;
        revision = sRevision;
    }
}
