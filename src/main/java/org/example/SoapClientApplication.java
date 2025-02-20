package org.example;


import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Controller("/soap")
public class SoapClientApplication {

    public static void main(String[] args) {
        Micronaut.run(SoapClientApplication.class, args);
    }

    @Singleton
    public class SoapClient {
        private final String KEYSTORE_PATH = "src/main/resources/keystore.jks";
        private final String KEYSTORE_PASSWORD = "asdf1357";
        private final String WSDL_URL = "https://qrbancamia.com:8443/ws/hello.wsdl";

        private SSLContext createSSLContext() throws Exception {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        }

        @Get("/call")
        public String callSoapService() {
            try {
                SSLContext sslContext = createSSLContext();
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

                // Ignorar la verificación del hostname (solo para desarrollo)
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

                URL url = new URL(WSDL_URL);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
                connection.setDoOutput(true);

                // Ejemplo de mensaje SOAP
                String soapRequest = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                    xmlns:web="http://your.namespace.com/">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <web:yourMethod>
                                <web:parameter>value</web:parameter>
                            </web:yourMethod>
                        </soapenv:Body>
                    </soapenv:Envelope>
                    """;

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = soapRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                return response.toString();

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
}