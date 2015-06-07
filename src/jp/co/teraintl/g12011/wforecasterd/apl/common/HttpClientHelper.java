package jp.co.teraintl.g12011.wforecasterd.apl.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.Uri.Builder;

/**
 * Http リクエストクライアントを生成し、リクエスト送信を管理するシングルトンクラス。
 * @author tsutomu
 */
public class HttpClientHelper {
	
	/** HttpClientHelper インスタンス */
	private static final HttpClientHelper instance = new HttpClientHelper();
	
	/** コンストラクタ */
	private HttpClientHelper(){}
	
	/**
	 * HttpClientHelper インスタンスを取得する
	 * @return HttpClientHelperインスタンス
	 */
	public static HttpClientHelper getInstance() {
		return instance;
	}
	
	/**
	 * httpリクエストを送信し、レスポンスを取得する。
	 * @param uriBuilder リクエスト送信uri情報
	 * @return httpレスポンス
	 * @throws IOException 入出力エラー 
	 * @throws ClientProtocolException httpプロトコルのエラーシグナル 
	 */
	public HttpResponse sendRequest(Builder uriBuilder) throws
			ClientProtocolException, IOException {
		
		// http クライアントの生成
		HttpClient httpClient = new DefaultHttpClient();
		
		// タイムアウトの設定
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 4000);
		HttpConnectionParams.setSoTimeout(params, 4000);
		
		// http リクエストを生成し、レスポンスを返す
		return httpClient.execute(new HttpGet(uriBuilder.toString()));
	}
	
	/**
	 * リクエスト送信先とのコネクションとなるインプットストリームを取得する。
	 * @param url リクエスト送信先
	 * @return ストリーム
	 * @throws IOException 入出力例外
	 * @throws MalformedURLException URL形式例外 
	 */
	public InputStream getConnectionStream(String url) throws MalformedURLException, IOException {
		return new URL(url).openConnection().getInputStream();
	}
}
