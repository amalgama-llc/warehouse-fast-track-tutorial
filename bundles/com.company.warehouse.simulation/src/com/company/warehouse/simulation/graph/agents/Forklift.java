package com.company.warehouse.simulation.graph.agents;

import java.util.ArrayList;
import java.util.List;

import com.amalgamasimulation.engine.Engine;
import com.amalgamasimulation.graphagent.GeometricGraphPosition;
import com.amalgamasimulation.graphagent.GraphAgent;
import com.amalgamasimulation.utils.TimeStatistics;
import com.amalgamasimulation.utils.Utils;
import com.company.warehouse.simulation.PalletPosition;
import com.company.warehouse.simulation.PalletPosition.Operation;
import com.company.warehouse.simulation.graph.Arc;
import com.company.warehouse.simulation.graph.Node;
import com.company.warehouse.simulation.graph.agents.stats.slots.AgentStatsSlot;
import com.company.warehouse.simulation.tasks.IEquipmentState;
import com.company.warehouse.simulation.tasks.MovePalletTask;

/**
 * This class represents an agent that lives in our simulation model. It extends
 * the GraphAgent class from Amalgama Platform's Graph Agent Library
 *
 */
public class Forklift extends GraphAgent<Node, Arc> {
	
	// This is the factor by which the agent's velocity will be multiplied
	// when it is passing by another agent
	protected final double BYPASSING_VELOCITY_MODIFIER = 0.1;
	
	// This is the duration of bypassing after meeting another agent
	protected final double BYPASSING_DURATION = 30;
	
	// Agent's name
	protected String name;
	
	// Base velocity of the agent
	protected double baseVelocity;
	
	// Current velocity of the agent, can be either its base velocity or
	// velocity reduced due to bypassing
	protected double currentVelocity;
	
	// The node to which the agent gets placed at the beginning of the simulation
	protected Node initialNode;

	// This TimeStatistics object is used to collect statistics on the
	// agent's bypasses count with 1 hour granularity
	protected TimeStatistics bypassesCountStatistics;
	
	// This list accumulates the statistics used to show the history
	// of agent's state changes on the gantt chart
	protected List<AgentStatsSlot> statsSlots = new ArrayList<>();
	
	// This is the callback called every time agent reaches its current
	// destination
	protected Runnable destinationArrivalHandler;
	
    private boolean loaded;

    // User-provided callback to announce completion of the current action
    private Runnable onComplete;
    
    private MovePalletTask task;
    private double utilization = 0;
	
	public Forklift(Engine engine, String name, Node initialNode, double baseVelocity) {
		super(engine);
		this.name = name;
		this.initialNode = initialNode;
		this.baseVelocity = baseVelocity;
		this.currentVelocity = baseVelocity;		
		this.bypassesCountStatistics = new TimeStatistics(engine.hour());
	}
	
	public void setDestinationArrivalHandler(Runnable destinationArrivalHandler) {
		this.destinationArrivalHandler = destinationArrivalHandler;
	}
	
	public TimeStatistics getBypassesCountStatistics() {
		return bypassesCountStatistics;
	}
	
	public boolean isLoaded() {
        return loaded;
    }

    private void resetAction(Runnable onComplete) {
        cancelMoving();
        this.onComplete = onComplete;
    }

    private void finishAction() {
        if (onComplete != null) {
            var callback = onComplete;
            onComplete = null;
            callback.run();
        }
    }
    
    public MovePalletTask getCurrentTask() {
        return task;
    }

    public void setCurrentTask(MovePalletTask task) {
        this.task = task;
    }

    public boolean isIdle() {
        return task == null;
    }

    public void load(PalletPosition container, Runnable onComplete) {
        resetAction(onComplete);
        loaded = true;
        container.performPalletOperation(Operation.UNLOADING);
        finishAction();
    }

    public void unload(PalletPosition container, Runnable onComplete) {
        resetAction(onComplete);
        loaded = false;
        container.performPalletOperation(Operation.LOADING);
        finishAction();
    }

    public void moveTo(Node node, Runnable onComplete) {
        resetAction(onComplete);
        moveTo(node, currentVelocity);
    }
	
	public void moveTo(Node position) {
		moveTo(position, currentVelocity);
	}
	
	public double getCurrentVelocity() {
		return currentVelocity;
	}
	
	public double getBaseVelocity() {
		return baseVelocity;
	}
	
	public int getBypassesCount() {
		return (int) bypassesCountStatistics.totalStatistics().getSum();
	}
	
	public List<AgentStatsSlot> getStatsSlots() {
		return statsSlots;
	}
	
	@Override
	public void onDestinationReached(GeometricGraphPosition<Node, Arc> destPosition) {
		super.onDestinationReached(destPosition);
        finishAction();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public AgentStatsSlot getCurrentStatsSlot() {
		return statsSlots.isEmpty() ? null : statsSlots.get(statsSlots.size() - 1);
	}
	
    public void putStatsSlot(IEquipmentState state) {
        var newSlot = new AgentStatsSlot(engine.time(), this, state);
        if(!statsSlots.isEmpty()) {
            var lastSlot = statsSlots.get(statsSlots.size() - 1);
            if(lastSlot.duration() > 0) {
                lastSlot.close();
                statsSlots.add(newSlot);
            } else {
                statsSlots.set(statsSlots.size() - 1, newSlot);
            }
        } else {
            statsSlots.add(newSlot);
        }

        utilization = Utils.zidz(
                statsSlots.stream().filter(slot -> slot.getState().isUtilized()).mapToDouble(slot -> slot.duration()).sum(),
                time());
    }
    
    public double getUtilization() {
        return utilization;
    }
}

