package pe.sga.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.sga.dto.request.ConductorRequestDTO;
import pe.sga.dto.response.ConductorResponseDTO;
import pe.sga.entity.Conductor;
import pe.sga.exception.BadRequestException;
import pe.sga.exception.ResourceNotFoundException;
import pe.sga.repository.ConductorRepository;
import pe.sga.service.IConductorService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConductorServiceImpl implements IConductorService {

    private final ConductorRepository conductorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConductorResponseDTO> listarTodos() {
        return conductorRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConductorResponseDTO> listarActivos() {
        return conductorRepository.findByEstadoActivo(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorResponseDTO buscarPorId(Integer idConductor) {
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + idConductor));
        return mapToResponse(conductor);
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorResponseDTO buscarPorLicencia(String numeroLicencia) {
        Conductor conductor = conductorRepository.findByNumeroLicencia(numeroLicencia)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con licencia: " + numeroLicencia));
        return mapToResponse(conductor);
    }

    @Override
    @Transactional
    public ConductorResponseDTO crear(ConductorRequestDTO dto) {
        String licencia = dto.getNumeroLicencia().trim().toUpperCase();
        if (conductorRepository.existsByNumeroLicencia(licencia)) {
            throw new BadRequestException("Ya existe un conductor registrado con la licencia: " + licencia);
        }

        Conductor conductor = Conductor.builder()
                .nombres(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .tipoLicencia(dto.getTipoLicencia() != null ? dto.getTipoLicencia().trim().toUpperCase() : "A-IIIc")
                .numeroLicencia(licencia)
                .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                .estadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true)
                .build();

        return mapToResponse(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public ConductorResponseDTO actualizar(Integer idConductor, ConductorRequestDTO dto) {
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + idConductor));

        String licencia = dto.getNumeroLicencia().trim().toUpperCase();
        if (!conductor.getNumeroLicencia().equalsIgnoreCase(licencia)) {
            if (conductorRepository.existsByNumeroLicencia(licencia)) {
                throw new BadRequestException("Ya existe otro conductor registrado con la licencia: " + licencia);
            }
            conductor.setNumeroLicencia(licencia);
        }

        if (dto.getNombres() != null) conductor.setNombres(dto.getNombres().trim());
        if (dto.getApellidos() != null) conductor.setApellidos(dto.getApellidos().trim());
        if (dto.getTipoLicencia() != null) conductor.setTipoLicencia(dto.getTipoLicencia().trim().toUpperCase());
        if (dto.getTelefono() != null) conductor.setTelefono(dto.getTelefono().trim());
        if (dto.getEstadoActivo() != null) conductor.setEstadoActivo(dto.getEstadoActivo());

        return mapToResponse(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public void eliminar(Integer idConductor) {
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + idConductor));
        // Borrado lógico estándar
        conductor.setEstadoActivo(false);
        conductorRepository.save(conductor);
    }

    @Override
    @Transactional
    public void reactivar(Integer idConductor) {
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + idConductor));
        conductor.setEstadoActivo(true);
        conductorRepository.save(conductor);
    }

    private ConductorResponseDTO mapToResponse(Conductor c) {
        return ConductorResponseDTO.builder()
                .idConductor(c.getIdConductor())
                .nombres(c.getNombres())
                .apellidos(c.getApellidos())
                .nombreCompleto(c.getNombres() + " " + c.getApellidos())
                .tipoLicencia(c.getTipoLicencia())
                .numeroLicencia(c.getNumeroLicencia())
                .telefono(c.getTelefono())
                .estadoActivo(c.getEstadoActivo())
                .build();
    }
}
