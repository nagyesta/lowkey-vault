package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.TestConstantsSecrets;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

@SpringBootTest
class Base64ZipSecretSerializerDeserializerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNull() throws IOException {
        //given

        //when
        final String json = objectMapper.writerFor(SecretBackupModel.class).writeValueAsString(null);
        final SecretBackupModel actual = objectMapper.reader().readValue(json, SecretBackupModel.class);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNullList() throws IOException {
        //given
        final SecretBackupModel valueWithNullList = new SecretBackupModel();

        //when
        final String json = objectMapper.writer().writeValueAsString(valueWithNullList);
        final SecretBackupModel actual = objectMapper.reader().readValue(json, SecretBackupModel.class);

        //then
        Assertions.assertEquals(valueWithNullList, actual);
    }

    @Test
    void testSerializeShouldConvertContentWhenCalledWithValidValue() throws IOException {
        //given
        final SecretBackupListItem item = getSecretBackupListItem(TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1,
                LOWKEY_VAULT, MimeTypeUtils.TEXT_PLAIN_VALUE,
                getSecretPropertiesModel());
        final SecretBackupModel input = getSecretBackupModel(item);

        //when
        final String json = objectMapper.writer().writeValueAsString(input);
        final SecretBackupModel actual = objectMapper.reader().readValue(json, SecretBackupModel.class);

        //then
        Assertions.assertEquals(input, actual);
    }

    private SecretBackupModel getSecretBackupModel(final SecretBackupListItem item) {
        final SecretBackupList list = new SecretBackupList();
        list.setVersions(List.of(item));
        final SecretBackupModel input = new SecretBackupModel();
        input.setValue(list);
        return input;
    }

    @SuppressWarnings("SameParameterValue")
    private SecretBackupListItem getSecretBackupListItem(final SecretEntityId id,
                                                         final String value,
                                                         final String contentType,
                                                         final SecretPropertiesModel propertiesModel) {
        final SecretBackupListItem item = new SecretBackupListItem();
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
        final SecretPropertiesModel propertiesModel = new SecretPropertiesModel();
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
