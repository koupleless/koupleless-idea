package com.multimodule.web;

import com.mock.multimodule.service.MockFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class MockController {

    @Autowired
    private MockFacade mockFacade;
}
