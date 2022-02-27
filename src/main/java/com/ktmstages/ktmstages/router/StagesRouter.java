package com.ktmstages.ktmstages.router;

import com.ktmstages.ktmstages.client.AssemblyOrderRemainsClient;
import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import com.ktmstages.ktmstages.dto.StagesFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ktmstages.ktmstages.utils.Constants.REGEX_ASSEMBLY_ORDER_REMAINS_INDEX;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StagesRouter {

    private final AssemblyOrderRemainsClient remainsClient;
    private final StagesFormDTO stagesFormDTO;

    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Possibly...
        // config.applyPermitDefaultValues()

        config.applyPermitDefaultValues();

        /*
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:8078");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*")

         */

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public RouterFunction<ServerResponse> route() {

        RequestPredicate routeGetStages = RequestPredicates.GET("/stages");
        RequestPredicate routeGetStagesWeb = RequestPredicates.GET("/web/stages");
        RequestPredicate routePostStagesWeb = RequestPredicates.POST("/web/stages").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON));
        RequestPredicate routePostStage = RequestPredicates.POST("/stages");
        RequestPredicate routeGetHome = RequestPredicates.GET("/home");

        Map<String, Object> model = getModel();

        return RouterFunctions
                .route(routeGetStages, request -> {
                    return ok().render("stages/list", model);
                })
                .andRoute(routeGetStagesWeb, request -> ok()
                        .body(remainsClient.getAssemblyOrderRemainsDTOFlux(), AssemblyOrderRemainsDTO.class)
                )
                .andRoute(routeGetHome, request -> ok().render("home/view"))
                .andRoute(routePostStage, request -> {
                    sendModifiedDataToOrderService(request, model);
                    //return ServerResponse.temporaryRedirect(URI.create("/stages")).build();
                    return ok().render("stages/list", model);
                })
                .andRoute(routePostStagesWeb, request -> {
                    //remainsClient.sendDataToAssemblyOrderService(List.of(""));
                    return request
                            .bodyToFlux(AssemblyOrderRemainsDTO.class)
                            .collectList().map(orderRemainsDTOList -> remainsClient.sendDataToAssemblyOrderService(orderRemainsDTOList))
                            .then(ok().build());
        });
    }

    private Map<String, Object> getModel() {

        List<AssemblyOrderRemainsDTO> remains = remainsClient.getAssemblyOrderRemainsDTOFlux().collectList().block();
        //StagesFormDTO stagesFormDTO = new StagesFormDTO();
        stagesFormDTO.setAssemblyOrderRemains(remains);

        return Collections.singletonMap("stagesFormDTO", stagesFormDTO);
    }

    private void sendModifiedDataToOrderService(ServerRequest request, Map<String, Object> model) {

        List<AssemblyOrderRemainsDTO> remains = ((StagesFormDTO) model.get("stagesFormDTO")).getAssemblyOrderRemains();

        Mono<MultiValueMap<String, String>> m = request.formData();
        m.subscribe(multiValueMap -> {

            List<AssemblyOrderRemainsDTO> remainsDTO = getListOfModifiedStages(multiValueMap, remains);
            if (!remainsDTO.isEmpty()) {
                remainsClient.sendDataToAssemblyOrderService(remainsDTO);
            }
        });
    }

    private List<AssemblyOrderRemainsDTO> getListOfModifiedStages(MultiValueMap<String, String> multiValueMap, List<AssemblyOrderRemainsDTO> remains) {
        List<AssemblyOrderRemainsDTO> remainsDTO = new ArrayList<>();

        multiValueMap.forEach((k, v) -> {
            Pattern pattern = Pattern.compile(REGEX_ASSEMBLY_ORDER_REMAINS_INDEX);
            Matcher matcher = pattern.matcher(k);
            if (matcher.find()) {

                int index = Integer.valueOf(matcher.group("idx"));
                int value = Integer.valueOf(v.get(0));

                if (value != 0) {
                    AssemblyOrderRemainsDTO assemblyOrderRemainsDTO = remains.get(index);
                    assemblyOrderRemainsDTO.setQtyDone(value);
                    remainsDTO.add(assemblyOrderRemainsDTO);
                }
            }
        });

        return remainsDTO;
    }
}