package com.company.warehouse.simulation.graph.agents.stats.slots;

import com.amalgamasimulation.core.scheduling.Slot;
import com.company.warehouse.simulation.graph.agents.Forklift;
import com.company.warehouse.simulation.tasks.IEquipmentState;

public class AgentStatsSlot extends Slot {

    private boolean closed;
    private Forklift agent;
    private IEquipmentState state;

    public AgentStatsSlot(double beginTime, Forklift agent, IEquipmentState state) {
        super(beginTime, beginTime);
        this.agent = agent;
        this.state = state;
    }

    @Override
    public double endTime() {
        return Math.max(beginTime(), closed ? super.endTime() : agent.time());
    }

    public void close() {
        max = agent.time();
        closed = true;
    }

    public IEquipmentState getState() {
        return state;
    }
}