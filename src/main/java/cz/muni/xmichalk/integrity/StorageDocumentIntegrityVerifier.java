package cz.muni.xmichalk.integrity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.SignedJWT;

import cz.muni.xmichalk.dto.token.Token;

public class StorageDocumentIntegrityVerifier implements IIntegrityVerifier {
    public static enum JwtHeaderItems {
        TRUSTED_PARTY_URI("trustedPartyUri");

        private final String label;

        JwtHeaderItems(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static enum JwtPayloadItems {
        HASH_ALGORITHM("hash_alg"),
        DOCUMENT_DIGEST("doc_digest"),
        DOCUMENT_TIMESTAMP("doc_iat"),
        ORGANIZATION_ID("org_id");

        private final String label;

        JwtPayloadItems(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(StorageDocumentIntegrityVerifier.class);

    public boolean verifyIntegrity(QualifiedName document, Token token) {
        return verifySignature(token) && verifyTokenExists(document, token);
    }

    public boolean verifySignature(Token token) {
        try {
            X509Certificate cert = this.getCertificate(token.jwt());
            ECPublicKey publicKey = (ECPublicKey) cert.getPublicKey();
            return SignedJWT.parse(token.jwt()).verify(new ECDSAVerifier(publicKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyTokenExists(QualifiedName document, Token token) {
        String trustedPartyUri = this.getTrustedPartyUri(token.jwt());
        String originatorId = this.getOriginatorId(token.jwt());
        String url = trustedPartyUri + "/api/v1/organizations/" + originatorId + "/tokens";
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Token>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Token>>() {
                });

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Get document token API call failed with status: {}", response.getStatusCode());
            return false;
        }

        if (response.getBody() == null) {
            return false;
        }

        return response.getBody().contains(token);

    }

    public PublicKey loadPublicKeyFromCertificate(String pemCert) throws Exception {
        ByteArrayInputStream certStream = new ByteArrayInputStream(pemCert.getBytes(StandardCharsets.UTF_8));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);
        return cert.getPublicKey();
    }

    public boolean verifyHash(String data, String hashFunctionName, String expectedHexHash) {
        MessageDigest hashFunction = getHashFunction(hashFunctionName);
        byte[] hash = hashString(data, hashFunction);
        String hashHex = bytesToHex(hash);
        return hashHex.equals(expectedHexHash);
    }

    public MessageDigest getHashFunction(String hashFunction) {
        try {
            return MessageDigest.getInstance(hashFunction);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported hash function: " + hashFunction, e);
        }
    }

    public byte[] hashString(String input, MessageDigest digest) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        digest.update(inputBytes);
        return digest.digest();
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private X509Certificate getCertificate(String jwt) {
        try {
            List<Base64> certChain = SignedJWT.parse(jwt).getHeader().getX509CertChain();

            if (certChain == null)
                throw new IllegalArgumentException(
                        "The X.509 certificate chain parameter is not specified in JWT Token.");

            if (certChain.size() == 0)
                throw new IllegalArgumentException(
                        "The X.509 certificate chain parameter is empty in JWT Token.");

            if (certChain.size() != 1)
                throw new IllegalArgumentException(
                        "There is more then one X.509 certificate in certificate chain parameter.");

            return certChain.stream()
                    .map(Base64::decode)
                    .map(ByteArrayInputStream::new)
                    .map(stream -> {
                        try {
                            return (X509Certificate) CertificateFactory
                                    .getInstance("X.509")
                                    .generateCertificate(stream);
                        } catch (CertificateException e) {
                            throw new IllegalArgumentException("Token is not valid JWT Token", e);
                        }
                    })
                    .collect(Collectors.toList())
                    .getFirst();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Token is not valid JWT Token", e);
        }
    }

    private String getTrustedPartyUri(String jwt) {
        try {
            Object uri = SignedJWT
                    .parse(jwt)
                    .getHeader().getCustomParam(JwtHeaderItems.TRUSTED_PARTY_URI.getLabel())
                    .toString();

            if (uri == null)
                throw new IllegalArgumentException("'trustedPartyUri' parameter is missing in JWT Header.");
            return (String) uri;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Token is not valid JWT Token", e);
        }
    }

    private String getOriginatorId(String jwt) {
        try {
            String id = SignedJWT
                    .parse(jwt)
                    .getJWTClaimsSet()
                    .getStringClaim(JwtPayloadItems.ORGANIZATION_ID.getLabel());

            if (id == null)
                throw new IllegalArgumentException("'org_id' parameter is missing in JWT Payload.");
            return id;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Token is not valid JWT Token", e);
        }
    }
}
