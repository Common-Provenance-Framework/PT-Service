package cz.muni.xmichalk.dto;

import tools.jackson.databind.JsonNode;

public class BundleQueryResultDTO {
    public String jwt;
    public JsonNode result;

    public BundleQueryResultDTO() {
    }

    public BundleQueryResultDTO(String jwt, JsonNode result) {
        this.jwt = jwt;
        this.result = result;
    }
}