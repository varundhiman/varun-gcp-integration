package com.varun.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class RestUtil<T> {

    private String url;
    private String username;
    private String password;
    private Map<String, String> pathParams;
    private MultiValueMap<String, String> queryParams;
    private Class<T> typeOfT;
    private String authMode;
    private String token;
    private String body;

    private final HttpHeaders headers = new HttpHeaders();
    private final RestTemplate restTemplate = new RestTemplate();

    private static Log LOG = LogFactory.getLog(RestUtil.class);

    public static final String DEFAULT_AUTH_MODE = "Basic "; //$NON-NLS-1$
    public static final String BEARER_TOKEN_AUTH_MODE = "Bearer ";

    public RestUtil() {

    }

    public RestUtil(final RestUtil copyFrom) {
        this.username = copyFrom.getUsername();
        this.password = copyFrom.getPassword();
        this.url = copyFrom.getUrl();
        this.pathParams = copyFrom.getPathParams();
        this.queryParams = copyFrom.getQueryParams();
        this.typeOfT = copyFrom.getTypeOfT();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Class<T> getTypeOfT() {
        return this.typeOfT;
    }

    public void setTypeOfT(final Class classToSet) {
        this.typeOfT = classToSet;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(final Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public MultiValueMap<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(final MultiValueMap<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public void setHeader(final String headerName, final String value) {
        final List<String> valList = new ArrayList<>();
        valList.add(value);
        headers.put(headerName, valList);
    }

    public String getHeader(final String headerName) {
        return headers.getFirst(headerName);
    }

    public T getQueryResult() throws HttpClientErrorException {
        T result = null;
        final String restUrl = constructUrl();

        setAuthCreds();
        final HttpEntity<T> request = new HttpEntity<T>(headers);

        result = restTemplate.exchange(restUrl, HttpMethod.GET, request, typeOfT).getBody();

        return result;
    }

    public T submitRequest(HttpMethod method) {
        setAuthCreds();
        final String restUrl = constructUrl();
        final HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(constructUrl(), method, request, typeOfT).getBody();
    }

    public void setAuthCreds() {
        if (this.authMode == null) {
            this.authMode = DEFAULT_AUTH_MODE;
            setBasicAuthHeader();
        } else if (authMode == BEARER_TOKEN_AUTH_MODE) {
            setBearerTokenAuthHeader();
        }

    }

    void setBearerTokenAuthHeader() {
        headers.remove(HttpHeaders.AUTHORIZATION);
        final String authHeader = authMode + this.getToken();
        headers.add(HttpHeaders.AUTHORIZATION, authHeader);

    }

    void setBasicAuthHeader() {
        headers.remove(HttpHeaders.AUTHORIZATION);
        final String plainCreds = this.getUsername() + ":" + this.getPassword();
        final byte[] plainCredsBytes = plainCreds.getBytes();
        final byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        final String base64Creds = new String(base64CredsBytes);
        final String authHeader = authMode + base64Creds;
        headers.add(HttpHeaders.AUTHORIZATION, authHeader);

    }

    public String constructUrl() {

        return UriComponentsBuilder.fromUriString(getUrlWithPathParams()).queryParams(queryParams).build()
                                   .toUriString();

    }

    public String getUrlWithPathParams() {
        String urlWithParams = url;
        if (null != pathParams) {
            for (final Map.Entry<String, String> paramEntry : pathParams.entrySet()) {
                urlWithParams = urlWithParams.replace(paramEntry.getKey(), paramEntry.getValue());
            }
        }
        return urlWithParams;
    }

    @SuppressWarnings("nls")
    public String getQueryDetails() {
     // @formatter:off
        return new ToStringBuilder(this)
                .append("pathParams", pathParams)
                .append("queryParams", queryParams)
                .toString();
     // @formatter:on
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        // @formatter:off
        return new ToStringBuilder(this)
            .append("url", url)
            .append("username", username)
            .append("password", password)
            .append("pathParams", pathParams)
            .append("queryParams", queryParams)
            .append("typeOfT", typeOfT)
            .append("headers", headers)
            .toString();
        // @formatter:on
    }
}
