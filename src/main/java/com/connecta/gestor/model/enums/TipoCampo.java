package com.connecta.gestor.model.enums;

import lombok.Getter;

@Getter
public enum TipoCampo {
    LOCALIZACAO("Localização", "Endereço completo, CEP, bairro, ponto de referência"),
    FOTO("Foto", "Upload de imagens"),
    DESCRICAO_DETALHADA("Descrição Detalhada", "Campo de texto longo para detalhes"),
    DADOS_SOLICITANTE("Dados do Solicitante", "Nome, CPF, telefone, email"),
    DATA_HORA("Data/Hora", "Data e hora desejada para atendimento"),
    PLACA_VEICULO("Placa de Veículo", "Placa do veículo"),
    NUMERO_IMOVEL("Número do Imóvel", "Número do imóvel/lote"),
    METRAGEM("Metragem/Dimensões", "Medidas em metros"),
    VALOR_DECLARADO("Valor Declarado", "Valor em reais"),
    DOCUMENTOS_ANEXOS("Documentos Anexos", "Upload de documentos PDF/DOC"),
    NUMERO_PROTOCOLO_ANTERIOR("Protocolo Anterior", "Número de protocolo relacionado"),
    OBSERVACOES("Observações", "Campo livre para observações gerais");

    private final String label;
    private final String descricao;

    TipoCampo(String label, String descricao) {
        this.label = label;
        this.descricao = descricao;
    }
}


