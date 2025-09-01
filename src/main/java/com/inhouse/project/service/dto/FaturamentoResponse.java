package com.inhouse.project.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaturamentoResponse {
    
    private int ano;
    private int mes;
    private BigDecimal valorTotal;
    private String mesNome;
    
    public FaturamentoResponse(int ano, int mes, BigDecimal valorTotal) {
        this.ano = ano;
        this.mes = mes;
        this.valorTotal = valorTotal;
        this.mesNome = getNomeMes(mes);
    }
    
    private String getNomeMes(int mes) {
        String[] meses = {
            "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return meses[mes];
    }
}