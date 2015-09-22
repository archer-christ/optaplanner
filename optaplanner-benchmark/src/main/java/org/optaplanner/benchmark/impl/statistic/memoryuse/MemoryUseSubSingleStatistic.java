/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.impl.statistic.memoryuse;

import java.util.List;

import org.optaplanner.benchmark.config.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.impl.statistic.ProblemBasedSubSingleStatistic;
import org.optaplanner.benchmark.impl.result.SolverProblemBenchmarkResult;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.solver.DefaultSolver;

public class MemoryUseSubSingleStatistic extends ProblemBasedSubSingleStatistic<MemoryUseStatisticPoint> {

    private long timeMillisThresholdInterval;

    private MemoryUseSubSingleStatisticListener listener;

    public MemoryUseSubSingleStatistic(SolverProblemBenchmarkResult solverProblemBenchmarkResult) {
        this(solverProblemBenchmarkResult, 1000L);
    }

    public MemoryUseSubSingleStatistic(SolverProblemBenchmarkResult solverProblemBenchmarkResult, long timeMillisThresholdInterval) {
        super(solverProblemBenchmarkResult, ProblemStatisticType.MEMORY_USE);
        if (timeMillisThresholdInterval <= 0L) {
            throw new IllegalArgumentException("The timeMillisThresholdInterval (" + timeMillisThresholdInterval
                    + ") must be bigger than 0.");
        }
        this.timeMillisThresholdInterval = timeMillisThresholdInterval;
        listener = new MemoryUseSubSingleStatisticListener();
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    public void open(Solver solver) {
        ((DefaultSolver) solver).addPhaseLifecycleListener(listener);
    }

    public void close(Solver solver) {
        ((DefaultSolver) solver).removePhaseLifecycleListener(listener);
    }

    private class MemoryUseSubSingleStatisticListener extends PhaseLifecycleListenerAdapter {

        private long nextTimeMillisThreshold = timeMillisThresholdInterval;

        @Override
        public void stepEnded(AbstractStepScope stepScope) {
            long timeMillisSpent = stepScope.getPhaseScope().calculateSolverTimeMillisSpent();
            if (timeMillisSpent >= nextTimeMillisThreshold) {
                pointList.add(new MemoryUseStatisticPoint(timeMillisSpent, MemoryUseMeasurement.create()));

                nextTimeMillisThreshold += timeMillisThresholdInterval;
                if (nextTimeMillisThreshold < timeMillisSpent) {
                    nextTimeMillisThreshold = timeMillisSpent;
                }
            }
        }

    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return MemoryUseStatisticPoint.buildCsvLine("timeMillisSpent", "usedMemory", "maxMemory");
    }

    @Override
    protected MemoryUseStatisticPoint createPointFromCsvLine(ScoreDefinition scoreDefinition,
            List<String> csvLine) {
        return new MemoryUseStatisticPoint(Long.valueOf(csvLine.get(0)),
                new MemoryUseMeasurement(Long.valueOf(csvLine.get(1)), Long.valueOf(csvLine.get(2))));
    }

}
