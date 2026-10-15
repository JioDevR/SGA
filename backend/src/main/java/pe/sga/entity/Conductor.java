package pe.sga.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conductor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conductor")
    private Integer idConductor;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "tipo_licencia", nullable = false, length = 10)
    @Builder.Default
    private String tipoLicencia = "A-IIIc";

    @Column(name = "numero_licencia", nullable = false, unique = true, length = 20)
    private String numeroLicencia;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "estado_activo", nullable = false)
    @Builder.Default
    private Boolean estadoActivo = true;
}
