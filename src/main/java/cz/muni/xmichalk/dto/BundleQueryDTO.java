package cz.muni.xmichalk.dto;

import org.openprovenance.prov.model.QualifiedName;

import tools.jackson.databind.JsonNode;

public class BundleQueryDTO {
    public QualifiedNameDTO bundleId;
    public QualifiedNameDTO startNodeId;
    public JsonNode querySpecification;

    public BundleQueryDTO() {

    }

    public BundleQueryDTO(QualifiedName bundleId, QualifiedName connectorId, JsonNode querySpecification) {
        this.bundleId = new QualifiedNameDTO().from(bundleId);
        this.startNodeId = new QualifiedNameDTO().from(connectorId);
        this.querySpecification = querySpecification;
    }
}
