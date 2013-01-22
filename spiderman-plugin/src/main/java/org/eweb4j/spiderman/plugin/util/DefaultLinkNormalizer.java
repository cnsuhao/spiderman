package org.eweb4j.spiderman.plugin.util;

/**
 * @author jonasabreu
 */
public class DefaultLinkNormalizer implements LinkNormalizer {

	private final String beginUrl;

	public DefaultLinkNormalizer(final String beginUrl) {
		if ((beginUrl == null) || (beginUrl.trim().length() == 0)) {
			throw new IllegalArgumentException("beginUrl cannot be null or empty");
		}
		this.beginUrl = beginUrl;
	}

	public String normalize(String url) {

		url = url.replaceAll("&amp;", "&");

		return UrlUtils.resolveUrl(beginUrl, url);
	}

}
