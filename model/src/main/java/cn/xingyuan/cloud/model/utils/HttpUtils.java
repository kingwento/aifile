package cn.xingyuan.cloud.model.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

/**
 * Http工具类
 */
public final class HttpUtils {

	//编码UTF-8
	public static final String ENCODING = "UTF-8";
	public static final String HTTPS = "https://";
	//串联符
	public static final String SPE1 = ",";
	//示意符
	public static final String SPE2 = ":";
	//连接符
	public static final String SPE3 = "&";
	//赋值符
	public static final String SPE4 = "=";
	//问号符
	public static final String SPE5 = "?";

//	public static void main(String[] args) throws Exception {
//		Response rb=httpGet("http://www.baidu.com", 10000, null,null);
//		System.out.println(rb.getBody());
//	}

	static HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	static class MiTM implements TrustManager, X509TrustManager {
		public void checkServerTrusted(X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

	private static void trustAllHttpsCertificates() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[1];
			TrustManager tm = new MiTM();
			trustAllCerts[0] = tm;
			SSLContext sc;
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	/**
	 * HTTP GET
	 *
	 * @param path 路径
	 * @param connectTimeout 连接超时时间
	 * @param headers 头数据
	 * @param querys 查询参数
	 * @throws Exception 外抛异常
	 * @return 返回结果对象
	 */
	public static Response httpGet(String path, int connectTimeout, Map<String, String> headers,
        Map<String, String> querys) throws Exception {
		if (path.startsWith("https://")) {
			return httpsGet(path, connectTimeout, headers, querys);
		}
		Response res = new Response();
		URL url = new URL(initUrl(path, querys));
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setUseCaches(false);
			if(null!=headers){
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			res = convert(conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(is);
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * HTTPS GET
	 *
	 * @param path      :请求地址
	 * @param connectTimeout     :连接超时时间
	 * @param headers   :头数据
	 * @param querys    :查询参数
	 * @throws Exception 外抛异常
	 * @return 返回结果对象
	 */
	public static Response httpsGet(String path, int connectTimeout, Map<String, String> headers,
									Map<String, String> querys) throws Exception {
		Response res = new Response();
		URL url = new URL(initUrl(path, querys));
		HttpsURLConnection conn = null;
		InputStream is = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			sslClient(conn);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setUseCaches(false);
			if (null != headers) {
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			res = convert(conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(is);
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * @param host host值
	 * @param path 路径
	 * @param connectTimeout 超时时间
	 * @param querys 请求参数
	 * @param body body参数值
	 * @return 返回GET请求的结果对象
	 */
	public static Response httpsGet(String host, String path, int connectTimeout, Map<String, String> querys, Map<String, String> body) {
		Response res = new Response();
		InputStreamReader insr = null;
		try {
			StringBuilder sb = new StringBuilder();
			URL url = new URL(initUrl(host, path, body));
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
			for (String set : querys.keySet()){
				httpsConn.setRequestProperty(set, querys.get(set));
			}
			httpsConn.setConnectTimeout(connectTimeout);
			httpsConn.setReadTimeout(connectTimeout);
			// 连接超时时间
			httpsConn.setConnectTimeout(10000);
			// 读取数据超时时间
			httpsConn.setReadTimeout(10000);
			insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
			int respInt = insr.read();
			while (respInt != -1) {
				sb.append((char) respInt);
				respInt = insr.read();
			}
			res.setStatusCode(httpsConn.getResponseCode());
			res.setBody(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (insr != null) {
					insr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * @param host host值
	 * @param path 路径
	 * @param connectTimeout 超时时间
	 * @param querys 请求参数
	 * @param body body参数值
	 * @return 返回GET请求的结果对象
	 */
	public static Response httpsDelete(String host, String path, int connectTimeout, Map<String, String> querys, Map<String, String> body) {
		Response res = new Response();
		InputStreamReader insr = null;
		try {
			StringBuilder sb = new StringBuilder();
			URL url = new URL(initUrl(host, path, body));
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
			for (String set : querys.keySet()){
				httpsConn.setRequestProperty(set, querys.get(set));
			}
			httpsConn.setConnectTimeout(connectTimeout);
			httpsConn.setReadTimeout(connectTimeout);
			// 连接超时时间
			httpsConn.setConnectTimeout(10000);
			// 读取数据超时时间
			httpsConn.setReadTimeout(10000);
			httpsConn.setRequestMethod("DELETE");
			insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
			int respInt = insr.read();
			while (respInt != -1) {
				sb.append((char) respInt);
				respInt = insr.read();
			}
			res.setStatusCode(httpsConn.getResponseCode());
			res.setBody(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (insr != null) {
					insr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return res;
	}


	/**
	 * HTTP POST表单
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param bodys bodys值
	 * @throws Exception 外抛异常
	 * @return 返回Post请求的结果对象
	 */
	public static Response httpPost(String path, int connectTimeout, Map<String, String> headers,
									Map<String, String> querys, Map<String, String> bodys) throws Exception {
		if (path.startsWith("https://")) {
			return httpsPost(path, connectTimeout, headers, querys, bodys.toString());
		}
		Response res = new Response();
		HttpURLConnection conn = null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");
			if (null != headers) {
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			if (null != bodys) {
				StringBuffer params = new StringBuffer();
				outputStream = conn.getOutputStream();
				for (String dataKey : bodys.keySet()) {
					params.append(dataKey);
					params.append("=");
					params.append(bodys.get(dataKey));
					params.append("&");
				}
				outputStream.write(params.toString().getBytes("UTF-8"));
				outputStream.close();
			}
			res = convert(conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if (null != querys) {
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * HTTPS POST表单
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param bodys bodys值
	 * @throws Exception 外抛异常
	 * @return 返回post请求后的结果对象
	 */
	public static Response httpsPost(String path, int connectTimeout, Map<String, String> headers,
									 Map<String, String> querys, JSONObject bodys) throws Exception {
		Response res = new Response();
		HttpsURLConnection conn = null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			sslClient(conn);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/json");
			if (null != headers) {
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			if (null != bodys) {
				StringBuffer params = new StringBuffer();
				outputStream = conn.getOutputStream();
				for (String dataKey : bodys.keySet()) {
					params.append(dataKey);
					params.append("=");
					params.append(bodys.get(dataKey));
					params.append("&");
				}
				outputStream.write(bodys.toJSONString().getBytes("UTF-8"));
				outputStream.close();
			}
			res = convert(conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if (null != querys) {
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * Http POST 字符串
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param body bodys值
	 * @throws Exception 外抛异常
	 * @return 返回post请求的结果对象
	 */
	public static Response httpPost(String path, int connectTimeout, Map<String, String> headers,
									Map<String, String> querys, String body) throws Exception {

		if (path.startsWith("https://")) {
			return httpsPost(path, connectTimeout, headers, querys, body);
		}
		Response res = new Response();
		HttpURLConnection conn = null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");
			if (null != headers) {
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			// 当有数据需要提交时
			if (StringUtils.isNotBlank(body)) {
				outputStream = conn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(body.getBytes("UTF-8"));
				outputStream.close();
			}
			res = convert(conn);
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * Https POST 字符串
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param body bodys值
	 * @throws Exception 外抛异常
	 * @return 返回post请求的结果对象
	 */
	public static Response httpsPost(String path, int connectTimeout, Map<String, String> headers,
									 Map<String, String> querys, String body) throws Exception {
		Response res = new Response();
		HttpsURLConnection conn = null;
		OutputStream outputStream = null;
		DataOutputStream out = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpsURLConnection) url.openConnection();
			sslClient(conn);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			if (null != headers) {
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
				}
			}
			// 当有数据需要提交时
			if (StringUtils.isNotBlank(body)) {
//				out = new DataOutputStream(conn.getOutputStream());
//				out.writeBytes(body);
//				out.flush();
//				out.close();
				outputStream = conn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(body.getBytes());
				outputStream.close();

			}
			res = convert(conn);
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * HTTP POST 字节数组
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param bodys bodys值
	 * @throws Exception 外抛异常
	 * @return 返回post请求的结果对象
	 */
	public static Response httpPost(String path, int connectTimeout, Map<String, String> headers,
									Map<String, String> querys, byte[] bodys) throws Exception {
		if (path.startsWith("https://")) {
			return httpsPost(path, connectTimeout, headers, querys, bodys);
		}
		Response res = new Response();
		HttpURLConnection conn = null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");

			outputStream = conn.getOutputStream();
			// 注意编码格式，防止中文乱码
			outputStream.write(bodys);
			outputStream.close();
			res = convert(conn);
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	/**
	 * HTTPS POST 字节数组
	 *
	 * @param path url路径
	 * @param connectTimeout 超时时间
	 * @param headers 头数据
	 * @param querys 查询数据
	 * @param bodys bodys值
	 * @throws Exception 外抛异常
	 * @return 返回post请求的结果对象
	 */
	public static Response httpsPost(String path, int connectTimeout, Map<String, String> headers,
									 Map<String, String> querys, byte[] bodys) throws Exception {
		Response res = new Response();
		HttpsURLConnection conn = null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(initUrl(path, querys));
			conn = (HttpsURLConnection) url.openConnection();
			sslClient(conn);
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "multipart/form-data; boundary=--WebKitFormBoundary7MA4YWxkTrZu0gW");

			outputStream = conn.getOutputStream();
			// 注意编码格式，防止中文乱码
			outputStream.write(bodys);
			outputStream.close();
			res = convert(conn);
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=headers){
				headers.clear();
			}
			if(null!=querys){
				querys.clear();
			}
			IOUtils.closeQuietly(outputStream);
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}

	private static String initUrl(String url, Map<String, String> querys) throws UnsupportedEncodingException {
		StringBuilder sbUrl = new StringBuilder();
		sbUrl.append(url);
		if (null != querys) {
			StringBuilder sbQuery = new StringBuilder();
			for (Map.Entry<String, String> query : querys.entrySet()) {
				if (0 < sbQuery.length()) {
					sbQuery.append(Constants.SPE3);
				}
				if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
					sbQuery.append(query.getValue());
				}
				if (!StringUtils.isBlank(query.getKey())) {
					sbQuery.append(query.getKey());
					if (!StringUtils.isBlank(query.getValue())) {
						sbQuery.append(Constants.SPE4);
						sbQuery.append(URLEncoder.encode(query.getValue(), Constants.ENCODING));
					}
				}
			}
			if (0 < sbQuery.length()) {
				sbUrl.append(Constants.SPE5).append(sbQuery);
			}
		}

		return sbUrl.toString();
	}

	private static String initUrl(String host, String path, Map<String, String> querys)
			throws UnsupportedEncodingException {
		StringBuilder sbUrl = new StringBuilder();
		sbUrl.append(host);
		if (!StringUtils.isBlank(path)) {
			sbUrl.append(path);
		}
		if (null != querys) {
			StringBuilder sbQuery = new StringBuilder();
			for (Map.Entry<String, String> query : querys.entrySet()) {
				if (0 < sbQuery.length()) {
					sbQuery.append(SPE3);
				}
				if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
					sbQuery.append(query.getValue());
				}
				if (!StringUtils.isBlank(query.getKey())) {
					sbQuery.append(query.getKey());
					if (!StringUtils.isBlank(query.getValue())) {
						sbQuery.append(SPE4);
						sbQuery.append(URLEncoder.encode(query.getValue(), ENCODING));
					}
				}
			}
			if (0 < sbQuery.length()) {
				sbUrl.append(SPE5).append(sbQuery);
			}

		}
		return sbUrl.toString();
	}


	/**
	 * @param timeout 超时时间
	 * @return 读取超时时间
	 */
	private static int getTimeout(int timeout) {
		if (timeout == 0) {
			return Constants.DEFAULT_TIMEOUT;
		}

		return timeout;
	}

	private static Response convert(HttpURLConnection conn) throws IOException {
		Response res = new Response();
		InputStream is = null;
		try {
			conn.connect();
			is = conn.getInputStream();
			res.setStatusCode(conn.getResponseCode());
			res.setBody(IOUtils.toString(is, "utf-8"));
			res.setErrorMessage(conn.getResponseMessage());
			res.setContentType(conn.getHeaderField("Content-Type"));
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatusCode(500);
			res.setErrorMessage("No Response");
		} finally {
			IOUtils.closeQuietly(is);
		}
		return res;
	}

	private static Response convert(HttpsURLConnection conn) throws IOException {
		Response res = new Response();
		InputStream is = null;
		try {
			conn.connect();
			is = conn.getInputStream();
			res.setStatusCode(conn.getResponseCode());
			res.setBody(IOUtils.toString(is, "UTF-8"));
			res.setErrorMessage(conn.getResponseMessage());
			String headerField = conn.getHeaderField("Content-Type");
			res.setContentType(headerField);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatusCode(500);
			res.setErrorMessage("No Response");
		} finally {
			IOUtils.closeQuietly(is);
		}
		return res;
	}

	/**
	 * 将流转换为字符串
	 * @param is 输入流对象
	 * @throws IOException 外抛异常
	 * @return 返回字符串结果
	 */
	public static String readStreamAsStr(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		WritableByteChannel dest = Channels.newChannel(bos);
		ReadableByteChannel src = Channels.newChannel(is);
		ByteBuffer bb = ByteBuffer.allocate(4096);

		while (src.read(bb) != -1) {
			bb.flip();
			dest.write(bb);
			bb.clear();
		}
		src.close();
		dest.close();

		return new String(bos.toByteArray(), Constants.ENCODING);
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url 发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static Response sendPost(String url, String param, Integer time) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		Response res = new Response();
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			if (time > 0) {
				conn.setConnectTimeout(time * 1000);
				conn.setReadTimeout(time * 1000);
			}
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		res.setBody(result);
		return res;
	}

	private static void sslClient(HttpsURLConnection httpClient) {
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			httpClient.setSSLSocketFactory(ssf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void buildUploadStream(String url,MultipartFile file) throws IOException {
		String fileName = file.getName();

		String newLine = "\r\n";
		String boundaryPrefix = "--";
		String boundary = "--WebKitFormBoundary7MA4YWxkTrZu0gW";

		URL postUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Type",
				        "multipart/form-data; boundary=" + boundary);

		OutputStream out = conn.getOutputStream();


		StringBuilder sb = new StringBuilder();

		sb.append(boundaryPrefix);
		sb.append(boundary);
		sb.append(newLine);

		sb.append("Content-Disposition: form-data; name=\"id\"");
		sb.append(newLine);
		sb.append(newLine);
		sb.append("testCodeUpload");
		sb.append(newLine);

		sb.append(boundaryPrefix);
		sb.append(boundary);
		sb.append(newLine);

		sb.append("Content-Disposition: form-data; name=\"name\"");
		sb.append(newLine);
		sb.append(newLine);
		sb.append(fileName);
		sb.append(newLine);

		sb.append(boundaryPrefix);
		sb.append(boundary);
		sb.append(newLine);

		sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
				        + fileName + "\"");
		sb.append("Content-Type: application/octet-stream");
		sb.append(newLine);
		sb.append(newLine);

		out.write(sb.toString().getBytes());
		out.write(file.getBytes());
		out.write(newLine.getBytes());
		byte[] end_data = (newLine + boundaryPrefix + boundary + boundaryPrefix + newLine)
		        .getBytes();
		out.write(end_data);
		out.flush();
		out.close();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				       conn.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
	}

	/**
	 * post请求提交form-data上传文件
	 *
	 * @param url 上传地址
	 * @param headers 请求头
	 * @param file 上传文件
	 * @return
	 */
	public static String doPostUploadFile(String url, Map<String, String> headers, MultipartFile file) {
		HttpPost httpPost = new HttpPost(url);
		packageHeader(headers, httpPost);
		String fileName = file.getName();

		CloseableHttpResponse response = null;

		String respContent = null;

		// 设置请求头 boundary边界不可重复，重复会导致提交失败
		String boundary = "------WebKitFormBoundary7MA4YWxkTrZu0gW";
		httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

		// 创建MultipartEntityBuilder
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		// 设置字符编码
		builder.setCharset(StandardCharsets.UTF_8);
		// 模拟浏览器
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		// 设置边界
		builder.setBoundary(boundary);
		// 设置multipart/form-data流文件
		File file1 = transferToFile(file);
		builder.addPart("file",  new FileBody(file1));
		// application/octet-stream代表不知道是什么格式的文件
		builder.addBinaryBody("media", file1, ContentType.create("application/octet-stream"), fileName);

		HttpEntity entity = builder.build();
		httpPost.setEntity(entity);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			response = httpClient.execute(httpPost);
			if (response != null && response.getStatusLine() != null && response.getStatusLine().getStatusCode() < 400) {
				HttpEntity he = response.getEntity();
				if (he != null) {
					respContent = EntityUtils.toString(he, "UTF-8");
				}
			} else {
				throw new RuntimeException();
			}
			return respContent;
		} catch (Exception e) {
			throw new RuntimeException();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if(null != httpClient){
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * 封装请求头
	 *
	 * @param paramsHeads
	 * @param httpMethod
	 */
	private static void packageHeader(Map<String, String> paramsHeads, HttpRequestBase httpMethod) {
		if (null!= paramsHeads && paramsHeads.size()>0) {
			Set<Map.Entry<String, String>> entrySet = paramsHeads.entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				httpMethod.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	public static File transferToFile(MultipartFile multipartFile) {
//        选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
		File file = null;
		try {
			String originalFilename = multipartFile.getOriginalFilename();
			String[] filename = originalFilename.split("\\.");
			file=File.createTempFile(filename[0], filename[1]);
			multipartFile.transferTo(file);
			file.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void main(String[] args) throws IOException {
		// 创建 HTTP POST 请求
		HttpPost post = new HttpPost("https://online-test.cpdf365.cn/openai/load_documents?token=650d2bb90f92fbe90cab676c");
		// 创建 HTTP 客户端
		HttpClient client = HttpClientBuilder.create().build();
		// 构造表单数据
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addPart("file", new FileBody(new File("/Users/kingwen/Desktop/中牟水务系统小程序代码.docx")));
		// 设置请求实体
		HttpEntity entity = builder.build();
		post.setEntity(entity);
		// 执行请求并获取响应
		String response = EntityUtils.toString(client.execute(post).getEntity());
		// 输出响应内容
		System.out.println(response);
	}

}