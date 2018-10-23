package cn.wycode.lambda;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.sun.jndi.toolkit.url.UrlUtil;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Lambda implements StreamRequestHandler {


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        FunctionComputeLogger logger = context.getLogger();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = input.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
        }
        logger.info("handleRequest>>>" + sb.toString());

        String firstLine = new BufferedReader(new StringReader(sb.toString())).readLine();
        String host;
        int port;
        try {
            if (firstLine.startsWith("CONNECT ")) {
                String[] urlArray = firstLine.split(" ")[1].split(":");
                host = urlArray[0];
                port = Integer.parseInt(urlArray[1]);
            } else {
                URL url = new URL(firstLine.split(" ")[1]);
                host = url.getHost();
                port = url.getPort();

                if (port == -1) {
                    if (url.getProtocol().equalsIgnoreCase("http")) {
                        port = 80;
                    } else if (url.getProtocol().equalsIgnoreCase("https")) {
                        port = 443;
                    } else {
                        throw new IllegalArgumentException("端口无法解析");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("url解析错误:" + e.getMessage());
            return;
        }
        logger.info("host->" + host);
        logger.info("port->" + port);

        Socket s = new Socket(host, port);
        if (firstLine.startsWith("CONNECT ")) {
            String response = "HTTP/1.1 200 Connection Established";
            output.write(response.getBytes(StandardCharsets.UTF_8));
            output.flush();
            sb.setLength(0);
            while ((n = input.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
            }
            logger.info("handleRequest>>>" + sb.toString());
        }

        OutputStream httpOutputStream = s.getOutputStream();
        httpOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));

        InputStream httpInputStream = s.getInputStream();
        sb.setLength(0);
        while ((n = httpInputStream.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
        }
        httpInputStream.close();
        httpOutputStream.close();

        s.close();
        input.close();
        output.close();
        logger.info("handleRequest<<<" + sb.toString());
    }
}
