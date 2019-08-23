package com.vikson.projects.model.values;

import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Prototype of Auto Desk Integration with docu tools.
 * @author vikram
 */
@Embeddable
public class AutoDeskConfig implements Serializable {

    @Column(name = "auto_desk_enabled", nullable = false)
    private boolean enabled = false;

    @PersistenceConstructor
    public AutoDeskConfig() {
    }

    public AutoDeskConfig(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoDeskConfig that = (AutoDeskConfig) o;
        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    @Override
    public String toString() {
        return "AutoDeskConfig{" +
                "enabled=" + enabled +
                '}';
    }
}
