package com.company.warehouse.simulation;

import java.util.ArrayList;
import java.util.List;

import com.company.warehouse.datamodel.Direction;

public class DockArea {

    /**
     * Whether this dock area is incoming (for unloading) or outgoing (for loading).
     */
    private final Direction direction;
    private List<PalletPosition> palletPositions = new ArrayList<>();

    public DockArea(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void addPalletPosition(PalletPosition palletPosition) {
        palletPositions.add(palletPosition);
    }
    
    public boolean tryAddCargo(int palletsToAdd) {
        return tryPerformPalletOperation(palletsToAdd, PalletPosition.Operation.LOADING);
    }

    public boolean tryRemoveCargo(int palletsToRemove) {
        return tryPerformPalletOperation(palletsToRemove, PalletPosition.Operation.UNLOADING);
    }

    private boolean tryPerformPalletOperation(int palletsCount, PalletPosition.Operation operation) {
        var emptyPalletPositions = palletPositions.stream().filter(pp -> pp.isAvailableFor(operation)).toList();
        if (emptyPalletPositions.size() < palletsCount) {
            return false;
        }
        for (int i = 0; i < palletsCount; i++) {
            emptyPalletPositions.get(i).performPalletOperation(operation);
        }
        return true;
    }
}