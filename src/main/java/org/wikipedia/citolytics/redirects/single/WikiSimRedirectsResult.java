package org.wikipedia.citolytics.redirects.single;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.java.tuple.Tuple4;
import org.wikipedia.citolytics.cpa.types.RecommendationPair;

import java.util.regex.Pattern;

/**
 * RecommendationPair as strings
 * 0: Hash
 * 1: Page A
 * 2: Page B
 * 3: Everything else
 */
public class WikiSimRedirectsResult extends Tuple4<Long, String, String, String> {
    public static final String delimiterPattern = Pattern.quote("|");

    public WikiSimRedirectsResult() {
        // Flink needs empty constructor
    }

    public WikiSimRedirectsResult(RecommendationPair result) {

        setField(result.getHash(), 0);
        setField(result.f1, 1);
        setField(result.f2, 2);

        String fields = "";
        for (int f = 3; f < result.getArity(); f++) {
            if (!fields.isEmpty()) {
                fields += "|";
            }
            fields += result.getField(f).toString();
        }

        setField(fields, 3);
    }

    public WikiSimRedirectsResult(String delimitedLine) {
        String[] cols = delimitedLine.split(delimiterPattern, getArity());

        setField(Long.valueOf(cols[0]), 0);
        setField(cols[1], 1);
        setField(cols[2], 2);
        setField(cols[3], 3); // rest of RecommendationPair
    }

    public void sumWith(WikiSimRedirectsResult otherResult) throws Exception {

        // sum rest of ResultSet
        String[] colsA = f3.split(delimiterPattern);
        String[] colsB = otherResult.f3.split(delimiterPattern);

        if (colsA.length != colsB.length)
            throw new Exception("Cannot sum results with different column length. A = " + colsA.length + " != B = " + colsB.length);

        long distance = Long.valueOf(colsA[0]) + Long.valueOf(colsB[0]);
        int count = Integer.valueOf(colsA[1]) + Integer.valueOf(colsB[1]);

        Double[] cpa = new Double[colsA.length - 2];
        for (int i = 0; i < cpa.length; i++) {
            cpa[i] = Double.valueOf(colsA[i + 2]) + Double.valueOf(colsB[i + 2]);
        }

        // back to String
        setField(distance + "|" + count + "|" + StringUtils.join(cpa, '|'), 3);
    }
}
