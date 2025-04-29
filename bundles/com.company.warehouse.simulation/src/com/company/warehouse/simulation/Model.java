package com.company.warehouse.simulation;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.amalgamasimulation.engine.Engine;
import com.amalgamasimulation.geometry.Point;
import com.amalgamasimulation.geometry.Polyline;
import com.amalgamasimulation.graphagent.GraphEnvironment;
import com.amalgamasimulation.utils.Pair;
import com.amalgamasimulation.utils.Utils;
import com.company.warehouse.datamodel.Direction;
import com.company.warehouse.datamodel.Scenario;
import com.company.warehouse.simulation.PalletPosition.Operation;
import com.company.warehouse.simulation.graph.Arc;
import com.company.warehouse.simulation.graph.Node;
import com.company.warehouse.simulation.graph.agents.Forklift;
import com.company.warehouse.simulation.tasks.MovePalletTask;

/**
 * This is the class that represents the simulation model itself. In our case,
 * it contains the graph environment and the list of agents.
 */
@SuppressWarnings("serial")
public class Model extends com.amalgamasimulation.engine.Model {
	private Scenario scenario;
	private Mapping mapping = new Mapping();
	
	// Graph environment from Amalgama Platform's Graph Agent Library.
	// The container class for the graph and agents living in this graph.
	protected GraphEnvironment<Node, Arc, Forklift> graphEnvironment;
	private Random random = new Random(0);
	private double endTime;
	
    protected List<PalletPosition> mainStoragePalletPositions = new ArrayList<>();
    protected Map<Direction, List<DockArea>> dockAreas = new HashMap<>();
    protected Map<Direction, List<PalletPosition>> palletPositionsAtDockAreas = new HashMap<>();
    
    // pallet positions that are used in currently running tasks
    private Set<PalletPosition> palletPositionsInTasks = new HashSet<>();
    
    private int receiptAttempts = 0;
    private int successfulReceipts = 0;
    private int shipmentAttempts = 0;
    private int successfulShipments = 0;
	
	/**
	 * This constructor takes a data model's scenario as its second argument and
	 * performs the initialization of the simulation model. During initialization,
	 * it creates a mapping between data model entities and simulation entities.
	 * This mapping is stored inside this class and can be retrieved with
	 * getMapping() method.
	 */
	public Model(Engine engine, Scenario scenario) {
		super(engine);
		this.scenario = scenario;
		engine().setTemporal(scenario.getBeginDate(), ChronoUnit.HOURS);
		setEndTime(dateToTime(scenario.getEndDate()));
		
		// Create a new empty graph environment upon creation of the model
		graphEnvironment = new GraphEnvironment<>();
		// Schedule the dispatch event for the simulation time 0 for all the agents.
		// Note this this event will be executed when the engine running the model starts
		engine.scheduleRelative(0, () -> getAgents().forEach(a -> dispatchAgent(a)));
		initializeNodes();
		initializeArcs();
		initializeAgents();
		initializeMainStorage();
		initializeDockAreas();
		engine.scheduleRelative(0, this::attemptReceipt);
	    engine.scheduleRelative(0, this::attemptShipment);
	}
	
	/**
	 * This is the method that is called every time an agent reaches its
	 * destination. It selects a random node reachable from the one
	 * it is currently in, and sends the agent to this node.
	 * 
	 */
	private void dispatchAgent(Forklift forklift) {
        if (tryDispatchAgent(forklift, palletPositionsAtDockAreas.get(Direction.IN), mainStoragePalletPositions)) {
            return;
        }
        if (tryDispatchAgent(forklift, mainStoragePalletPositions, palletPositionsAtDockAreas.get(Direction.OUT))) {
            return;
        }
        if (tryDispatchAgent(forklift, palletPositionsAtDockAreas.get(Direction.IN), palletPositionsAtDockAreas.get(Direction.OUT))) {
            return;
        }
    }
	
