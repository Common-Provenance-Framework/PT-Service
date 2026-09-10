package cz.muni.xmichalk.dto;

import cz.muni.xmichalk.traversalPriority.ETraversalPriority;
import cz.muni.xmichalk.validity.EValidityCheck;
import tools.jackson.databind.JsonNode;

import java.util.List;

public class TraversalParamsDTO {
    public QualifiedNameDTO bundleId;
    public QualifiedNameDTO startNodeId;
    public String versionPreference;
    public ETraversalPriority traversalPriority;
    public List<EValidityCheck> validityChecks;
    public JsonNode querySpecification;

    public TraversalParamsDTO() {

    }

    public TraversalParamsDTO(
            QualifiedNameDTO bundleId,
            QualifiedNameDTO connectorId,
            String versionPreference,
            ETraversalPriority traversalPriority,
            List<EValidityCheck> validityChecks,
            JsonNode querySpecification
    ) {
        this.bundleId = bundleId;
        this.startNodeId = connectorId;
        this.versionPreference = versionPreference;
        this.traversalPriority = traversalPriority;
        this.validityChecks = validityChecks;
        this.querySpecification = querySpecification;
    }
}
