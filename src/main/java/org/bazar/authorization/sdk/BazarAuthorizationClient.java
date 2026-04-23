package org.bazar.authorization.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.bazar.authorization.grpc.AuthorizationServiceGrpc;
import org.bazar.authorization.grpc.AuthorizeRequest;
import org.bazar.authorization.grpc.AuthorizeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC client for Bazar Authorization Service.
 * Provides methods to check authorization for resources and actions.
 */
public class BazarAuthorizationClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BazarAuthorizationClient.class);
    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel channel;
    private final AuthorizationServiceGrpc.AuthorizationServiceBlockingStub baseStub;
    private BazarAuthorizationClient(ManagedChannel channel) {
        this.channel = channel;
        this.baseStub = AuthorizationServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Check if an action is allowed on a resource in a space
     *
     * @param request the authorization request
     * @return true if the action is allowed, false otherwise
     * @throws AuthorizationException if the authorization check fails
     */
    public boolean authorize(AuthorizationRequest request) {
        try {
            AuthorizeRequest grpcRequest = AuthorizeRequest.newBuilder()
                    .setSpaceId(request.getSpaceId())
                    .setResource(request.getPermission().getResource())
                    .setAction(request.getPermission().getAction())
                    .build();

            AuthorizeResponse grpcResponse = stubForCall(request).authorize(grpcRequest);
            logger.debug("Authorization check completed for spaceId={}, permission={}",
                    request.getSpaceId(), request.getPermission());

            return grpcResponse.getAllowed();
        } catch (io.grpc.StatusRuntimeException e) {
            logger.error("Authorization service error: {}", e.getStatus(), e);
            throw new AuthorizationException("Failed to authorize request", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authorization", e);
            throw new AuthorizationException("Unexpected error during authorization", e);
        }
    }

    private AuthorizationServiceGrpc.AuthorizationServiceBlockingStub stubForCall(AuthorizationRequest request) {
        String authorizationHeader = bearerHeader(request.getBearerToken());

        Metadata headers = new Metadata();
        headers.put(AUTHORIZATION_HEADER_KEY, authorizationHeader);
        return baseStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private static String bearerHeader(String token) {
        validateNonBlank(token, "token must not be blank");
        return "Bearer " + token;
    }

    private static void validateNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Close the connection
     */
    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
            logger.info("AuthorizationClient channel closed");
        }
    }

    /**
     * Create a new builder for AuthorizationClient
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating AuthorizationClient
     */
    public static class Builder {
        private String host;
        private int port;
        private boolean usePlaintext = true;

        /**
         * Set the host address of the authorization service
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Set the port of the authorization service
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set whether to use plaintext communication (default: true)
         */
        public Builder usePlaintext(boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
            return this;
        }

        /**
         * Build the AuthorizationClient
         */
        public BazarAuthorizationClient build() {
            ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                    .forAddress(host, port);

            if (usePlaintext) {
                channelBuilder.usePlaintext();
            }

            ManagedChannel channel = channelBuilder.build();
            logger.info("Created AuthorizationClient connected to {}:{}", host, port);
            return new BazarAuthorizationClient(channel);
        }
    }
}

