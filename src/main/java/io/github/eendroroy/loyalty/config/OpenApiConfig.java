package io.github.eendroroy.loyalty.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI 3.1 specification exposed at {@code /v3/api-docs}
 * and the Swagger UI served at {@code /swagger-ui.html}.
 *
 * <p>All admin endpoints are grouped under the {@code /api/admin} base path.
 * Tag-level groupings are defined on each controller with {@code @Tag}.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the top-level {@link OpenAPI} descriptor with project metadata,
     * contact information, license, and a local development server entry.
     *
     * @return fully configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI loyaltyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loyalty Management System API")
                        .description("""
                                Admin API for the Loyalty Management System.

                                Provides endpoints for:
                                - **Data Sources** — configure FILE / API / DATABASE ingestion sources \
                                and define their typed field schemas.
                                - **Rules** — author dynamic reward rules using the custom \
                                `WHEN … THEN …` expression language, manage their lifecycle \
                                (DRAFT → ACTIVE → INACTIVE), and attach reward actions.
                                - **Metadata** — retrieve the field-alias registry used by the \
                                rule-expression editor for autocomplete.
                                """)
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("eendroroy")
                                .url("https://github.com/eendroroy"))
                        .license(new License()
                                .name("GNU Affero General Public License v3.0")
                                .url("https://www.gnu.org/licenses/agpl-3.0.txt")))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub Repository")
                        .url("https://github.com/eendroroy/loyalty"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")))
                .components(new Components());
    }
}

