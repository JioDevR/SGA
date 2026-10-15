package pe.sga.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "unidad_transporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "placa_tracto", nullable = false, unique = true, length = 15)
    private String placaTracto;

    @Column(name = "placa_carreta", length = 15)
    private String placaCarreta;

    @Column(name = "marca_modelo", nullable = false, length = 100)
    private String marcaModelo;

    @Column(name = "tipo_vehiculo", nullable = false, length = 50)
    @Builder.Default
    private String tipoVehiculo = "TRACTO_PLATAFORMA";

    @Column(name = "capacidad_carga_kg", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal capacidadCargaKg = new BigDecimal("30000.00");

    @Column(name = "estado_operativo", nullable = false, length = 20)
    @Builder.Default
    private String estadoOperativo = "DISPONIBLE";

    @Column(name = "estado_activo", nullable = false)
    @Builder.Default
    private Boolean estadoActivo = true;
}
