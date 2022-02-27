package com.ktmstages.ktmstages.client;

import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import com.ktmstages.ktmstages.dto.StagesFormDTO;
import com.sun.jdi.VoidType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssemblyOrderRemainsClient {

    private final WebClient client;
    private final StagesFormDTO stagesFormDTO;

    /*
    public List<AssemblyOrderRemainsDTO> updateList(Flux<AssemblyOrderRemainsDTO> remainsDTOFlux) {

        List<AssemblyOrderRemainsDTO> orderRemainsDTOList stagesFormDTO.getAssemblyOrderRemains();
        remainsDTOFlux.subscribe();
        return
    }
     */


    public Flux<AssemblyOrderRemainsDTO> getAssemblyOrderRemainsDTOFlux() {
        log.info("Reading data from order service ...");
        return this.client.get()
                .uri("/orders/stages/remains")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(AssemblyOrderRemainsDTO.class)
                .doOnNext(assemblyOrderRemainsDTO -> System.out.println(assemblyOrderRemainsDTO))
                .map(aor -> {
                    aor.setQty(aor.getQty() - aor.getQtyDone());
                    aor.setQtyDone(0);
                    return aor;
                });
    }

    public Mono<Void> sendDataToAssemblyOrderService(List<AssemblyOrderRemainsDTO> assemblyOrderRemains) {
        log.info("Sending data to order service ...");
        this.client
                .post()
                .uri("/orders/stages/remains")
                //.contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(assemblyOrderRemains))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();

        return Mono.just("").then();


    }
}