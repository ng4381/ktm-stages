package com.ktmstages.ktmstages.router;

import com.ktmstages.ktmstages.client.AssemblyOrderRemainsClient;
import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import com.ktmstages.ktmstages.dto.StagesFormDTO;
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
public class StagesRouter {

    private AssemblyOrderRemainsClient remainsClient;

    public StagesRouter(AssemblyOrderRemainsClient remainsClient) {
        this.remainsClient = remainsClient;
    }

    @Bean
    public RouterFunction<ServerResponse> route() {

        RequestPredicate routeGetStages = RequestPredicates.GET("/stages");
        RequestPredicate routePostStage = RequestPredicates.POST("/stages");
        RequestPredicate routeGetHome = RequestPredicates.GET("/home");

        List<AssemblyOrderRemainsDTO> remains = remainsClient.getAssemblyOrderRemainsDTOFlux().collectList().block();
        StagesFormDTO stagesFormDTO = new StagesFormDTO();
        stagesFormDTO.setAssemblyOrderRemains(remains);

        Map<String, Object> map = Collections.singletonMap("stagesFormDTO", stagesFormDTO);

        return RouterFunctions
                .route(routeGetStages, request -> ServerResponse.ok().render("stages/list", map))
                .andRoute(routeGetHome, request -> ServerResponse.ok().render("home/view"))
                .andRoute(routePostStage, request -> {
                    sendModifiedDataToOrderService(request, remains);
                    return ServerResponse.ok().render("stages/list", map);
                });
    }

    private void sendModifiedDataToOrderService(ServerRequest request, List<AssemblyOrderRemainsDTO> remains) {

        Mono<MultiValueMap<String, String>> m = request.formData();
        m.subscribe(multiValueMap -> {

            List<AssemblyOrderRemainsDTO> remainsDTO = getListOfModifiedStages(multiValueMap, remains);
            if (!remainsDTO.isEmpty()) {
                remainsClient.sendDataToAssemblyOrderService(remainsDTO);
                setValueToDefault(remainsDTO);
            }
        });
    }

    private void setValueToDefault(List<AssemblyOrderRemainsDTO> remainsDTO) {
        remainsDTO.forEach(assemblyOrderRemainsDTO -> assemblyOrderRemainsDTO.setQtyDone(0));
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