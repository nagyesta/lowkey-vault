package com.github.nagyesta.lowkeyvault.context;

import com.azure.core.util.ServiceVersion;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public abstract class CommonTestContext<E, D, P, C, V extends ServiceVersion> {

    public static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

    private ApacheHttpClientProvider provider;
    @Nullable
    private C client;
    private Map<String, List<E>> createdEntities = new ConcurrentHashMap<>();
    @Nullable
    private E lastResult;
    private Map<String, Map<String, E>> fetchedVersions = new ConcurrentHashMap<>();
    private D lastDeleted;
    private P updateProperties;
    @Nullable
    private List<String> listedIds;
    @Nullable
    private List<String> listedManagedIds;
    @Nullable
    private List<String> deletedRecoveryIds;
    private final Map<String, byte[]> backups = new HashMap<>();

    public CommonTestContext(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    public ApacheHttpClientProvider getProvider() {
        return provider;
    }

    public void setProvider(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    public synchronized C getClient(final V version) {
        if (client == null) {
            client = providerToClient(getProvider(), version);
        }
        return client;
    }

    public void setClient(@Nullable final C client) {
        this.client = client;
    }

    protected abstract C providerToClient(ApacheHttpClientProvider provider, V version);

    public Map<String, List<E>> getCreatedEntities() {
        return createdEntities;
    }

    public void setCreatedEntities(final Map<String, List<E>> createdEntities) {
        this.createdEntities = createdEntities;
    }

    public Map<String, Map<String, E>> getFetchedVersions() {
        return fetchedVersions;
    }

    public void setFetchedVersions(final Map<String, Map<String, E>> fetchedVersions) {
        this.fetchedVersions = fetchedVersions;
    }

    public void addCreatedEntity(
            final String name,
            final E entity) {
        createdEntities.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(entity);
        lastResult = entity;
    }

    public void addFetchedEntity(
            final String name,
            final E entity,
            final Function<E, String> versionFunction) {
        fetchedVersions.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).put(versionFunction.apply(entity), entity);
        lastResult = entity;
    }

    public E getLastResult() {
        return Objects.requireNonNull(lastResult, "Last result cannot be null.");
    }

    public D getLastDeleted() {
        return lastDeleted;
    }

    public void setLastDeleted(final D lastDeleted) {
        this.lastDeleted = lastDeleted;
    }

    public List<String> getListedIds() {
        return Objects.requireNonNull(listedIds, "Listed ids cannot be null.");
    }


    public List<String> getListedManagedIds() {
        return Objects.requireNonNull(listedManagedIds, "Listed managed ids cannot be null.");
    }

    public void setListedIds(final List<String> listedIds) {
        this.listedIds = listedIds;
    }

    public void setListedManagedIds(final List<String> listedManagedIds) {
        this.listedManagedIds = listedManagedIds;
    }

    public List<String> getDeletedRecoveryIds() {
        return Objects.requireNonNull(deletedRecoveryIds, "Deleted recovery ids cannot be null.");
    }

    public void setDeletedRecoveryIds(final List<String> deletedRecoveryIds) {
        this.deletedRecoveryIds = deletedRecoveryIds;
    }

    public P getUpdateProperties() {
        return updateProperties;
    }

    public void setUpdateProperties(final P updateProperties) {
        this.updateProperties = updateProperties;
    }

    public void setBackupBytes(
            final String name,
            final byte[] bytes) {
        backups.put(name, bytes);
    }

    public byte[] getBackupBytes(final String name) {
        return backups.get(name);
    }
}
