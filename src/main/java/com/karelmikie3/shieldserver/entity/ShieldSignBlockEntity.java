package com.karelmikie3.shieldserver.entity;

public interface ShieldSignBlockEntity {
    enum GateState {
        NOT(0),
        WORKING(1),
        FAILED(2),
        REMOVED(3);

        public final byte id;

        GateState(byte id) {
            this.id = id;
        }

        GateState(int id) {
            this((byte) id);
        }

        public static GateState byId(byte id) {
            for (GateState value : values()) {
                if (value.id == id)
                    return value;
            }

            return null;
        }
    }

    void setGateState(GateState state);
    GateState getGateState();
}
