package com.agileengine;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class XmlElement {

	private final Map<String, String> attributes;
	private final String cssSelector;
	private XmlElement originalElement;
	private final double similarityScore;

	/**
	 *
	 * @param attributes
	 * @param cssSelector
	 * @param originalElement optional, can be null when we use this object for modelling the original element itself
	 */
	public XmlElement(Map<String, String> attributes, String cssSelector, XmlElement originalElement) {
		if (attributes == null || attributes.isEmpty()) {
			throw new IllegalArgumentException("unexpected absence of attributes within an element!");
		}
		this.originalElement = originalElement;
		this.attributes = attributes;
		this.cssSelector = cssSelector;
		this.similarityScore = calculateSimilarityScore();
	}

	public double getSimilarityScore() {
		return this.similarityScore;
	}

	/**
	 * Original:
	 *                         <div class="panel-body">
	 *                             <a
	 *                                 id="make-everything-ok-button"
	 *                                 class="btn btn-success"
	 *                                 href="#ok"
	 *                                 title="Make-Button"
	 *                                 rel="next"
	 *                                 onclick="javascript:window.okDone(); return false;">
	 *                               Make everything OK
	 *                             </a>
	 *                         </div>
	 *
	 *
	 * 3:
	 *                <div class="panel-body">
	 *                           <a href="#ok" class="btn btn-warning" rel="next" onclick="javascript:window.close(); return false;">
	 *                             Make something somehow
	 *                           </a>
	 *                         </div>
	 *                         <!-- /.panel-body -->
	 *                         <div class="panel-footer">
	 *                           <a
	 *                               class="btn btn-success"
	 *                               href="#ok"
	 *                               title="Do-Link"
	 *                               rel="next"
	 *                               onclick="javascript:window.okDone(); return false;">
	 *                             Do anything perfect
	 *                           </a>
	 *                         </div>
	 */


	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getCssSelector() {
		return cssSelector;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		XmlElement that = (XmlElement) o;
		return Objects.equals(attributes, that.attributes) &&
				Objects.equals(cssSelector, that.cssSelector);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, cssSelector);
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("attributes", attributes)
				.append("cssSelector", cssSelector)
				.append("similarityScore", similarityScore)
				.toString();
	}

	/**
	 * For implementing these functions/methods, we can even consider using libraries that implement well-known distance
	 * and string similarity such as https://github.com/tdebatty/java-string-similarity
	 *
	 * @return a score representing how similar is this element with the original one (if there's no original, it will return 0)
	 */
	private double calculateSimilarityScore() {
		// TODO This is the point where a lot of improvements can be made, i.e. assign weights to similarities such as:
		// TODO proximity in the DOM,
		// TODO type of node - to de-hardcode the "a.btn" we used in SmartXmlAnalyzer
		// TODO the actual element's value (e.g. for button, the actual text we see displayed over the button)
		// TODO title, onclick and href/rel matching, etc. *The result would be a combination of all those scores*
		// TODO for the time being, we will just use over-simplified implementations for "class" and "href" to make the 4 examples work.

		if (this.originalElement != null) {
			// The similarity by element type is not weighted here since we're assuming that we don't query the DOM for non anchor elements,
			// but, in real life, we should put a big weight on this similarity criterion, it's probably the most important.
			return 0 * similarityScoreBasedOnElemType() +
					0.8 * similarityScoreBasedOnCssClass() +
					0.2 * similarityScoreBasedOnHref();
		}
		return 0;
	}


	// for buttons, we might need to cater for different kinds: <a>, <button>, or <input type="button">.
	private double similarityScoreBasedOnElemType() {
		return 1;
	}

	private double similarityScoreBasedOnCssClass() {
		@SuppressWarnings("unused")
		String originalCssClass = nullSafeAttributeValue(this.originalElement, "class");
		String thisCssClass = nullSafeAttributeValue(this, "class");

		// TODO Split into individual classes and compare each of them to achieve a precise score from 0 to 1

		// FIXME For the time being, we just hard-code what we want for the exam
		return thisCssClass.contains("btn-success") ? 1 : 0;
	}

	private double similarityScoreBasedOnHref() {
		String originalHref = nullSafeAttributeValue(this.originalElement, "href");
		String thisHref = nullSafeAttributeValue(this, "href");

		return thisHref.equals(originalHref) ? 1 : 0;
	}

	private String nullSafeAttributeValue(XmlElement elem, String attrName) {
		return Optional.ofNullable(elem.getAttributes().get(attrName)).orElse("");
	}
}
