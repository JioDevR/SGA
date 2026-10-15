package pe.sga.dto.seed;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitialSeedDataDTO {

    private AdminSeedDTO admin;
    private List<SedeSeedDTO> sedes;
    private List<PuertoSeedDTO> puertos;
    private List<TipoCargaSeedDTO> tiposCarga;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminSeedDTO {
        private String username;
        private String password;
        private String nombres;
        private String apellidos;
        private String correoInstitucional;
        private String tipoDocumento;
        private String numeroDocumento;
        private String telefono;
        private String cargoTitulo;
        private String codigoSunat;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SedeSeedDTO {
        private String codigoSede;
        private String nombre;
        private String tipo;
        private String direccion;
        private String distrito;
        private String telefono;
        private String estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PuertoSeedDTO {
        private String codigoPuerto;
        private String nombre;
        private String pais;
        private String tipo;
        private String estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TipoCargaSeedDTO {
        private String codigo;
        private String nombre;
        private String tipoContenedorSugerido;
        private BigDecimal taraSugeridaKg;
        private String estado;
    }
}
