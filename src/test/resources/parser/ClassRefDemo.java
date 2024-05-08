package com.mock;

import com.mock.module.OrderListQueryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mock.src.BaseController;
import com.mock.src.BaseFacade;
import com.mock.src.MockException;
import com.mock.src.CommonResult;
import com.mock.src.CustomClassAnno;
import com.mock.module.UserInfo;
import com.mock.module.UserInfoQueryRequest;
import com.mock.module.AException;
import com.mock.module.CustomMethodAnno;
import com.mock.outside.MyClass;
import java.util.ArrayList;

@RestController
@RequestMapping("/v1/user")
@CustomClassAnno
public class ClassRefDemo extends BaseController implements BaseFacade{

    private UserInfo userInfo = new UserInfo();
    @GetMapping
    @RequestMapping("/order")
    @CustomMethodAnno
    public OrderListQueryResponse queryOrderList(ArrayList<UserInfo> userInfo) throws MockException{
        try{
            UserInfoQueryRequest request = new UserInfoQueryRequest();
            CommonResult<OrderListQueryResponse> result = businessQueryService.queryOrderList(request);

            for(UserInfo user : userInfo){
                System.out.println(user.getName());
            }

            for (MyClass obj = new MyClass(0); obj.getValue() < 5; obj = new MyClass(obj.getValue() + 1)) {
                System.out.println(obj.getValue());
            }

        }catch (AException e){
            e.printStackTrace();
        }

        return result.getData();
    }

}
