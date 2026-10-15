package pe.sga.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespachoTransporteResponseDTO {

    private Integer idDespachoTransporte;
    private String codigoTracking;
    private String tipoTraslado;

    // Expediente asociado
    private Integer idExpediente;
    private String codigoExpediente;
    private String numeroDam;
    private String clienteRazonSocial;

    // Recursos Asignados 3NF
    private Integer idConductor;
    private String conductorNombre;
    private String conductorLicencia;
    private String conductorTelefono;

    private Integer idUnidad;
    private String unidadPlacaTracto;
    private String unidadPlacaCarreta;
    private String unidadMarcaModelo;

    // Documentación y Ruta
    private String guiaRemisionTransporte;
    private String direccionPartida;
    private String direccionLlegada;
    private String distritoLlegada;
    private String contactoRecepcion;

    // Control de Tiempos
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaSalidaRuta;
    private LocalDateTime fechaEntregaConforme;

    // Estado
    private String estadoTransporte;
    private String detalleIncidencia;
    private String observaciones;
}
