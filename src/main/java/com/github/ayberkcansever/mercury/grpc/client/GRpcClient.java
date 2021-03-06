package com.github.ayberkcansever.mercury.grpc.client;

import com.github.ayberkcansever.mercury.grpc.Mercury;
import com.github.ayberkcansever.mercury.grpc.MessageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;

public class GRpcClient {

    private MessageServiceGrpc.MessageServiceBlockingStub stub;
    @Getter private String serverUrl;

    public GRpcClient(String serverUrl) {
        this.serverUrl = serverUrl;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverUrl)
                .usePlaintext(true)
                .build();
        stub = MessageServiceGrpc.newBlockingStub(channel);
    }

    public String sendMessage(String from, String to, String message) {
        Mercury.MessageRequest messageRequest = Mercury.MessageRequest.newBuilder()
                .setFrom(from)
                .setTo(to)
                .setMessage(message).build();
        return stub.send(messageRequest).getResp();
    }
}
