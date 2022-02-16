package com.ktmstages.ktmstages.router;

import com.ktmstages.ktmstages.client.AssemblyOrderRemainsClient;
import com.ktmstages.ktmstages.dto.AssemblyOrderRemainsDTO;
import com.ktmstages.ktmstages.dto.StagesFormDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.*;
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

        RequestPredicate route = RequestPredicates.GET("/stages");
        RequestPredicate routemng = RequestPredicates.POST("/stages");

        List<AssemblyOrderRemainsDTO> remains = remainsClient.getAssemblyOrderRemainsDTOFlux().collectList().block();
        StagesFormDTO stagesFormDTO = new StagesFormDTO();
        stagesFormDTO.setAssemblyOrderRemains(remains);

        Map<String, Object> map = Collections.singletonMap("stagesFormDTO", stagesFormDTO);

        return RouterFunctions
                .route(route, request -> {
                    return ServerResponse.ok().render("stages/list", map);
                })
                .andRoute(routemng, request -> {

                    Mono<MultiValueMap<String, String>> m = request.formData();
                    m.subscribe(stringStringMultiValueMap -> {

                        List<AssemblyOrderRemainsDTO> remainsDTO = new ArrayList<>();

                        stringStringMultiValueMap.forEach((k, v) -> {
                            Pattern pattern = Pattern.compile(REGEX_ASSEMBLY_ORDER_REMAINS_INDEX);
                            Matcher matcher = pattern.matcher(k);
                            if(matcher.find()) {

                                int index = Integer.valueOf(matcher.group("idx"));
                                int value = Integer.valueOf(v.get(0));

                                System.out.println("Index: " + index + " Old value: " + remains.get(index) + " New value: " + value);

                                if (value != 0) {
                                    AssemblyOrderRemainsDTO assemblyOrderRemainsDTO = null;
                                    try {
                                        assemblyOrderRemainsDTO = (AssemblyOrderRemainsDTO) remains.get(index).clone();
                                    } catch (CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                    remainsDTO.add(assemblyOrderRemainsDTO);
                                }
                            }
                        });

                        remainsDTO.forEach(System.out::println);

                        remainsClient.sendDataToAssemblyOrderService(remainsDTO);


                    });

                    return ServerResponse.ok().render("stages/list", map);
                    //return ServerResponse.ok().render("redirect:/stages", map);
                });
    }


}