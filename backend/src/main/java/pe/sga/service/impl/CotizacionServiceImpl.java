package pe.sga.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sga.dto.request.CotizacionRequestDTO;
import pe.sga.dto.request.CotizacionRevisionDTO;
import pe.sga.dto.response.CotizacionResponseDTO;
import pe.sga.entity.*;
import pe.sga.entity.enums.EstadoCotizacion;
import pe.sga.exception.BadRequestException;
import pe.sga.exception.ResourceNotFoundException;
import pe.sga.repository.*;
import pe.sga.service.ICotizacionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements ICotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoMercanciaRepository mercanciaRepository;
    private final CatalogoTipoCargaRepository tipoCargaRepository;
    private final CatalogoPuertoRepository puertoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResponseDTO> listarTodas(Boolean soloActivas) {
        if (soloActivas != null && soloActivas) {
            return cotizacionRepository.findByEstadoNot(EstadoCotizacion.ANULADA).stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return cotizacionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResponseDTO> listarPorEstado(EstadoCotizacion estado) {
        if (estado == null) {
            return listarTodas(false);
        }
        return cotizacionRepository.findByEstado(estado).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResponseDTO> listarPorCliente(Integer idCliente) {
        return cotizacionRepository.findByCliente_IdCliente(idCliente).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionResponseDTO buscarPorId(Integer id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));
        return mapToResponse(cotizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionResponseDTO buscarPorCodigo(String codigoCotizacion) {
        Cotizacion cotizacion = cotizacionRepository.findByCodigoCotizacion(codigoCotizacion)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con código: " + codigoCotizacion));
        return mapToResponse(cotizacion);
    }

    @Override
    @Transactional
    public CotizacionResponseDTO crear(CotizacionRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getIdCliente()));

        Usuario usuarioComercial = usuarioRepository.findById(dto.getIdUsuarioComercial())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario comercial no encontrado con id: " + dto.getIdUsuarioComercial()));

        CatalogoMercancia mercancia = null;
        if (dto.getIdMercancia() != null) {
            mercancia = mercanciaRepository.findById(dto.getIdMercancia())
                    .orElseThrow(() -> new ResourceNotFoundException("Mercancía no encontrada con id: " + dto.getIdMercancia()));
        }

        CatalogoTipoCarga tipoCarga = null;
        if (dto.getIdTipoCarga() != null) {
            tipoCarga = tipoCargaRepository.findById(dto.getIdTipoCarga())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de carga no encontrado con id: " + dto.getIdTipoCarga()));
        }

        CatalogoPuerto puertoEnvio = null;
        if (dto.getIdPuertoEnvio() != null) {
            puertoEnvio = puertoRepository.findById(dto.getIdPuertoEnvio())
                    .orElseThrow(() -> new ResourceNotFoundException("Puerto de envío no encontrado con id: " + dto.getIdPuertoEnvio()));
        }

        CatalogoPuerto puertoDestino = null;
        if (dto.getIdPuertoDestino() != null) {
            puertoDestino = puertoRepository.findById(dto.getIdPuertoDestino())
                    .orElseThrow(() -> new ResourceNotFoundException("Puerto de destino no encontrado con id: " + dto.getIdPuertoDestino()));
        }

        String codigo = dto.getCodigoCotizacion();
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = generarCodigoCotizacion();
        } else {
            codigo = codigo.trim().toUpperCase();
            if (cotizacionRepository.existsByCodigoCotizacion(codigo)) {
                throw new BadRequestException("El código de cotización ya existe: " + codigo);
            }
        }

        LocalDate fecha = dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDate.now();

        Cotizacion cotizacion = Cotizacion.builder()
                .codigoCotizacion(codigo)
                .cliente(cliente)
                .usuarioComercial(usuarioComercial)
                .mercancia(mercancia)
                .tipoCarga(tipoCarga)
                .regimenAduanero(dto.getRegimenAduanero() != null ? dto.getRegimenAduanero().trim().toUpperCase() : "IMPORTACION_DEFINITIVA")
                .puertoEnvio(puertoEnvio)
                .puertoDestino(puertoDestino)
                .servicioTransporte(dto.getServicioTransporte() != null ? dto.getServicioTransporte() : false)
                .fechaEmision(fecha)
                .validezDias(dto.getValidezDias() != null ? dto.getValidezDias() : 15)
                .montoFleteEstimado(dto.getMontoFleteEstimado() != null ? dto.getMontoFleteEstimado() : BigDecimal.ZERO)
                .montoHonorariosEstimado(dto.getMontoHonorariosEstimado() != null ? dto.getMontoHonorariosEstimado() : BigDecimal.ZERO)
                .montoTributosEstimado(dto.getMontoTributosEstimado() != null ? dto.getMontoTributosEstimado() : BigDecimal.ZERO)
                .totalCotizado(dto.getTotalCotizado())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCotizacion.PENDIENTE)
                .observaciones(dto.getObservaciones())
                .build();

        return mapToResponse(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionResponseDTO actualizar(Integer id, CotizacionRequestDTO dto) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        if (cotizacion.getEstado() == EstadoCotizacion.APROBADA || cotizacion.getEstado() == EstadoCotizacion.ANULADA) {
            throw new BadRequestException("No se puede editar una cotización en estado: " + cotizacion.getEstado());
        }

        if (dto.getIdCliente() != null && !dto.getIdCliente().equals(cotizacion.getCliente().getIdCliente())) {
            Cliente nuevoCliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getIdCliente()));
            cotizacion.setCliente(nuevoCliente);
        }

        if (dto.getIdMercancia() != null) {
            CatalogoMercancia m = mercanciaRepository.findById(dto.getIdMercancia())
                    .orElseThrow(() -> new ResourceNotFoundException("Mercancía no encontrada con id: " + dto.getIdMercancia()));
            cotizacion.setMercancia(m);
        }

        if (dto.getIdTipoCarga() != null) {
            CatalogoTipoCarga tc = tipoCargaRepository.findById(dto.getIdTipoCarga())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de carga no encontrado con id: " + dto.getIdTipoCarga()));
            cotizacion.setTipoCarga(tc);
        }

        if (dto.getIdPuertoEnvio() != null) {
            CatalogoPuerto pe = puertoRepository.findById(dto.getIdPuertoEnvio())
                    .orElseThrow(() -> new ResourceNotFoundException("Puerto de envío no encontrado con id: " + dto.getIdPuertoEnvio()));
            cotizacion.setPuertoEnvio(pe);
        }

        if (dto.getIdPuertoDestino() != null) {
            CatalogoPuerto pd = puertoRepository.findById(dto.getIdPuertoDestino())
                    .orElseThrow(() -> new ResourceNotFoundException("Puerto de destino no encontrado con id: " + dto.getIdPuertoDestino()));
            cotizacion.setPuertoDestino(pd);
        }

        if (dto.getRegimenAduanero() != null) cotizacion.setRegimenAduanero(dto.getRegimenAduanero());
        if (dto.getServicioTransporte() != null) cotizacion.setServicioTransporte(dto.getServicioTransporte());
        if (dto.getValidezDias() != null) cotizacion.setValidezDias(dto.getValidezDias());
        if (dto.getMontoFleteEstimado() != null) cotizacion.setMontoFleteEstimado(dto.getMontoFleteEstimado());
        if (dto.getMontoHonorariosEstimado() != null) cotizacion.setMontoHonorariosEstimado(dto.getMontoHonorariosEstimado());
        if (dto.getMontoTributosEstimado() != null) cotizacion.setMontoTributosEstimado(dto.getMontoTributosEstimado());
        if (dto.getTotalCotizado() != null) cotizacion.setTotalCotizado(dto.getTotalCotizado());
        if (dto.getObservaciones() != null) cotizacion.setObservaciones(dto.getObservaciones());

        return mapToResponse(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionResponseDTO revisarTecnicamente(Integer id, CotizacionRevisionDTO dto) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        Usuario agenteRevisor = usuarioRepository.findById(dto.getIdAgenteRevisor())
                .orElseThrow(() -> new ResourceNotFoundException("Agente aduanero revisor no encontrado con id: " + dto.getIdAgenteRevisor()));

        if (dto.getEstado() != EstadoCotizacion.APROBADA &&
            dto.getEstado() != EstadoCotizacion.OBSERVADA &&
            dto.getEstado() != EstadoCotizacion.RECHAZADA) {
            throw new BadRequestException("El estado de revisión debe ser APROBADA, OBSERVADA o RECHAZADA");
        }

        if (dto.getEstado() == EstadoCotizacion.OBSERVADA && (dto.getMotivoObservacion() == null || dto.getMotivoObservacion().trim().isEmpty())) {
            throw new BadRequestException("Debe ingresar el motivo de observación para la cotización");
        }

        cotizacion.setAgenteRevisor(agenteRevisor);
        cotizacion.setFechaRevision(LocalDateTime.now());
        cotizacion.setEstado(dto.getEstado());
        cotizacion.setMotivoObservacion(dto.getMotivoObservacion());
        if (dto.getObservaciones() != null) {
            cotizacion.setObservaciones(dto.getObservaciones());
        }

        return mapToResponse(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionResponseDTO cambiarEstado(Integer id, EstadoCotizacion estado, String motivoObservacion) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        cotizacion.setEstado(estado);
        if (motivoObservacion != null) {
            cotizacion.setMotivoObservacion(motivoObservacion);
        }

        return mapToResponse(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public void anular(Integer id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));
        cotizacion.setEstado(EstadoCotizacion.ANULADA);
        cotizacionRepository.save(cotizacion);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Borrado lógico estándar
        anular(id);
    }

    private String generarCodigoCotizacion() {
        int year = LocalDate.now().getYear();
        long count = cotizacionRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("COT-%d-%04d", year, count);
            count++;
        } while (cotizacionRepository.existsByCodigoCotizacion(codigo));
        return codigo;
    }

    private CotizacionResponseDTO mapToResponse(Cotizacion c) {
        return CotizacionResponseDTO.builder()
                .idCotizacion(c.getIdCotizacion())
                .codigoCotizacion(c.getCodigoCotizacion())
                .idCliente(c.getCliente() != null ? c.getCliente().getIdCliente() : null)
                .clienteRazonSocial(c.getCliente() != null ? c.getCliente().getRazonSocial() : null)
                .clienteNumeroDocumento(c.getCliente() != null ? c.getCliente().getNumeroDocumento() : null)
                .idUsuarioComercial(c.getUsuarioComercial() != null ? c.getUsuarioComercial().getIdUsuario() : null)
                .usuarioComercialNombre(c.getUsuarioComercial() != null ? c.getUsuarioComercial().getNombreCompleto() : null)
                .idMercancia(c.getMercancia() != null ? c.getMercancia().getIdMercancia() : null)
                .mercanciaCodigo(c.getMercancia() != null ? c.getMercancia().getCodigo() : null)
                .mercanciaNombre(c.getMercancia() != null ? c.getMercancia().getNombre() : null)
                .idTipoCarga(c.getTipoCarga() != null ? c.getTipoCarga().getIdTipoCarga() : null)
                .tipoCargaNombre(c.getTipoCarga() != null ? c.getTipoCarga().getNombre() : null)
                .regimenAduanero(c.getRegimenAduanero())
                .idPuertoEnvio(c.getPuertoEnvio() != null ? c.getPuertoEnvio().getIdPuerto() : null)
                .puertoEnvioNombre(c.getPuertoEnvio() != null ? c.getPuertoEnvio().getNombre() : null)
                .idPuertoDestino(c.getPuertoDestino() != null ? c.getPuertoDestino().getIdPuerto() : null)
                .puertoDestinoNombre(c.getPuertoDestino() != null ? c.getPuertoDestino().getNombre() : null)
                .servicioTransporte(c.getServicioTransporte())
                .fechaEmision(c.getFechaEmision())
                .validezDias(c.getValidezDias())
                .montoFleteEstimado(c.getMontoFleteEstimado())
                .montoHonorariosEstimado(c.getMontoHonorariosEstimado())
                .montoTributosEstimado(c.getMontoTributosEstimado())
                .totalCotizado(c.getTotalCotizado())
                .estado(c.getEstado())
                .idAgenteRevisor(c.getAgenteRevisor() != null ? c.getAgenteRevisor().getIdUsuario() : null)
                .agenteRevisorNombre(c.getAgenteRevisor() != null ? c.getAgenteRevisor().getNombreCompleto() : null)
                .fechaRevision(c.getFechaRevision())
                .motivoObservacion(c.getMotivoObservacion())
                .observaciones(c.getObservaciones())
                .tieneExpediente(false)
                .build();
    }
}
