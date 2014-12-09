package de.tuberlin.dima.schubotz.cpa;

import de.tuberlin.dima.schubotz.cpa.contracts.DocumentProcessor;
import de.tuberlin.dima.schubotz.cpa.io.WikiDocumentDelimitedInputFormat;
import de.tuberlin.dima.schubotz.cpa.types.WikiDocument;
import de.tuberlin.dima.schubotz.cpa.utils.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts "See also" links from Wikipedia articles and creates CSV for DB import
 * <p/>
 * TODO: Check if link exists in article body
 * <p/>
 * table structure: article (primary key), "see also"-link target, position, total count of "see also"-links,
 */
public class SeeAlsoExtractor {

    public static String csvRowDelimiter = "\n";
    public static String csvFieldDelimiter = "\t";

    public static void main(String[] args) throws Exception {

        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        if (args.length <= 1) {
            System.err.println("Input/output parameters missing!");
            System.err.println(new WikiSim().getDescription());
            System.exit(1);
        }

        String inputFilename = args[0];
        String outputFilename = args[1];

        DataSource<String> text = env.readFile(new WikiDocumentDelimitedInputFormat(), inputFilename);

        // ArticleCounter, Links (, AvgDistance
        DataSet<Tuple4<String, String, Integer, Integer>> output = text.flatMap(new FlatMapFunction<String, Tuple4<String, String, Integer, Integer>>() {
            public void flatMap(String content, Collector out) {

                WikiDocument doc = DocumentProcessor.processDoc(content, true);

                if (doc == null) return;

                List<Map.Entry<String, Integer>> links = doc.getOutLinks();

                int pos = 1;
                for (Map.Entry<String, Integer> outLink : links) {

                    out.collect(new Tuple4<>(StringUtils.addCsvEnclosures(doc.getTitle()), StringUtils.addCsvEnclosures(outLink.getKey()), pos, links.size()));
                    pos++;
                }
            }
        });

        output.writeAsCsv(outputFilename, csvRowDelimiter, csvFieldDelimiter, FileSystem.WriteMode.OVERWRITE);

        env.execute("WikiSeeAlsoExtractor");
    }


}
