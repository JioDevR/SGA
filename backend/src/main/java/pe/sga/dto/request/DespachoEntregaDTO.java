package pe.sga.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespachoEntregaDTO {

    private LocalDateTime fechaEntregaConforme;

    @Size(max = 100, message = "El contacto de recepción no puede exceder 100 caracteres")
    private String contactoRecepcion;

    private String detalleIncidencia;

    private String observaciones;
}
