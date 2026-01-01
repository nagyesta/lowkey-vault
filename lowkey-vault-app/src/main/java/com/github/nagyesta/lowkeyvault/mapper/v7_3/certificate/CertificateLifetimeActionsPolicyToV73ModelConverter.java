package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionTriggerModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CertificateLifetimeActionsPolicyToV73ModelConverter
        implements BaseConverter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>> {

    @Override
    public List<CertificateLifetimeActionModel> convert(@Nullable final LifetimeActionPolicy source) {
        if (source == null) {
            return List.of();
        }
        return source.getLifetimeActions().entrySet().stream()
                .map(this::convertLifetimeAction)
                .toList();
    }

    private CertificateLifetimeActionModel convertLifetimeAction(
            final Map.Entry<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> e) {
        final var actionModel = new CertificateLifetimeActionModel();
        actionModel.setAction(e.getKey());
        actionModel.setTrigger(new CertificateLifetimeActionTriggerModel(e.getValue()));
        return actionModel;
    }

}
