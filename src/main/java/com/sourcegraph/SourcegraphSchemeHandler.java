package com.sourcegraph;


import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SourcegraphSchemeHandler extends CefResourceHandlerAdapter {
    private byte[] data_;
    private String mime_type_;
    private int offset_ = 0;

    public synchronized boolean processRequest(CefRequest request, CefCallback callback) {
        boolean handled = false;
        String url = request.getURL();

        if (url.endsWith(".html")) {
            handled = loadContent(url.substring(url.lastIndexOf('/') + 1));
            mime_type_ = "text/html";
            if (!handled) {
                String html = "<html><head><title>Error 404</title></head>";
                html += "<body><h1>Error 404</h1>";
                html += "File  " + url.substring(url.lastIndexOf('/') + 1) + " ";
                html += "does not exist</body></html>";
                data_ = html.getBytes();
                handled = true;
            }
        }

        if (handled) {
            // Indicate the headers are available.
            callback.Continue();
            return true;
        }

        return false;
    }

    public void getResponseHeaders(
            CefResponse response, IntRef response_length, StringRef redirectUrl) {
        response.setMimeType(mime_type_);
        response.setStatus(200);

        // Set the resulting response length
        response_length.set(data_.length);
        System.out.println("Response length: "+data_.length);
    }

    public synchronized boolean readResponse(
            byte[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
        boolean has_data = false;

        if (offset_ < data_.length) {
            // Copy the next block of data into the buffer.
            int transfer_size = Math.min(bytes_to_read, (data_.length - offset_));
            System.arraycopy(data_, offset_, data_out, 0, transfer_size);
            offset_ += transfer_size;
            System.out.println("Read "+transfer_size+".  Offset now "+offset_);
            bytes_read.set(transfer_size);
            has_data = true;
        } else {
            offset_ = 0;
            bytes_read.set(0);
        }

        return has_data;
    }

    private boolean loadContent(String resName) {
        System.out.println(resName);
        System.out.println("/html/"+resName);
        InputStream inStream = getClass().getResourceAsStream("/html/"+resName);
        System.out.println(inStream);
        if (inStream != null) {
            try {
                ByteArrayOutputStream outFile = new ByteArrayOutputStream();
                int readByte = -1;
                while ((readByte = inStream.read()) >= 0) outFile.write(readByte);
                data_ = outFile.toByteArray();
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }
}