package org.ferbator.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class HotelClient {
    private final RestClient client;

    public HotelClient(RestClient.Builder builder) {
        this.client = builder.baseUrl("http://hotel-service").build();
    }

    @CircuitBreaker(name = "hotelClient")
    @Retry(name = "hotelClient")
    @TimeLimiter(name = "hotelClient")
    public CompletableFuture<Void> hold(
            Long roomId,
            LocalDate start,
            LocalDate end,
            String requestId,
            String bearer
    ) {
        return CompletableFuture.supplyAsync(() -> {
            client.post()
                    .uri("/internal/rooms/{id}/confirm-availability", roomId)
                    .header("Authorization", bearer)
                    .body(
                            Map.of(
                                    "startDate", start.toString(),
                                    "endDate", end.toString(),
                                    "requestId", requestId
                            )
                    )
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    @CircuitBreaker(name = "hotelClient")
    @Retry(name = "hotelClient")
    @TimeLimiter(name = "hotelClient")
    public CompletableFuture<Void> confirm(
            Long roomId,
            String requestId,
            String bearer
    ) {
        return CompletableFuture.supplyAsync(() -> {
            client.post()
                    .uri(
                            uriBuilder -> uriBuilder
                                    .path("/internal/rooms/{id}/confirm")
                                    .queryParam("requestId", requestId)
                                    .build(roomId)
                    )
                    .header("Authorization", bearer)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    @CircuitBreaker(name = "hotelClient")
    @Retry(name = "hotelClient")
    @TimeLimiter(name = "hotelClient")
    public CompletableFuture<Void> release(
            Long roomId,
            String requestId,
            String bearer
    ) {
        return CompletableFuture.supplyAsync(() -> {
            client.post()
                    .uri(
                            uriBuilder -> uriBuilder.path("/internal/rooms/{id}/release")
                                    .queryParam("requestId", requestId)
                                    .build(roomId)
                    )
                    .header("Authorization", bearer)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public List<RoomDto> recommend(
            LocalDate start,
            LocalDate end,
            String bearer
    ) {
        var arr = client.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/api/rooms/recommend")
                                .queryParam("start", start.toString())
                                .queryParam("end", end.toString())
                                .build()
                )
                .header("Authorization", bearer)
                .retrieve()
                .body(RoomDto[].class);
        return Arrays.asList(arr == null ? new RoomDto[0] : arr);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoomDto(
            Long id,
            Long hotelId,
            String number,
            boolean available,
            long timesBooked
    ) {
    }
}
