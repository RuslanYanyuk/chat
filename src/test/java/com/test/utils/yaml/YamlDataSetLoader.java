package com.test.utils.yaml;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ruslan Yaniuk
 * @date September 2015
 */
public class YamlDataSetLoader {

    public static IDataSet load(final String resourceFile) throws IOException, DataSetException {
        Resource resource = new ClassPathResource(resourceFile);
        IDataSet dataSet;

        if (isYamlEmpty(resource.getInputStream())) {
            throw new FileNotFoundException("File is empty");

        } else {
            final InputStream inputStream = resource.getInputStream();

            dataSet = new YamlDataSet(inputStream);

            if (inputStream != null) {
                inputStream.close();
            }
        }

        return dataSet;
    }

    private static boolean isYamlEmpty(InputStream inputStream) throws IOException {
        final boolean isEmpty = new Yaml().load(inputStream) == null;

        if (inputStream != null) {
            inputStream.close();
        }

        return isEmpty;
    }
}
