package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import lombok.NonNull;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.core.convert.converter.Converter;

public class EcPrivateKeyToJsonWebKeyImportRequestConverter
        implements Converter<BCECPrivateKey, JsonWebKeyImportRequest> {

    @Override
    public JsonWebKeyImportRequest convert(final @NonNull BCECPrivateKey source) {
        final var importRequest = new JsonWebKeyImportRequest();
        importRequest.setKeyType(KeyType.EC);
        final var sourceAlgorithm = ((ECNamedCurveParameterSpec) source.getParameters()).getName();
        importRequest.setCurveName(KeyCurveName.forAlg(sourceAlgorithm));
        importRequest.setD(source.getD().toByteArray());
        final var point = source.getParameters().getG().multiply(source.getD()).normalize();
        importRequest.setX(point.getAffineXCoord().getEncoded());
        importRequest.setY(point.getAffineYCoord().getEncoded());
        return importRequest;
    }
}
