package com.bizket;

import com.bizket.marketing.api.controller.MarketingContentController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(controllers = {
    MarketingContentController.class
})
public abstract class ControllerTestSupport {

}
