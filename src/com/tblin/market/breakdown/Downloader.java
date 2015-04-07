package com.tblin.market.breakdown;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.tblin.embedmarket.MyLog;

public class Downloader {
	private String urlstr;// 下载的地址
	private String localfile;// 保存路径
	private int threadcount;// 线程数
	private Dao dao;// 工具类
	private int fileSize;// 所要下载的文件的大小
	private List<DownloadInfo> infos;// 存放下载信息类的集合
	private static final int STATE_INIT = 1;// 定义三种下载的状态：初始化状态，正在下载状态，暂停状态
	private static final int STATE_DOWNLOADING = 2;
	private static final int STATE_PAUSE = 3;
	private static final int STATE_CANCEL = 4;
	private volatile int state = STATE_INIT;
	private DownloadListener loadLsnr;
	private int compeleteSize;
	private volatile boolean started;
	// private Context mContext;
	private Object locker = new Object();// 同步started锁
	private static final float SAVE_PERCENT_INTERVAL = 0.05f;
	private static final String TAG = Downloader.class.toString();

	public Downloader(String urlstr, String localfile, int threadcount,
			Context context) {
		// mContext = context;
		this.urlstr = urlstr;
		this.localfile = localfile;
		this.threadcount = threadcount;
		dao = Dao.getInstance(context);
		initDownloadInfo();
	}

	/**
	 * 设置下载监听器，监听器的事件会在主线程执行，所以可以 在里面刷新界面
	 */
	public void setDownloadListener(DownloadListener loadLsnr) {
		this.loadLsnr = loadLsnr;
	}

