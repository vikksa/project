package com.vikson.projects.model.values;

/**
 * The state of an {@link BimplusProjectConfig}.
 */
public enum BimplusProjectState {
    /**
     * Indicates that the project is linked to a bimplus project
     */
    LINKED,
    /**
     * Indicates that the project got unlinked from the bimplus project
     */
    UNLINKED,
    /**
     * This project is not linked at all to a bimplus project
     */
    NOT_LINKED
}
