package de.tuberlin.tfdacmacs.centralserver.ciphertext;

import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.centralserver.ciphertext.db.CipherTextDB;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingCryptEngine;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CipherTextService {

    private final CipherTextDB cipherTextDB;
    private final PairingCryptEngine pairingCryptEngine;
    private final AccessPolicyUtils accessPolicyUtils;
    private final GlobalPublicParameterProvider globalPublicParameterProvider;

    public void insert(@NonNull CipherTextEntity cipherTextEntity) {
        cipherTextDB.insert(cipherTextEntity);
    }

    public Optional<CipherTextEntity> findCipherText(String id) {
        return cipherTextDB.findEntity(id);
    }

    public List<CipherTextEntity> findAll() {
        return cipherTextDB.findAll();
    }

    public List<CipherTextEntity> findAllByPolicyContaining(List<String> attributeIds) {
        return cipherTextDB.findAllByPolicyContaining(attributeIds);
    }

    public List<CipherTextEntity> findAllByOwnerId(String ownerId) {
        return cipherTextDB.findAllByOwnerId(ownerId);
    }

    public List<CipherTextEntity> update(String ownerId, Set<CipherText2FAUpdateKey> cipherText2FAUpdateKeys) {
        GlobalPublicParameter gpp = globalPublicParameterProvider.getGlobalPublicParameter();

        return findAllByOwnerId(ownerId)
                .stream()
                .map(CipherTextEntity::toCipherText)
                .peek(cipherText -> log.info("Updating cipher text [{}] of data owner [{}] and access policy {}...",
                        cipherText.getId(),
                        cipherText.getOwnerId(),
                        cipherText.getAccessPolicy()))
                .map(cipherText -> pairingCryptEngine.update(
                        cipherText,
                        accessPolicyUtils.buildAccessPolicy(cipherText.getAccessPolicy()),
                        cipherText2FAUpdateKeys,
                        gpp))
                .peek(cipherText -> log.info("Finished cipher text update [{}] ", cipherText.getId()))
                .map(CipherTextEntity::from)
                .peek(cipherTextDB::update)
                .collect(Collectors.toList());
    }
}
