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
    public static String inputstreamToString(InputStream in) {
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



    /**
     * InputStream is being closed here (after extracting String or error).
     *
     * @param in
     * @return
     * @throws IOException
     */
    /*
    public static JsonArray inputstreamToJsonArray(InputStream in) throws IOException {
        try (final Reader reader = new InputStreamReader(in)) {
            //String json = CharStreams.toString(reader);
            JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonArray();
        }
    }

     */


    /**
     * InputStream is being closed here (after extracting String or error).
     * This is just temporary method for results that are not actually JSON arrays, like this [uuid:0eaa6730-9068-11dd-97de-000d606f5dc6]
     *
     * @param in
     * @return
     * @throws IOException
     * @deprecated After fixing item/{pid}/parents endpoint, use inputstreamToJsonArray instead
     */
    /*
    public static JsonArray inputstreamToJsonArrayTmp(InputStream in) throws IOException {
        try (final Reader reader = new InputStreamReader(in)) {
            String data = CharStreams.toString(reader);
            String withoutBrackets = data.substring(1, data.length() - 1);
            String[] pids = withoutBrackets.split(",");
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            for (int i = 0; i < pids.length; i++) {
                jsonBuilder.append('"').append(pids[i]).append('"');
                if (i != pids.length - 1) {
                    jsonBuilder.append(',');
                }
            }
            jsonBuilder.append("]");
            JsonParser parser = new JsonParser();
            return parser.parse(jsonBuilder.toString()).getAsJsonArray();
        }
    }

     */
}
