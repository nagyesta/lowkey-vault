package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsSecrets;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

@LaunchAbortArmed
@SpringBootTest
class Base64ZipSecretSerializerDeserializerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNull() {
        //given

        //when
        final var json = objectMapper.writerFor(SecretBackupModel.class).writeValueAsString(null);
        final var actual = objectMapper.readerFor(SecretBackupModel.class).readValue(json);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNullList() {
        //given
        final var valueWithNullList = new SecretBackupModel();

        //when
        final var json = objectMapper.writer().writeValueAsString(valueWithNullList);
        final var actual = objectMapper.readerFor(SecretBackupModel.class).readValue(json);

        //then
        Assertions.assertEquals(valueWithNullList, actual);
    }

    @Test
    void testSerializeShouldConvertContentWhenCalledWithValidValue() {
        //given
        final var item = getSecretBackupListItem(TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1,
                LOWKEY_VAULT, MimeTypeUtils.TEXT_PLAIN_VALUE,
                getSecretPropertiesModel());
        final var input = getSecretBackupModel(item);

        //when
        final var json = objectMapper.writer().writeValueAsString(input);
        final var actual = objectMapper.readerFor(SecretBackupModel.class).readValue(json);

        //then
        Assertions.assertEquals(input, actual);
    }

    private SecretBackupModel getSecretBackupModel(final SecretBackupListItem item) {
        final var list = new SecretBackupList();
        list.setVersions(List.of(item));
        final var input = new SecretBackupModel();
        input.setValue(list);
        return input;
    }

    @SuppressWarnings("SameParameterValue")
    private SecretBackupListItem getSecretBackupListItem(
            final SecretEntityId id,
            final String value,
            final String contentType,
            final SecretPropertiesModel propertiesModel) {
        final var item = new SecretBackupListItem();
        item.setId(id.id());
        item.setVaultBaseUri(id.vault());
        item.setVersion(id.version());
        item.setValue(value);
        item.setContentType(contentType);
        item.setManaged(true);
        item.setTags(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2));
        item.setAttributes(propertiesModel);
        return item;
    }

    private SecretPropertiesModel getSecretPropertiesModel() {
        final var propertiesModel = new SecretPropertiesModel();
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
