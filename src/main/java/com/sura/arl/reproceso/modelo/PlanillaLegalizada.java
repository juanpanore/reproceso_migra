package com.sura.arl.reproceso.modelo;

public class PlanillaLegalizada {

    private final Long numeroFormulario;

    public PlanillaLegalizada(Long numeroFormulario) {
        this.numeroFormulario = numeroFormulario;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long numeroFormulario;

        public Builder numeroFormulario(Long numeroFormulario) {
            this.numeroFormulario = numeroFormulario;
            return this;
        }

        public PlanillaLegalizada build() {
            return new PlanillaLegalizada(numeroFormulario);
        }

    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

}
