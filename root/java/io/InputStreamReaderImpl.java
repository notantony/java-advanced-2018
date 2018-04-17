package java.io;

public class InputStreamReaderImpl extends InputStreamReader {
    public InputStreamReaderImpl(java.io.InputStream arg0, java.nio.charset.CharsetDecoder arg1) {
        super(arg0, arg1);
    }
    public InputStreamReaderImpl(java.io.InputStream arg0, java.nio.charset.Charset arg1) {
        super(arg0, arg1);
    }
    public InputStreamReaderImpl(java.io.InputStream arg0, java.lang.String arg1) throws java.io.UnsupportedEncodingException {
        super(arg0, arg1);
    }
    public InputStreamReaderImpl(java.io.InputStream arg0) {
        super(arg0);
    }
    public int read(char[] arg0, int arg1, int arg2) throws java.io.IOException {
        return 0;
    }
    public void close() throws java.io.IOException {
        return;
    }
}
