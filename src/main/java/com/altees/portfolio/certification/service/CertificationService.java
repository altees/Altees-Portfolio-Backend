package com.altees.portfolio.certification.service;

import com.altees.portfolio.certification.dto.CertificationRequest;
import com.altees.portfolio.certification.dto.CertificationResponse;
import com.altees.portfolio.certification.entity.Certification;
import com.altees.portfolio.certification.repository.CertificationRepository;
import com.altees.portfolio.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CertificationService {

    private final CertificationRepository certificationRepository;

    public CertificationService(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> findAll() {
        return certificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CertificationResponse findById(Long id) {
        return certificationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", id));
    }

    public CertificationResponse create(CertificationRequest request) {
        Certification certification = new Certification();
        applyRequest(certification, request);
        return toResponse(certificationRepository.save(certification));
    }

    public CertificationResponse update(Long id, CertificationRequest request) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", id));
        applyRequest(certification, request);
        return toResponse(certificationRepository.save(certification));
    }

    public void delete(Long id) {
        if (!certificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certification", id);
        }
        certificationRepository.deleteById(id);
    }

    private void applyRequest(Certification certification, CertificationRequest request) {
        certification.setName(request.name());
        certification.setIssuingOrganization(request.issuingOrganization());
        certification.setIssueDate(request.issueDate());
        certification.setExpiryDate(request.expiryDate());
        certification.setCredentialId(request.credentialId());
        certification.setCredentialUrl(request.credentialUrl());
    }

    public CertificationResponse toResponse(Certification certification) {
        return new CertificationResponse(
                certification.getId(),
                certification.getName(),
                certification.getIssuingOrganization(),
                certification.getIssueDate(),
                certification.getExpiryDate(),
                certification.getCredentialId(),
                certification.getCredentialUrl(),
                certification.getCreatedAt(),
                certification.getUpdatedAt()
        );
    }
}
