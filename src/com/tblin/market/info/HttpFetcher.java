package com.tblin.market.info;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tblin.embedmarket.MyLog;

/**
 * 这个类负责把给服务器的消息发送出去,开始工作时 要先open，退出程序时要close
 * 
 */
public class HttpFetcher implements Runnable {
	private static final String TAG = HttpFetcher.class.getName();
	private static final int MAX_QUEUE = 100;
	private static final Data EOF = new Data(null, null);
	private boolean isOpen = false;
	private static final HttpFetcher INSTANCE = new HttpFetcher();

	public static HttpFetcher getInstance() {
		return INSTANCE;
	}

	private volatile boolean running;
	private Thread thread;
	private final BlockingQueue<Data> outQ;

	public HttpFetcher() {
		this.outQ = new LinkedBlockingQueue<Data>(MAX_QUEUE);
	}

	public void open() {
		if (isOpen) {
			return;
		}
		isOpen = true;
		running = true;
		outQ.clear();
		thread = new Thread(this);
		thread.start();
	}

	public void close() {
		if (!isOpen) {
			return;
		}
		running = false;
		try {
			outQ.put(EOF);
		} catch (InterruptedException e) {

		}
		if (null != thread) {
			try {
				thread.join(100);
			} catch (InterruptedException e) {

			}
			thread.interrupt();
			thread = null;
		}
		isOpen = false;
	}

	public boolean invoke(HttpUriRequest req, HttpResult callback)
			throws InterruptedException {
		Data d = new Data(req, callback);
		return outQ.offer(d, 100, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		while (running) {
			try {
				Data d = outQ.take();
				if (EOF == d)
					break;
				process(d);
			} catch (InterruptedException e) {
				running = false;
				break;
			} catch (Exception e) {
				MyLog.w(TAG, e);
			}
		}
	}

	public void join(long millis) throws InterruptedException {
		if (null != thread) {
			thread.join(millis);
		}
	}

	private void process(Data d) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse resp = httpClient.execute(d.req);
			d.cb.notify(d.req, resp);
		} catch (IOException e) {
			d.cb.notify(d.req, e);
		}
	}

	private static final class Data {
		protected HttpUriRequest req;
		protected HttpResult cb;

		protected Data(HttpUriRequest req, HttpResult cb) {
			this.req = req;
			this.cb = cb;
		}
	}

	public interface HttpResult {
		public void notify(HttpRequest req, HttpResponse resp)
				throws IOException;

		public void notify(HttpRequest req, IOException e) throws IOException;
	}
}
