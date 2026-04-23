package org.bazar.authorization.sdk;

/**
 * Request model for AuthorizationAdminService.DeleteSpace.
 */
public class DeleteSpaceRequest {
    private final long spaceId;
    private final String bearerToken;

    private DeleteSpaceRequest(Builder builder) {
        this.spaceId = builder.spaceId;
        this.bearerToken = builder.bearerToken;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long spaceId;
        private String bearerToken;

        public Builder spaceId(long spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public DeleteSpaceRequest build() {
            if (spaceId == 0) {
                throw new IllegalArgumentException("spaceId is required");
            }
            if (bearerToken == null || bearerToken.isBlank()) {
                throw new IllegalArgumentException("bearerToken is required");
            }
            return new DeleteSpaceRequest(this);
        }
    }
}

