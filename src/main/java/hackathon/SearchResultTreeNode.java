package hackathon;

import javax.swing.tree.DefaultMutableTreeNode;

public class SearchResultTreeNode extends DefaultMutableTreeNode {
    public SearchResultTreeNode(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public SearchResultTreeNode(Object userObject, SearchResult searchResult) {
        super(userObject);
        this.searchResult = searchResult;
    }

    public SearchResultTreeNode(Object userObject, boolean allowsChildren, SearchResult searchResult) {
        super(userObject, allowsChildren);
        this.searchResult = searchResult;
    }

    private final SearchResult searchResult;

    public SearchResult getSearchResult() {
        return searchResult;
    }
}
