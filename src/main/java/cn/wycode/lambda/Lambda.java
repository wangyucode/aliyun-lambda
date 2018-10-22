package cn.wycode.lambda;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.PojoRequestHandler;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lambda implements PojoRequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context) {
        FunctionComputeLogger logger = context.getLogger();

        Response response = new Response();

        logger.info(request.body);

//        try {
//            URL url = new URL("http://www.baidu.com");
//            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
//            httpUrlConnection.setConnectTimeout(30000);
//            httpUrlConnection.setReadTimeout(30000);
//            httpUrlConnection.setRequestMethod("GET");
//            httpUrlConnection.setUseCaches(false);
//            StringBuilder sb = new StringBuilder();
//            logger.info(Charset.defaultCharset().name());
//            InputStream httpInputStream = httpUrlConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(httpInputStream, StandardCharsets.UTF_8));
//            String line = reader.readLine();
//            while (line != null) {
//                sb.append(line);
//                logger.info(line);
//                line = reader.readLine();
//            }
//            response.content = sb.toString();
//            httpInputStream.close();
//            httpUrlConnection.disconnect();
//        } catch (Exception e) {
//            response.error = e.getMessage();
//            logger.error(response.error);
//            return response;
//        }
//        return response;

        logger.info("method->" + request.method);
        logger.info("uri->" + request.uri);
        logger.info("protocolVersion->" + request.protocolVersion);
        logger.info("uri->" + request.body);

        URL url = null;
        try {
            url = new URI(request.uri).toURL();
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
            logger.info(Charset.defaultCharset().name());
            InputStream httpInputStream = httpUrlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpInputStream, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line!=null) {
                sb.append(line);
                logger.info(line);
                line = reader.readLine();
            }
            response.content = sb.toString();
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
