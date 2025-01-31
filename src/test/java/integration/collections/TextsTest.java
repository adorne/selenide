package integration.collections;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.ListSizeMismatch;
import com.codeborne.selenide.ex.TextsMismatch;
import integration.ITest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;

import static com.codeborne.selenide.CollectionCondition.texts;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TextsTest extends ITest {
  @BeforeEach
  void openPage() {
    openFile("page_with_list_of_elements.html");
  }

  @Test
  void checksTexts() {
    $$(".element").shouldHave(texts("One", "Two", "Three"));
    $$(".element").shouldHave(texts(asList("One", "Two", "Three")));
  }

  @Test
  void checksOrderOfTexts() {
    assertThatThrownBy(() -> $$(".element").shouldHave(texts("Two", "One", "Three")))
      .isInstanceOf(TextsMismatch.class)
      .hasMessageContaining("Actual: [One, Two, Three]")
      .hasMessageContaining("Expected: [Two, One, Three]");
  }

  @Test
  void ignoresWhitespacesInTexts() {
    $$("ol.spaces li").shouldHave(
      texts("The \t\n first\n\r", "    The second      ", "The     third")
    );
  }

  @Test
  void acceptsSubstrings() {
    $$(".element").shouldHave(texts("On", "Two", "Three"));
    $$(".element").shouldHave(texts("One", "wo", "Three"));
    $$(".element").shouldHave(texts("One", "Two", "hre"));
  }

  @Test
  void errorMessage() {
    assertThatThrownBy(() -> $$(".element").shouldHave(texts("Three", "Two", "One")))
      .isInstanceOf(TextsMismatch.class)
      .hasMessageStartingWith("Text #0 mismatch (expected: \"Three\", actual: \"One\")")
      .hasMessageContaining("Actual: [One, Two, Three]")
      .hasMessageContaining("Expected: [Three, Two, One]")
      .hasMessageContaining("Collection: .element");
  }

  @Test
  void ignoresCase() {
    $$(".element").shouldHave(texts("one", "two", "three"));
    $$(".element").shouldHave(texts("ONE", "TWO", "THREE"));
    $$(".element").shouldHave(texts("onE", "twO", "threE"));
  }

  @Test
  void checksElementsCount() {
    assertThatThrownBy(() -> $$(".element").shouldHave(texts("One", "Two", "Three", "Four")))
      .isInstanceOf(ListSizeMismatch.class)
      .hasMessageStartingWith("List size mismatch: expected: = 4, actual: 3, collection: .element");
  }

  @Test
  void throwsElementNotFound() {
    assertThatThrownBy(() -> $$(".non-existing-elements").shouldHave(texts("content1", "content2")))
      .isInstanceOf(ElementNotFound.class)
      .hasMessageStartingWith("Element not found {.non-existing-elements}");
  }

  @Test
  void shouldThrow_ElementNotFound_causedBy_IndexOutOfBoundsException_first() {
    ElementsCollection elementsCollection = $$("not-existing-locator").first().$$("#multirowTable");
    String description = "Check throwing ElementNotFound for %s";

    assertThatThrownBy(() -> elementsCollection.shouldHave(texts("any text")))
      .as(description, "texts").isInstanceOf(ElementNotFound.class)
      .hasCauseExactlyInstanceOf(NoSuchElementException.class);
  }

  @Test
  void shouldThrow_ElementNotFound_causedBy_IndexOutOfBoundsException() {
    ElementsCollection elementsCollection = $$("not-existing-locator").get(1).$$("#multirowTable");
    String description = "Check throwing ElementNotFound for %s";

    assertThatThrownBy(() -> elementsCollection.shouldHave(texts("any text")))
      .as(description, "texts").isInstanceOf(ElementNotFound.class)
      .hasCauseExactlyInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void emptyArrayIsNotAllowed() {
    assertThatThrownBy(() -> $$(".element").shouldHave(texts()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("No expected texts given");
  }

  @Test
  void emptyListIsNotAllowed() {
    assertThatThrownBy(() -> $$(".element").shouldHave(texts(emptyList())))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("No expected texts given");
  }

  @Test
  void textsInsideSvg() {
    openFile("page_with_svg.html");
    $$("#banana svg tspan").shouldHave(texts("not", "apple", "fruit"));
    $$("#banana svg text").shouldHave(texts("are not a banana", "are not an apple", "are the Fruit"));
  }
}
