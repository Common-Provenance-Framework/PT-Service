package cz.muni.xmichalk.DocumentLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.xmichalk.DocumentLoader.StorageDTO.GetDocumentResponse;
import cz.muni.xmichalk.DocumentLoader.StorageDTO.GetMetaResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static cz.muni.xmichalk.Util.ProvDocumentUtils.deserialize;
import static cz.muni.xmichalk.Util.ProvDocumentUtils.prepareForDeserialization;

public class StorageDocumentLoader implements IDocumentLoader {
    private static final Formats.ProvFormat FORMAT = Formats.ProvFormat.JSON;
    private static final String FORMAT_QUERY_PARAM = "format=json";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public DocumentWithIntegrity loadDocument(String uri) {
        try {
            uri += (uri.contains("?") ? "&" : "?") + FORMAT_QUERY_PARAM;
            String responseBody = getRequest(uri);
            ObjectMapper mapper = new ObjectMapper();
            GetDocumentResponse storageResponse = mapper.readValue(responseBody, GetDocumentResponse.class);
            String decodedDocument = decodeData(storageResponse.document);
            Document document = deserialize(prepareForDeserialization(decodedDocument, FORMAT), FORMAT);
            boolean integrity = StorageDocumentIntegrityVerifier.verifyIntegrity(decodedDocument, storageResponse.token);
            return new DocumentWithIntegrity(document, integrity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve document " + uri, e);
        }
    }

    @Override
    public DocumentWithIntegrity loadMetaDocument(String uri) {
        try {
            uri += (uri.contains("?") ? "&" : "?") + FORMAT_QUERY_PARAM;
            String responseBody = getRequest(uri);
            ObjectMapper mapper = new ObjectMapper();
            GetMetaResponse storageResponse = mapper.readValue(responseBody, GetMetaResponse.class);
            String decodedDocument = decodeData(storageResponse.graph);
            Document document = deserialize(prepareForDeserialization(decodedDocument, FORMAT), FORMAT);
            boolean integrity = StorageDocumentIntegrityVerifier.verifyIntegrity(decodedDocument, storageResponse.token);
            return new DocumentWithIntegrity(document, integrity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve document " + uri, e);
        }
    }

    private static String getRequest(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Unexpected response code: " + statusCode);
                }
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    private static String decodeData(String base64Data) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return new String(decodedBytes, CHARSET);
    }
}
