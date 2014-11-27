package de.tuberlin.dima.schubotz.cpa.types;

import de.tuberlin.dima.schubotz.cpa.WikiSim;
import de.tuberlin.dima.schubotz.cpa.utils.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;

public class LinkTuple extends Tuple2<String, String> {
    public void LinkTuple() {

    }

    public void LinkTuple(String first, String second) {
        setFirst(first);
        setSecond(second);
    }

    public void setFirst(String first) {
        setField(first, 0);
    }

    public void setSecond(String second) {
        setField(second, 1);
    }

    public String getFirst() {
        return getField(0);
    }

    public String getSecond() {
        return getField(1);
    }

    @Override
    public String toString() {
        return String.valueOf(getField(0))
                + WikiSim.csvFieldDelimiter
                + String.valueOf(getField(1));
    }

    public long getHash() {
        return hash(getFirst() + getSecond());
    }

    public static long hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }
}
