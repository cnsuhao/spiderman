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
		url = url.replaceAll(SectionXmlEnum.binghao.getXmlSr(), SectionXmlEnum.binghao.getHtmlSr());
		url = url.replaceAll(SectionXmlEnum.danyin.getXmlSr(), SectionXmlEnum.danyin.getHtmlSr());
		url = url.replaceAll(SectionXmlEnum.shuangyin.getXmlSr(), SectionXmlEnum.shuangyin.getHtmlSr());
		url = url.replaceAll(SectionXmlEnum.xiaoyu.getXmlSr(), SectionXmlEnum.xiaoyu.getHtmlSr());
		url = url.replaceAll(SectionXmlEnum.dayu.getXmlSr(), SectionXmlEnum.dayu.getHtmlSr());
		return UrlUtils.resolveUrl(beginUrl, url);
	}
	
	public static String normalizeXml(String url)
	{
		if(url != null && !"".equals(url))
		{
			url = url.replaceAll(SectionXmlEnum.binghao.getXmlSr(), SectionXmlEnum.binghao.getHtmlSr());
			url = url.replaceAll(SectionXmlEnum.danyin.getXmlSr(), SectionXmlEnum.danyin.getHtmlSr());
			url = url.replaceAll(SectionXmlEnum.shuangyin.getXmlSr(), SectionXmlEnum.shuangyin.getHtmlSr());
			url = url.replaceAll(SectionXmlEnum.xiaoyu.getXmlSr(), SectionXmlEnum.xiaoyu.getHtmlSr());
			url = url.replaceAll(SectionXmlEnum.dayu.getXmlSr(), SectionXmlEnum.dayu.getHtmlSr());
		}
		return url;
	}
}
