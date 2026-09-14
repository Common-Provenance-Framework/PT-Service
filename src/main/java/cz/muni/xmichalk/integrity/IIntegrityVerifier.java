package cz.muni.xmichalk.integrity;

import org.openprovenance.prov.model.QualifiedName;

public interface IIntegrityVerifier {
    boolean verifyIntegrity(QualifiedName document, String token);
}
