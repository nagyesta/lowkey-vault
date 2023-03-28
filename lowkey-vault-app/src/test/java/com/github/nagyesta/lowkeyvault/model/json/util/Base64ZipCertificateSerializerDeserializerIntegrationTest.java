package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsCertificates;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

@LaunchAbortArmed
@SpringBootTest
class Base64ZipCertificateSerializerDeserializerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNull() throws IOException {
        //given

        //when
        final String json = objectMapper.writerFor(CertificateBackupModel.class).writeValueAsString(null);
        final CertificateBackupModel actual = objectMapper.reader().readValue(json, CertificateBackupModel.class);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNullList() throws IOException {
        //given
        final CertificateBackupModel valueWithNullList = new CertificateBackupModel();

        //when
        final String json = objectMapper.writer().writeValueAsString(valueWithNullList);
        final CertificateBackupModel actual = objectMapper.reader().readValue(json, CertificateBackupModel.class);

        //then
        Assertions.assertEquals(valueWithNullList, actual);
    }

    @Test
    void testSerializeShouldConvertContentWhenCalledWithValidValue() throws IOException {
        //given
        final CertificateBackupListItem item = getCertificateBackupListItem(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_2,
                LOWKEY_VAULT, getCertificatePropertiesModel());
        final CertificateBackupModel input = getCertificateBackupModel(item);

        //when
        final String json = objectMapper.writer().writeValueAsString(input);
        final CertificateBackupModel actual = objectMapper.reader().readValue(json, CertificateBackupModel.class);

        //then
        Assertions.assertEquals(input, actual);
    }

    private CertificateBackupModel getCertificateBackupModel(final CertificateBackupListItem item) {
        final CertificateBackupList list = new CertificateBackupList();
        list.setVersions(List.of(item));
        final CertificateBackupModel input = new CertificateBackupModel();
        input.setValue(list);
        return input;
    }

    @SuppressWarnings("SameParameterValue")
    private CertificateBackupListItem getCertificateBackupListItem(final CertificateEntityId id,
                                                                   final String value,
                                                                   final CertificatePropertiesModel propertiesModel) {
        final CertificateBackupListItem item = new CertificateBackupListItem();
        item.setId(id.id());
        item.setVaultBaseUri(id.vault());
        item.setVersion(id.version());
        item.setCertificate(value.getBytes());
        item.setManaged(true);
        item.setTags(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2));
        item.setAttributes(propertiesModel);
        return item;
    }

    private CertificatePropertiesModel getCertificatePropertiesModel() {
        final CertificatePropertiesModel propertiesModel = new CertificatePropertiesModel();
        propertiesModel.setCreatedOn(TIME_10_MINUTES_AGO);
        propertiesModel.setUpdatedOn(NOW.minusSeconds(1));
        propertiesModel.setNotBefore(NOW);
        propertiesModel.setExpiresOn(TIME_IN_10_MINUTES);
        propertiesModel.setEnabled(true);
        propertiesModel.setRecoveryLevel(RecoveryLevel.PURGEABLE);
        propertiesModel.setRecoverableDays(null);
        return propertiesModel;
    }
}
