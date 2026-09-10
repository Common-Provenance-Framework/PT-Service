package cz.muni.xmichalk.dto;

import cz.muni.xmichalk.dto.token.Token;
import tools.jackson.databind.JsonNode;

public class BundleQueryResultDTO {
    public Token token;
    public JsonNode result;

    public BundleQueryResultDTO() {
    }

    public BundleQueryResultDTO(Token token, JsonNode result) {
        this.token = token;
        this.result = result;
    }
}