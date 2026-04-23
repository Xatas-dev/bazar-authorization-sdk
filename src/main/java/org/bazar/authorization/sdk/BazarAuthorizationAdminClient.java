package org.bazar.authorization.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC admin client for Bazar Authorization Service.
 *
 * <p>All methods use request objects that include Bearer token for per-call auth.
 */
public class BazarAuthorizationAdminClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BazarAuthorizationAdminClient.class);
    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel channel;
    private final AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub baseStub;

    private BazarAuthorizationAdminClient(ManagedChannel channel) {
        this.channel = channel;
        this.baseStub = AuthorizationAdminServiceGrpc.newBlockingStub(channel);
    }

    public boolean createUser(CreateUserRequest request) {
        try {
            org.bazar.authorization.grpc.CreateUserRequest grpcRequest =
                    org.bazar.authorization.grpc.CreateUserRequest.newBuilder()
                            .setUserId(request.getUserId())
                            .setSpaceId(request.getSpaceId())
                            .setCreator(request.isCreator())
                            .build();

            org.bazar.authorization.grpc.CreateUserResponse grpcResponse =
                    stubForBearerToken(request.getBearerToken()).createUser(grpcRequest);
            return grpcResponse.getSuccess();
        } catch (io.grpc.StatusRuntimeException e) {
            logger.error("Authorization admin service error on createUser: {}", e.getStatus(), e);
            throw new AuthorizationException("Failed to create user in authorization service", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during createUser", e);
            throw new AuthorizationException("Unexpected error during createUser", e);
        }
    }

    public boolean deleteUser(DeleteUserRequest request) {
        try {
            org.bazar.authorization.grpc.DeleteUserRequest grpcRequest =
                    org.bazar.authorization.grpc.DeleteUserRequest.newBuilder()
                            .setUserId(request.getUserId())
                            .setSpaceId(request.getSpaceId())
                            .build();

            org.bazar.authorization.grpc.DeleteUserResponse grpcResponse =
                    stubForBearerToken(request.getBearerToken()).deleteUser(grpcRequest);
            return grpcResponse.getSuccess();
        } catch (io.grpc.StatusRuntimeException e) {
            logger.error("Authorization admin service error on deleteUser: {}", e.getStatus(), e);
            throw new AuthorizationException("Failed to delete user in authorization service", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during deleteUser", e);
            throw new AuthorizationException("Unexpected error during deleteUser", e);
        }
    }

    public boolean deleteSpace(DeleteSpaceRequest request) {
        try {
            org.bazar.authorization.grpc.DeleteSpaceRequest grpcRequest =
                    org.bazar.authorization.grpc.DeleteSpaceRequest.newBuilder()
                            .setSpaceId(request.getSpaceId())
                            .build();

            org.bazar.authorization.grpc.DeleteSpaceResponse grpcResponse =
                    stubForBearerToken(request.getBearerToken()).deleteSpace(grpcRequest);
            return grpcResponse.getSuccess();
        } catch (io.grpc.StatusRuntimeException e) {
            logger.error("Authorization admin service error on deleteSpace: {}", e.getStatus(), e);
            throw new AuthorizationException("Failed to delete space in authorization service", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during deleteSpace", e);
            throw new AuthorizationException("Unexpected error during deleteSpace", e);
        }
    }

    private AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub stubForBearerToken(String token) {
        Metadata headers = new Metadata();
        headers.put(AUTHORIZATION_HEADER_KEY, bearerHeader(token));
        return baseStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private static String bearerHeader(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return "Bearer " + token;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
            logger.info("AuthorizationAdminClient channel closed");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host;
        private int port;
        private boolean usePlaintext = true;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder usePlaintext(boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
            return this;
        }

        public BazarAuthorizationAdminClient build() {
            ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);
            if (usePlaintext) {
                channelBuilder.usePlaintext();
            }

            ManagedChannel channel = channelBuilder.build();
            logger.info("Created AuthorizationAdminClient connected to {}:{}", host, port);
            return new BazarAuthorizationAdminClient(channel);
        }
    }
}

