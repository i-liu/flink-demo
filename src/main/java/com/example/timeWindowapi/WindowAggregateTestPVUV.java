package com.example.timeWindowapi;

import com.example.common.Event;
import com.example.sourceapi.ClickParallelSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.HashSet;

/**
 * @author liuwei
 * @date 2022/5/23 17:14
 */
public class WindowAggregateTestPVUV {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(3);
        environment.addSource(new ClickParallelSource()).assignTimestampsAndWatermarks(WatermarkStrategy.
                <Event>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(
                (SerializableTimestampAssigner<Event>) (element, recordTimestamp) -> element.timestamp
        )).keyBy(data -> true).window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunctionPV()).print();


        environment.execute();
    }

    public static class AggregateFunctionPV implements AggregateFunction<Event, Tuple2<Long, HashSet<String>>, Double> {
        @Override
        public Tuple2<Long, HashSet<String>> createAccumulator() {
            return Tuple2.of(0L, new HashSet<>());
        }

        @Override
        public Tuple2<Long, HashSet<String>> add(Event value, Tuple2<Long, HashSet<String>> accumulator) {
            accumulator.f1.add(value.user);
            return Tuple2.of(accumulator.f0 + 1, accumulator.f1);
        }

        @Override
        public Double getResult(Tuple2<Long, HashSet<String>> accumulator) {
            return (double) accumulator.f0 / accumulator.f1.size();
        }

        @Override
        public Tuple2<Long, HashSet<String>> merge(Tuple2<Long, HashSet<String>> a, Tuple2<Long, HashSet<String>> b) {
            return null;
        }
    }
}
