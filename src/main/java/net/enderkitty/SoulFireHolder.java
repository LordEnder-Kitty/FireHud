package net.enderkitty;

/**
 * Whether something is burning with soul fire rather than normal fire. Implemented on both {@code Entity} and
 * {@code EntityRenderState} so the flag reads the same on either side of render state extraction.
 */
public interface SoulFireHolder {
    boolean fireHud$isOnSoulFire();
    void fireHud$setOnSoulFire(boolean onSoulFire);
}
