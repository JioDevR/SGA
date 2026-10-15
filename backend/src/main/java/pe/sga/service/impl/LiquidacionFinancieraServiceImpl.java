package pe.sga.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sga.dto.request.LiquidacionFinancieraRequestDTO;
import pe.sga.dto.request.LiquidacionPagoDTO;
import pe.sga.dto.response.LiquidacionFinancieraResponseDTO;
import pe.sga.entity.Expediente;
import pe.sga.entity.LiquidacionFinanciera;
import pe.sga.exception.BadRequestException;
import pe.sga.exception.ResourceNotFoundException;
import pe.sga.repository.ExpedienteRepository;
import pe.sga.repository.LiquidacionFinancieraRepository;
import pe.sga.service.ILiquidacionFinancieraService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionFinancieraServiceImpl implements ILiquidacionFinancieraService {

    private final LiquidacionFinancieraRepository liquidacionRepository;
    private final ExpedienteRepository expedienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionFinancieraResponseDTO> listarTodas(Boolean soloPendientes) {
        if (soloPendientes != null && soloPendientes) {
            return liquidacionRepository.findByEstadoPago("PENDIENTE").stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return liquidacionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionFinancieraResponseDTO> listarPorEstado(String estadoPago) {
        return liquidacionRepository.findByEstadoPago(estadoPago).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionFinancieraResponseDTO buscarPorId(Integer idLiquidacion) {
        LiquidacionFinanciera lf = liquidacionRepository.findById(idLiquidacion)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con ID: " + idLiquidacion));
        return mapToResponse(lf);
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionFinancieraResponseDTO buscarPorCodigo(String codigoLiquidacion) {
        LiquidacionFinanciera lf = liquidacionRepository.findByCodigoLiquidacion(codigoLiquidacion)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con código: " + codigoLiquidacion));
        return mapToResponse(lf);
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionFinancieraResponseDTO buscarPorExpediente(Integer idExpediente) {
        LiquidacionFinanciera lf = liquidacionRepository.findByExpediente_IdExpediente(idExpediente)
                .orElseThrow(() -> new ResourceNotFoundException("No existe liquidación para el expediente ID: " + idExpediente));
        return mapToResponse(lf);
    }

    @Override
    @Transactional
    public LiquidacionFinancieraResponseDTO emitir(LiquidacionFinancieraRequestDTO dto) {
        Expediente expediente = expedienteRepository.findById(dto.getIdExpediente())
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + dto.getIdExpediente()));

        if (liquidacionRepository.existsByExpediente_IdExpediente(dto.getIdExpediente())) {
            throw new BadRequestException("Ya existe una liquidación financiera para el expediente ID: " + dto.getIdExpediente());
        }

        BigDecimal tributos = dto.getTotalTributosSunat() != null ? dto.getTotalTributosSunat() : BigDecimal.ZERO;
        BigDecimal almacen = dto.getGastosAlmacenPuerto() != null ? dto.getGastosAlmacenPuerto() : BigDecimal.ZERO;
        BigDecimal flete = dto.getFleteTransporteLocal() != null ? dto.getFleteTransporteLocal() : BigDecimal.ZERO;
        BigDecimal honorarios = dto.getHonorariosAgencia() != null ? dto.getHonorariosAgencia() : BigDecimal.ZERO;
        BigDecimal totalPen = tributos.add(almacen).add(flete).add(honorarios);

        BigDecimal tc = dto.getTipoCambio() != null ? dto.getTipoCambio() : new BigDecimal("3.7500");
        LocalDate emision = dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDate.now();
        String codigo = generarCodigoLiquidacion();

        LiquidacionFinanciera lf = LiquidacionFinanciera.builder()
                .codigoLiquidacion(codigo)
                .expediente(expediente)
                .totalTributosSunat(tributos)
                .gastosAlmacenPuerto(almacen)
                .fleteTransporteLocal(flete)
                .honorariosAgencia(honorarios)
                .totalGeneralPen(totalPen)
                .tipoCambio(tc)
                .estadoPago("PENDIENTE")
                .numeroFactura(dto.getNumeroFactura() != null ? dto.getNumeroFactura().trim() : null)
                .fechaEmision(emision)
                .observaciones(dto.getObservaciones())
                .build();

        return mapToResponse(liquidacionRepository.save(lf));
    }

    @Override
    @Transactional
    public LiquidacionFinancieraResponseDTO actualizar(Integer idLiquidacion, LiquidacionFinancieraRequestDTO dto) {
        LiquidacionFinanciera lf = liquidacionRepository.findById(idLiquidacion)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con ID: " + idLiquidacion));

        if (lf.getEstadoPago().equalsIgnoreCase("PAGADO") || lf.getEstadoPago().equalsIgnoreCase("ANULADO")) {
            throw new BadRequestException("No se puede editar una liquidación en estado: " + lf.getEstadoPago());
        }

        if (dto.getTotalTributosSunat() != null) lf.setTotalTributosSunat(dto.getTotalTributosSunat());
        if (dto.getGastosAlmacenPuerto() != null) lf.setGastosAlmacenPuerto(dto.getGastosAlmacenPuerto());
        if (dto.getFleteTransporteLocal() != null) lf.setFleteTransporteLocal(dto.getFleteTransporteLocal());
        if (dto.getHonorariosAgencia() != null) lf.setHonorariosAgencia(dto.getHonorariosAgencia());
        if (dto.getTipoCambio() != null) lf.setTipoCambio(dto.getTipoCambio());
        if (dto.getNumeroFactura() != null) lf.setNumeroFactura(dto.getNumeroFactura().trim());
        if (dto.getObservaciones() != null) lf.setObservaciones(dto.getObservaciones());

        // Recalcular total consolidado
        BigDecimal total = lf.getTotalTributosSunat()
                .add(lf.getGastosAlmacenPuerto())
                .add(lf.getFleteTransporteLocal())
                .add(lf.getHonorariosAgencia());
        lf.setTotalGeneralPen(total);

        return mapToResponse(liquidacionRepository.save(lf));
    }

    @Override
    @Transactional
    public LiquidacionFinancieraResponseDTO registrarPago(Integer idLiquidacion, LiquidacionPagoDTO dto) {
        LiquidacionFinanciera lf = liquidacionRepository.findById(idLiquidacion)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con ID: " + idLiquidacion));

        if (lf.getEstadoPago().equalsIgnoreCase("PAGADO")) {
            throw new BadRequestException("Esta liquidación ya se encuentra registrada como pagada");
        }
        if (lf.getEstadoPago().equalsIgnoreCase("ANULADO")) {
            throw new BadRequestException("No se puede registrar pago para una liquidación anulada");
        }

        lf.setMetodoPago(dto.getMetodoPago().trim().toUpperCase());
        lf.setNumeroOperacion(dto.getNumeroOperacion().trim());
        if (dto.getNumeroFactura() != null) {
            lf.setNumeroFactura(dto.getNumeroFactura().trim());
        }
        lf.setFechaPago(dto.getFechaPago() != null ? dto.getFechaPago() : LocalDateTime.now());
        lf.setEstadoPago("PAGADO");
        if (dto.getObservaciones() != null) {
            lf.setObservaciones(dto.getObservaciones());
        }

        return mapToResponse(liquidacionRepository.save(lf));
    }

    @Override
    @Transactional
    public void anular(Integer idLiquidacion) {
        LiquidacionFinanciera lf = liquidacionRepository.findById(idLiquidacion)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidación no encontrada con ID: " + idLiquidacion));
        lf.setEstadoPago("ANULADO");
        liquidacionRepository.save(lf);
    }

    private String generarCodigoLiquidacion() {
        int year = LocalDate.now().getYear();
        long count = liquidacionRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("LIQ-%d-%04d", year, count);
            count++;
        } while (liquidacionRepository.existsByCodigoLiquidacion(codigo));
        return codigo;
    }

    private LiquidacionFinancieraResponseDTO mapToResponse(LiquidacionFinanciera lf) {
        return LiquidacionFinancieraResponseDTO.builder()
                .idLiquidacion(lf.getIdLiquidacion())
                .codigoLiquidacion(lf.getCodigoLiquidacion())
                .idExpediente(lf.getExpediente() != null ? lf.getExpediente().getIdExpediente() : null)
                .codigoExpediente(lf.getExpediente() != null ? lf.getExpediente().getCodigoExpediente() : null)
                .numeroDam(lf.getExpediente() != null ? lf.getExpediente().getNumeroDam() : null)
                .clienteRazonSocial(lf.getExpediente() != null && lf.getExpediente().getCliente() != null ? lf.getExpediente().getCliente().getRazonSocial() : null)
                .clienteNumeroDocumento(lf.getExpediente() != null && lf.getExpediente().getCliente() != null ? lf.getExpediente().getCliente().getNumeroDocumento() : null)
                .totalTributosSunat(lf.getTotalTributosSunat())
                .gastosAlmacenPuerto(lf.getGastosAlmacenPuerto())
                .fleteTransporteLocal(lf.getFleteTransporteLocal())
                .honorariosAgencia(lf.getHonorariosAgencia())
                .totalGeneralPen(lf.getTotalGeneralPen())
                .tipoCambio(lf.getTipoCambio())
                .estadoPago(lf.getEstadoPago())
                .numeroFactura(lf.getNumeroFactura())
                .metodoPago(lf.getMetodoPago())
                .numeroOperacion(lf.getNumeroOperacion())
                .fechaEmision(lf.getFechaEmision())
                .fechaPago(lf.getFechaPago())
                .observaciones(lf.getObservaciones())
                .build();
    }
}
