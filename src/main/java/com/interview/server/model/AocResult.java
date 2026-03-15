package com.interview.server.model;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("aoc_results")
public class AocResult {

    @PrimaryKeyColumn(
        name = "day",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private int day;

    @PrimaryKeyColumn(
        name = "run_time",
        ordinal = 1,
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.DESCENDING
    )
    private UUID runTime;

    @Column("task")
    private int task;

    @Column("input")
    private String input;

    @Column("output")
    private String output;

    @Column("result")
    private AocResultStatus result;

    @Column("expected_output")
    private String expectedOutput;

    @Column("test")
    private boolean test;

    public AocResult() {}

    public AocResult(
        int day,
        UUID runTime,
        int task,
        String input,
        String output,
        AocResultStatus result,
        String expectedOutput,
        boolean test
    ) {
        this.day = day;
        this.runTime = runTime;
        this.task = task;
        this.input = input;
        this.output = output;
        this.result = result;
        this.expectedOutput = expectedOutput;
        this.test = test;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public UUID getRunTime() {
        return runTime;
    }

    public void setRunTime(UUID runTime) {
        this.runTime = runTime;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public AocResultStatus getResult() {
        return result;
    }

    public void setResult(AocResultStatus result) {
        this.result = result;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
