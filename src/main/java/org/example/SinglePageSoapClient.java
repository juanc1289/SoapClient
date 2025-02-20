package org.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.Micronaut;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.URL;

public class SinglePageSoapClient {

    @Inject
    SoapCaller soapCaller;

    public static void main(String[] args) {
        // 1. Configurar la confianza en el keystore autofirmado antes de arrancar Micronaut
        //    Ajusta la ruta y contraseña de tu keystore
//        System.setProperty("javax.net.ssl.trustStore", "classpath:keystore.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword", "asdf1357");


        // 2. Arrancar Micronaut y obtener el bean de esta clase para invocar el método que llama al servicio
        ApplicationContext app = Micronaut.run(SinglePageSoapClient.class, args);
        SinglePageSoapClient client = app.getBean(SinglePageSoapClient.class);
        client.callSoapService();
        app.stop();
    }

    /**
     * Invoca el servicio SOAP remoto.
     */
    public void callSoapService() {
        // Ejemplo de request SOAP "getHelloRequest"
        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:hel="http://example.com/hello">
           <soapenv:Header/>
           <soapenv:Body>
              <hel:getHelloRequest>
                 <hel:name>Micronaut</hel:name>
              </hel:getHelloRequest>
           </soapenv:Body>
        </soapenv:Envelope>
        """;



        String response = soapCaller.invokeHello(soapRequest);
        System.out.println("Respuesta del servicio SOAP:\n" + response);
    }

    /**
     * Bean que inyecta el cliente y expone un método sencillo para la invocación.
     */
    @Singleton
    static class SoapCaller {
        private final SoapClient soapClient;

        @Inject
        SoapCaller(SoapClient soapClient) {
            this.soapClient = soapClient;
        }

        public String invokeHello(String request) {

            return soapClient.callHello(request);
        }
    }

    /**
     * Cliente HTTP de Micronaut que envía peticiones SOAP al dominio HTTPS.
     * Ajusta la URL según la exposición de tu servicio (por ejemplo: https://mi-dominio.com:8443/ws).
     */
//    @Client("https://localhost:8443/ws")
    @Client("https://qrbancamia.com:8443/ws")
    @Header(name = HttpHeaders.CONTENT_TYPE, value = "text/xml;charset=UTF-8")
    interface SoapClient {
        // Si tu endpoint final es /ws, con @Post("/") ya invocarás la raíz "/ws".
        // Ajusta el path si es necesario, por ejemplo @Post("/otraRuta").
        @Post("/")
        String callHello(@Body String soapRequest);
    }
}