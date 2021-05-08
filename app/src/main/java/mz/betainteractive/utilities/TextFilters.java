package mz.betainteractive.utilities;

import java.util.ArrayList;
import java.util.List;

public class TextFilters {

    private String text;
    private Filter filterType;
    private String filterText;
    private String[] filterTexts;
    private final String wildcard = "%";

    public enum Filter {
        STARTSWITH, ENDSWITH, CONTAINS, MULTIPLE_CONTAINS, NONE, EMPTY
    }

    public TextFilters(String text) {
        this.text = text;
        detectWildcards();
    }

    private void detectWildcards() {

        if (text == null || text.isEmpty()) {
            filterType = Filter.EMPTY;
            return;
        }

        if (!text.contains(wildcard)) {
            filterType = Filter.NONE;
            filterText = text;
        } else {

            int first_index = text.indexOf(wildcard);
            int next_index = text.indexOf(wildcard, first_index+1);
            int last_index = text.lastIndexOf(wildcard);

            if (first_index==0 && last_index==text.length()-1 && next_index == last_index) {
                filterType = Filter.CONTAINS;
                filterText = text.replaceAll(wildcard, "");
                return;
            }

            if (last_index == first_index && first_index==0) { //then the last_index is not the last, otherwise the if before would be true
                filterType = Filter.STARTSWITH;
                filterText = text.replaceAll(wildcard, "");
                return;
            }

            if (last_index == first_index && last_index==text.length()-1 ) {
                filterType = Filter.ENDSWITH;
                filterText = text.replaceAll(wildcard, "");
                return;
            }

            //not at beginning not at the end, split words
            filterType = Filter.MULTIPLE_CONTAINS;

            String[] split = text.split(wildcard);
            List<String> list = new ArrayList<>();

            for (String item : split) {
                if (item != null && !item.isEmpty()){
                    list.add(item);
                }
            }

            filterTexts = list.toArray(list.toArray(new String[list.size()]));

        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        detectWildcards();
    }

    public Filter getFilterType() {
        return filterType;
    }

    public void setFilterType(Filter filterType) {
        this.filterType = filterType;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String[] getFilterTexts() {
        return filterTexts;
    }

    public void setFilterTexts(String[] filterTexts) {
        this.filterTexts = filterTexts;
    }
}
