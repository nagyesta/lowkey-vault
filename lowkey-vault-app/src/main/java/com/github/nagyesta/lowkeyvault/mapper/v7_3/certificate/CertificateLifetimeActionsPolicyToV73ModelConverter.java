package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.ApiVersionAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionTriggerModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class CertificateLifetimeActionsPolicyToV73ModelConverter
        implements ApiVersionAwareConverter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>> {

    private final CertificateConverterRegistry registry;

    @Autowired
    public CertificateLifetimeActionsPolicyToV73ModelConverter(@NonNull final CertificateConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerLifetimeActionConverter(this);
    }

    @Override
    public List<CertificateLifetimeActionModel> convert(@NonNull final LifetimeActionPolicy source) {
        return source.getLifetimeActions().entrySet().stream()
                .map(this::convertLifetimeAction)
                .collect(Collectors.toList());
    }

    private CertificateLifetimeActionModel convertLifetimeAction(
            final Map.Entry<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> e) {
        final CertificateLifetimeActionModel actionModel = new CertificateLifetimeActionModel();
        actionModel.setAction(e.getKey());
        actionModel.setTrigger(new CertificateLifetimeActionTriggerModel(e.getValue()));
        return actionModel;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3;
    }
}
