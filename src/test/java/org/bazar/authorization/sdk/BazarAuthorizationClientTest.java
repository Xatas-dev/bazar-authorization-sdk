package org.bazar.authorization.sdk;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.bazar.authorization.grpc.AuthorizationServiceGrpc;
import org.bazar.authorization.grpc.AuthorizeRequest;
import org.bazar.authorization.grpc.AuthorizeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthorizationClient
 */
public class BazarAuthorizationClientTest {
    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final AtomicReference<String> LAST_AUTHORIZATION_HEADER = new AtomicReference<>();

    private static Server server;
    private static final int PORT = 9091;

    @BeforeAll
    public static void setupServer() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(ServerInterceptors.intercept(new MockAuthorizationService(), new HeaderCaptureInterceptor()))
                .build()
                .start();
    }

    @AfterAll
    public static void teardownServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    @BeforeEach
    public void resetCapturedHeaders() {
        LAST_AUTHORIZATION_HEADER.set(null);
    }

    @Test
    public void testAuthorizeAllowed() {
        try (BazarAuthorizationClient client = BazarAuthorizationClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {

            boolean result = client.authorize(AuthorizationRequest.builder()
                    .spaceId(1L)
                    .permission(Permission.SPACE_WRITE)
                    .bearerToken("token-1")
                    .build());
            assertTrue(result);
            assertEquals("Bearer token-1", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testAuthorizeNotAllowed() {
        try (BazarAuthorizationClient client = BazarAuthorizationClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {

            boolean result = client.authorize(AuthorizationRequest.builder()
                    .spaceId(999L)
                    .permission(Permission.SPACE_DELETE)
                    .bearerToken("token-2")
                    .build());
            assertFalse(result);
        }
    }

    @Test
    public void testAuthorizeWithRequest() {
        try (BazarAuthorizationClient client = BazarAuthorizationClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {

            AuthorizationRequest request = AuthorizationRequest.builder()
                    .spaceId(1L)
                    .permission(Permission.CHAT_MESSAGES_READ)
                    .bearerToken("request-token")
                    .build();

            boolean result = client.authorize(request);
            assertTrue(result);
            assertEquals("Bearer request-token", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testAuthorizeWithSpaceUserAddPermission() {
        try (BazarAuthorizationClient client = BazarAuthorizationClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {

            boolean result = client.authorize(AuthorizationRequest.builder()
                    .spaceId(1L)
                    .permission(Permission.SPACE_USER_ADD)
                    .bearerToken("abc123")
                    .build());
            assertTrue(result);
            assertEquals("Bearer abc123", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testBlankBearerTokenThrowsAtRequestBuild() {
        try (BazarAuthorizationClient client = BazarAuthorizationClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {

            assertThrows(IllegalArgumentException.class,
                    () -> AuthorizationRequest.builder()
                            .spaceId(1L)
                            .permission(Permission.SPACE_USER_ADD)
                            .bearerToken("  ")
                            .build());
        }
    }

    /**
     * Mock implementation of AuthorizationService for testing
     */
    public static class MockAuthorizationService extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {
        @Override
        public void authorize(AuthorizeRequest request, StreamObserver<AuthorizeResponse> responseObserver) {
            boolean allowed = request.getSpaceId() == 1L;

            AuthorizeResponse response = AuthorizeResponse.newBuilder()
                    .setAllowed(allowed)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private static class HeaderCaptureInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                Metadata headers,
                ServerCallHandler<ReqT, RespT> next) {
            LAST_AUTHORIZATION_HEADER.set(headers.get(AUTHORIZATION_HEADER_KEY));
            return next.startCall(call, headers);
        }
    }
}

