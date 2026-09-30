package com.sakura.runtime;

/**
 * Marker interface for services accepting a ScreenRendererInstance. Allows callers to propagate
 * renderer type-safely without reflection.
 */
public interface ScreenRendererAware {
    void setRenderer(ScreenRendererInstance renderer);
}
