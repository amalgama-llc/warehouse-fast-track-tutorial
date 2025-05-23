package com.company.warehouse.application;

import java.awt.Color;
import java.util.Map;

import com.amalgamasimulation.utils.Colors;
import com.company.warehouse.simulation.tasks.IEquipmentState;
import com.company.warehouse.simulation.tasks.MovePalletTask;

public class EquipmentStateUI {

    public static String nameOf(IEquipmentState state) {
        return names.get(state);
    }

    public static Color colorOf(IEquipmentState state) {
        return colors.get(state);
    }

    private static Map<IEquipmentState, String> names = Map.of(
            MovePalletTask.State.PENDING, "-",
            MovePalletTask.State.MOVE_TO_LOADING, "Move to loading",
            MovePalletTask.State.LOADING, "Loading",
            MovePalletTask.State.MOVE_TO_UNLOADING, "Move to unloading",
            MovePalletTask.State.UNLOADING, "Unloading",
            MovePalletTask.State.FINISHED, "-"
            );

    private static Map<IEquipmentState, Color> colors = Map.of(
            MovePalletTask.State.PENDING, Colors.gray,
            MovePalletTask.State.MOVE_TO_LOADING, Colors.orange,
            MovePalletTask.State.LOADING, Colors.blue,
            MovePalletTask.State.MOVE_TO_UNLOADING, Colors.orangeRed,
            MovePalletTask.State.UNLOADING, Colors.green,
            MovePalletTask.State.FINISHED, Colors.gray
            );

}