package org.bazar.authorization.sdk;

/**
 * Request model for AuthorizationAdminService.DeleteUser.
 */
public class DeleteUserRequest {
    private final String userId;
    private final long spaceId;
    private final String bearerToken;

    private DeleteUserRequest(Builder builder) {
        this.userId = builder.userId;
        this.spaceId = builder.spaceId;
        this.bearerToken = builder.bearerToken;
    }

    public String getUserId() {
        return userId;
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
        private String userId;
        private long spaceId;
        private String bearerToken;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder spaceId(long spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public DeleteUserRequest build() {
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("userId is required");
            }
            if (spaceId == 0) {
                throw new IllegalArgumentException("spaceId is required");
            }
            if (bearerToken == null || bearerToken.isBlank()) {
                throw new IllegalArgumentException("bearerToken is required");
            }
            return new DeleteUserRequest(this);
        }
    }
}

