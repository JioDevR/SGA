package pe.sga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.sga.dto.seed.InitialSeedDataDTO;
import pe.sga.entity.CatalogoPuerto;
import pe.sga.entity.CatalogoTipoCarga;
import pe.sga.entity.Rol;
import pe.sga.entity.Sede;
import pe.sga.entity.Usuario;
import pe.sga.entity.enums.RolUsuario;
import pe.sga.repository.CatalogoPuertoRepository;
import pe.sga.repository.CatalogoTipoCargaRepository;
import pe.sga.repository.RolRepository;
import pe.sga.repository.SedeRepository;
import pe.sga.repository.UsuarioRepository;

import java.io.InputStream;
import java.math.BigDecimal;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoPuertoRepository puertoRepository;
    private final CatalogoTipoCargaRepository tipoCargaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataInitializer(
            RolRepository rolRepository,
            SedeRepository sedeRepository,
            UsuarioRepository usuarioRepository,
            CatalogoPuertoRepository puertoRepository,
            CatalogoTipoCargaRepository tipoCargaRepository,
            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.sedeRepository = sedeRepository;
        this.usuarioRepository = usuarioRepository;
        this.puertoRepository = puertoRepository;
        this.tipoCargaRepository = tipoCargaRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando verificación de datos iniciales del sistema SGA...");

        // 1. Roles institucionales base (según enum de negocio)
        for (RolUsuario rolEnum : RolUsuario.values()) {
            if (!rolRepository.existsByNombreRol(rolEnum)) {
                rolRepository.save(Rol.builder()
                        .nombreRol(rolEnum)
                        .descripcion("Rol operativo: " + rolEnum.name())
                        .estadoActivo(true)
                        .build());
            }
        }

        // 2. Cargar datos configurables desde initial-data.json
        try {
            Resource resource = new ClassPathResource("initial-data.json");
            if (!resource.exists()) {
                log.warn("No se encontró initial-data.json en el classpath. Se omite carga inicial.");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                InitialSeedDataDTO seedData = objectMapper.readValue(is, InitialSeedDataDTO.class);
                if (seedData == null) {
                    log.warn("El archivo initial-data.json está vacío.");
                    return;
                }

                // Cargar sedes desde JSON
                if (seedData.getSedes() != null && sedeRepository.count() == 0) {
                    for (InitialSeedDataDTO.SedeSeedDTO s : seedData.getSedes()) {
                        if (!sedeRepository.existsByCodigoSede(s.getCodigoSede())) {
                            sedeRepository.save(Sede.builder()
                                    .codigoSede(s.getCodigoSede())
                                    .nombre(s.getNombre())
                                    .tipo(s.getTipo() != null ? s.getTipo() : "SEDE_CENTRAL")
                                    .direccion(s.getDireccion())
                                    .distrito(s.getDistrito())
                                    .telefono(s.getTelefono())
                                    .estado(s.getEstado() != null ? s.getEstado() : "OPERATIVO")
                                    .build());
                        }
                    }
                    log.info("Sedes cargadas desde initial-data.json con éxito.");
                }

                // Cargar puertos desde JSON
                if (seedData.getPuertos() != null && puertoRepository.count() == 0) {
                    for (InitialSeedDataDTO.PuertoSeedDTO p : seedData.getPuertos()) {
                        if (!puertoRepository.existsByCodigoPuerto(p.getCodigoPuerto())) {
                            puertoRepository.save(CatalogoPuerto.builder()
                                    .codigoPuerto(p.getCodigoPuerto())
                                    .nombre(p.getNombre())
                                    .pais(p.getPais())
                                    .tipo(p.getTipo() != null ? p.getTipo() : "MARITIMO")
                                    .estado(p.getEstado() != null ? p.getEstado() : "ACTIVO")
                                    .build());
                        }
                    }
                    log.info("Catálogo de puertos cargado desde initial-data.json con éxito.");
                }

                // Cargar tipos de carga desde JSON
                if (seedData.getTiposCarga() != null && tipoCargaRepository.count() == 0) {
                    for (InitialSeedDataDTO.TipoCargaSeedDTO tc : seedData.getTiposCarga()) {
                        if (!tipoCargaRepository.existsByCodigo(tc.getCodigo())) {
                            tipoCargaRepository.save(CatalogoTipoCarga.builder()
                                    .codigo(tc.getCodigo())
                                    .nombre(tc.getNombre())
                                    .estado(tc.getEstado() != null ? tc.getEstado() : "ACTIVO")
                                    .build());
                        }
                    }
                    log.info("Catálogo de tipos de carga cargado desde initial-data.json con éxito.");
                }

                    // Cargar usuario administrador desde JSON
                    Rol rolAdminTi = rolRepository.findByNombreRol(RolUsuario.ADMIN_TI)
                            .or(() -> rolRepository.findByNombreRol(RolUsuario.ADMINISTRADOR))
                            .orElse(null);

                    Sede sede = sedeRepository.findAll().stream().findFirst().orElse(null);

                    if (seedData.getAdmin() != null && !usuarioRepository.existsByUsername(seedData.getAdmin().getUsername())) {
                        InitialSeedDataDTO.AdminSeedDTO adminDto = seedData.getAdmin();
                        usuarioRepository.save(Usuario.builder()
                                .rol(rolAdminTi)
                                .sede(sede)
                                .username(adminDto.getUsername())
                                .passwordHash(passwordEncoder.encode(adminDto.getPassword()))
                                .nombres(adminDto.getNombres())
                                .apellidos(adminDto.getApellidos())
                                .correoInstitucional(adminDto.getCorreoInstitucional())
                                .tipoDocumento(adminDto.getTipoDocumento() != null ? adminDto.getTipoDocumento() : "DNI")
                                .numeroDocumento(adminDto.getNumeroDocumento())
                                .telefono(adminDto.getTelefono())
                                .cargoTitulo(adminDto.getCargoTitulo())
                                .codigoSunat(adminDto.getCodigoSunat())
                                .estadoActivo(true)
                                .build());
                        log.info("Usuario inicial '{}' cargado exitosamente desde initial-data.json.", adminDto.getUsername());
                    }

                    // Sembrar usuarios de prueba para los roles principales si no existen
                    seedUserIfMissing("gerente", "Gerente General", "Operaciones", "gerente@sga.pe", RolUsuario.GERENTE, sede);
                    seedUserIfMissing("vendedor", "Vendedor", "Comercial", "ventas@sga.pe", RolUsuario.VENTAS, sede);
                    seedUserIfMissing("supervisor", "Supervisor", "Aduanero", "supervisor@sga.pe", RolUsuario.SUPERVISION, sede);
                    seedUserIfMissing("agente", "Agente", "Aduanas", "agente@sga.pe", RolUsuario.AGENTE_ADUANERO, sede);
            }
        } catch (Exception e) {
            log.error("Error al procesar initial-data.json: {}", e.getMessage(), e);
        }
    }

    private void seedUserIfMissing(String username, String nombres, String apellidos, String correo, RolUsuario rolEnum, Sede sede) {
        if (!usuarioRepository.existsByUsername(username)) {
            rolRepository.findByNombreRol(rolEnum).ifPresent(rol -> {
                usuarioRepository.save(Usuario.builder()
                        .rol(rol)
                        .sede(sede)
                        .username(username)
                        .passwordHash(passwordEncoder.encode("Password123!"))
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .correoInstitucional(correo)
                        .tipoDocumento("DNI")
                        .numeroDocumento("7000000" + (username.length()))
                        .telefono("+51 900 000 000")
                        .cargoTitulo("Especialista " + rolEnum.name())
                        .codigoSunat("SUNAT-" + rolEnum.name())
                        .estadoActivo(true)
                        .build());
                log.info("Usuario semilla '{}' creado con rol {}.", username, rolEnum);
            });
        }
    }

}
