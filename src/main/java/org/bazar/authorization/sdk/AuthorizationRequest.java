package org.bazar.authorization.sdk;

/**
 * Builder class for creating authorization requests.
 * Provides a type-safe way to construct authorization requests.
 */
public class AuthorizationRequest {
    private final long spaceId;
    private final Permission permission;
    private final String bearerToken;

    private AuthorizationRequest(Builder builder) {
        this.spaceId = builder.spaceId;
        this.permission = builder.permission;
        this.bearerToken = builder.bearerToken;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public Permission getPermission() {
        return permission;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * Create a new builder for AuthorizationRequest
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for fluent API
     */
    public static class Builder {
        private long spaceId;
        private Permission permission;
        private String bearerToken;

        /**
         * Set the space ID
         */
        public Builder spaceId(long spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        /**
         * Set the permission (resource + action combined)
         */
        public Builder permission(Permission permission) {
            this.permission = permission;
            return this;
        }

        /**
         * Set Bearer token for this request. Value must be token only (without "Bearer ").
         */
        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        /**
         * Build the AuthorizationRequest
         */
        public AuthorizationRequest build() {
            if (spaceId == 0) {
                throw new IllegalArgumentException("spaceId is required");
            }
            if (permission == null) {
                throw new IllegalArgumentException("permission is required");
            }
            if (bearerToken == null || bearerToken.isBlank()) {
                throw new IllegalArgumentException("bearerToken is required");
            }
            return new AuthorizationRequest(this);
        }
    }
}

