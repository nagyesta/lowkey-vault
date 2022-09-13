package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

@LaunchAbortArmed
@SpringBootTest
class Base64ZipKeySerializerDeserializerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNull() throws IOException {
        //given

        //when
        final String json = objectMapper.writerFor(KeyBackupModel.class).writeValueAsString(null);
        final KeyBackupModel actual = objectMapper.reader().readValue(json, KeyBackupModel.class);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testSerializeShouldReturnNullWhenCalledWithNullList() throws IOException {
        //given
        final KeyBackupModel valueWithNullList = new KeyBackupModel();

        //when
        final String json = objectMapper.writer().writeValueAsString(valueWithNullList);
        final KeyBackupModel actual = objectMapper.reader().readValue(json, KeyBackupModel.class);

        //then
        Assertions.assertEquals(valueWithNullList, actual);
    }

    @Test
    void testSerializeShouldConvertContentWhenCalledWithValidValue() throws IOException {
        //given
        final KeyBackupListItem item = getKeyBackupListItem(TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1,
                getKeyMaterial(TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1, KeyGenUtil.generateEc(KeyCurveName.P_256)),
                getKeyPropertiesModel());
        final KeyBackupModel input = getKeyBackupModel(item);

        //when
        final String json = objectMapper.writer().writeValueAsString(input);
        final KeyBackupModel actual = objectMapper.reader().readValue(json, KeyBackupModel.class);

        //then
        Assertions.assertEquals(input, actual);
    }

    private KeyBackupModel getKeyBackupModel(final KeyBackupListItem item) {
        final KeyBackupList list = new KeyBackupList();
        list.setVersions(List.of(item));
        final KeyBackupModel input = new KeyBackupModel();
        input.setValue(list);
        return input;
    }

    @SuppressWarnings("SameParameterValue")
    private KeyBackupListItem getKeyBackupListItem(final KeyEntityId id,
                                                   final JsonWebKeyImportRequest keyMaterial,
                                                   final KeyPropertiesModel propertiesModel) {
        final KeyBackupListItem item = new KeyBackupListItem();
        item.setId(id.id());
        item.setVaultBaseUri(id.vault());
        item.setVersion(id.version());
        item.setKeyMaterial(keyMaterial);
        item.setManaged(true);
        item.setTags(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2));
        item.setAttributes(propertiesModel);
        return item;
    }

    private KeyPropertiesModel getKeyPropertiesModel() {
        final KeyPropertiesModel propertiesModel = new KeyPropertiesModel();
        propertiesModel.setCreatedOn(TIME_10_MINUTES_AGO);
        propertiesModel.setUpdatedOn(NOW.minusSeconds(1));
        propertiesModel.setNotBefore(NOW);
        propertiesModel.setExpiresOn(TIME_IN_10_MINUTES);
        propertiesModel.setEnabled(true);
        propertiesModel.setRecoveryLevel(RecoveryLevel.PURGEABLE);
        propertiesModel.setRecoverableDays(null);
        return propertiesModel;
    }

    @SuppressWarnings("SameParameterValue")
    private JsonWebKeyImportRequest getKeyMaterial(final KeyEntityId id, final KeyPair expected) {
        final JsonWebKeyImportRequest keyMaterial = new JsonWebKeyImportRequest();
        keyMaterial.setKeyType(KeyType.EC);
        keyMaterial.setX(((BCECPublicKey) expected.getPublic()).getQ().getAffineXCoord().getEncoded());
        keyMaterial.setY(((BCECPublicKey) expected.getPublic()).getQ().getAffineYCoord().getEncoded());
        keyMaterial.setD(((BCECPrivateKey) expected.getPrivate()).getD().toByteArray());
        keyMaterial.setCurveName(KeyCurveName.P_256);
        keyMaterial.setId(id.asUri(id.vault()).toString());
        return keyMaterial;
    }
}
