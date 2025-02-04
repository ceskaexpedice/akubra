package org.ceskaexpedice.akubra.utils;

import com.google.common.io.CharStreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Utils {

    /**
     * InputStream is being closed here (after extracting String or error).
     *
     * @param in
     * @return String or null (when in is null)
     * @throws IOException
     */
    public static String streamToString(InputStream in) {
        try {
            if (in == null) {
                return null;
            }
            try (final Reader reader = new InputStreamReader(in)) {
                return CharStreams.toString(reader);
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

}