	private boolean tryDispatchAgent(Forklift forklift, List<PalletPosition> possibleSources, List<PalletPosition> possibleDestinations) {
        possibleSources = possibleSources   .stream()
                                            .filter(pp -> !palletPositionsInTasks.contains(pp))
                                            .filter(pp -> pp.isAvailableFor(Operation.UNLOADING))
                                            .toList();
        possibleDestinations = possibleDestinations .stream()
                                                    .filter(pp -> !palletPositionsInTasks.contains(pp))
                                                    .filter(pp -> pp.isAvailableFor(Operation.LOADING))
                                                    .toList();
        if (possibleSources.isEmpty() || possibleDestinations.isEmpty()) {
            return false;
        }
        var from = possibleSources.get(random.nextInt(possibleSources.size()));
        var to = possibleDestinations.get(random.nextInt(possibleDestinations.size()));
        var movingTask = new MovePalletTask(engine(), forklift, from, to);
        // starting the task
        palletPositionsInTasks.add(from);
        palletPositionsInTasks.add(to);
        forklift.setCurrentTask(movingTask);
        movingTask.start(() -> {
            palletPositionsInTasks.remove(from);
            palletPositionsInTasks.remove(to);
            forklift.setCurrentTask(null);
            dispatchIdleForklifts();
        });
        return true;
    }
	
    private void dispatchIdleForklifts() {
        engine().scheduleRelative(0, () -> getAgents()  .stream()
                                                        .filter(Forklift::isIdle)
                                                        .forEach(this::dispatchAgent));
    }
	
	/**
	 * This method sets the simulation end time of the model and schedules the stop
	 * of the engine at this time.
	 */
	public void setEndTime(double endTime) {
		this.endTime = endTime;
		engine().scheduleStop(endTime, "Simulation end");
	}
	
	/**
	 * Returns the mapping between data model entities and simulation entities.
	 */
	public Mapping getMapping() {
		return mapping;
	}
	
	/**
	 * Returns the date model scenario from which this model had been created.
	 */
	public Scenario getScenario() {
		return scenario;
	}
	
	/**
	 * Adds an new agent to the model and performs all the necessary settings. This
	 * method must be used during initialization of the model.
	 */
	public Forklift addAgent(String name, Node baseNode, double velocity) {
		Forklift agent = new Forklift(engine(), name, baseNode, velocity);
		agent.setDestinationArrivalHandler(() -> dispatchAgent(agent));
		agent.setGraphEnvironment(graphEnvironment);
		agent.jumpTo(baseNode);
		return agent;
	}
	
	/**
	 * Adds an new network node to the model and performs all the necessary
	 * settings. This method must be used during initialization of the model.
	 */
	public Node addNode(Point point) {
		Node node = new Node(point);
		graphEnvironment.addNode(node);
		return node;		
	}
	
	/**
	 * Adds an new network pair of arcs (i.e., a single bidirectional arc) to the
	 * model and performs all the necessary settings. This method must be used
	 * during initialization of the model.
	 */
	public Pair<Arc, Arc> addArcs(Node source, Node dest, Polyline forwardPolyline) {
		var result = graphEnvironment.addArc(	source,
												dest,
												new Arc(forwardPolyline),
												new Arc(forwardPolyline.getReversed()));		
		return new Pair<>(result.first.getValue(), result.second.getValue());
	}

	public Random getRandom() {
		return random;
	}
	
	
	public List<Node> getNodes() {
		return graphEnvironment.getNodeValues();
	}

	public List<Arc> getArcs() {
		return graphEnvironment.getArcValues();
	}
	
	public List<Forklift> getAgents() {
		return graphEnvironment	.getAgents()
								.stream()
								.map(Forklift.class::cast)
								.toList();
	}
	
	public double getEndTime() {
		return endTime;
	}
	
	public List<PalletPosition> getAllPositions() {
        return Utils.union(mainStoragePalletPositions, palletPositionsAtDockAreas.get(Direction.IN), palletPositionsAtDockAreas.get(Direction.OUT));
    }
	
    private PalletPosition newPalletPosition(com.company.warehouse.datamodel.Node scenarioNode, boolean busy) {
        var node = mapping.nodesMap.get(scenarioNode);
        var p = new PalletPosition(node);
        if (busy) {
            p.performPalletOperation(Operation.LOADING);
        }
        return p;
    }

    private void initializeMainStorage() {
        scenario.getStoragePlaces()
                .stream()
                .forEach(scenarioNode -> mainStoragePalletPositions.add(newPalletPosition(scenarioNode, randomTrue(0.5))));
    }

