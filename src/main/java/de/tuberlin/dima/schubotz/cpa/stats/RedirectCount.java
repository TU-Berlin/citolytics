package de.tuberlin.dima.schubotz.cpa.stats;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.core.fs.FileSystem;

import java.util.regex.Pattern;

public class RedirectCount {
    public static void main(String[] args) throws Exception {

        String outputFilename = args[2];

        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // article|link target
        DataSet<Tuple1<String>> links = env.readTextFile(args[0])
                .map(new MapFunction<String, Tuple1<String>>() {
                    @Override
                    public Tuple1<String> map(String s) throws Exception {
                        String[] cols = s.split(Pattern.quote("|"));
                        return new Tuple1<>(cols[1]);
                    }
                });

        // source|redirect target
        DataSet<Tuple1<String>> redirects = env.readTextFile(args[1])
                .map(new MapFunction<String, Tuple1<String>>() {
                    @Override
                    public Tuple1<String> map(String s) throws Exception {
                        String[] cols = s.split(Pattern.quote("|"));
                        return new Tuple1<>(cols[0]);
                    }
                });

        DataSet<Tuple1<Integer>> res = links.join(redirects)
                .where(0)
                .equalTo(0)
                .with(new JoinFunction<Tuple1<String>, Tuple1<String>, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> join(Tuple1<String> a, Tuple1<String> b) throws Exception {
                        return new Tuple1<>(1);
                    }
                })
                .sum(0);

        if (outputFilename.equals("print")) {
            res.print();
        } else {
            res.writeAsText(outputFilename, FileSystem.WriteMode.OVERWRITE);
        }

        env.execute();

    }
}
