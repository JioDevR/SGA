package pe.sga.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadTransporteResponseDTO {

    private Integer idUnidad;
    private String placaTracto;
    private String placaCarreta;
    private String marcaModelo;
    private String tipoVehiculo;
    private BigDecimal capacidadCargaKg;
    private String estadoOperativo;
    private Boolean estadoActivo;
}
