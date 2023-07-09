package com.shuai.process.analyze.bpmn.config;

import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig{	
	private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

	@Value("${http.max-connection}")
    private int maxConnection;
	
	@Value("${http.max-per-route}")
    private int  maxPerRoute;

	
	@Value("${http.max-retry}")
    private int maxRetry;
	
	@Value("${http.connect-timeout}")
    private int connectTimeout;
	
	@Value("${http.read-timeout}")
    private int readTimeout;
	
	@Value("${http.request-timeout}")
    private int  requestTimeout;
	
//	 max-connection: 200
//	   max-per-route: 100
//	   max-retry: 3
//	   timeout: 20000
//	   read-timeout: 60000
//	   request-timeout: 20000
	

  @Bean
  public RestTemplate restTemplate() {
      RestTemplate restTemplate = new RestTemplateCustom();
      restTemplate.setRequestFactory(clientHttpRequestFactory());
      restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
      return restTemplate;
  }
 
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            httpClientBuilder.setSSLContext(sslContext);
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslConnectionSocketFactory).build();// 注册http和https请求
            // 开始设置连接池
            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            poolingHttpClientConnectionManager.setMaxTotal(maxConnection); // 最大连接数200
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute); // 同路由并发数100
            httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(maxRetry, true)); // 重试次数
            HttpClient httpClient = httpClientBuilder.build();
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                    httpClient); // httpClient连接配置
            clientHttpRequestFactory.setConnectTimeout(connectTimeout); // 连接超时
            clientHttpRequestFactory.setReadTimeout(readTimeout); // 数据读取超时时间
            clientHttpRequestFactory.setConnectionRequestTimeout(requestTimeout); // 连接不够用的等待时间
            return clientHttpRequestFactory;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
        	logger.error("初始化HTTP连接池出错", e);
        }
        return null;
    }




}
