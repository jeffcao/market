package com.tblin.market.info;

public interface SecurityCoder {

	/**
	 * 加密url,注意最有一个/后面的字符为holder,不能加密 加密后需要URLEncoder.encode
	 */
	public String code(String url);

}
