package org.zalando.logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

final class DefaultLogbook implements Logbook {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;
    private final Obfuscation obfuscation;

    DefaultLogbook(final HttpLogFormatter formatter, final HttpLogWriter writer, final Obfuscation obfuscation) {
        this.formatter = formatter;
        this.writer = writer;
        this.obfuscation = obfuscation;
    }

    @Override
    public Optional<String> write(final RawHttpRequest rawHttpRequest) throws IOException {
        if (writer.isActive(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final HttpRequest httpRequest = obfuscation.obfuscate(rawHttpRequest.withBody());

            writer.writeRequest(formatter.format(correlationId, httpRequest));

            return Optional.of(correlationId);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void write(final RawHttpResponse response, final String correlationId) throws IOException {
        final HttpResponse httpResponse = obfuscation.obfuscate(response.withBody());
        final String message = formatter.format(correlationId, httpResponse);
        writer.writeResponse(message);
    }

}
