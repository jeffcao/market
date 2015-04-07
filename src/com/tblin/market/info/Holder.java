package com.tblin.market.info;

import org.json.JSONObject;

public class Holder extends ServerMessageParser {

	public static class HolderCreator {

		private static int id = 1;

		public static Holder creatHolder(AbstractJsonParser parser,
				DataListener lsnr) {
			return new Holder(id++, parser, lsnr);
		}
	}

	private int id;
	private DataListener lsnr;

	private Holder(int id, AbstractJsonParser parser, DataListener lsnr) {
		super(parser);
		this.id = id;
		this.lsnr = lsnr;
	}

	/**
	 * 这里不能直接调用继承于父类的parse方法，因为父类 并没有DataListener, DataListener是在这里传入的
	 */
	public void parse(String text) {
		parse(text, lsnr);
	}

	public void parse(JSONObject obj) {
		parse(obj, lsnr);
	}

	public int getId() {
		return id;
	}

	public DataListener getDataListener() {
		return lsnr;
	}

}
