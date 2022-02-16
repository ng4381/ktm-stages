package com.ktmstages.ktmstages.client;

import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AssemblyOrderRemainsClient {

    private final WebClient client;

    public AssemblyOrderRemainsClient(WebClient.Builder builder) {
        this.client = builder.baseUrl("http://localhost:8079").build();
    }

    public Flux<AssemblyOrderRemainsDTO> getAssemblyOrderRemainsDTOFlux() {
        return this.client.get().uri("/orders/stages/remains").accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(AssemblyOrderRemainsDTO.class)
                .map(assemblyOrderRemainsDTO -> {
                    assemblyOrderRemainsDTO.setQtyDone(0);
                    return assemblyOrderRemainsDTO;
                });
    }

    public void sendDataToAssemblyOrderService(List<AssemblyOrderRemainsDTO> assemblyOrderRemains) {
        this.client.post()
                .uri("/orders/stages/remains")
                .body(Mono.just(assemblyOrderRemains), AssemblyOrderRemainsDTO.class)
                .retrieve()
                .bodyToMono(AssemblyOrderRemainsDTO.class);
    }

}