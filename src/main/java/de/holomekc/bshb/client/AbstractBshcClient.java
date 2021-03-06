package de.holomekc.bshb.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import de.holomekc.bshb.CertificateStorage;
import de.holomekc.bshb.exception.BshbException;
import de.holomekc.bshb.security.BshbKeyManager;
import de.holomekc.bshb.security.BshbTrustManager;
import io.reactivex.Observable;

/**
 * @author Christopher Holomek
 * @since 12.01.2020
 */
public abstract class AbstractBshcClient {

    protected static final int PUBLIC_PORT = 8446;
    protected static final int COMMON_PORT = 8444;
    protected static final int PAIR_PORT = 8443;

    private static final String TLS_VERSION = "TLSv1.2";

    private final Client client;
    private final CertificateStorage certificateStorage;
    private final String host;

    protected AbstractBshcClient(final String host, final CertificateStorage certificateStorage) {
        this.host = host;
        this.certificateStorage = certificateStorage;
        this.client = this.createClient();
    }

    protected Client createClient() {
        final Logger logger = Logger.getLogger(getClass().getName());

        final Feature feature = new LoggingFeature(logger, Level.INFO, null, null);

        final ClientBuilder clientBuilder = extendClient( // wrap
                ClientBuilder.newBuilder().sslContext(createSslContext())
                        .hostnameVerifier((hostname, sslSession) -> hostname.equalsIgnoreCase(this.host))
                        .register(JacksonFeature.class).register(ObjectMapperProvider.INSTANCE).register(feature)
                //wrap
        );

        return clientBuilder.build();
    }

    private SSLContext createSslContext() {
        try {
            final SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
            sslContext.init(new KeyManager[] { new BshbKeyManager(this.certificateStorage.getClientCert(),
                            this.certificateStorage.getClientPrivateKey()) }, new TrustManager[] { new BshbTrustManager() },
                    new SecureRandom());

            return sslContext;
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("TLS version not found. This should not happen");
        }
    }

    protected ClientBuilder extendClient(final ClientBuilder clientBuilder) {
        // use this if you want to configure builder further
        return clientBuilder;
    }

    protected <D> Observable<BshbResponse> simpleCall(final int port, final String method, final String path,
            final D data) {
        return this.simpleCall(port, method, path, data, options -> options);
    }

    protected <D> Observable<BshbResponse> simpleCall(final int port, final String method, final String path,
            final D data, final CallOptions options) {

        return Observable.create(observer -> {
            final WebTarget webTarget =
                    this.client.target(UriBuilder.fromPath(path).scheme("https").host(this.host).port(port));

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            invocationBuilder = options.configureInvocation(invocationBuilder);

            try {
                final Response response;
                if (data != null) {
                    response =
                            invocationBuilder.build(method, Entity.entity(data, MediaType.APPLICATION_JSON)).invoke();
                } else {
                    response = invocationBuilder.build(method).invoke();
                }
                observer.onNext(new BshbResponse(response));
            } catch (final RuntimeException e) {
                observer.onError(new BshbException("error during parsing response from BSHC:", e));
            } finally {
                observer.onComplete();
            }

            //final JsonNode result = response.readEntity(JsonNode.class);
            //final String result = response.readEntity(String.class);

        });
    }
}
