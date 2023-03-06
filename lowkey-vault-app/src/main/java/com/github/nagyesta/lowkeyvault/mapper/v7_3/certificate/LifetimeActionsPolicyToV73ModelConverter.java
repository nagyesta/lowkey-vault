package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionTriggerModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LifetimeActionsPolicyToV73ModelConverter implements Converter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>> {

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
}
