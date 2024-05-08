package com.alipay.mock.web;

import com.mock.module.UserInfo;
import com.mock.module.AResource;
import com.mock.outside.MyClass;
import com.mock.outside.ATemplate;

@Controller
public class BeanDependOnDemo {
    @Autowired
    private UserInfo userInfo;

    @Qualifier("myAClass")
    private MyClass myClass;

    @Resource("aResource")
    private AResource resource;

    private ATemplate template;

    // 如果是以 xml 定义的bean，那么：
    // 1. byType 注入，则会找 ATemplate 类型的 bean
    // 2. byName 注入，则会找 id = bTemplate 的 bean
    public void setTemplate(ATemplate bTemplate) {
        return this.template = bTemplate;
    }
}
