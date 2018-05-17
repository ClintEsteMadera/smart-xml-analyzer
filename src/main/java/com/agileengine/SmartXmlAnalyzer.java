package com.agileengine;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SmartXmlAnalyzer {

	private static Logger LOGGER = LoggerFactory.getLogger(SmartXmlAnalyzer.class);

	private String originalFilePath;
	private final String diffCaseFilePath;
	private final String targetElementID;

	public SmartXmlAnalyzer(String originalFilePath, String diffCaseFilePath, String targetElementID) {
		this.originalFilePath = originalFilePath;
		this.diffCaseFilePath = diffCaseFilePath;
		this.targetElementID = targetElementID;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException("Wrong parameter list size: 3 params are expected!");
		}
		final String originalFilePath = args[0];
		final String diffCaseFilePath = args[1];
		final String targetElementID = args[2];

		LOGGER.info("originalFilePath = {}", originalFilePath);
		LOGGER.info("diffCaseFilePath = {}", diffCaseFilePath);
		LOGGER.info("targetElementID = {}\n", targetElementID);

		SmartXmlAnalyzer analyzer = new SmartXmlAnalyzer(originalFilePath, diffCaseFilePath, targetElementID);

		Optional<XmlElement> maybeOriginalElement = analyzer.findElementToBeMatched();

		if (!maybeOriginalElement.isPresent()) {
			throw new IllegalArgumentException(format("Could not find the element with ID %s in %s", targetElementID, originalFilePath));
		}
		XmlElement originalElement = maybeOriginalElement.get();
		LOGGER.info("Original element: [{}]", originalElement);

		analyzer.findMostSimilarElementBasedOn(originalElement);
	}

	/**
	 * @return the CSS selector that uniquely identifies the element in <code>diffCaseFilePath</code> most similar to <code>originalElement</code>.
	 */
	private void findMostSimilarElementBasedOn(XmlElement originalElement) {

		// FIXME Hard-coded
		String cssQuery = "a.btn";
		Optional<Elements> maybeSimilarElements = HtmlElementFinder.findElementsByQuery(new File(diffCaseFilePath), cssQuery);

		if (!maybeSimilarElements.isPresent()) {
			throw new IllegalStateException("Could not find similar elements in " + diffCaseFilePath);
		}
		List<XmlElement> xmlElements = this.toXmlElements(maybeSimilarElements.get(), originalElement);

		LOGGER.trace("Similar elements:\n" + xmlElements);

		XmlElement mostSimilarElement = this.mostSimilarElement(xmlElements);

		LOGGER.info("MOST SIMILAR ELEMENT: " + mostSimilarElement);
	}

	private XmlElement mostSimilarElement(List<XmlElement> elements) {
		return elements.stream()
				.max(comparingDouble(XmlElement::getSimilarityScore))
				.orElseThrow(() -> new IllegalStateException("Problem occurred while sorting similar elements by similarity!"));
	}

	private Optional<XmlElement> findElementToBeMatched() {
		Optional<Element> maybeButton = HtmlElementFinder.findElementById(new File(this.originalFilePath), this.targetElementID);
		return maybeButton.map(element -> toXmlElement(element, null));
	}

	private Map<String, String> elementAttributes(Element element) {
		return element.attributes().asList()
						.stream()
						.collect(toMap(Attribute::getKey, Attribute::getValue));
	}

	private List<XmlElement> toXmlElements(Elements elements, XmlElement originalElement) {
		return elements
				.stream()
				.map(element -> toXmlElement(element, originalElement))
				.collect(toList());
	}

	private XmlElement toXmlElement(Element element, XmlElement originalElement) {
		return new XmlElement(elementAttributes(element), element.cssSelector(), originalElement);
	}
}
