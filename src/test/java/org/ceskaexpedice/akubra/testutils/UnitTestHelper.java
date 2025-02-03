package org.ceskaexpedice.akubra.testutils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitTestHelper {
  static final String SOURCE_LOCATION = "./testdata";
  static final String RESULT_LOCATION = "./testoutput";

  String dirName;

  /**
   * Constructor for the UnitTestHelper()
   * @param clazz The class which is being tested. The class name will be used to locate the appropriate testdata directories,
   *        typically ./testdata/<i>classname</i> and ./testdata/testoutput/<i>classname</i>
   */
  public UnitTestHelper(Class<?> clazz) {
    String name = clazz.getName();
    dirName = name.substring(0, name.lastIndexOf('.'));
  }

  /**
   * Get the relative path name for a test data source file.
   * @param sourceFile Filename of source file (with no directory).
   * @return Relative path name of source file.
   */
  public String getSourceFilePath(String sourceFile) {
    return SOURCE_LOCATION + "/" + dirName + "/" + sourceFile;
  }

  /**
   * Get the relative path name for a test data results file.
   * @param resultFile Filename of results file (with no directory).
   * @return Relative path name of results file.
   */
  public String getResultFilePath(String resultFile) {
    return RESULT_LOCATION + "/" + dirName + "/" + resultFile;
  }

  /**
   * Get a File object for a test data source file.
   * @param sourceFile File name of source file (just the file, no directory path).
   * @return File object for the source file.
   */
  public File getSourceFile(String sourceFile) {
    return new File(getSourceFilePath(sourceFile));
  }

  /**
   * Get a source file as a URL. NOTE: The source file is automatically encoded with URLEncoder.encode().
   * @param sourceFile The source file name relative to the class's testdata directory (typically:
   *        "testdata/[full-classname]/[file-name]". Note: Deprecated due to confusion. Use
   *        {@link #getSourceFileAsEncodedURL(String)} instead.
   * @return The URL to the file (with a file: protocol) as a string, encoded for sending to an http server.
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   * @deprecated
   */
  @Deprecated
  public String getSourceFileAsURL(String sourceFile) throws UnsupportedEncodingException, MalformedURLException {
    return URLEncoder.encode(getSourceFile(sourceFile).toURI().toURL().toString(), "UTF-8");
  }

  /**
   * Get a source file as a URL. The source file is automatically encoded with URLEncoder.encode().
   * @param sourceFile The source file name relative to the class's testdata directory (typically:
   *        "testdata/[full-classname]/[file-name]".
   * @return The URL to the file (with a file: protocol) as a string, encoded for sending to an http server.
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public String getSourceFileAsEncodedURL(String sourceFile) throws UnsupportedEncodingException, MalformedURLException {
    return URLEncoder.encode(getSourceFile(sourceFile).toURI().toURL().toString(), "UTF-8");
  }

  /**
   * Takes a simple source file (assumed to be relative to the class's test data directory) and returns a simple, unencoded, URL
   * string to the file.
   * @param sourceFile The file name of the file desired. Relative to the class's testdata directory (typically:
   *        "testdata/[full-classname]/[file-name]"
   * @return The URL (with a file: protocol) as a string, not encoded.
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   */
  public String getSourceFileAsUnencodedURL(String sourceFile) throws UnsupportedEncodingException, MalformedURLException {
    return getSourceFile(sourceFile).toURI().toURL().toString();
  }

  /**
   * Get a File object for a test data results file. Note: This call will create all nested sub-directories necessary for the
   * results file if they don't already exist.
   * @param resultFile File name of results file (just the file, no directory path).
   * @return File object for the results file.
   */
  public File getResultFile(String resultFile) throws IOException {
    File f = new File(getResultFilePath(resultFile));
    File d = f.getParentFile();
    if (!d.exists())
      if (!d.mkdirs()) {
        throw new IOException("Unable to create parent path to " + resultFile);
      }
    return f;
  }

  /**
   * Copies a file from the testdata area to the testoutput area. This is good for complex tests that require setting up a number of
   * files that get modified.
   * <p>
   * Before copying the file, this method automatically checks to see if the output directory exists and creates it if necessary.
   * @param fileName The file to copy from the source area to the result area.
   * @throws Exception
   */
  public void copySourceToResultFile(String fileName) throws Exception {
    createDirForFile(getResultFilePath(fileName));

    InputStream in = getInputStream(fileName);
    OutputStream out = getOutputStream(fileName);

    copyStream(in, out, 0);
    in.close();
    out.close();
  }

  /**
   * Copy a file from one location to another. The files in this case are specified as full path names to files in the system (not
   * necessarily within the testdata or testoutput directories).
   * @param fromFileName The file to copy from.
   * @param toFileName The file to copy to.
   * @throws Exception
   */
  public static void copyFile(String fromFileName, String toFileName) throws Exception {
    createDirForFile(toFileName);

    InputStream in = new FileInputStream(fromFileName);
    OutputStream out = new FileOutputStream(toFileName);

    copyStream(in, out, 0);
    in.close();
    out.close();
  }

  /**
   * Copy a file in the output directory from one location to another
   * @param fromFileName The file to copy from
   * @param toFileName The file to copy to
   * @throws Exception
   */
  public void copyResultFile(String fromFileName, String toFileName) throws Exception {
    copyFile(getResultFilePath(fromFileName), getResultFilePath(toFileName));
  }

  /**
   * Creates the directory for the specified file. For example, if the file is "c:/x/y/z.txt", this method will create the directory
   * "c:/x/y" (and all parent directories, as necessary).
   * @param filePath The file path whose directory needs to be created.
   * @throws Exception
   */
  public static void createDirForFile(String filePath) throws Exception {
    File outFile = new File(filePath);
    File outDir = outFile.getParentFile();

    if (!outDir.exists())
      if (!outDir.mkdirs())
        throw new Exception("Unable to create directory " + outDir.getAbsolutePath());

  }

  /**
   * Deletes a file or directory (recursively)
   * @param it the item to delete.
   * @return true if the item was deleted.
   */
  public boolean delete(File it) {
    if (!it.exists())
      return true;

    if (it.isFile()) {
      return it.delete();
    }

    File[] files = it.listFiles();
    if (files == null)
      return false;
    for (File f:files) {
      if (!delete(f))
        return false;
    }
    return it.delete();
  }

  /**
   * Deletes a file or directory (recursively)
   * @param it the item to delete.
   * @return true if the item was deleted.
   */
  public boolean delete(String it) {
    return delete(new File(it));
  }

  /**
   * Creates an empty directory in the result area - so that it is available for storing/comparing with results down the line.
   *
   * <p>
   * If the directory exists, any files and sub-directories are removed
   *
   * @param resultDir The result directory (in the testoutput area) to create.
   * @throws Exception
   */
  public void clearResultDir(String resultDir) throws Exception {
    File f = new File(getResultFilePath(resultDir));
    delete(f);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        throw new Exception("Unable to create parent path to " + resultDir);
      }
    }
  }

  /**
   * Create a directory in the result area - so that it is available for storing/comparing with results down the line. This is
   * typically done automatically, but there are cases where it is not, and so it's nice to have a method to do it.
   *
   * @param resultDir The result directory (in the testoutput area) to create.
   * @throws Exception
   */
  public void createResultDir(String resultDir) throws Exception {
    File f = new File(getResultFilePath(resultDir));
    if (!f.exists()) {
      if (!f.mkdirs()) {
        throw new Exception("Unable to create parent path to " + resultDir);
      }
    }
  }

  /**
   * Create a directory in the source area
   *
   * @param srcDir The source directory (in the testdata area) to create.
   * @throws Exception
   */
  public void createSourceDir(String srcDir) throws Exception {
    File f = new File(getSourceFilePath(srcDir));
    if (!f.exists()) {
      if (!f.mkdirs()) {
        throw new Exception("Unable to create parent path to " + srcDir);
      }
    }
  }

  /**
   * Get a buffered "InputStream" object for a test data source file.
   * @param sourceFile The source file name (no directory).
   * @return The InputStream object.
   * @throws FileNotFoundException
   */
  public InputStream getInputStream(String sourceFile) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(getSourceFile(sourceFile)));
  }

  /**
   * Get a buffered "OutputStream" object for a test data result file.
   * @param resultFile The result file name (no directory).
   * @return The resulting OutputStream object.
   * @throws FileNotFoundException
   */
  public OutputStream getOutputStream(String resultFile) throws FileNotFoundException, IOException {
    File f = getResultFile(resultFile);
    return new BufferedOutputStream(new FileOutputStream(f));
  }

  /**
   * Get a PrintWriter object for a test data result file. The print writer object is an easy way for unit tests to write formatted
   * data to output (using print() printf(), etc. methods), similar to System.out.print*(). The results will go into a file which
   * can be compared to a known good file for automated regression testing.
   *
   * @param resultFile The result file name (no directory).
   * @return The resulting PrintWriter object.
   * @throws Exception
   */
  public PrintWriter getPrintWriter(String resultFile) throws Exception {
    File f = getResultFile(resultFile);
    return new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
  }

  /**
   * Get a UTF-8 PrintWriter object for a test data result file. The print writer object is an easy way for unit tests to write
   * formatted data to output (using print() printf(), etc. methods), similar to System.out.print*(). The results will go into a
   * file which can be compared to a known good file for automated regression testing.
   *
   * @param resultFile The result file name (no directory).
   * @return The resulting PrintWriter object.
   * @throws Exception
   */
  public PrintWriter getUTF8PrintWriter(String resultFile) throws Exception {
    File f = getResultFile(resultFile);
    return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8")));
  }

  /**
   * Download a URL to a result file. Fetches the URL from wherever it is and stores the result in the specified result file (which
   * will be in the testoutput... directory path).
   * @param urlInput The URL to download.
   * @param resultFile The result file where the URL's contents will be stored.
   * @throws Exception
   */
  public void downloadURLToResult(String urlInput, String resultFile) throws Exception {
    URL url = new URL(urlInput);
    InputStream urlStream = null;
    OutputStream outStream = null;
    try {
      urlStream = url.openStream();
      outStream = getOutputStream(resultFile);
      copyStream(urlStream, outStream, 0);
    }
    finally {
      if (urlStream != null)
        urlStream.close();
      if (outStream != null)
        outStream.close();
    }
  }

  /**
   * Download an HTTP URL to a result file. Fetches the URL from wherever it is and stores the result in the specified result file
   * (which will be in the testoutput... directory path).
   * <p>
   * Note: Because it is specifically an http URL (as opposed to any other protocol), we can download the error URL data if an error
   * is thrown by the server. Therefore, use this method for all error handling.
   * @param urlInput The URL to download.
   * @param resultFile The result file where the URL's contents will be stored.
   * @throws Exception
   */
  public void downloadHttpURLWithErrorToResult(String urlInput, String resultFile) throws Exception {
    URL url = new URL(urlInput);
    InputStream urlStream = null;
    OutputStream outStream = null;
    try {
      HttpURLConnection http = (HttpURLConnection) url.openConnection();
      try {
        urlStream = http.getInputStream();
      }
      catch (IOException ioe) {
        urlStream = http.getErrorStream();
      }
      outStream = getOutputStream(resultFile);
      copyStream(urlStream, outStream, 0);
    }
    finally {
      if (urlStream != null)
        urlStream.close();
      if (outStream != null)
        outStream.close();
    }
  }

  /**
   * Write a buffer to a result file in the test output area. Used for writing document content from Aspire Documents to files.
   * @param buffer The buffer to write.
   * @param resultFile The file in the testoutput results area where the file should be written.
   */
  public void writeBufferToResult(byte[] buffer, String resultFile) {
    InputStream inStream = null;
    OutputStream outStream = null;
    try {
      try {
        inStream = new ByteArrayInputStream(buffer);
        outStream = getOutputStream(resultFile);
        copyStream(inStream, outStream, 0);
      }
      finally {
        if (inStream != null)
          inStream.close();
        if (outStream != null)
          outStream.close();
      }
    }
    catch (IOException e) {
      /* ignore */ }
  }

  /**
   * Write a stream to a result file in the test output area. Used for writing document content from Aspire Documents to files.
   * @param iStream The stream to write.
   * @param resultFile The file in the testoutput results area where the file should be written.
   */
  public void writeStreamToFile(InputStream iStream, String resultFile) throws IOException {
    OutputStream oStream = getOutputStream(resultFile);
    copyStream(iStream, oStream, 0);
    oStream.close();
  }

  /**
   * Compare a result file with a known good copy of the result file. The result file will be stored in
   * ./testoutput/&lt;packagename&gt;/&lt;fileName&gt;, and the source fill must be stored in
   * ./testdata/&lt;packagename&gt;/&lt;fileName&gt; .
   * @param fileName The file name to be compared against the known good copy. This will be the same file name in both the testdata
   *        and testoutput directories.
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws Exception
   */
  public boolean compareFiles(String fileName) throws Exception {
    return compareFiles(fileName, (Comparator<File>) null, Boolean.getBoolean("replaceFiles"));
  }

  public boolean compareFiles(String fileName, Comparator<File> comparator) throws Exception {
    return compareFiles(fileName, comparator, Boolean.getBoolean("replaceFiles"));
  }

  private boolean compareFiles(String fileName, Comparator<File> comparator, boolean replace) throws Exception {
    return compareFiles(fileName, fileName, comparator, replace);

  }

  static boolean isByteWhiteSpace(int b) {
    return (b == 0x0A) || (b == 0x0D) || (b == 0x09) || (b == 0x20);
  }

  /**
   * Compare two files where the file names are different. Note that the sourceFileName must still be in the testdata directory, and
   * the resultFileName must still be in the testoutput directory. This method is good for checking when two tests produce the same
   * result, for example, two different ways to get the component status.
   * @param sourceFileName The file name to use as the source of the comparison. This is a file located in
   *        ./testdata/&lt;packagename&gt;/&lt;fileName&gt;.
   * @param resultFileName The file name to use as the result of the comparison. This will need to be located in
   *        ./testoutput/&lt;packagename&gt;/&lt;fileName&gt;.
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws IOException
   */
  public boolean compareFiles(String sourceFileName, String resultFileName) throws IOException {
    return compareFiles(sourceFileName, resultFileName, null, Boolean.getBoolean("replaceFiles"));
  }

  public boolean compareFiles(String sourceFileName, String resultFileName, Comparator<File> comparator) throws IOException {
    return compareFiles(sourceFileName, resultFileName, comparator, Boolean.getBoolean("replaceFiles"));
  }

  private boolean compareFiles(String sourceFileName, String resultFileName, Comparator<File> comparator, boolean replace) throws IOException {
    File sourceFile = getSourceFile(sourceFileName);
    File resultFile = getResultFile(resultFileName);
    if (comparator == null)
      return compareFiles(sourceFile, resultFile, replace);
    else
      return comparator.compare(sourceFile, resultFile) == 0;
  }

  /**
   * Compare two files based on their file Objects. This method allows the files to be in any location whatsoever. (Version 0.2)
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws IOException
   */
  public static boolean compareFiles(File sourceFile, File resultFile) throws IOException {
    return compareFiles(sourceFile, resultFile, Boolean.getBoolean("replaceFiles"));
  }

  public static boolean compareFiles(File sourceFile, File resultFile, boolean replace) throws IOException {
    boolean result = compareRawFiles(sourceFile, resultFile);
    if (!result && replace) {
      System.out.printf("Replacing unequal expected file %s with %s\n", sourceFile.getPath(), resultFile.getPath());
      Files.copy(resultFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      result = compareRawFiles(sourceFile, resultFile);
    }
    return result;
  }

  private static boolean compareRawFiles(File sourceFile, File resultFile) throws IOException {
    // Some checking
    if (sourceFile == null)
      throw new NullPointerException("UnitTestHelper.compareFiles: Null source file");
    if (resultFile == null)
      throw new NullPointerException("UnitTestHelper.compareFiles: Null result file");
    if (!sourceFile.isFile()) {
      throw new IOException("UnitTestHelper.compareFiles: Source file does not exist: " + sourceFile.getPath());
    }
    if (!resultFile.isFile()) {
      throw new IOException("UnitTestHelper.compareFiles: Result file does not exist: " + resultFile.getPath());
    }

    /**
     * Now the compare
     */
    InputStream in1 = null;
    InputStream in2 = null;
    boolean isEqual = true;
    try {
      try {
        in1 = new BufferedInputStream(new FileInputStream(sourceFile));
        in2 = new BufferedInputStream(new FileInputStream(resultFile));
      }
      catch (IOException e) {
        return false;
      }

      int b1 = 0, b2 = 0;
      while (b1 == b2 && b1 >= 0 && b2 >= 0) {
        do {
          b1 = in1.read();
        } while (b1 > 0 && isByteWhiteSpace(b1));

        do {
          b2 = in2.read();
        } while (b2 > 0 && isByteWhiteSpace(b2));
      }

      isEqual = (b1 == b2);
    }
    finally {
      if (in1 != null)
        in1.close();
      if (in2 != null)
        in2.close();
    }

    return isEqual;
  }

  /**
   * Compare a result file with a known good copy of the result file. The result file will be stored in
   * ./testoutput/&lt;packagename&gt;/&lt;fileName&gt;, and the source fill must be stored in
   * ./testdata/&lt;packagename&gt;/&lt;fileName&gt; .
   * @param fileName The file name to be compared against the known good copy. This will be the same file name in both the testdata
   *        and testoutput directories.
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws Exception
   */
  public boolean compareBinaryFiles(String fileName) throws Exception {
    return compareBinaryFiles(fileName, fileName);
  }

  /**
   * Compare two files where the file names are different. Note that the sourceFileName must still be in the testdata directory, and
   * the resultFileName must still be in the testoutput directory. This method is good for checking when two tests produce the same
   * result, for example, two different ways to get the component status.
   * @param sourceFileName The file name to use as the source of the comparison. This is a file located in
   *        ./testdata/&lt;packagename&gt;/&lt;fileName&gt;.
   * @param resultFileName The file name to use as the result of the comparison. This will need to be located in
   *        ./testoutput/&lt;packagename&gt;/&lt;fileName&gt;.
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws IOException
   */
  public boolean compareBinaryFiles(String sourceFileName, String resultFileName) throws IOException {
    return compareBinaryFiles(getSourceFile(sourceFileName), getResultFile(resultFileName));
  }

  /**
   * Compare two files based on their file Objects. This method allows the files to be in any location whatsoever. (Version 0.2)
   * @return Returns true if the two files are equal, or false otherwise.
   * @throws IOException
   */
  public static boolean compareBinaryFiles(File sourceFile, File resultFile) throws IOException {
    // Some checking
    if (sourceFile == null)
      throw new NullPointerException("UnitTestHelper.compareFiles: Null source file");
    if (resultFile == null)
      throw new NullPointerException("UnitTestHelper.compareFiles: Null result file");
    if (!sourceFile.isFile()) {
      throw new IOException("UnitTestHelper.compareFiles: Source file does not exist: " + sourceFile.getPath());
    }
    if (!resultFile.isFile()) {
      throw new IOException("UnitTestHelper.compareFiles: Result file does not exist: " + resultFile.getPath());
    }

    /**
     * Now the compare
     */
    InputStream in1 = null;
    InputStream in2 = null;
    boolean isEqual = true;
    try {
      try {
        in1 = new BufferedInputStream(new FileInputStream(sourceFile));
        in2 = new BufferedInputStream(new FileInputStream(resultFile));
      }
      catch (IOException e) {
        return false;
      }

      int b1 = 0, b2 = 0;
      while (b1 == b2 && b1 >= 0 && b2 >= 0) {
        b1 = in1.read();
        b2 = in2.read();
      }

      isEqual = (b1 == b2);
    }
    finally {
      if (in1 != null)
        in1.close();
      if (in2 != null)
        in2.close();
    }

    return isEqual;
  }

  /**
   * Scans a file in the testoutput area for a string and returns true if the string pattern is found.
   * @param resultFileName The file in the result area to scan. This will need to be located in
   *        ./testoutput/&lt;packagename&gt;/&lt;fileName&gt;.
   * @param regex The string pattern to locate. This can be any standard Java regular expression (see the Java Pattern class for
   *        more details).
   * @return True if the patter is found inside the file.
   * @throws Exception
   */
  public boolean scanFileForRegex(String resultFileName, String regex) throws Exception {
    return scanFileForRegex(getResultFile(resultFileName), regex);
  }

  /**
   * Scans a file for a string and returns true if the string pattern is found.
   * @param inFp The file to scan.
   * @param regex The string pattern to locate. This can be any standard Java regular expression (see the Java Pattern class for
   *        more details).
   * @return true if the patter is found inside the file.
   * @throws Exception
   */
  public static boolean scanFileForRegex(File inFp, String regex) throws Exception {
    Reader r = new FileReader(inFp);

    StringBuffer sb = new StringBuffer((int) inFp.length());

    for (;;) {
      int c = r.read();
      if (c < 0)
        break;
      sb.append((char) c);
    }
    r.close();

    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(sb);

    return m.find();
  }

  /**
   * Remove or replace text in a file in place
   * @param inFp the file to process
   * @param regex the pattern to replace
   * @param replacement the text to replace the pattern with
   * @throws Exception
   */
  public static void cleanseFile(File inFp, String regex, String replacement) throws Exception {
    cleanseFile(inFp, regex, replacement, false);
  }

  /**
   * Remove or replace text in a file in place, using utf8 encoding for both input and output
   * @param inFp the file to process
   * @param regex the pattern to replace
   * @param replacement the text to replace the pattern with
   * @throws Exception
   */
  public static void cleanseUTF8File(File inFp, String regex, String replacement) throws Exception {
    cleanseFile(inFp, regex, replacement, true);
  }

  /**
   * Cleanse a file
   * @param inFp the file
   * @param regex the pattern to search
   * @param replacement the replacement
   * @param utf8 use utf8 for input and ouput
   * @throws Exception
   */
  private static void cleanseFile(File inFp, String regex, String replacement, boolean utf8) throws Exception {
    Reader r = utf8 ? new BufferedReader(new InputStreamReader(new FileInputStream(inFp), "UTF8")) : new FileReader(inFp);

    StringBuffer sb = new StringBuffer((int) inFp.length());

    for (;;) {
      int c = r.read();
      if (c < 0)
        break;
      sb.append((char) c);
    }
    r.close();

    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(sb);

    Writer w = utf8 ? new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inFp), "UTF8")) : new FileWriter(inFp);
    w.write(m.replaceAll(replacement));
    w.close();
  }

  /**
   * Remove or replace text in a file in place
   * @param resultFile the result file to process
   * @param regex the pattern to replace
   * @param replacement the text to replace the pattern with
   * @throws Exception
   */
  public void cleanseResultFile(String resultFile, String regex, String replacement) throws Exception {
    File inFp = getResultFile(resultFile);
    cleanseFile(inFp, regex, replacement, false);
  }

  /**
   * Remove or replace text in a file in place, using utf8 encoding for both input and output
   * @param resultFile the result file to process
   * @param regex the pattern to replace
   * @param replacement the text to replace the pattern with
   * @throws Exception
   */
  public void cleanseUTF8ResultFile(String resultFile, String regex, String replacement) throws Exception {
    File inFp = getResultFile(resultFile);
    cleanseFile(inFp, regex, replacement, true);
  }

  /**
   * Scans a string for a regex and returns true if the string matches the regex.
   * <p>
   * Note: Made static in 0.2.
   * @param stringValue The string to be tested.
   * @param regex The string pattern to locate. This can be any standard Java regular expression (see the Java Pattern class for
   *        more details).
   * @return True if the pattern is found inside the string.
   * @throws Exception
   */
  public static boolean scanStringForRegex(String stringValue, String regex) throws Exception {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(stringValue);
    return m.find();
  }

  /**
   * Buffer for copy actions
   */
  static final int BUFFER_SIZE = 16 * 1024;

  /**
   * Copy an input stream to an output stream. Uses java NIO (channels!) for (hopefully) fast copying.
   * @param in The input stream to copy from.
   * @param out The output stream to copy to.
   * @param maxBytes The maximum number of bytes to copy. Use 0 for all bytes. Note: This value is <i>approximate</i>. The actual
   *        number of bytes copied may be greater by 16 kilobytes.
   * @throws IOException If I/O exception occurs
   */
  public static void copyStream(InputStream in, OutputStream out, long maxBytes) throws IOException {
    byte[] buf = new byte[BUFFER_SIZE];

    long totalBytes = 0;
    while (maxBytes == 0 || totalBytes < maxBytes) {
      int len;
      if (maxBytes == 0)
        len = BUFFER_SIZE;
      else if ((maxBytes - totalBytes) > BUFFER_SIZE)
        len = BUFFER_SIZE;
      else
        len = (int) (maxBytes - totalBytes);

      len = in.read(buf, 0, len);
      if (len < 0)
        break;
      out.write(buf, 0, len);
      totalBytes += len;
    }
  }
}
