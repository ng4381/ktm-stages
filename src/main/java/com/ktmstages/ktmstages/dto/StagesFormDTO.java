package com.ktmstages.ktmstages.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StagesFormDTO {
    private List<AssemblyOrderRemainsDTO> assemblyOrderRemains = new ArrayList<>();

    public List<AssemblyOrderRemainsDTO> getAssemblyOrderRemains() {
        return assemblyOrderRemains;
    }

    public void setAssemblyOrderRemains(List<AssemblyOrderRemainsDTO> assemblyOrderRemains) {
        this.assemblyOrderRemains = assemblyOrderRemains;
    }
}
