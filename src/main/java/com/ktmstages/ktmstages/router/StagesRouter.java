package com.ktmstages.ktmstages.router;

import com.ktmstages.ktmstages.client.AssemblyOrderRemainsClient;
import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import com.ktmstages.ktmstages.dto.StagesFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ktmstages.ktmstages.utils.Constants.REGEX_ASSEMBLY_ORDER_REMAINS_INDEX;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StagesRouter {

    private final AssemblyOrderRemainsClient remainsClient;

    @Bean
    public RouterFunction<ServerResponse> route() {

        RequestPredicate routeGetStages = RequestPredicates.GET("/stages");
        RequestPredicate routePostStage = RequestPredicates.POST("/stages");
        RequestPredicate routeGetHome = RequestPredicates.GET("/home");

        Map<String, Object> model = getModel();

        return RouterFunctions
                .route(routeGetStages, request -> ServerResponse.ok().render("stages/list", model))
                .andRoute(routeGetHome, request -> ServerResponse.ok().render("home/view"))
                .andRoute(routePostStage, request -> {
                    sendModifiedDataToOrderService(request, model);
                    //return ServerResponse.temporaryRedirect(URI.create("/stages")).build();
                    return ServerResponse.ok().render("stages/list", model);
                });
    }

    private Map<String, Object> getModel() {

        List<AssemblyOrderRemainsDTO> remains = remainsClient.getAssemblyOrderRemainsDTOFlux().collectList().block();
        StagesFormDTO stagesFormDTO = new StagesFormDTO();
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