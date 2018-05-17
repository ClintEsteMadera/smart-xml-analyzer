package com.agileengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public final class HtmlElementFinder {

	private static Logger LOGGER = LoggerFactory.getLogger(HtmlElementFinder.class);

	private static String CHARSET_NAME = "utf8";

	private HtmlElementFinder() {
		super();
	}

	public static Optional<Element> findElementById(File htmlFile, String targetElementId) {
		try {
			Document doc = parseDocument(htmlFile);

			return Optional.of(doc.getElementById(targetElementId));

		} catch (IOException e) {
			LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
			return Optional.empty();
		}
	}

	public static Optional<Elements> findElementsByQuery(File htmlFile, String cssQuery) {
		try {
			Document doc = parseDocument(htmlFile);

			return Optional.of(doc.select(cssQuery));

		} catch (IOException e) {
			LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
			return Optional.empty();
		}
	}

	private static Document parseDocument(File htmlFile) throws IOException {
		return Jsoup.parse(
				htmlFile,
				CHARSET_NAME,
				htmlFile.getAbsolutePath());
	}
}
