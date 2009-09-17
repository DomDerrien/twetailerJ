package domderrien.i18n;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

public class MockLabelExtractor extends LabelExtractor {

    static class MockResourceBundle extends ListResourceBundle {
        private Object[][] contents;
        public MockResourceBundle(Object[][] bundleContent) {
            contents = bundleContent;
        }
        @Override
        protected Object[][] getContents() {
            return contents;
        }
    }

    private static ResourceBundle originalRB;

    public static void init(Object [][] bundleContent) {
        originalRB = LabelExtractor.setResourceBundle(new MockResourceBundle(bundleContent), null);
    }

    public static void close() {
        LabelExtractor.setResourceBundle(originalRB, null);
    }
}
