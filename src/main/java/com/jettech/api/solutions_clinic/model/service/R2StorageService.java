package com.jettech.api.solutions_clinic.model.service;

import com.jettech.api.solutions_clinic.config.R2Config;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Gera presigned URLs para upload direto de arquivos de exame no Cloudflare R2.
 * O frontend usa a URL para enviar o arquivo (PUT) e depois chama o backend para confirmar a chave.
 */
@Service
public class R2StorageService {

    private final S3Presigner presigner;
    private final R2Config r2Config;

    public R2StorageService(
            @Autowired(required = false) S3Presigner r2S3Presigner,
            R2Config r2Config) {
        this.presigner = r2S3Presigner;
        this.r2Config = r2Config;
    }

    /**
     * Gera uma presigned URL para upload PUT do arquivo no bucket R2.
     *
     * @param objectKey chave do objeto no bucket (ex: "tenants/{tenantId}/exams/{examId}/resultado.pdf")
     * @param expiryMinutes validade da URL em minutos (recomendado 5)
     * @return URL para PUT do arquivo, ou null se R2 não estiver configurado
     */
    public String createPresignedPutUrl(String objectKey, int expiryMinutes) {
        if (presigner == null || !r2Config.isConfigured()) {
            return null;
        }
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(objectKey)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .putObjectRequest(putRequest)
                .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }

    /**
     * Gera uma presigned URL para download/visualização GET do arquivo no bucket R2.
     */
    public String createPresignedGetUrl(String objectKey, int expiryMinutes) {
        if (presigner == null || !r2Config.isConfigured()) {
            return null;
        }
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(getRequest)
                .build();
        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    public boolean isConfigured() {
        return presigner != null && r2Config.isConfigured();
    }
}
