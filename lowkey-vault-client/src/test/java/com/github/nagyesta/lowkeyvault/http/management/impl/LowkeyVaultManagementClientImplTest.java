package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.azure.core.http.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import com.github.nagyesta.lowkeyvault.http.management.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.http.management.TimeShiftContext;
import com.github.nagyesta.lowkeyvault.http.management.VaultModel;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.nagyesta.lowkeyvault.http.management.impl.ResponseEntity.VAULT_MODEL_LIST_TYPE_REF;
import static org.mockito.Mockito.*;

class LowkeyVaultManagementClientImplTest {

    private static final String SIMPLE_JSON = "{\"property\":42}";
    private static final String SIMPLE_JSON_PRETTY = "{\n\t\"property\": 42\n}";
    private static final String HTTPS_LOCALHOST = "https://localhost";
    private static final String HTTPS_ALIAS_LOCALHOST = "https://alias.localhost";
    private static final String JSON = "{}";
    private static final int RECOVERABLE_DAYS = 90;

    public static Stream<Arguments> nullCreateProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(URI.create(HTTPS_LOCALHOST), null, null))
                .add(Arguments.of(null, RecoveryLevel.PURGEABLE, null))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, mock(HttpClient.class), mock(ObjectMapper.class)))
                .add(Arguments.of(HTTPS_LOCALHOST, null, mock(ObjectMapper.class)))
                .add(Arguments.of(HTTPS_LOCALHOST, mock(HttpClient.class), null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final String baseUri, final HttpClient httpClient, final ObjectMapper objectMapper) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LowkeyVaultManagementClientImpl(baseUri, httpClient, objectMapper));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("nullCreateProvider")
    void testCreateVaultShouldThrowExceptionWhenCalledWithNull(
            final URI baseUri, final RecoveryLevel recoveryLevel, final Integer recoverableDays) {
        //given
        final var httpClient = mock(HttpClient.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest =
                new LowkeyVaultManagementClientImpl(HTTPS_LOCALHOST, httpClient, objectMapper);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createVault(baseUri, recoveryLevel, recoverableDays));

        //then + exception
    }

    @Nested
    class FunctionalTest {

        @Mock
        private ObjectReader objectReader;
        @Mock
        private ObjectWriter objectWriter;
        @Mock
        private ObjectMapper objectMapper;
        @Mock
        private HttpClient httpClient;
        @Captor
        private ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor;
        private LowkeyVaultManagementClientImpl underTest;
        private AutoCloseable openMocks;

        @BeforeEach
        void setUp() {
            openMocks = MockitoAnnotations.openMocks(this);
            when(objectMapper.reader()).thenReturn(objectReader);
            when(objectMapper.writer()).thenReturn(objectWriter);
            underTest = new LowkeyVaultManagementClientImpl(HTTPS_LOCALHOST, httpClient, objectMapper);
        }

        @AfterEach
        void tearDown() throws Exception {
            verify(objectMapper).reader();
            verify(objectMapper).writer();
            openMocks.close();
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testVerifyConnectivityShouldThrowExceptionWhenExceptionSupplierIsNull() {
            //given
            final var retries = 2;
            final var waitMillis = 1;
            final Supplier<RuntimeException> exceptionProvider = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> underTest.verifyConnectivity(retries, waitMillis, exceptionProvider));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testVerifyConnectivityShouldKeepRetryingUntilTheLimitIsReachedWhenContainerIsNotRunning() {
            //given
            final var retries = 2;
            final var waitMillis = 1;
            when(httpClient.send(any())).thenThrow(LowkeyVaultException.class);

            //when
            Assertions.assertThrows(IllegalStateException.class,
                    () -> underTest.verifyConnectivity(retries, waitMillis, IllegalStateException::new));

            //then
            verify(httpClient, times(retries)).send(ArgumentMatchers.argThat(argument -> argument.getUrl().getPath().equals("/ping")));
        }

        @Test
        void testVerifyConnectivityShouldReturnAfterFirstSuccessWhenContainerIsStarted() throws InterruptedException {
            //given
            final var retries = 2;
            final var waitMillis = 1;
            final var response = mock(HttpResponse.class);
            when(httpClient.send(any())).thenReturn(Mono.just(response));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.empty());

            //when
            underTest.verifyConnectivity(retries, waitMillis, IllegalStateException::new);

            //then
            verify(httpClient, atMostOnce()).send(ArgumentMatchers.argThat(argument -> argument.getUrl().getPath().equals("/ping")));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
        }

        @Test
        void testCreateVaultShouldTransformInputToModelWhenCalledWithValidInput() throws JsonProcessingException {
            //given
            final var baseUri = URI.create(HTTPS_LOCALHOST);
            final var recoveryLevel = RecoveryLevel.RECOVERABLE;
            final Integer recoverableDays = RECOVERABLE_DAYS;
            final var result = new VaultModel();
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VaultModel.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(result);
            final var expectedRequestVault = new VaultModel(baseUri, null, recoveryLevel, recoverableDays, null, null);
            when(objectWriter.writeValueAsString(expectedRequestVault)).thenReturn(JSON);

            //when
            final var actual = underTest.createVault(baseUri, recoveryLevel, recoverableDays);

            //then
            Assertions.assertEquals(result, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            final var actualBody = new String(Objects.requireNonNull(request.getBody().single().block()).array());
            Assertions.assertEquals(JSON, actualBody);
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VaultModel.class);
            verify(objectReader).readValue(anyString());
            verify(objectWriter).writeValueAsString(expectedRequestVault);
        }

        @Test
        void testListVaultsShouldReturnVaultsWhenCalled() throws JsonProcessingException {
            //given
            final var result = Collections.singletonList(new VaultModel());
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VAULT_MODEL_LIST_TYPE_REF)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(result);

            //when
            final var actual = underTest.listVaults();

            //then
            Assertions.assertEquals(result, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
            Assertions.assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VAULT_MODEL_LIST_TYPE_REF);
            verify(objectReader).readValue(anyString());
        }

        @Test
        void testListDeletedVaultsShouldReturnVaultsWhenCalled() throws JsonProcessingException {
            //given
            final var result = Collections.singletonList(new VaultModel());
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VAULT_MODEL_LIST_TYPE_REF)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(result);

            //when
            final var actual = underTest.listDeletedVaults();

            //then
            Assertions.assertEquals(result, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/deleted", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
            Assertions.assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VAULT_MODEL_LIST_TYPE_REF);
            verify(objectReader).readValue(anyString());
        }

        @Test
        void testDeleteShouldReturnBooleanStatusWhenCalled() throws JsonProcessingException {
            //given
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(Boolean.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(true);

            //when
            final var actual = underTest.delete(URI.create(HTTPS_LOCALHOST));

            //then
            Assertions.assertTrue(actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
            Assertions.assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(Boolean.class);
            verify(objectReader).readValue(anyString());
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testDeleteShouldThrowExceptionWhenUriIsNull() {
            //given
            final URI uri = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.delete(uri));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testRecoverShouldReturnVaultModelStatusWhenCalled() throws JsonProcessingException {
            //given
            final var vaultModel = new VaultModel();
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VaultModel.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(vaultModel);

            //when
            final var actual = underTest.recover(URI.create(HTTPS_LOCALHOST));

            //then
            Assertions.assertEquals(vaultModel, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/recover", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VaultModel.class);
            verify(objectReader).readValue(anyString());
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testRecoverShouldThrowExceptionWhenUriIsNull() {
            //given
            final URI uri = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.recover(uri));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testAddAliasShouldReturnVaultModelStatusWhenCalled() throws JsonProcessingException {
            //given
            final var vaultModel = new VaultModel();
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VaultModel.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(vaultModel);

            //when
            final var actual = underTest.addAlias(URI.create(HTTPS_LOCALHOST), URI.create(HTTPS_ALIAS_LOCALHOST));

            //then
            Assertions.assertEquals(vaultModel, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/alias", request.getUrl().getPath());
            final var queryString = "add=" + URLEncoder.encode(HTTPS_ALIAS_LOCALHOST, StandardCharsets.UTF_8)
                    + "&baseUri=" + URLEncoder.encode(HTTPS_LOCALHOST, StandardCharsets.UTF_8);
            Assertions.assertEquals(queryString, request.getUrl().getQuery());
            Assertions.assertEquals(HttpMethod.PATCH, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VaultModel.class);
            verify(objectReader).readValue(anyString());
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testAddAliasShouldThrowExceptionWhenBaseUriIsNull() {
            //given
            final URI uri = null;
            final var alias = URI.create(HTTPS_ALIAS_LOCALHOST);

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addAlias(uri, alias));

            //then
            verifyNoInteractions(httpClient);
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testAddAliasShouldThrowExceptionWhenAliasIsNull() {
            //given
            final var uri = URI.create(HTTPS_LOCALHOST);
            final URI alias = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addAlias(uri, alias));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testRemoveAliasShouldReturnVaultModelStatusWhenCalled() throws JsonProcessingException {
            //given
            final var vaultModel = new VaultModel();
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(VaultModel.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(vaultModel);

            //when
            final var actual = underTest.removeAlias(URI.create(HTTPS_LOCALHOST), URI.create(HTTPS_ALIAS_LOCALHOST));

            //then
            Assertions.assertEquals(vaultModel, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/alias", request.getUrl().getPath());
            final var queryString = "baseUri=" + URLEncoder.encode(HTTPS_LOCALHOST, StandardCharsets.UTF_8)
                    + "&remove=" + URLEncoder.encode(HTTPS_ALIAS_LOCALHOST, StandardCharsets.UTF_8);
            Assertions.assertEquals(queryString, request.getUrl().getQuery());
            Assertions.assertEquals(HttpMethod.PATCH, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(VaultModel.class);
            verify(objectReader).readValue(anyString());
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testRemoveAliasShouldThrowExceptionWhenBaseUriIsNull() {
            //given
            final URI uri = null;
            final var alias = URI.create(HTTPS_ALIAS_LOCALHOST);

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.removeAlias(uri, alias));

            //then
            verifyNoInteractions(httpClient);
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testAddRemoveShouldThrowExceptionWhenAliasIsNull() {
            //given
            final var uri = URI.create(HTTPS_LOCALHOST);
            final URI alias = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.removeAlias(uri, alias));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testPurgeShouldReturnPurgeStatusWhenCalled() throws JsonProcessingException {
            //given
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(objectReader.forType(Boolean.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(true);

            //when
            final var actual = underTest.purge(URI.create(HTTPS_LOCALHOST));

            //then
            Assertions.assertTrue(actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/purge", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
            Assertions.assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader).forType(Boolean.class);
            verify(objectReader).readValue(anyString());
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testPurgeShouldThrowExceptionWhenUriIsNull() {
            //given
            final URI uri = null;

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.purge(uri));

            //then
            verifyNoInteractions(httpClient);
        }

        @Test
        void testVaultModelAsStringShouldThrowExceptionWhenSerializationFails() throws JsonProcessingException {
            //given
            when(objectWriter.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

            //when
            Assertions.assertThrows(LowkeyVaultException.class,
                    () -> underTest.vaultModelAsString(null, null, null));

            //then + exception
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testTimeShiftShouldThrowExceptionWhenCalledWithNull() {
            //given

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(null));

            //then + exception
        }

        @Test
        void testTimeShiftShouldSucceedWhenCalledWithOnlyTime() {
            //given
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.empty());
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
            final var context = TimeShiftContext.builder()
                    .addDays(1)
                    .build();

            //when
            underTest.timeShift(context);

            //then
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/time/all", request.getUrl().getPath());
            Assertions.assertEquals("seconds=86400", request.getUrl().getQuery());
            Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
        }

        @Test
        void testTimeShiftShouldSucceedWhenCalledWithUriAndTime() {
            //given
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.empty());
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
            final var context = TimeShiftContext.builder()
                    .vaultBaseUri(URI.create("http://localhost"))
                    .addSeconds(1)
                    .build();

            //when
            underTest.timeShift(context);

            //then
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/time", request.getUrl().getPath());
            Assertions.assertEquals("baseUri=http%3A%2F%2Flocalhost&seconds=1", request.getUrl().getQuery());
            Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
        }

        @Test
        void testTimeShiftShouldSucceedWhenCalledWithTimeAndRegenerateFlag() {
            //given
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.empty());
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
            final var context = TimeShiftContext.builder()
                    .regenerateCertificates()
                    .addSeconds(1)
                    .build();

            //when
            underTest.timeShift(context);

            //then
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/time/all", request.getUrl().getPath());
            Assertions.assertEquals("regenerateCertificates=true&seconds=1", request.getUrl().getQuery());
            Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
            Assertions.assertEquals(APPLICATION_JSON, request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
        }

        @Test
        void testExportActiveShouldReturnFullResponseWhenCalledOnRunningServer() {
            //given
            final var expected = "value";
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(expected));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);

            //when
            final var actual = underTest.exportActive();

            //then
            Assertions.assertEquals(expected, actual);
            verify(httpClient, atMostOnce()).send(any());
            final var request = httpRequestArgumentCaptor.getValue();
            Assertions.assertEquals("/management/vault/export", request.getUrl().getPath());
            Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
            Assertions.assertNull(request.getHeaders().get(HttpHeaderName.CONTENT_TYPE));
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
        }

        @Test
        void testSendAndProcessShouldThrowExceptionWhenResponseCodeIsNot2xx() throws JsonProcessingException {
            //given
            final var vaultModel = new VaultModel();
            final var request = mock(HttpRequest.class);
            final var response = mock(HttpResponse.class);
            when(httpClient.send(httpRequestArgumentCaptor.capture())).thenReturn(Mono.just(response));
            when(response.getBodyAsString(StandardCharsets.UTF_8)).thenReturn(Mono.just(JSON));
            when(response.getStatusCode()).thenReturn(HttpStatus.SC_METHOD_NOT_ALLOWED);
            when(objectReader.forType(VaultModel.class)).thenReturn(objectReader);
            when(objectReader.readValue(JSON)).thenReturn(vaultModel);

            //when
            Assertions.assertThrows(LowkeyVaultException.class,
                    () -> underTest.sendAndProcess(request, r -> r.getResponseObject(VaultModel.class)));

            //then + exception
            verify(httpClient, atMostOnce()).send(any());
            verify(response).getStatusCode();
            verify(response).getBodyAsString(StandardCharsets.UTF_8);
            verify(objectReader, never()).forType(VaultModel.class);
            verify(objectReader, never()).readValue(anyString());
        }

        @Test
        void testUnpackBackupShouldThrowExceptionWhenCalledWithNull() {
            //given

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.unpackBackup(null));

            //then + exception
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        void testCompressBackupShouldThrowExceptionWhenCalledWithNull() {
            //given

            //when
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.compressBackup(null));

            //then + exception
        }

        @Test
        void testUnpackBackupShouldProduceFormattedJsonWhenCalledWithValidInput() throws IOException {
            //given
            final var input = underTest.compressBackup(SIMPLE_JSON);
            final var node = mock(JsonNode.class);
            when(objectReader.readTree(SIMPLE_JSON)).thenReturn(node);
            when(node.toPrettyString()).thenReturn(SIMPLE_JSON_PRETTY);

            //when
            final var actual = underTest.unpackBackup(input);

            //then
            Assertions.assertEquals(SIMPLE_JSON_PRETTY, actual);
            final var inOrder = inOrder(objectReader, node);
            inOrder.verify(objectReader).readTree(SIMPLE_JSON);
            inOrder.verify(node).toPrettyString();
            verifyNoMoreInteractions(objectReader, node);
        }

        @Test
        void testCompressBackupShouldProduceGzipBytesWhenCalledWithValidInput() throws IOException {
            //given
            final var out = new BigInteger("239366333208093937709170404274988390036218345476049221665072896269330010352532848640")
                    .toByteArray();

            //when
            final var actual = underTest.compressBackup(SIMPLE_JSON);

            //then
            Assertions.assertArrayEquals(out, actual);
        }
    }
}
