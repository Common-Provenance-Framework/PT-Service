package cz.muni.xmichalk.validity;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import cz.muni.xmichalk.dto.BundleQueryResultDTO;
import cz.muni.xmichalk.models.ItemToTraverse;
import cz.muni.xmichalk.provServiceAPI.IProvServiceAPI;

import java.io.InputStream;

public class DemoValidityVerifier implements IValidityVerifier {
    private final IProvServiceAPI provServiceAPI;
    private final JsonNode validitySpecification;
    private final String authorizationHeader;

    public DemoValidityVerifier(
            IProvServiceAPI provServiceAPI,
            InputStream validitySpecificationJson,
            String authorizationHeader
    ) {
        this.provServiceAPI = provServiceAPI;
        this.authorizationHeader = authorizationHeader;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            validitySpecification = objectMapper.readTree(validitySpecificationJson);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

    }


    @Override public boolean verify(ItemToTraverse itemToTraverse, BundleQueryResultDTO queryResult) {
        String provServiceUri = itemToTraverse.provServiceUri;

        BundleQueryResultDTO result = provServiceAPI.fetchBundleQueryResult(
                provServiceUri,
                itemToTraverse.bundleId,
                itemToTraverse.connectorId,
                authorizationHeader,
                validitySpecification
        );

        if (result == null) {
            throw new RuntimeException(
                    "Fetch TEST_FITS result for bundle: " + itemToTraverse.bundleId.getUri() + " returned null.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(result.result, Boolean.class);
    }
}
