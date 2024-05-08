package com.mock;

import com.mock.module.OrderListQueryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mock.src.BaseController;
import com.mock.src.BaseFacade;
import com.mock.src.MockException;
import com.mock.src.CommonResult;
import com.mock.module.UserInfo;
import com.mock.module.UserInfoQueryRequest;
import com.mock.module.AException;
import com.mock.outside.MyClass;
import java.util.ArrayList;
import static com.mock.outside.MockClass.StaticMethod;
import com.mock.ModelA;
import com.mock.ModelB;
import com.mock.ModelC;
import com.mock.ModelD;

@RestController
@RequestMapping("/v1/user")
public class ModuleClassReferBySrcBaseClassDemo extends BaseController implements BaseFacade{

    private UserInfo userInfo = new UserInfo();
    private ModelA model = new ModelA();

    @GetMapping
    @RequestMapping("/order")
    public OrderListQueryResponse queryOrderList(ArrayList<UserInfo> userInfo,ModelB modelb,ArrayList<ModelC> modelCs) throws MockException{
        try{
            UserInfoQueryRequest request = new UserInfoQueryRequest();
            CommonResult<OrderListQueryResponse> result = businessQueryService.queryOrderList(request);

            for(UserInfo user : userInfo){
                System.out.println(user.getName());
            }

            for (MyClass obj = new MyClass(0); obj.getValue() < 5; obj = new MyClass(obj.getValue() + 1)) {
                System.out.println(obj.getValue());
            }
            for(ModelC modelC : modelCs){
                ModelD modelD = new ModelD();
                System.out.println(modelCs.getName());
            }

        }catch (AException e){
            e.printStackTrace();
        }

        return result.getData();
    }

}
