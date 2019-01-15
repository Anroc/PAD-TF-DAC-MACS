package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceUpdateRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CAClientRestTemplate implements CAClient {

    private RestTemplate restTemplate;

    private final AttributeAuthorityConfig attributeAuthorityConfig;
    private final KeyStoreConfig keyStoreConfig;

    @PostConstruct
    public void initRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile(keyStoreConfig.getTrustStore()), keyStoreConfig.getTrustStorePassword().toCharArray())
                    .loadKeyMaterial(
                            ResourceUtils.getFile(keyStoreConfig.getKeyStore()),
                            keyStoreConfig.getKeyStorePassword().toCharArray(),
                            keyStoreConfig.getKeyPassword().toCharArray()
                    ).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            restTemplate = new RestTemplateBuilder().rootUri(attributeAuthorityConfig.getCaRootUrl()).build();
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        log.info("Asking CA for [{}:{}]", httpMethod, url);

        ResponseEntity<T> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                responseType
        );

        postProcessResponse(response, url, httpMethod);
        log.info("Asking CA for [{}:{}]: {}", httpMethod, url, response.getStatusCode());
        return response.getBody();
    }

    private <T> List<T> listRequest(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        log.info("Asking CA for [{}:{}]", httpMethod, url);

        ResponseEntity<List<T>> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<T>>(){}
        );

        postProcessResponse(response, url, httpMethod);
        log.info("Asking CA for [{}:{}]: {}", httpMethod, url, response.getStatusCode());
        return response.getBody();
    }

    private void postProcessResponse(ResponseEntity<?> response, String url, HttpMethod httpMethod) {

        if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() >= 300) {
            throw new InterServiceCallError(httpMethod, url, response.getStatusCode());
        }
    }

    @Override
    public GlobalPublicParameterDTO getGPP() {
        return request("/gpp", HttpMethod.GET, GlobalPublicParameterDTO.class, null);
    }

    @Override
    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        return request("/users", HttpMethod.POST, UserResponse.class, userCreationRequest);
    }

    @Override
    public UserResponse getUser(String id) {
        return request(String.format("/users/%s", id), HttpMethod.GET, UserResponse.class, null);
    }

    @Override
    public DeviceResponse updateDevice(String userId, String deviceId, DeviceUpdateRequest deviceUpdateRequest) {
        return request(String.format("/users/%s/devices/%s", userId, deviceId), HttpMethod.PUT, DeviceResponse.class, deviceUpdateRequest);
    }

    @Override
    public AttributeAuthorityResponse updateAuthorityPublicKey(String authorityId, AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest) {
        return request(String.format("/authorities/%s/public-key", authorityId), HttpMethod.PUT, AttributeAuthorityResponse.class, attributeAuthorityPublicKeyRequest);
    }

    @Override
    public CertificateResponse getCentralAuthorityCertificate() {
        return getCertificate("root");
    }

    @Override
    public CertificateResponse getCertificate(String id) {
        return request(String.format("/certificates/%s", id), HttpMethod.GET, CertificateResponse.class, null);
    }
}
