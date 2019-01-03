package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CertificateUtils {

    public String extractCommonName(@NonNull X509Certificate x509Certificate) {
        try {
            X500Name x500name = new JcaX509CertificateHolder(x509Certificate).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void validateCertificate(@NonNull String trustStore, @NonNull String trustStorePw, @NonNull X509Certificate x509Certificate, @NonNull X509Certificate... x509Certificates)
            throws FileNotFoundException {
        validateCertificate(ResourceUtils.getFile(trustStore), trustStorePw, x509Certificate, x509Certificates);
    }

    public void validateCertificate(@NonNull File trustStore, @NonNull String trustStorePw, @NonNull X509Certificate x509Certificate, @NonNull X509Certificate... x509Certificates) {
        try {
            InputStream trustStoreInput = new FileInputStream(trustStore);
            char[] password = trustStorePw.toCharArray();
            List<X509Certificate> chain = Lists.asList(x509Certificate, x509Certificates);
            Collection<X509CRL> crls = Lists.newArrayList();

            /* Construct a valid path. */
            KeyStore anchors = KeyStore.getInstance(KeyStore.getDefaultType());
            anchors.load(trustStoreInput, password);
            X509CertSelector target = new X509CertSelector();
            target.setCertificate(x509Certificate);
            PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, target);
            CertStoreParameters intermediates = new CollectionCertStoreParameters(chain);
            params.addCertStore(CertStore.getInstance("Collection", intermediates));
            // TODO: enable revocation later
            params.setRevocationEnabled(false);
            CertStoreParameters revoked = new CollectionCertStoreParameters(crls);
            params.addCertStore(CertStore.getInstance("Collection", revoked));
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            /*
             * If build() returns successfully, the certificate is valid. More details
             * about the valid path can be obtained through the PKIXBuilderResult.
             * If no valid path can be found, a CertPathBuilderException is thrown.
             */
            builder.build(params);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
