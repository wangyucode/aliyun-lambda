package cn.wycode.lambda;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.PojoRequestHandler;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

public class Lambda implements PojoRequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) {
        FunctionComputeLogger logger = context.getLogger();

        Response response = new Response();

        logger.info(request.body);

        logger.info("method->" + request.method);
        logger.info("uri->" + request.uri);
        logger.info("protocolVersion->" + request.protocolVersion);
        logger.info("body->" + request.body);

        URL url;
        try {
            if (request.uri.startsWith("http://") || request.uri.startsWith("https://") || request.uri.startsWith("HTTP://") || request.uri.startsWith("HTTPS://")) {

            } else {
                request.uri = "http://" + request.uri;
            }
            URI uri = new URI(request.uri);
            url = uri.toURL();
        } catch (Exception e) {
            response.error = "uri格式错误";
            logger.error(response.error);
            return response;
        }
        logger.info("url->" + url.toString());
        try {
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setConnectTimeout(30000);
            httpUrlConnection.setReadTimeout(30000);
            httpUrlConnection.setRequestMethod(request.method);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setInstanceFollowRedirects(false);
            for (Map.Entry<String, String> header : request.headers.entrySet()) {
                httpUrlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            response.code = httpUrlConnection.getResponseCode();
            Map<String, List<String>> responseHeaders = httpUrlConnection.getHeaderFields();
            response.headers = new HashMap<>(responseHeaders.size());
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> resHeader : responseHeaders.entrySet()) {
                if (resHeader.getKey() == null) {
                    continue;
                }
                for (String s : resHeader.getValue()) {
                    sb.append(s);
                }
                response.headers.put(resHeader.getKey(), sb.toString());
                sb.setLength(0);
            }
            InputStream httpInputStream = httpUrlConnection.getInputStream();

//            String encoding = httpUrlConnection.getContentEncoding();
//            if ("gzip".equalsIgnoreCase(encoding)) {
//                httpInputStream = new GZIPInputStream(httpInputStream);
//                response.headers.remove("Content-Encoding");
//            } else if ("deflate".equalsIgnoreCase(encoding)) {
//                httpInputStream = new DeflaterInputStream(httpInputStream);
//                response.headers.remove("Content-Encoding");
//            }
//            BufferedReader reader = new BufferedReader(new InputStreamReader(httpInputStream, StandardCharsets.UTF_8));
//            String line = reader.readLine();
//            while (line!=null) {
//                sb.append(line);
//                sb.append('\r');
//                sb.append('\n');
//                logger.info(line);
//                line = reader.readLine();
//            }

            ByteArrayOutputStream bios = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = httpInputStream.read(buffer)) != -1) {
                bios.write(buffer, 0, n);
            }

            response.content = Base64.getEncoder().encodeToString(bios.toByteArray());
            bios.close();
            httpInputStream.close();
            httpUrlConnection.disconnect();
        } catch (Exception e) {
            response.error = e.getMessage();
            logger.error(response.error);
            return response;
        }
        return response;
    }
}
