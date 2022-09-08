package com.vn.castscreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AndroidHTTPServer extends NanoHTTPD {

    public AndroidHTTPServer(int port) {
        super(port);
    }

    private Response serveFile(Map<String, String> header,
                               File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath()
                    + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
                            NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }
                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
                            fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime,
                            new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN,
                    NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        return res;
    }


    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession iHTTPSession) {

        Map<String, String> headers = iHTTPSession.getHeaders();
        NanoHTTPD.Method method = iHTTPSession.getMethod();
        String uri = iHTTPSession.getUri();
        String mimeTypeForFile = getMimeTypeForFile(uri);

        HashMap hashMap = new HashMap();
        if (NanoHTTPD.Method.POST.equals(method) || NanoHTTPD.Method.PUT.equals(method)) {
            try {
                iHTTPSession.parseBody(hashMap);
            } catch (NanoHTTPD.ResponseException e) {
                return newFixedLengthResponse(e.getStatus(), "text/plain", e.getMessage());
            } catch (IOException e2) {
                return getResponse("Internal Error IO Exception: " + e2.getMessage());
            }
        }

//        if (CastMediaApplication.sAppInstance.mDevice.getIpAddress().equalsIgnoreCase(Constants.ipLocal)) {
//            File file = new File(CastMediaApplication.sAppInstance.pathMedia);
//            try {
//                InputStream  fileStream = new FileInputStream(file);
//                return newFixedLengthResponse(Response.Status.OK, getMimeTypeForFile(String.valueOf(Uri.fromFile(file))), fileStream, fileStream.available());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }

        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        File file = new File(uri);

        try {
            return serveFile(headers, file, mimeTypeForFile);
        } catch (Exception e) {
            e.printStackTrace();
            return  getResponse("Internal Error IO Exception: " + e.getMessage());
        }

    }


    private NanoHTTPD.Response createResponse(NanoHTTPD.Response.Status status, String str, InputStream inputStream) {
        NanoHTTPD.Response newChunkedResponse = newChunkedResponse(status, str, inputStream);
        newChunkedResponse.addHeader("Accept-Ranges", "bytes");
        return newChunkedResponse;
    }


    private NanoHTTPD.Response createResponse(NanoHTTPD.Response.Status status, String str, String str2) {
        NanoHTTPD.Response newFixedLengthResponse = newFixedLengthResponse(status, str, str2);
        newFixedLengthResponse.addHeader("Accept-Ranges", "bytes");
        return newFixedLengthResponse;
    }

    private NanoHTTPD.Response getResponse(String str) {
        return createResponse(NanoHTTPD.Response.Status.OK, "text/plain", str);
    }
}