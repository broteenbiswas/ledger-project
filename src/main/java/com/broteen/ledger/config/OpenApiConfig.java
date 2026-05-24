package com.broteen.ledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ledgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Ledger API")
                        .description("""
                                A financial transaction event ledger API with built-in support for:
                                - **Idempotency**: duplicate eventId submissions are safely ignored and return the original event.
                                - **Out-of-order tolerance**: events are always listed and balanced in chronological order regardless of arrival order.
                                - **Balance computation**: net balance = sum(CREDIT) - sum(DEBIT).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BroteenCodes")
                                .email("enigmaticbrot@gmail.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ));
    }
}
