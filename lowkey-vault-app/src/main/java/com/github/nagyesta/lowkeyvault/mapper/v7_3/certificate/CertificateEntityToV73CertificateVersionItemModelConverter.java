package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CertificateEntityToV73CertificateVersionItemModelConverter
        implements RecoveryAwareConverter<ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel> {
    @Override
    public KeyVaultCertificateItemModel convert(
            final ReadOnlyKeyVaultCertificateEntity source, final @NonNull URI vaultUri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull DeletedKeyVaultCertificateItemModel convertDeleted(
            final @NonNull ReadOnlyKeyVaultCertificateEntity source, final @NonNull URI vaultUri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
