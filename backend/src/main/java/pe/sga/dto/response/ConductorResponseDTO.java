package pe.sga.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorResponseDTO {

    private Integer idConductor;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String tipoLicencia;
    private String numeroLicencia;
    private String telefono;
    private Boolean estadoActivo;
}
