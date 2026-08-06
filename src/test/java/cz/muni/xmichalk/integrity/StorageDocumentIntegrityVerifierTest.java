package cz.muni.xmichalk.integrity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;

import cz.muni.xmichalk.dto.token.Token;

public class StorageDocumentIntegrityVerifierTest {
  private final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6InRydXN0ZWQtcGFydHk6ODAyMCIsIng1YyI6WyJNSUlDTWpDQ0FkaWdBd0lCQWdJVVNMajVZN1BYSVMxM3FQRVBEZGxJTkJuUXpvZ3dDZ1lJS29aSXpqMEVBd0l3YlRFTE1Ba0dBMVVFQmhNQ1JWVXhPakE0QmdOVkJBb01NVVJwYzNSeWFXSjFkR1ZrSUZCeWIzWmxibUZ1WTJVZ1JHVnRieUJEWlhKMGFXWnBZMkYwWlNCQmRYUm9iM0pwZEhreElqQWdCZ05WQkFNTUdVUlFSQ0JEWlhKMGFXWnBZMkYwWlNCQmRYUm9iM0pwZEhrd0hoY05NalF4TVRFMk1ESTFPVFV5V2hjTk16UXhNVEUwTURJMU9UVXlXakJkTVFzd0NRWURWUVFHRXdKRFdqRXlNREFHQTFVRUNnd3BSR2x6ZEhKcFluVjBaV1FnVUhKdmRtVnVZVzVqWlNCRVpXMXZJRlJ5ZFhOMFpXUWdVR0Z5ZEhreEdqQVlCZ05WQkFNTUVVUlFSQ0JVY25WemRHVmtJRkJoY25SNU1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRStWOGtUNGprdkVXbVgzMDFLQVM5ZWtsbW5STmk2Z1U5K0tIeHVRcGtTT2hNVHE5NkNCWEZwZm9rUmQ3dDVWZHJSeTB1cVpzeVNOcDVrVzBoblFNSldhTm1NR1F3RWdZRFZSMFRBUUgvQkFnd0JnRUIvd0lCQURBT0JnTlZIUThCQWY4RUJBTUNBWVl3SFFZRFZSME9CQllFRk1DblBSamlYb2tUN3F1d1pSQjE2QUFnejdibk1COEdBMVVkSXdRWU1CYUFGQ3lFS3dpMWp2ZFBxZmlVK05kSC9udmg3UFlaTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDeVpyVVNoVnFyb2hEcWR6ZE9GbUF5RkRwd01BTzhJNmphaHZnMUZSQVpZZ0lnVmg0UzJ0UW4xMlhZZGQ1SVNzQ3BBQnNoNlpyalNpVllydDJUMU8xblFzdz0iXX0.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy8xNmQzNmUxMC02MmUwLTQ5ZjctYWY2Mi1iNGVjNTg5Y2ZhMjgiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI5MjViZTFkYWQ2MzgyNDc1ODZlNzNmZWUyNDA2ZThiMDM5MWM2MTIwYTE4MTQxZjY3MThjNDdmYzNkMTc1OGUwIiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzg1ODU4NTI3LCJkb2NfaWF0IjoxNzg1ODU4NTI3fQ.zLNng8xokNjaYqZ9iRZc9DUunw7HhPRQ2Qwm4_voXXyd5-ptuli_Iu6RYrRyMbVkgfhR-kwHoL3tJx9c3bggWg";

  @Test
  void verifySignature_validJWT_returnsTrue() {
    boolean isValidSignature = new StorageDocumentIntegrityVerifier().verifySignature(new Token(JWT));

    assertEquals(true, isValidSignature,
        "should have valid signature");
  }

  @Test
  void getCertChain_validJWT_returnsCertChain() throws Exception {
    StorageDocumentIntegrityVerifier verifier = new StorageDocumentIntegrityVerifier();

    Method method = StorageDocumentIntegrityVerifier.class
        .getDeclaredMethod("getCertificate", String.class);
    method.setAccessible(true);
    X509Certificate cert = (X509Certificate) method.invoke(verifier, JWT);

    assertEquals("X.509", cert.getType(),
        "should have exact type");
    assertEquals("SHA256withECDSA", cert.getSigAlgName(),
        "should have exact  signature algorithm");
  }

  @Test
  void getTrustedPartyUri_validJWT_returnsTrustedPartyUri() throws Exception {
    StorageDocumentIntegrityVerifier verifier = new StorageDocumentIntegrityVerifier();

    Method method = StorageDocumentIntegrityVerifier.class
        .getDeclaredMethod("getTrustedPartyUri", String.class);
    method.setAccessible(true);

    String tpUri = (String) method.invoke(verifier, JWT);

    assertEquals("trusted-party:8020", tpUri,
        "should have exact TrustedParty (NRO) uri in header");
  }

  @Test
  void getOriginatorId_validJWT_returnsOriginatorId() throws Exception {
    StorageDocumentIntegrityVerifier verifier = new StorageDocumentIntegrityVerifier();

    Method method = StorageDocumentIntegrityVerifier.class
        .getDeclaredMethod("getOriginatorId", String.class);
    method.setAccessible(true);

    String tpUri = (String) method.invoke(verifier, JWT);

    assertEquals("6fb292aa-ee38-48ae-998f-079ad9d01e7c", tpUri,
        "should have exact org_id (originator) in payload");
  }
}
