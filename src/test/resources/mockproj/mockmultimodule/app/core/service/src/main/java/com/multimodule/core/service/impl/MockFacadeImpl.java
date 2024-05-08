package com.multimodule.core.service.impl;

import com.mock.core.model.MockModel;
import com.mock.core.service.MockFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MockFacadeImpl implements MockFacade {

    @Override
    public void store(MockModel model) {}
}
