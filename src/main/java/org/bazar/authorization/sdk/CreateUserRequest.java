package org.bazar.authorization.sdk;

/**
 * Request model for AuthorizationAdminService.CreateUser.
 */
public class CreateUserRequest {
    private final String userId;
    private final long spaceId;
    private final boolean creator;
    private final String bearerToken;

    private CreateUserRequest(Builder builder) {
        this.userId = builder.userId;
        this.spaceId = builder.spaceId;
        this.creator = builder.creator;
        this.bearerToken = builder.bearerToken;
    }

    public String getUserId() {
        return userId;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public boolean isCreator() {
        return creator;
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
        private boolean creator;
        private String bearerToken;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder spaceId(long spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder creator(boolean creator) {
            this.creator = creator;
            return this;
        }

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public CreateUserRequest build() {
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("userId is required");
            }
            if (spaceId == 0) {
                throw new IllegalArgumentException("spaceId is required");
            }
            if (bearerToken == null || bearerToken.isBlank()) {
                throw new IllegalArgumentException("bearerToken is required");
            }
            return new CreateUserRequest(this);
        }
    }
}

