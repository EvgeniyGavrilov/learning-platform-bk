package com.medical_learning_platform.app.auth.code;

import com.medical_learning_platform.app.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Random;
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeRepository codeRepository;
    private final EmailService emailService;

    public Mono<Void> generateAndSendCode(String email) {
        String generatedCode = String.format("%06d", new Random().nextInt(1_000_000));
        String hash = DigestUtils.sha256Hex(generatedCode); // Apache Commons Codec
        log.info("generatedCode: {}", generatedCode);
        Code code = new Code();
        code.setEmail(email);
        code.setHash(hash);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        return codeRepository.deleteByEmail(email)
            .then(codeRepository.save(code))
            .then(sendEmail(email, generatedCode))
            .then();
    }

    public Mono<Boolean> verifyCode(String email, String code) {
        return codeRepository.findByEmail(email)
            .filter(lc -> lc.getExpiresAt().isAfter(LocalDateTime.now()))
            .filter(lc -> DigestUtils.sha256Hex(code).equals(lc.getHash()))
            .flatMap(c -> codeRepository.deleteByEmail(email).thenReturn(true))
            .defaultIfEmpty(false);
    }

    private Mono<Void> sendEmail(String email, String code) {
        log.info("sendEmail: email {}, code {}", email, code);
        return this.emailService.sendEmail(email, "Verification code", code);
    }
}
