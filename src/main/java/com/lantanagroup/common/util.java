package com.lantanagroup.common;

import java.lang.Class;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

import com.apicatalog.jsonld.loader.FileLoader;

import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;


public class util {


/*
    public List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    public List<String> getResourceFiles(String path, String pattern) throws IOException {
        List<String> filenames = getResourceFiles(path);

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
*/

    public static String loadResource(String fileName) {
        try {
            InputStream in = FileLoader.class.getClassLoader().getResourceAsStream(fileName);
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            
            BufferedReader br = new BufferedReader(isr);
            String text = br.lines()
                    .collect(Collectors.joining("\n"));
            
            isr.close();
            br.close();
            return text;
        } catch (Exception e) {
            throw new UnclassifiedServerFailureException(500, "Could not load server initialization resources");
        }
    }
}
