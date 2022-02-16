package com.ktmstages.ktmstages.dto;

import java.util.ArrayList;
import java.util.List;

public class StagesFormDTO {
    private List<AssemblyOrderRemainsDTO> assemblyOrderRemains = new ArrayList<>();

    public List<AssemblyOrderRemainsDTO> getAssemblyOrderRemains() {
        return assemblyOrderRemains;
    }

    public void setAssemblyOrderRemains(List<AssemblyOrderRemainsDTO> assemblyOrderRemains) {
        this.assemblyOrderRemains = assemblyOrderRemains;
    }
}
