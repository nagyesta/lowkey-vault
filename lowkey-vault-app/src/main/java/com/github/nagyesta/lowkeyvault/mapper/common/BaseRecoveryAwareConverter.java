package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.common.DeletedModel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyDeletedEntity;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Base converter supporting both active and deleted entities.
 *
 * @param <V>  The entityId type.
 * @param <S>  The source type.
 * @param <T>  The active target type.
 * @param <DT> The deleted target type.
 */
public abstract class BaseRecoveryAwareConverter<V extends EntityId, S extends ReadOnlyDeletedEntity<V>, T, DT extends T>
        implements RecoveryAwareConverter<S, T, DT> {

    private final Supplier<T> modelSupplier;
    private final Supplier<DT> deletedModelSupplier;

    protected BaseRecoveryAwareConverter(final Supplier<T> modelSupplier, final Supplier<DT> deletedModelSupplier) {
        this.modelSupplier = modelSupplier;
        this.deletedModelSupplier = deletedModelSupplier;
        Assert.isInstanceOf(DeletedModel.class, deletedModelSupplier.get());
    }

    protected void mapDeletedFields(final S source, final DeletedModel model, final URI vaultUri) {
        model.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
        model.setDeletedDate(source.getDeletedDate().orElseThrow());
        model.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
    }

    @Override
    @org.springframework.lang.NonNull
    public T convert(@org.springframework.lang.NonNull final S source, @org.springframework.lang.NonNull final URI vaultUri) {
        return mapActiveFields(source, modelSupplier.get(), vaultUri);
    }

    @Override
    @org.springframework.lang.NonNull
    public DT convertDeleted(@org.springframework.lang.NonNull final S source, @org.springframework.lang.NonNull final URI vaultUri) {
        final DT model = deletedModelSupplier.get();
        mapDeletedFields(source, (DeletedModel) model, vaultUri);
        return mapActiveFields(source, model, vaultUri);
    }

    protected abstract <M extends T> M mapActiveFields(S source, M model, URI vaultUri);
}
