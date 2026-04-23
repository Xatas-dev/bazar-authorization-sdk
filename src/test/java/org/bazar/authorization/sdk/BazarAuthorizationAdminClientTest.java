package org.bazar.authorization.sdk;

import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc;
import org.bazar.authorization.grpc.CreateUserResponse;
import org.bazar.authorization.grpc.DeleteSpaceResponse;
import org.bazar.authorization.grpc.DeleteUserResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BazarAuthorizationAdminClientTest {
    private static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final AtomicReference<String> LAST_AUTHORIZATION_HEADER = new AtomicReference<>();

    private static Server server;
    private static final int PORT = 9092;

    @BeforeAll
    public static void setupServer() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(ServerInterceptors.intercept(new MockAuthorizationAdminService(), new HeaderCaptureInterceptor()))
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
    public void testCreateUser() {
        try (BazarAuthorizationAdminClient client = BazarAuthorizationAdminClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {
            boolean success = client.createUser(CreateUserRequest.builder()
                    .userId("u-1")
                    .spaceId(10L)
                    .creator(true)
                    .bearerToken("token-a")
                    .build());

            assertTrue(success);
            assertEquals("Bearer token-a", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testDeleteUser() {
        try (BazarAuthorizationAdminClient client = BazarAuthorizationAdminClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {
            boolean success = client.deleteUser(DeleteUserRequest.builder()
                    .userId("u-1")
                    .spaceId(10L)
                    .bearerToken("token-b")
                    .build());

            assertTrue(success);
            assertEquals("Bearer token-b", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testDeleteSpace() {
        try (BazarAuthorizationAdminClient client = BazarAuthorizationAdminClient.builder()
                .host("localhost")
                .port(PORT)
                .build()) {
            boolean success = client.deleteSpace(DeleteSpaceRequest.builder()
                    .spaceId(10L)
                    .bearerToken("token-c")
                    .build());

            assertTrue(success);
            assertEquals("Bearer token-c", LAST_AUTHORIZATION_HEADER.get());
        }
    }

    @Test
    public void testCreateUserBlankTokenThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CreateUserRequest.builder()
                        .userId("u-1")
                        .spaceId(10L)
                        .creator(false)
                        .bearerToken("  ")
                        .build());
    }

    private static class MockAuthorizationAdminService extends AuthorizationAdminServiceGrpc.AuthorizationAdminServiceImplBase {
        @Override
        public void createUser(org.bazar.authorization.grpc.CreateUserRequest request,
                               StreamObserver<CreateUserResponse> responseObserver) {
            responseObserver.onNext(CreateUserResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        }

        @Override
        public void deleteUser(org.bazar.authorization.grpc.DeleteUserRequest request,
                               StreamObserver<DeleteUserResponse> responseObserver) {
            responseObserver.onNext(DeleteUserResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        }

        @Override
        public void deleteSpace(org.bazar.authorization.grpc.DeleteSpaceRequest request,
                                StreamObserver<DeleteSpaceResponse> responseObserver) {
            responseObserver.onNext(DeleteSpaceResponse.newBuilder().setSuccess(true).build());
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

