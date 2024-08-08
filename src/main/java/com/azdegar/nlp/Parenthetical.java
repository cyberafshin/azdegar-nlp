package com.azdegar.nlp;

import edu.stanford.nlp.ling.CoreLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Afshin
 */
@Getter
@Setter
public class Parenthetical extends ArrayList<CoreLabel> {

    private CoreLabel opening;
    private CoreLabel closing;

    public Parenthetical() {
    }

    public Parenthetical(List<CoreLabel> in) {
        addAll(in);
    }

    @Override
    public CoreLabel get(int index) {
        if (index >= 0 && index < size()) {
            return super.get(index);
        } else {
            return new CoreLabel();
        }
    }

    @Override
    public boolean add(CoreLabel e) {
        if (isEmpty() && (e.tag().equals("-LRB-") || e.word().matches("[\"‘“—]"))) {
            opening = e;
            return true;
        } else if (!isEmpty() && (e.tag().equals("-RRB-") || e.word().matches("[\"’”—]"))) {
            closing = e;
            return true;
        }
        return super.add(e);
    }

    @Override
    public String toString() {
        String s = (opening != null) ? opening.word() : "";
        s += stream().map(label -> (label.value() == null) ? ((CoreLabel) label).word() : label.value()).collect(Collectors.joining(" "));
        s += (closing != null) ? closing.word() : "";
        return s;
    }

    public String getTaggedString() {
        String s = (opening != null) ? opening.word() : "";
        s += stream().map(label -> (label.value() == null) ? ((CoreLabel) label).word() + "/" + ((CoreLabel) label).tag() : label.value()).collect(Collectors.joining(" "));
        s += (closing != null) ? closing.word() : "";
        return s;
    }

}
