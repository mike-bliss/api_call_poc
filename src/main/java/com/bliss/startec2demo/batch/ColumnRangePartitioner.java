package com.bliss.startec2demo.batch;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ColumnRangePartitioner implements Partitioner {

    private final Long delegatedActionId;
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ColumnRangePartitioner(Long delegatedActionId) {
        this.delegatedActionId = delegatedActionId;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /**
     * Create a set of distinct {@link ExecutionContext} instances together with
     * a unique identifier for each one. The identifiers should be short,
     * mnemonic values, and only have to be unique within the return value (e.g.
     * use an incrementer).
     *
     * Partition a database table assuming that the data in the column specified
     * are uniform distributed. The execution values will have keys
     * <code> minValue</code> and <code>maxValue</code> specifying the range of
     * values to consider in each partition.
     *
     * @param gridSize the size of the map to return
     * @return a map from identifier to input parameters
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int minId = jdbcTemplate.queryForObject("select min(id) from violations where delegated_action_id = ?", Integer.class, this.delegatedActionId);
        int maxId = jdbcTemplate.queryForObject("select max(id) from violations where delegated_action_id = ?", Integer.class, this.delegatedActionId);
        int targetSize = (maxId - minId) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        int number = 0;
        int startId = minId;
        int endId = startId + targetSize - 1;

        while (startId <= maxId) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (endId >= maxId) {
                endId = maxId;
            }
            value.putInt("minValue", startId);
            value.putInt("maxValue", endId);
            startId += targetSize;
            endId += targetSize;
            number++;
        }
        return result;
    }
}
