package md2html;
import expression.exceptions.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class Md2HtmlSource {
    public static char END = '\0';

    private char c;
    private final Reader reader;
    private final Writer writer;
    private int sep = 0;

    public Md2HtmlSource (final String inputFile, final String outputFile) throws IOException {
        reader = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
        writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8));
    }

    private char readChar() throws IOException {
        final int read = reader.read();
        if (c == (int)'\n') {
            sep++;
        } else if (c != (int)'\r') {
            sep = 0;
        }
        return read == -1 ? END : (char)read;
    }

    protected void writeChar(char c) throws IOException {
        writer.write(c);
    }

    public void skipSpaces() throws IOException {
        while (Character.isWhitespace(getChar())) {
            nextChar();
        }
    }

    public void writeString(String s) throws IOException {
        writer.write(s);
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
    }

    public char getChar() {
        return c;
    }

    public char nextChar() throws IOException {
        c = readChar();
        return c;
    }

    boolean EOLn() {
        return c == '\n' || c == END;
    }

    public void skipSeparators() throws IOException {
        while (getChar() == '\r' || getChar() == '\n') {
            nextChar();
        }
        sep = 0;
    }

    public int getSeparators() {
        return sep;
    }
}