package com.yash.Drift.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class OpenApiParserService {

    public OpenAPI parse(MultipartFile file) throws IOException {

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return new OpenAPIV3Parser()
                .readContents(content)
                .getOpenAPI();
    }
}