	/**
	 * 判断是否正在下载
	 */
	public boolean isdownloading() {
		return state == STATE_DOWNLOADING;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getCompeleteSize() {
		return compeleteSize;
	}

	/**
	 * 第一次下载要进行初始化，并将下载器的信息保存到数据库中 如果不是第一次下载，那就要从数据库中读出之前下载的信息（起始位置，结束为止，文件大小等）
	 */
	public void initDownloadInfo() {
		if (isFirst(urlstr)) {
			firstDownload();
		} else {
			// 得到数据库中已有的urlstr的下载器的具体信息
			infos = dao.getInfos(urlstr);
			DownloadInfo firstInfo = infos.get(0);
			File f = new File(firstInfo.getFilePath());
			if (f.exists()) {
				int size = 0;
				compeleteSize = 0;
				for (DownloadInfo info : infos) {
					compeleteSize += info.getCompeleteSize();
					size += info.getEndPos() - info.getStartPos() + 1;
				}
				fileSize = size;
			} else {
				delete(urlstr);
				firstDownload();
			}
		}
	}

	/**
	 * 对于第一次下载的情况
	 */
	private void firstDownload() {
		MyLog.i(TAG, "is first download");
		init();
		int range = fileSize / threadcount;
		infos = new ArrayList<DownloadInfo>();
		for (int i = 0; i < threadcount - 1; i++) {
			DownloadInfo info = new DownloadInfo(i, i * range, (i + 1) * range
					- 1, 0, urlstr, localfile);
			infos.add(info);
		}
		DownloadInfo info = new DownloadInfo(threadcount - 1, (threadcount - 1)
				* range, fileSize - 1, 0, urlstr, localfile);
		infos.add(info);
		// 保存infos中的数据到数据库
		dao.saveInfos(infos);
	}

	private void init() {
		try {
			URL url = new URL(urlstr);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();
			File file = new File(localfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 本地访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (Exception e) {

		}
	}

	/**
	 * 判断是否是第一次 下载
	 */
	private boolean isFirst(String urlstr) {
		return !dao.isHasInfors(urlstr);
	}

	/**
	 * 利用线程开始下载数据
	 */
	public void download() {
		if (infos != null) {
			if (state == STATE_DOWNLOADING)
				return;
			state = STATE_DOWNLOADING;
			for (DownloadInfo info : infos) {
				new DownThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompeleteSize(),
						info.getUrl()).start();
			}
		}
	}

	public class DownThread extends Thread {

		public static final String LENGTH_KEY = "length";
		public static final String URL_KEY = "url";
		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String urlstr;
		private float lastSavepercent;

		public DownThread(int threadId, int startPos, int endPos,
				int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			this.compeleteSize = compeleteSize;
			this.urlstr = urlstr;
			this.lastSavepercent = (float) compeleteSize / (float) fileSize;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			BufferedInputStream bis = null;
			try {
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				// 设置范围，格式为Range：bytes x-y;
				connection.setRequestProperty("Range", "bytes="
						+ (startPos + compeleteSize) + "-" + endPos);
				MyLog.i(TAG, "before raf");
				randomAccessFile = new RandomAccessFile(localfile, "rwd");
				randomAccessFile.seek(startPos + compeleteSize);
				MyLog.i(TAG, "after raf");
				/*
				 * FileChannel fco = randomAccessFile.getChannel(); Log.i("",
				 * "pos:" + (fco.position())); MappedByteBuffer mbbo =
				 * fco.map(FileChannel.MapMode.READ_WRITE, startPos +
				 * compeleteSize, fco.size());
				 */
				// 将要下载的文件写到保存在保存路径下的文件中
				is = connection.getInputStream();
				bis = new BufferedInputStream(is);
				byte[] buffer = new byte[4 * 1024];
				int length = -1;
				synchronized (locker) {
					if (!started) {
						started = true;
						onStart();
					}
				}
				MyLog.i(TAG, "start:" + Downloader.this.compeleteSize + "/"
						+ fileSize + ":" + urlstr);
				MyLog.i(TAG, "state = pause?" + (state == STATE_PAUSE));
				do {
					if (state == STATE_PAUSE) {
						dao.updataInfos(threadId, compeleteSize, urlstr);
						MyLog.i(TAG, "pause:" + Downloader.this.compeleteSize
								+ "/" + fileSize + ":" + urlstr);
						onPause();
						return;
					} else if (state == STATE_CANCEL) {
						MyLog.i(TAG, "cancel:" + urlstr);
						dao.delete(urlstr);
						onCanel(urlstr);
						return;
					}

					if (bis.available() > 0) {
						length = bis.read(buffer);
						// mbbo.put(buffer, 0, length);
						randomAccessFile.write(buffer, 0, length);
						compeleteSize += length;
						float currentPercent = (float) compeleteSize
								/ (float) fileSize;
						MyLog.d(TAG, "currentPercent:" + currentPercent + "/"
								+ lastSavepercent);
						if (currentPercent - lastSavepercent >= SAVE_PERCENT_INTERVAL) {
							dao.updataInfos(threadId, compeleteSize, urlstr);
							lastSavepercent = currentPercent;
						}
						onLoading(urlstr, length);
					} else {
						Thread.sleep(50);
					}
				} while (length != -1);

			} catch (Exception e) {
				for (StackTraceElement ste : e.getStackTrace()) {
					MyLog.e(TAG, ste.toString());
				}
				MyLog.e(TAG, e.toString());
				dao.updataInfos(threadId, compeleteSize, urlstr);
				state = STATE_PAUSE;
				onNetworkError();
			} finally {
				try {
					if (is!=null)
						is.close();
					if (bis!=null)
						bis.close();
					randomAccessFile.close();
					connection.disconnect();
					dao.closeDb();
				} catch (Exception e) {
				}
			}
		}
	}

	private void onNetworkError() {
		loadLsnr.onNetworkError(urlstr);
	}

	private void onStart() {
		loadLsnr.onStart(fileSize, compeleteSize, urlstr);
	}

	private void onFinish(String url) {
		delete(url);
		onReset();
		loadLsnr.onFinish(url);
	}

	private void onLoading(String url, int length) {
		loadLsnr.onLoading(url, length);
		compeleteSize += length;
		if (compeleteSize == fileSize) {
			onFinish(url);
		}
	}

	private void onCanel(String url) {
		loadLsnr.onCancel(url);
	}

	// 删除数据库中urlstr对应的下载器信息
	public void delete(String urlstr) {
		dao.delete(urlstr);
	}

	public void cancel() {
		MyLog.i(TAG, "call downloader cancel");
		state = STATE_CANCEL;
	}

	public void pause() {
		state = STATE_PAUSE;
		started = false;
		MyLog.i(TAG, "change started to false");
	}

	// 下载过程中的一切异常都会导致pause()
	private void onPause() {
		loadLsnr.onPause(urlstr);
	}

	// 重置下载状态
	private void onReset() {
		state = STATE_INIT;
		loadLsnr.onReset(urlstr);
	}
}
