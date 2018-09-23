package org.vaadin.fieldbinder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.vaadin.fieldbinder.FieldBinder.FieldBinding;

import com.vaadin.data.HasValue;
import com.vaadin.data.Result;
import com.vaadin.data.ValidationResult;

public class FieldBindingValidationStatus<TARGET> implements Serializable {

    /**
     * Status of the validation.
     * <p>
     * The status is the part of {@link FieldBindingValidationStatus} which indicates
     * whether the validation failed or not, or whether it is in unresolved
     * state (e.g. after clear or reset).
     */
    public enum Status {
        /** Validation passed. */
        OK,
        /** Validation failed. */
        ERROR,
        /**
         * Unresolved status, e.g field has not yet been validated because value
         * was cleared.
         * <p>
         * In practice this status means that the value might be invalid, but
         * validation errors should be hidden.
         */
        UNRESOLVED;
    }

    private final Status status;
    private final List<ValidationResult> results;
    private final FieldBinding<TARGET> binding;
    private Result<TARGET> result;

    /**
     * Convenience method for creating a {@link Status#UNRESOLVED} validation
     * status for the given binding.
     *
     * @param source
     *            the source binding
     * @return unresolved validation status
     * @param <TARGET>
     *            the target data type of the binding for which the validation
     *            status was reset
     */
    public static <TARGET> FieldBindingValidationStatus<TARGET> createUnresolvedStatus(
            FieldBinding<TARGET> source) {
        return new FieldBindingValidationStatus<TARGET>(null, source);
    }

    /**
     * Creates a new status change event.
     * <p>
     * If {@code result} is {@code null}, the {@code status} is
     * {@link Status#UNRESOLVED}.
     *
     * @param result
     *            the related result object, may be {@code null}
     * @param source
     *            field whose status has changed, not {@code null}
     */
    public FieldBindingValidationStatus(Result<TARGET> result,
            FieldBinding<TARGET> source) {
        Objects.requireNonNull(source, "Event source may not be null");

        binding = source;
        if (result != null) {
            this.status = result.isError() ? Status.ERROR : Status.OK;
            if (result instanceof FieldValidationResultWrap) {
                results = ((FieldValidationResultWrap<TARGET>) result)
                        .getValidationResults();
            } else {
                results = Collections.emptyList();
            }
        } else {
            this.status = Status.UNRESOLVED;
            results = Collections.emptyList();
        }
        this.result = result;
    }

    /**
     * Gets status of the validation.
     *
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets whether the validation failed or not.
     *
     * @return {@code true} if validation failed, {@code false} if validation
     *         passed
     */
    public boolean isError() {
        return status == Status.ERROR;
    }

    /**
     * Gets error validation message if status is {@link Status#ERROR}.
     *
     * @return an optional validation error status or an empty optional if
     *         status is not an error
     */
    public Optional<String> getMessage() {
        if (getStatus() == Status.OK || result == null) {
            return Optional.empty();
        }
        return result.getMessage();
    }

    /**
     * Gets the validation result if status is either {@link Status#OK} or
     * {@link Status#ERROR} or an empty optional if status is
     * {@link Status#UNRESOLVED}.
     *
     * @return the validation result
     */
    public Optional<ValidationResult> getResult() {
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result.isError()
                ? ValidationResult.error(result.getMessage().orElse(""))
                : ValidationResult.ok());
    }

    /**
     * Gets all the validation results related to this binding validation
     * status.
     *
     * @return list of validation results
     */
    public List<ValidationResult> getValidationResults() {
        return Collections.unmodifiableList(results);
    }

    /**
     * Gets the source binding of the validation status.
     *
     * @return the source binding
     */
    public FieldBinding<TARGET> getBinding() {
        return binding;
    }

    /**
     * Gets the bound field for this status.
     *
     * @return the field
     */
    public HasValue<?> getField() {
        return getBinding().getField();
    }
}
