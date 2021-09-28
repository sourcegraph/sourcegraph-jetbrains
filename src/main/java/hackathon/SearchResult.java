package hackathon;

public class SearchResult {
    String repo;
    String file;
    String path;
    String preview;
    String type;
    String content;
    OffsetAndLength offsetAndLength;
    int lineNumber;

    public OffsetAndLength getOffsetAndLength() {
        return offsetAndLength;
    }

    public void setOffsetAndLength(OffsetAndLength offsetAndLength) {
        this.offsetAndLength = offsetAndLength;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "repo='" + repo + '\'' +
                ", file='" + file + '\'' +
                ", path='" + path + '\'' +
                ", preview='" + preview + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", offsetAndLength=" + offsetAndLength +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
