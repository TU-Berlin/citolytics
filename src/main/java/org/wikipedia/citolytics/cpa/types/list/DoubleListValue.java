package org.wikipedia.citolytics.cpa.types.list;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.types.DoubleValue;
import org.apache.flink.types.ListValue;
import org.wikipedia.citolytics.cpa.utils.WikiSimConfiguration;

public class DoubleListValue extends ListValue<DoubleValue> {
    public String toString() {
        // removes brackets
        return StringUtils.join(this, WikiSimConfiguration.csvFieldDelimiter);
    }

    public static DoubleListValue valueOf(double[] array) {
        DoubleListValue list = new DoubleListValue();
        for (double s : array) {
            list.add(new DoubleValue(s));
        }
        return list;
    }

    public static DoubleListValue valueOf(String delimitedString, String delimiterPattern) {
        String[] dbs = delimitedString.split(delimiterPattern);
        DoubleListValue list = new DoubleListValue();
        for (String db : dbs) {
            list.add(new DoubleValue(Double.valueOf(db)));
        }
        return list;
    }

    public static DoubleListValue sum(DoubleListValue firstList, DoubleListValue secondList) throws Exception {
        if (firstList == null || secondList == null) {
            throw new Exception("Cannot sum lists if one list NULL.");
        } else if (firstList.size() == 0 && secondList.size() == 0) {
            throw new Exception("Cannot sum lists if both lists are empty.");
        } else if (firstList.size() == 0 && secondList.size() > 0) {
            return secondList;
        } else if (secondList.size() == 0 && firstList.size() > 0) {
            return firstList;
        } else if (firstList.size() != secondList.size()) {
            throw new Exception("Cannot sum lists with different size.");
        }

        int i = 0;
        for (DoubleValue firstValue : firstList) {
            firstList.set(i, new DoubleValue(firstValue.getValue() + secondList.get(i).getValue()));
            i++;
        }

        return firstList;
    }
}
