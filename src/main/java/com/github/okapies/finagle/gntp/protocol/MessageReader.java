package com.github.okapies.finagle.gntp.protocol;

import java.io.IOException;
import java.io.Reader;

public class MessageReader {

    private final Reader reader;

    private final StringBuilder builder = new StringBuilder();

    public MessageReader(Reader reader) {
        this.reader = reader;
    }

    public String readLine() throws IOException {
        int ch = reader.read();

        if (ch < 0) {
            return null; // End of stream
        }

        builder.setLength(0); // clear

        while (ch >= 0) {
            if (ch == '\r') { // CR('\r')
                int ch2 = reader.read();
                if (ch2 == '\n') { // End of line (CRLF)
                    break;
                } else { // non LF('\n') character or EOS
                    builder.append('\r');
                    ch = ch2;
                }
            } else {
                builder.append((char) ch);
                ch = reader.read();
            }
        }

        return builder.toString();
    }

}
