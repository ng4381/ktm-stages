package com.ktmstages.ktmstages.client;

import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssemblyOrderRemainsClient {

    private final WebClient client;


    public Flux<AssemblyOrderRemainsDTO> getAssemblyOrderRemainsDTOFlux() {
        return this.client.get()
                .uri("/orders/stages/remains")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(AssemblyOrderRemainsDTO.class)
                .map(aor -> {
                    aor.setQty(aor.getQty() - aor.getQtyDone());
                    aor.setQtyDone(0);
                    return aor;
                });
    }

    public void sendDataToAssemblyOrderService(List<AssemblyOrderRemainsDTO> assemblyOrderRemains) {

        log.info("Sending data to order service ...");

        this.client
                .post()
                .uri("/orders/stages/remains")
                //.contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assemblyOrderRemains))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}