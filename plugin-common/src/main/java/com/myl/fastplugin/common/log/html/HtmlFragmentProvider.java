package com.myl.fastplugin.common.log.html;

import java.io.IOException;

public interface HtmlFragmentProvider {
    /**
     * provide a piece of HTML code
     */
    void provideHtmlCode(Appendable appendable) throws IOException;

    void reset();
}
