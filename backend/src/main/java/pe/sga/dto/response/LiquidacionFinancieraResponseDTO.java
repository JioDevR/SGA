package pe.sga.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionFinancieraResponseDTO {

    private Integer idLiquidacion;
    private String codigoLiquidacion;

    // Expediente asociado
    private Integer idExpediente;
    private String codigoExpediente;
    private String numeroDam;
    private String clienteRazonSocial;
    private String clienteNumeroDocumento;

    // Desglose de Gastos
    private BigDecimal totalTributosSunat;
    private BigDecimal gastosAlmacenPuerto;
    private BigDecimal fleteTransporteLocal;
    private BigDecimal honorariosAgencia;
    private BigDecimal totalGeneralPen;
    private BigDecimal tipoCambio;

    // Estado y Pagos
    private String estadoPago;
    private String numeroFactura;
    private String metodoPago;
    private String numeroOperacion;
    private LocalDate fechaEmision;
    private LocalDateTime fechaPago;
    private String observaciones;
}
