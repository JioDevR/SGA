package pe.sga.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sga.dto.request.DespachoAsignarRecursosDTO;
import pe.sga.dto.request.DespachoEntregaDTO;
import pe.sga.dto.request.DespachoRutaDTO;
import pe.sga.dto.request.DespachoTransporteCrearDTO;
import pe.sga.dto.response.DespachoTransporteResponseDTO;
import pe.sga.entity.Conductor;
import pe.sga.entity.DespachoTransporte;
import pe.sga.entity.Expediente;
import pe.sga.entity.UnidadTransporte;
import pe.sga.exception.BadRequestException;
import pe.sga.exception.ResourceNotFoundException;
import pe.sga.repository.ConductorRepository;
import pe.sga.repository.DespachoTransporteRepository;
import pe.sga.repository.ExpedienteRepository;
import pe.sga.repository.UnidadTransporteRepository;
import pe.sga.service.IDespachoTransporteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DespachoTransporteServiceImpl implements IDespachoTransporteService {

    private final DespachoTransporteRepository despachoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final ConductorRepository conductorRepository;
    private final UnidadTransporteRepository unidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DespachoTransporteResponseDTO> listarTodos(Boolean soloActivos) {
        if (soloActivos != null && soloActivos) {
            return despachoRepository.findByEstadoTransporteNot("CANCELADO").stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return despachoRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoTransporteResponseDTO> listarPorEstado(String estadoTransporte) {
        return despachoRepository.findByEstadoTransporte(estadoTransporte).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoTransporteResponseDTO> listarPorConductor(Integer idConductor) {
        return despachoRepository.findByConductor_IdConductor(idConductor).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoTransporteResponseDTO> listarPorUnidad(Integer idUnidad) {
        return despachoRepository.findByUnidad_IdUnidad(idUnidad).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DespachoTransporteResponseDTO buscarPorId(Integer idDespachoTransporte) {
        DespachoTransporte dt = despachoRepository.findById(idDespachoTransporte)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + idDespachoTransporte));
        return mapToResponse(dt);
    }

    @Override
    @Transactional(readOnly = true)
    public DespachoTransporteResponseDTO buscarPorTracking(String codigoTracking) {
        DespachoTransporte dt = despachoRepository.findByCodigoTracking(codigoTracking)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con tracking: " + codigoTracking));
        return mapToResponse(dt);
    }

    @Override
    @Transactional(readOnly = true)
    public DespachoTransporteResponseDTO buscarPorExpediente(Integer idExpediente) {
        DespachoTransporte dt = despachoRepository.findByExpediente_IdExpediente(idExpediente)
                .orElseThrow(() -> new ResourceNotFoundException("No existe despacho para el expediente ID: " + idExpediente));
        return mapToResponse(dt);
    }

    @Override
    @Transactional
    public DespachoTransporteResponseDTO programar(DespachoTransporteCrearDTO dto) {
        Expediente expediente = expedienteRepository.findById(dto.getIdExpediente())
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + dto.getIdExpediente()));

        if (despachoRepository.existsByExpediente_IdExpediente(dto.getIdExpediente())) {
            throw new BadRequestException("Ya existe un despacho de transporte para el expediente ID: " + dto.getIdExpediente());
        }

        Conductor conductor = null;
        if (dto.getIdConductor() != null) {
            conductor = conductorRepository.findById(dto.getIdConductor())
                    .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + dto.getIdConductor()));
        }

        UnidadTransporte unidad = null;
        if (dto.getIdUnidad() != null) {
            unidad = unidadRepository.findById(dto.getIdUnidad())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + dto.getIdUnidad()));
        }

        String tracking = generarCodigoTracking();
        String estadoInicial = (conductor != null && unidad != null) ? "RECURSOS_ASIGNADOS" : "PENDIENTE_ASIGNACION";

        DespachoTransporte dt = DespachoTransporte.builder()
                .expediente(expediente)
                .codigoTracking(tracking)
                .tipoTraslado(dto.getTipoTraslado() != null ? dto.getTipoTraslado().trim().toUpperCase() : "ENTREGA_IMPORTACION")
                .conductor(conductor)
                .unidad(unidad)
                .guiaRemisionTransporte(dto.getGuiaRemisionTransporte() != null ? dto.getGuiaRemisionTransporte().trim() : null)
                .direccionPartida(dto.getDireccionPartida().trim())
                .direccionLlegada(dto.getDireccionLlegada().trim())
                .distritoLlegada(dto.getDistritoLlegada().trim())
                .contactoRecepcion(dto.getContactoRecepcion() != null ? dto.getContactoRecepcion().trim() : null)
                .fechaProgramada(dto.getFechaProgramada())
                .estadoTransporte(estadoInicial)
                .observaciones(dto.getObservaciones())
                .build();

        return mapToResponse(despachoRepository.save(dt));
    }

    @Override
    @Transactional
    public DespachoTransporteResponseDTO asignarRecursos(Integer idDespachoTransporte, DespachoAsignarRecursosDTO dto) {
        DespachoTransporte dt = despachoRepository.findById(idDespachoTransporte)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + idDespachoTransporte));

        Conductor conductor = conductorRepository.findById(dto.getIdConductor())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + dto.getIdConductor()));

        UnidadTransporte unidad = unidadRepository.findById(dto.getIdUnidad())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + dto.getIdUnidad()));

        dt.setConductor(conductor);
        dt.setUnidad(unidad);
        if (dto.getGuiaRemisionTransporte() != null) {
            dt.setGuiaRemisionTransporte(dto.getGuiaRemisionTransporte().trim());
        }
        if (dto.getObservaciones() != null) {
            dt.setObservaciones(dto.getObservaciones());
        }
        dt.setEstadoTransporte("RECURSOS_ASIGNADOS");

        return mapToResponse(despachoRepository.save(dt));
    }

    @Override
    @Transactional
    public DespachoTransporteResponseDTO iniciarRuta(Integer idDespachoTransporte, DespachoRutaDTO dto) {
        DespachoTransporte dt = despachoRepository.findById(idDespachoTransporte)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + idDespachoTransporte));

        if (dt.getConductor() == null || dt.getUnidad() == null) {
            throw new BadRequestException("No se puede iniciar ruta sin antes haber asignado conductor y unidad de transporte");
        }

        dt.setFechaSalidaRuta(dto.getFechaSalidaRuta() != null ? dto.getFechaSalidaRuta() : LocalDateTime.now());
        if (dto.getGuiaRemisionTransporte() != null) {
            dt.setGuiaRemisionTransporte(dto.getGuiaRemisionTransporte().trim());
        }
        if (dto.getObservaciones() != null) {
            dt.setObservaciones(dto.getObservaciones());
        }
        dt.setEstadoTransporte("EN_RUTA");

        // Actualizar vehículo a EN_RUTA
        UnidadTransporte unidad = dt.getUnidad();
        unidad.setEstadoOperativo("EN_RUTA");
        unidadRepository.save(unidad);

        return mapToResponse(despachoRepository.save(dt));
    }

    @Override
    @Transactional
    public DespachoTransporteResponseDTO confirmarEntrega(Integer idDespachoTransporte, DespachoEntregaDTO dto) {
        DespachoTransporte dt = despachoRepository.findById(idDespachoTransporte)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + idDespachoTransporte));

        dt.setFechaEntregaConforme(dto.getFechaEntregaConforme() != null ? dto.getFechaEntregaConforme() : LocalDateTime.now());
        if (dto.getContactoRecepcion() != null) {
            dt.setContactoRecepcion(dto.getContactoRecepcion().trim());
        }
        if (dto.getDetalleIncidencia() != null) {
            dt.setDetalleIncidencia(dto.getDetalleIncidencia());
        }
        if (dto.getObservaciones() != null) {
            dt.setObservaciones(dto.getObservaciones());
        }
        dt.setEstadoTransporte("ENTREGADO");

        // Liberar vehículo a DISPONIBLE
        if (dt.getUnidad() != null) {
            UnidadTransporte unidad = dt.getUnidad();
            unidad.setEstadoOperativo("DISPONIBLE");
            unidadRepository.save(unidad);
        }

        return mapToResponse(despachoRepository.save(dt));
    }

    @Override
    @Transactional
    public void anular(Integer idDespachoTransporte) {
        DespachoTransporte dt = despachoRepository.findById(idDespachoTransporte)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + idDespachoTransporte));

        dt.setEstadoTransporte("CANCELADO");

        if (dt.getUnidad() != null) {
            UnidadTransporte unidad = dt.getUnidad();
            unidad.setEstadoOperativo("DISPONIBLE");
            unidadRepository.save(unidad);
        }

        despachoRepository.save(dt);
    }

    private String generarCodigoTracking() {
        int year = LocalDate.now().getYear();
        long count = despachoRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("TRK-%d-%04d", year, count);
            count++;
        } while (despachoRepository.existsByCodigoTracking(codigo));
        return codigo;
    }

    private DespachoTransporteResponseDTO mapToResponse(DespachoTransporte dt) {
        return DespachoTransporteResponseDTO.builder()
                .idDespachoTransporte(dt.getIdDespachoTransporte())
                .codigoTracking(dt.getCodigoTracking())
                .tipoTraslado(dt.getTipoTraslado())
                .idExpediente(dt.getExpediente() != null ? dt.getExpediente().getIdExpediente() : null)
                .codigoExpediente(dt.getExpediente() != null ? dt.getExpediente().getCodigoExpediente() : null)
                .numeroDam(dt.getExpediente() != null ? dt.getExpediente().getNumeroDam() : null)
                .clienteRazonSocial(dt.getExpediente() != null && dt.getExpediente().getCliente() != null ? dt.getExpediente().getCliente().getRazonSocial() : null)
                .idConductor(dt.getConductor() != null ? dt.getConductor().getIdConductor() : null)
                .conductorNombre(dt.getConductor() != null ? dt.getConductor().getNombres() + " " + dt.getConductor().getApellidos() : null)
                .conductorLicencia(dt.getConductor() != null ? dt.getConductor().getNumeroLicencia() : null)
                .conductorTelefono(dt.getConductor() != null ? dt.getConductor().getTelefono() : null)
                .idUnidad(dt.getUnidad() != null ? dt.getUnidad().getIdUnidad() : null)
                .unidadPlacaTracto(dt.getUnidad() != null ? dt.getUnidad().getPlacaTracto() : null)
                .unidadPlacaCarreta(dt.getUnidad() != null ? dt.getUnidad().getPlacaCarreta() : null)
                .unidadMarcaModelo(dt.getUnidad() != null ? dt.getUnidad().getMarcaModelo() : null)
                .guiaRemisionTransporte(dt.getGuiaRemisionTransporte())
                .direccionPartida(dt.getDireccionPartida())
                .direccionLlegada(dt.getDireccionLlegada())
                .distritoLlegada(dt.getDistritoLlegada())
                .contactoRecepcion(dt.getContactoRecepcion())
                .fechaProgramada(dt.getFechaProgramada())
                .fechaSalidaRuta(dt.getFechaSalidaRuta())
                .fechaEntregaConforme(dt.getFechaEntregaConforme())
                .estadoTransporte(dt.getEstadoTransporte())
                .detalleIncidencia(dt.getDetalleIncidencia())
                .observaciones(dt.getObservaciones())
                .build();
    }
}
