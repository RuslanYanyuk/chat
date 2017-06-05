package com.test.utils.yaml;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;

import java.io.*;

/**
 * DBUnit data set produced from YAML format. Each table has its own
 * section in the YAML document, where its' name is the root element
 * indicating that following entries are describing rows using column name : value pairs.
 * Each new row is denoted by '-', as in following example:
 * <pre><code>
 * useraccount:
 *   - id: 1
 *     firstname: Clark
 *     lastname: Kent
 *     username: superman
 *     password: kryptonite
 * address:
 *   - id: 1
 *     streetname: "Kryptonite Street"
 *     houseNumber: 7
 *     city: Metropolis
 *     zipCode: 1234
 * useraccount_address:
 *   - useraccount_id: 1
 *     addresses_id: 1
 * </code></pre>
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class YamlDataSet extends CachedDataSet {

    public YamlDataSet(YamlDataSetProducer producer, boolean caseSensitiveTableNames) throws DataSetException {
        super(producer, caseSensitiveTableNames);
    }

    public YamlDataSet(File file, boolean caseSensitiveTableNames) throws DataSetException, FileNotFoundException {
        this(new FileInputStream(file), caseSensitiveTableNames);
    }

    public YamlDataSet(File file) throws IOException, DataSetException {
        this(new FileInputStream(file), false);
    }

    public YamlDataSet(YamlDataSetProducer producer) throws DataSetException {
        this(producer, false);
    }

    public YamlDataSet(InputStream inputStream) throws DataSetException {
        this(inputStream, false);
    }

    public YamlDataSet(InputStream inputStream, boolean caseSensitiveTableNames) throws DataSetException {
        this(new YamlDataSetProducer(inputStream), caseSensitiveTableNames);
    }
}