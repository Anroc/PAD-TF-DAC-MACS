package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CspClientRestTemplate extends ClientRestTemplate implements CSPClient {

    @Override
    public CspClientRestTemplate withHeaders(@NonNull HttpHeaders headers) {
        return (CspClientRestTemplate) super.withHeaders(headers);
    }

    @Autowired
    public CspClientRestTemplate(@Qualifier(RestTemplateFactory.CSP_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public void createCipherText(CipherTextDTO cipherTextDTO) {
        request("/ciphertexts",
                HttpMethod.POST,
                CipherTextDTO.class,
                cipherTextDTO
        );
    }

    @Override
    public void createFile(String id, MultiValueMap<String, Object> file) {
        request("/files",
                HttpMethod.POST,
                Void.class,
                file
        );
    }
}