    private void initializeDockAreas() {
        for (var direction : Direction.values()) {
            dockAreas.put(direction, new ArrayList<>());
            palletPositionsAtDockAreas.put(direction, new ArrayList<>());
        }
        for (var scenarioDockArea : scenario.getDockAreas()) {
            var dockArea = new DockArea(scenarioDockArea.getDirection());
            var direction = scenarioDockArea.getDirection();
            for (var scenarioNode : scenarioDockArea.getStoragePlaces()) {
                var palletPosition = newPalletPosition(scenarioNode, randomTrue(0.5));
                dockArea.addPalletPosition(palletPosition);
                palletPositionsAtDockAreas.get(direction).add(palletPosition);
            }
            dockAreas.get(direction).add(dockArea);
        }
    }

    private boolean randomTrue(double probability) {
        return random.nextDouble() < probability;
    }
	
	private void initializeNodes() {
		for (var scenarioNode : scenario.getNodes()) {
			Node node = addNode(new Point(scenarioNode.getX(), scenarioNode.getY()));
			mapping.nodesMap.put(scenarioNode, node);
		}
	}
	
	private void initializeAgents() {
		for (var scenarioAgent : scenario.getForklifts()) {
			if (scenarioAgent.isIncluded()) {
				Node basePosition = mapping.nodesMap.get(scenarioAgent.getBasePosition());
				Forklift agent = addAgent("Agent #" + getAgents().size(), basePosition, scenarioAgent.getVelocity());
				mapping.agentsMap.put(scenarioAgent, agent);
			}
		}
	}


	private void initializeArcs() {
		for (var scenarioArc : scenario.getArcs()) {
			Polyline polyline = createPolyline(scenarioArc);
			if (polyline.getLength() != 0) {
				Node sourceNode = mapping.nodesMap.get(scenarioArc.getSource());
				Node destNode = mapping.nodesMap.get(scenarioArc.getDest());
				Pair<Arc, Arc> arcs = addArcs(sourceNode, destNode, polyline);
				mapping.forwardArcsMap.put(scenarioArc, arcs.first);
			}
		}
	}

	private Polyline createPolyline(com.company.warehouse.datamodel.Arc dmArc) {
		List<Point> points = new ArrayList<>();
		points.add(new Point(dmArc.getSource().getX(), dmArc.getSource().getY()));
		dmArc.getPoints().forEach(bendpoint -> points.add(new Point(bendpoint.getX(), bendpoint.getY())));
		points.add(new Point(dmArc.getDest().getX(), dmArc.getDest().getY()));
		return new Polyline(Utils.toList(points.stream().distinct()));
	}
	
	private void attemptReceipt() {
        final int receiptSize = 6;
        final double receiptInterval = 1 * day();
        for (var dockArea : dockAreas.getOrDefault(Direction.IN, List.of())) {
            receiptAttempts++;
            if (dockArea.tryAddCargo(receiptSize)) {
                successfulReceipts++;
                dispatchIdleForklifts();
            }
        }
        engine().scheduleRelative(receiptInterval, this::attemptReceipt);
    }

    private void attemptShipment() {
        final int shipmentSize = 1;
        final double shipmentInterval = 4 * hour();
        for (var dockArea : dockAreas.getOrDefault(Direction.OUT, List.of())) {
            shipmentAttempts++;
            if (dockArea.tryRemoveCargo(shipmentSize)) {
                successfulShipments++;
                dispatchIdleForklifts();
            }
        }
        engine().scheduleRelative(shipmentInterval, this::attemptShipment);
    }

    public int getReceiptAttempts() {
        return receiptAttempts;
    }

    public int getSuccessfulReceipts() {
        return successfulReceipts;
    }

    public int getShipmentAttempts() {
        return shipmentAttempts;
    }

    public int getSuccessfulShipments() {
        return successfulShipments;
    }

    public double getServiceLevel() {
        return Utils.zidz(successfulReceipts + successfulShipments, receiptAttempts + shipmentAttempts);
    }

    public double getUtilization() {
        return getAgents().stream().mapToDouble(agent -> agent.getUtilization()).average().orElse(0.0);
    }
}

