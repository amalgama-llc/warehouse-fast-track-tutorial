package com.company.warehouse.simulation;

import com.company.warehouse.simulation.graph.Node;

/**
 * A location in the main storage area or in a dock area that can contain at max one pallet.
 */
public class PalletPosition {

    public enum Operation {
        // Pallet is moved into the container
        LOADING,
        // Pallet is moved out of the container
        UNLOADING}

    private final Node node;

    /**
     * Whether a pallet is present here
     */
    private boolean busy;

    public PalletPosition(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isAvailableFor(Operation operation) {
        return (busy && operation == Operation.UNLOADING) || (!busy && operation == Operation.LOADING);
    }

    public void performPalletOperation(Operation operation) {
        if (!isAvailableFor(operation)) {
            throw new IllegalStateException("Cannot " + operation);
        }
        busy = !busy;
    }
}