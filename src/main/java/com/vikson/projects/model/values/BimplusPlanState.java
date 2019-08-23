package com.vikson.projects.model.values;

public enum BimplusPlanState {

    /**
     * Bimplus attachment is linked to a plan in vikson
     */
    LINKED,
    /**
     * There was a problem with linking the attachment from bimplus to the plan
     */
    ERROR,
    /**
     * Bimplus attachment got unlinked from the plan
     */
    UNLINKED,
    /**
     * Plan is not linked to any bimplus attachment
     */
    NOT_LINKED
}
