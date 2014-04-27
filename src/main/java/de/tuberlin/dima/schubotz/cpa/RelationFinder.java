/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package de.tuberlin.dima.schubotz.cpa;

import de.tuberlin.dima.schubotz.cpa.contracts.DocumentProcessor;
import de.tuberlin.dima.schubotz.cpa.contracts.calculateCPA;
import de.tuberlin.dima.schubotz.cpa.io.WikiDocumentEmitter;
import de.tuberlin.dima.schubotz.cpa.types.LinkTuple;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.Program;
import eu.stratosphere.api.common.ProgramDescription;
import eu.stratosphere.api.common.operators.FileDataSink;
import eu.stratosphere.api.common.operators.FileDataSource;
import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.common.operators.Ordering;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.operators.MapOperator;
import eu.stratosphere.api.java.record.operators.ReduceOperator;
import eu.stratosphere.types.DoubleValue;
import eu.stratosphere.types.IntValue;

public class RelationFinder implements Program, ProgramDescription {

    /**
    * {@inheritDoc}
    */
    @Override
    public Plan getPlan( String... args ) {
        // parse job parameters
        String dataSet = args[0];
        String output = args[1];
        
        /*String alpha = args[3];
        String threshold = args[6];*/

        FileDataSource source = new FileDataSource(WikiDocumentEmitter.class, dataSet, "Dumps");

        MapOperator doc = MapOperator
                .builder( DocumentProcessor.class )
                .name( "Processing Documents" )
                .input( source )
                .build();


        ReduceOperator filter = ReduceOperator
                .builder(calculateCPA.class, LinkTuple.class, 0)
                .name("Filter")
                .input(doc)
                .build();

        filter.setGroupOrder(new Ordering(2, IntValue.class, Order.DESCENDING));

/*        filter.setParameter( "THRESHOLD", threshold );
        filter.setParameter( "α", alpha );*/


        FileDataSink out = new FileDataSink( CsvOutputFormat.class, output, filter, "Output" );
        CsvOutputFormat.configureRecordFormat( out )
                .recordDelimiter('\n')
                .fieldDelimiter(';')
                .field(LinkTuple.class, 0)
                .field(DoubleValue.class, 1)
                .field(IntValue.class, 2)
        ;

        return new Plan(out, "CPA-demo");
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getDescription() {
        return "Parameters: [DATASET] [OUTPUT]";// [ALPHA] [THRESHOLD]";
    }
    
}
